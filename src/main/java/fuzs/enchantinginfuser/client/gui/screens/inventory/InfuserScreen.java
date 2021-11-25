package fuzs.enchantinginfuser.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.gui.components.IconButton;
import fuzs.enchantinginfuser.world.inventory.InfuserMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> {
    private static final ResourceLocation INFUSER_LOCATION = new ResourceLocation(EnchantingInfuser.MOD_ID, "textures/gui/container/enchanting_infuser.png");

    private final Random random = new Random();
    private final ScrollingList scrollingList = new ScrollingList();
    private List<FormattedCharSequence> activeTooltip;
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
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
        this.searchBox = new EditBox(this.font, this.leftPos + 67, this.topPos + 6, 116, 9, new TranslatableComponent("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(16777215);
        this.addWidget(this.searchBox);
        this.enchantButton = this.addRenderableWidget(new IconButton(this.leftPos + 7, this.topPos + 55, 18, 18, 126, 185, INFUSER_LOCATION, button -> {}));
        this.updateButtons();
    }

    private void updateButtons() {
        this.enchantButton.active = this.menu.canEnchant(this.minecraft.player);
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

    private void refreshSearchResults() {
        this.scrollingList.items.clear();
        String s = this.searchBox.getValue();
//        if (s.isEmpty()) {
//            for(Item item : Registry.ITEM) {
//                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, this.scrollingList.items);
//            }
//        } else {
//            SearchTree<ItemStack> searchtree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES);
//            this.scrollingList.items.addAll(searchtree.search(s.toLowerCase(Locale.ROOT)));
//        }

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
            int i = (this.scrollingList.items.size() + 9 - 1) / 9 - 5;
            this.scrollOffs = (float)((double)this.scrollOffs - pDelta / (double)i);
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
    public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.activeTooltip = null;
        this.renderBackground(poseStack);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);
        this.renderEnchantingPower(pMouseX, pMouseY);
        this.renderEnchantingCost(poseStack, pMouseX, pMouseY);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.activeTooltip != null) {
            this.renderTooltip(poseStack, this.activeTooltip, pMouseX, pMouseY);
        } else {
            this.renderTooltip(poseStack, pMouseX, pMouseY);
        }
    }

    private void renderEnchantingCost(PoseStack poseStack, int mouseX, int mouseY) {
        final boolean canEnchant = this.menu.canEnchant(this.minecraft.player);
        final int costColor = canEnchant ? 8453920 : 16733525;
        final int cost = this.menu.getCost();
        final int posX = this.leftPos + 7;
        final int posY = this.topPos + 55;
        this.renderReadableText(poseStack, posX + 1, posY + 1, String.valueOf(cost), costColor);
        if (!canEnchant && mouseX >= posX && mouseY >= posY && mouseX < posX + 18 && mouseY < posY + 18) {
            this.activeTooltip = Lists.newArrayList(new TranslatableComponent("container.enchant.level.requirement", cost).withStyle(ChatFormatting.RED).getVisualOrderText());
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
            this.activeTooltip = Lists.newArrayList(new TranslatableComponent("gui.enchantinginfuser.tooltip.enchanting_power", power).withStyle(ChatFormatting.YELLOW).getVisualOrderText());
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

    private static class ScrollingList {
        public List<Enchantment> items = Lists.newArrayList(null, null, null, null, null, null, null, null, null, null, null);
        public void scrollTo(float pos) {
            if (pos < 0.0F || pos > 1.0F) throw new IllegalArgumentException("pos must be of interval 0 to 1");
            // TODO
        }

        public boolean canScroll() {
            return this.items.size() > 4;
        }
    }
}
