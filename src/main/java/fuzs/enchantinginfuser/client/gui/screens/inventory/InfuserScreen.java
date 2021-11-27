package fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> {
    private static final ResourceLocation INFUSER_LOCATION = new ResourceLocation(EnchantingInfuser.MOD_ID, "textures/gui/container/enchanting_infuser.png");

    private final int enchantmentSeed = new Random().nextInt();
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
        this.searchBox = new EditBox(this.font, this.leftPos + 67, this.topPos + 6, 116, 9, new TranslatableComponent("itemGroup.search")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                // left click clears text
                if (this.isVisible() && button == 1) {
                    this.setValue("");
                    InfuserScreen.this.refreshSearchResults();
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
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
            this.searchBox.setValue("");
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
        this.renderEnchantingPower(poseStack, mouseX, mouseY);
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
        if (mouseX >= posX && mouseY >= posY && mouseX < posX + 18 && mouseY < posY + 18) {
            List<FormattedText> list = Lists.newArrayList();
            if (canEnchant) {
                this.menu.getValidEnchantments()
                        .map(e -> e.getKey().getFullname(e.getValue()))
                        .forEach(list::add);
                list.add(TextComponent.EMPTY);
                MutableComponent mutablecomponent1;
                if (cost == 1) {
                    mutablecomponent1 = new TranslatableComponent("container.enchant.level.one");
                } else {
                    mutablecomponent1 = new TranslatableComponent("container.enchant.level.many", cost);
                }
                list.add(mutablecomponent1.withStyle(ChatFormatting.GRAY));
            } else {
                list.add(new TranslatableComponent("container.enchant.level.requirement", cost).withStyle(ChatFormatting.RED));
            }
            this.setActiveTooltip(Language.getInstance().getVisualOrder(list));
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

    private void renderEnchantingPower(PoseStack poseStack, int mouseX, int mouseY) {
        ItemStack itemstack = new ItemStack(Items.BOOKSHELF);
        this.itemRenderer.blitOffset = 100.0F;
        int posX = this.leftPos + 196;
        int posY = this.topPos + 161;
        this.itemRenderer.renderAndDecorateFakeItem(itemstack, posX, posY);
        String power = String.valueOf(this.menu.getCurrentPower());
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 300.0);
        this.font.drawShadow(poseStack, power, posX + 19 - 2 - this.font.width(power), posY + 6 + 3, 16777215);
        poseStack.popPose();
//        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, posX, posY);
        this.itemRenderer.blitOffset = 0.0F;
        if (mouseX >= posX && mouseY >= posY && mouseX < posX + 16 && mouseY < posY + 16) {
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

    private class ScrollingList extends AbstractContainerEventHandler implements Widget, NarratableEntry {
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
            pEntry.setList(this);
            this.markOthersIncompatible();
        }

        protected int getItemCount() {
            return this.children.size();
        }

        public void markOthersIncompatible() {
            final List<EnchantmentListEntry> activeEnchants = this.children.stream()
                    .filter(EnchantmentListEntry::isActive)
                    .toList();
            for (EnchantmentListEntry entry : this.children) {
                if (!entry.isActive()) {
                    entry.markIncompatible(activeEnchants.stream()
                            .filter(e -> e.isIncompatibleWith(entry))
                            .collect(Collectors.toSet()));
                }
            }
        }

        @Nullable
        protected final EnchantmentListEntry getEntryAtPosition(double mouseX, double mouseY) {
            if (this.isMouseOver(mouseX, mouseY)) {
                final int index = this.scrollPosition + (int) ((mouseY - this.posY) / this.itemHeight);
                return index < this.children.size() ? this.children.get(index) : null;
            }
            return null;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= this.posX && mouseX < this.posX + this.itemWidth && mouseY >= this.posY && mouseY < this.posY + this.itemHeight * this.length;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isMouseOver(mouseX, mouseY)) {
                return false;
            } else {
                EnchantmentListEntry entry = this.getEntryAtPosition(mouseX, mouseY);
                if (entry != null) {
                    if (entry.mouseClicked(mouseX, mouseY, button)) {
                        this.setFocused(entry);
                        this.setDragging(true);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
            if (this.getFocused() != null) {
                this.getFocused().mouseReleased(pMouseX, pMouseY, pButton);
            }
            return false;
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
        private final int maxLevel;
        private final Button decrButton;
        private final Button incrButton;
        private int level;
        private ScrollingList list;
        @Nullable
        private GuiEventListener focused;
        private boolean dragging;
        private Set<Enchantment> incompatible = Sets.newHashSet();

        public EnchantmentListEntry(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.maxLevel = InfuserScreen.this.menu.getMaxLevel(enchantment);
            this.level = level;
            this.decrButton = new IconButton(0, 0, 10, 16, 220, 0, INFUSER_LOCATION, button -> {
                final int newLevel = InfuserScreen.this.menu.clickEnchantmentButton(this.enchantment, false);
                if (newLevel == -1) return;
                this.level = newLevel;
                EnchantingInfuser.NETWORK.sendToServer(new C2SAddEnchantLevelMessage(InfuserScreen.this.menu.containerId, this.enchantment, false));
                this.updateButtons();
                this.list.markOthersIncompatible();
            });
            this.incrButton = new IconButton(0, 0, 10, 16, 230, 0, INFUSER_LOCATION, button -> {
                final int newLevel = InfuserScreen.this.menu.clickEnchantmentButton(this.enchantment, true);
                if (newLevel == -1) return;
                this.level = newLevel;
                EnchantingInfuser.NETWORK.sendToServer(new C2SAddEnchantLevelMessage(InfuserScreen.this.menu.containerId, this.enchantment, true));
                this.updateButtons();
                this.list.markOthersIncompatible();
            }, (button, matrixStack, mouseX, mouseY) -> {
                if (this.level >= this.maxLevel && !this.isObfuscated()) {
                    InfuserScreen.this.setActiveTooltip(InfuserScreen.this.font.split(LOW_POWER_COMPONENT, 175));
                }
            });
            this.updateButtons();
        }

        public void setList(ScrollingList list) {
            this.list = list;
        }

        public void markIncompatible(Collection<EnchantmentListEntry> incompatibleList) {
            this.incompatible = incompatibleList.stream()
                    .map(e -> e.enchantment)
                    .collect(Collectors.toSet());
            final boolean compatible = incompatibleList.isEmpty();
            if (!compatible) this.level = 0;
            this.updateButtons();
            this.decrButton.active = compatible;
            this.incrButton.active &= compatible;
        }

        private void updateButtons() {
            this.decrButton.visible = this.level > 0;
            this.incrButton.visible = this.level < this.enchantment.getMaxLevel();
            this.incrButton.active = this.level < this.maxLevel;
        }

        public boolean isActive() {
            return this.level > 0;
        }

        public boolean isIncompatible() {
            return !this.incompatible.isEmpty();
        }

        public boolean isObfuscated() {
            return this.maxLevel == 0;
        }

        private int getYImage() {
            return this.isIncompatible() || this.isObfuscated() ? 0 : this.isActive() ? 2 : 1;
        }

        public boolean isIncompatibleWith(EnchantmentListEntry other) {
            if (other == this) return false;
            return (this.isActive() || other.isActive()) && !this.enchantment.isCompatibleWith(other.enchantment);
        }

        public void render(PoseStack poseStack, int leftPos, int topPos, int width, int height, int mouseX, int mouseY, float partialTicks) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, INFUSER_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            InfuserScreen.this.blit(poseStack, leftPos + 18, topPos, 0, 185 + this.getYImage() * 18, 126, 18);
            FormattedCharSequence formattedCharSequence = this.getRenderingName(this.enchantment, width);
            GuiComponent.drawCenteredString(poseStack, InfuserScreen.this.font, formattedCharSequence, leftPos + width / 2, topPos + 5, this.isIncompatible() || this.isObfuscated() ? 6839882 : -1);
            if (mouseX >= leftPos + 18 && mouseX < leftPos + 18 + 126 && mouseY >= topPos && mouseY < topPos + 18) {
                this.handleTooltip(this.enchantment);
            }
            this.decrButton.x = leftPos + 4;
            this.decrButton.y = topPos + 1;
            this.decrButton.render(poseStack, mouseX, mouseY, partialTicks);
            this.incrButton.x = leftPos + width - 10 - 4;
            this.incrButton.y = topPos + 1;
            this.incrButton.render(poseStack, mouseX, mouseY, partialTicks);
        }

        private FormattedCharSequence getRenderingName(Enchantment enchantment, int maxWidth) {
            FormattedCharSequence formattedCharSequence = null;
            if (this.isObfuscated()) {
                EnchantmentNames.getInstance().initSeed(InfuserScreen.this.enchantmentSeed + ((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID(enchantment));
                FormattedText formattedtext = EnchantmentNames.getInstance().getRandomName(InfuserScreen.this.font, (int) (maxWidth * 0.72F));
                final List<FormattedCharSequence> list = InfuserScreen.this.font.split(formattedtext, (int) (maxWidth * 0.72F));
                if (!list.isEmpty()) {
                    formattedCharSequence = list.get(0);
                }
            }
            if (formattedCharSequence == null) {
                final MutableComponent component = new TranslatableComponent(enchantment.getDescriptionId());
                if (this.isActive()) {
                    component.append(" ").append(new TranslatableComponent("enchantment.level." + this.level));
                }
                formattedCharSequence = component.getVisualOrderText();
            }
            return formattedCharSequence;
        }

        private void handleTooltip(Enchantment enchantment) {
            if (this.isObfuscated()) {
                InfuserScreen.this.setActiveTooltip(InfuserScreen.this.font.split(UNKNOWN_ENCHANT_COMPONENT, 175));
            } else if (this.isIncompatible()) {
                final Component incompatibleComponent = new TranslatableComponent("gui.enchantinginfuser.tooltip.incompatible", this.incompatible.stream()
                        .map(e -> (MutableComponent) new TranslatableComponent(e.getDescriptionId()))
                        .reduce((o1, o2) -> o1.append(", ").append(o2))
                        .get().withStyle(ChatFormatting.GRAY));
                InfuserScreen.this.setActiveTooltip(InfuserScreen.this.font.split(incompatibleComponent, 175));
            } else {
                List<FormattedCharSequence> list = Lists.newArrayList();
                if (Language.getInstance().has(enchantment.getDescriptionId() + ".desc")) {
                    list.addAll(InfuserScreen.this.font.split(new TranslatableComponent(enchantment.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY), 175));
                } else if (Language.getInstance().has(enchantment.getDescriptionId() + ".description")) {
                    list.addAll(InfuserScreen.this.font.split(new TranslatableComponent(enchantment.getDescriptionId() + ".description").withStyle(ChatFormatting.GRAY), 175));
                }
                // kinda useless for there to just be a name on the tooltip without a description
                // descriptions may be provided by enchantment descriptions mod
                if (!list.isEmpty()) {
                    final MutableComponent levelsComponent = new TranslatableComponent("enchantment.level." + enchantment.getMinLevel());
                    if (enchantment.getMinLevel() != enchantment.getMaxLevel()) {
                        levelsComponent.append("-").append( new TranslatableComponent("enchantment.level." + enchantment.getMaxLevel()));
                    }
                    final Component wrappedComponent = new TextComponent("(").append(levelsComponent).append(")").withStyle(ChatFormatting.GRAY);
                    list.add(0, new TranslatableComponent(enchantment.getDescriptionId()).append(" ").append(wrappedComponent).getVisualOrderText());
                    InfuserScreen.this.setActiveTooltip(list);
                }
            }
        }

        @Override
        public boolean isMouseOver(double pMouseX, double pMouseY) {
            return Objects.equals(this.list.getEntryAtPosition(pMouseX, pMouseY), this);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.decrButton, this.incrButton);
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
