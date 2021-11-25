package fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.gui.components.IconButton;
import fuzs.enchantinginfuser.network.client.message.C2SAddEnchantLevelMessage;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;
import java.util.*;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> {
    private static final ResourceLocation INFUSER_LOCATION = new ResourceLocation(EnchantingInfuser.MOD_ID, "textures/gui/container/enchanting_infuser.png");

    private final Random random = new Random();
    private List<FormattedCharSequence> activeTooltip;
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
    private ScrollingList scrollingList;
    private boolean ignoreTextInput;
    private Button enchantButton;

    public InfuserScreen(InfuserMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 220;
        this.imageHeight = 185;
        this.inventoryLabelX = 30;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.searchBox.tick();
        this.updateButtons();
    }

    @Override
    protected void init() {
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.searchBox = new EditBox(this.font, this.leftPos + 67, this.topPos + 6, 116, 9, TextComponent.EMPTY);
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(16777215);
        this.addWidget(this.searchBox);
        this.scrollingList = new ScrollingList(this.leftPos + 29, this.topPos + 17, 162, 18, 4);
        this.addWidget(this.scrollingList);
        this.enchantButton = this.addRenderableWidget(new IconButton(this.leftPos + 7, this.topPos + 55, 18, 18, 126, 185, INFUSER_LOCATION, button -> {
            if (this.menu.clickMenuButton(this.minecraft.player, 0)) {
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0);
            }
        }));
        this.updateButtons();
    }

    private void updateButtons() {
        this.enchantButton.active = this.menu.canEnchant(this.minecraft.player);
    }

    public void setActiveTooltip(List<FormattedCharSequence> activeTooltip) {
        this.activeTooltip = activeTooltip;
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        String s = this.searchBox.getValue();
        super.resize(pMinecraft, pWidth, pHeight);
        this.searchBox.setValue(s);
        if (!this.searchBox.getValue().isEmpty()) {
            this.refreshSearchResults();
        }
    }

    @Override
    public void removed() {
        super.removed();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (this.ignoreTextInput) {
            return false;
        }
        String s = this.searchBox.getValue();
        if (this.searchBox.charTyped(pCodePoint, pModifiers)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        this.ignoreTextInput = false;
        if (!this.searchBox.isFocused()) {
            if (this.minecraft.options.keyChat.matches(pKeyCode, pScanCode)) {
                this.ignoreTextInput = true;
                this.searchBox.setFocus(true);
                return true;
            } else {
                return super.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
        } else {
            boolean flag = this.hoveredSlot != null && this.hoveredSlot.hasItem();
            boolean flag1 = InputConstants.getKey(pKeyCode, pScanCode).getNumericKeyValue().isPresent();
            if (flag && flag1 && this.checkHotbarKeyPressed(pKeyCode, pScanCode)) {
                this.ignoreTextInput = true;
                return true;
            } else {
                String s = this.searchBox.getValue();
                if (this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
                    if (!Objects.equals(s, this.searchBox.getValue())) {
                        this.refreshSearchResults();
                    }
                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && pKeyCode != 256 || super.keyPressed(pKeyCode, pScanCode, pModifiers);
                }
            }
        }
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        this.ignoreTextInput = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    public void refreshSearchResults() {
        this.scrollingList.clearEntries();
        String s = this.searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        if (s.isEmpty()) {
            this.menu.getSortedEntries().stream()
                    .map(e -> new EnchantmentListEntry(e.getKey(), e.getValue()))
                    .forEach(this.scrollingList::addEntry);
        } else {
            this.menu.getSortedEntries().stream()
                    .filter(e -> new TranslatableComponent(e.getKey().getDescriptionId()).getString().toLowerCase(Locale.ROOT).contains(s))
                    .map(e -> new EnchantmentListEntry(e.getKey(), e.getValue()))
                    .forEach(this.scrollingList::addEntry);
        }
        this.scrollOffs = 0.0F;
        this.scrollingList.scrollTo(0.0F);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            if (this.insideScrollbar(pMouseX, pMouseY)) {
                this.scrolling = this.scrollingList.canScroll();
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    protected boolean insideScrollbar(double pMouseX, double pMouseY) {
        int fromX = this.leftPos + 197;
        int fromY = this.topPos + 17;
        int toX = fromX + 14;
        int toY = fromY + 72;
        return pMouseX >= (double)fromX && pMouseY >= (double)fromY && pMouseX < (double)toX && pMouseY < (double)toY;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (!this.scrollingList.canScroll()) {
            return false;
        } else {
            this.scrollOffs = (float)((double)this.scrollOffs - pDelta / (this.scrollingList.getItemCount() - 4));
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.scrollingList.scrollTo(this.scrollOffs);
            return true;
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.scrolling) {
            int i = this.topPos + 17;
            int j = i + 72;
            this.scrollOffs = ((float) pMouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.scrollingList.scrollTo(this.scrollOffs);
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.activeTooltip = null;
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.scrollingList.render(poseStack, mouseX, mouseY, partialTick);
        this.renderEnchantingPower(mouseX, mouseY);
        this.renderEnchantingCost(poseStack, mouseX, mouseY);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.activeTooltip != null) {
            this.renderTooltip(poseStack, this.activeTooltip, mouseX, mouseY);
        } else {
            this.renderTooltip(poseStack, mouseX, mouseY);
        }
    }

    private void renderEnchantingCost(PoseStack poseStack, int mouseX, int mouseY) {
        final int cost = this.menu.getCost();
        if (cost <= 0) return;
        final boolean canEnchant = this.menu.canEnchant(this.minecraft.player);
        final int costColor = canEnchant ? 8453920 : 16733525;
        final int posX = this.leftPos + 7;
        final int posY = this.topPos + 55;
        this.renderReadableText(poseStack, posX + 1, posY + 1, String.valueOf(cost), costColor);
        if (!canEnchant && mouseX >= posX && mouseY >= posY && mouseX < posX + 18 && mouseY < posY + 18) {
            this.setActiveTooltip(Lists.newArrayList(new TranslatableComponent("container.enchant.level.requirement", cost).withStyle(ChatFormatting.RED).getVisualOrderText()));
        }
    }

    private void renderReadableText(PoseStack poseStack, int posX, int posY, String text, int color) {
        posX += 19 - 2 - this.font.width(text);
        posY += 6 + 3;
        // render shadow on every side due avoid clashing with colorful background
        this.font.draw(poseStack, text, posX - 1, posY, 0);
        this.font.draw(poseStack, text, posX + 1, posY, 0);
        this.font.draw(poseStack, text, posX, posY - 1, 0);
        this.font.draw(poseStack, text, posX, posY + 1, 0);
        this.font.draw(poseStack, text, posX, posY, color);
    }

    private void renderEnchantingPower(int mouseX, int mouseY) {
        final int power = this.menu.getEnchantingPower();
        ItemStack itemstack = new ItemStack(Items.BOOKSHELF, power);
        this.itemRenderer.blitOffset = 100.0F;
        int posX = this.leftPos + 196;
        int posY = this.topPos + 161;
        this.itemRenderer.renderAndDecorateFakeItem(itemstack, posX, posY);
        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, posX, posY);
        this.itemRenderer.blitOffset = 0.0F;
        if (!itemstack.isEmpty() && mouseX >= posX && mouseY >= posY && mouseX < posX + 16 && mouseY < posY + 16) {
            this.setActiveTooltip(Lists.newArrayList(new TranslatableComponent("gui.enchantinginfuser.tooltip.enchanting_power", power).withStyle(ChatFormatting.YELLOW).getVisualOrderText()));
        }
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, INFUSER_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pPoseStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        this.searchBox.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, INFUSER_LOCATION);
        int sliderX = this.leftPos + 197 - 2;
        int sliderY = this.topPos + 17 - 2;
        int sliderRange = sliderY + 72 + 2 + 2;
        this.blit(pPoseStack, sliderX, sliderY + (int)((float)(sliderRange - sliderY - 18) * this.scrollOffs), 220, 48 + (this.scrollingList.canScroll() ? 18 : 0), 18, 18);
    }

    private static class ScrollingList extends AbstractContainerEventHandler implements Widget, NarratableEntry {
        private final List<EnchantmentListEntry> children = Lists.newArrayList();
        private final int posX;
        private final int posY;
        private final int itemWidth;
        private final int itemHeight;
        private final int length;
        private int scrollPosition;

        public ScrollingList(int posX, int posY, int itemWidth, int itemHeight, int length) {
            this.posX = posX;
            this.posY = posY;
            this.itemWidth = itemWidth;
            this.itemHeight = itemHeight;
            this.length = length;
        }

        public void scrollTo(float pos) {
            if (pos < 0.0F || pos > 1.0F) throw new IllegalArgumentException("pos must be of interval 0 to 1");
            if (this.canScroll()) {
                // important to round instead of int cast
                this.scrollPosition = Math.round((this.getItemCount() - this.length) * pos);
            } else {
                this.scrollPosition = 0;
            }
        }

        public boolean canScroll() {
            return this.getItemCount() > this.length;
        }

        protected final void clearEntries() {
            this.children.clear();
        }

        protected void addEntry(EnchantmentListEntry pEntry) {
            this.children.add(pEntry);
        }

        protected int getItemCount() {
            return this.children.size();
        }

        @Override
        public List<EnchantmentListEntry> children() {
            return this.children;
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            for (int i = 0; i < Math.min(this.length, this.getItemCount()); i++) {
                this.children.get(this.scrollPosition + i).render(poseStack, this.posX, this.posY + this.itemHeight * i, this.itemWidth, this.itemHeight, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public NarrationPriority narrationPriority() {
            // TODO proper implementation
            return NarratableEntry.NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
            // TODO proper implementation
        }
    }

    private class EnchantmentListEntry implements ContainerEventHandler {
        private static final TranslatableComponent UNKNOWN_ENCHANT_COMPONENT = new TranslatableComponent("gui.enchantinginfuser.tooltip.unknown_enchantment");
        private static final TranslatableComponent LOW_POWER_COMPONENT = new TranslatableComponent("gui.enchantinginfuser.tooltip.low_power");

        private final Enchantment enchantment;
        private final Button decrement;
        private final Button increment;
        private int level;
        @Nullable
        private GuiEventListener focused;
        private boolean dragging;

        public EnchantmentListEntry(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
            this.decrement = new IconButton(0, 0, 10, 16, 220, 0, INFUSER_LOCATION, button -> {
                this.level = Math.max(0, --this.level);
                this.updateButtons();
                EnchantingInfuser.NETWORK.sendToServer(new C2SAddEnchantLevelMessage(InfuserScreen.this.menu.containerId, this.enchantment, false));
            });
            this.increment = new IconButton(0, 0, 10, 16, 230, 0, INFUSER_LOCATION, button -> {
                this.level = Math.min(this.enchantment.getMaxLevel(), ++this.level);
                this.updateButtons();
                EnchantingInfuser.NETWORK.sendToServer(new C2SAddEnchantLevelMessage(InfuserScreen.this.menu.containerId, this.enchantment, true));
            }, (button, matrixStack, mouseX, mouseY) -> {
                if (!button.active) {
                    InfuserScreen.this.setActiveTooltip(InfuserScreen.this.font.split(LOW_POWER_COMPONENT, 175));
                }
            });
            this.updateButtons();
        }

        private void updateButtons() {
            this.decrement.visible = this.level > 0;
            this.increment.visible = this.level < this.enchantment.getMaxLevel();
            // TODO proper check for enough power
            this.increment.active = true;
        }

        public void render(PoseStack poseStack, int leftPos, int topPos, int width, int height, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, INFUSER_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            InfuserScreen.this.blit(poseStack, leftPos + 18, topPos, 0, this.level > 0 ? 221 : 203, 126, 18);
            final MutableComponent component = new TranslatableComponent(this.enchantment.getDescriptionId());
            if (this.level > 0) {
                component.append(" ").append(new TranslatableComponent("enchantment.level." + this.level));
            }
            GuiComponent.drawCenteredString(poseStack, InfuserScreen.this.font, component, leftPos + width / 2, topPos + 5, -1);
            if (mouseX >= leftPos + 18 && mouseX < leftPos + 18 + 126 && mouseY >= topPos && mouseY < topPos + 18) {
                InfuserScreen.this.setActiveTooltip(InfuserScreen.this.font.split(UNKNOWN_ENCHANT_COMPONENT, 175));
            }
            this.decrement.x = leftPos + 4;
            this.decrement.y = topPos + 1;
            this.decrement.render(poseStack, mouseX, mouseY, partialTicks);
            this.increment.x = leftPos + width - 10 - 4;
            this.increment.y = topPos + 1;
            this.increment.render(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.decrement, this.increment);
        }

        @Override
        public boolean isDragging() {
            return this.dragging;
        }

        @Override
        public void setDragging(boolean pDragging) {
            this.dragging = pDragging;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener pListener) {
            this.focused = pListener;
        }

        @Override
        @Nullable
        public GuiEventListener getFocused() {
            return this.focused;
        }
    }
}
