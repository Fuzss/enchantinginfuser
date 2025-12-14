package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.EnchantmentComponent;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipBuilder;
import fuzs.puzzleslib.api.util.v1.CommonHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

public abstract class EnchantingOperationButton extends SpritelessImageButton {
    private final boolean isPowerTooLow;

    public EnchantingOperationButton(EnchantmentComponent enchantmentComponent, int x, int y, int xTexOffset, OnPress onPress) {
        super(x, y, 18, 18, 220 + xTexOffset, 0, InfuserScreen.INFUSER_LOCATION, onPress);
        this.visible = !enchantmentComponent.isNotAvailable() && this.getVisibleValue(enchantmentComponent);
        this.active = this.getActiveValue(enchantmentComponent);
        Component component = this.getTooltipComponent(enchantmentComponent);
        this.isPowerTooLow = component != null;
        if (this.isPowerTooLow) {
            TooltipBuilder.create(enchantmentComponent.getWeakPowerTooltip(component)).splitLines().build(this);
        }
        this.setTextureLayout(LEGACY_TEXTURE_LAYOUT);
    }

    protected abstract boolean getVisibleValue(EnchantmentComponent enchantmentComponent);

    protected abstract boolean getActiveValue(EnchantmentComponent enchantmentComponent);

    @Nullable
    protected abstract Component getTooltipComponent(EnchantmentComponent enchantmentComponent);

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.isActive() && CommonHelper.hasShiftDown()) {
            int yImage = this.isHoveredOrFocused() ? 2 : 1;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    this.identifier,
                    this.getX() + 3,
                    this.getY(),
                    this.xTexStart,
                    this.yTexStart + yImage * this.yDiffTex,
                    this.width,
                    this.height,
                    this.textureWidth,
                    this.textureHeight,
                    ARGB.white(this.alpha));
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                    this.identifier,
                    this.getX() - 3,
                    this.getY(),
                    this.xTexStart,
                    this.yTexStart + yImage * this.yDiffTex,
                    this.width,
                    this.height,
                    this.textureWidth,
                    this.textureHeight,
                    ARGB.white(this.alpha));
        } else {
            super.renderContents(guiGraphics, mouseX, mouseY, partialTicks);
        }

        if (this.isPowerTooLow && this.isHoveredOrFocused()) {
            InfuserScreen.setIsPowerTooLow(true);
        }
    }

    public static class Add extends EnchantingOperationButton {

        public Add(EnchantmentComponent enchantmentComponent, int x, int y, OnPress onPress) {
            super(enchantmentComponent, x, y, 18, onPress);
        }

        @Override
        protected boolean getVisibleValue(EnchantmentComponent enchantmentComponent) {
            return enchantmentComponent.enchantmentLevel() < enchantmentComponent.enchantmentValues().maxLevel();
        }

        @Override
        protected boolean getActiveValue(EnchantmentComponent enchantmentComponent) {
            return !enchantmentComponent.isIncompatible()
                    && enchantmentComponent.enchantmentLevel() < enchantmentComponent.enchantmentValues()
                    .availableLevel();
        }

        @Nullable
        @Override
        protected Component getTooltipComponent(EnchantmentComponent enchantmentComponent) {
            if (enchantmentComponent.enchantmentLevel() >= enchantmentComponent.enchantmentValues().availableLevel()
                    && !enchantmentComponent.isNotAvailable()) {
                return EnchantmentTooltipHelper.INCREASE_LEVEL_COMPONENT;
            } else {
                return null;
            }
        }
    }

    public static class Remove extends EnchantingOperationButton {

        public Remove(EnchantmentComponent enchantmentComponent, int x, int y, OnPress onPress) {
            super(enchantmentComponent, x, y, 0, onPress);
        }

        @Override
        protected boolean getVisibleValue(EnchantmentComponent enchantmentComponent) {
            return enchantmentComponent.isPresent();
        }

        @Override
        protected boolean getActiveValue(EnchantmentComponent enchantmentComponent) {
            return !enchantmentComponent.isIncompatible()
                    && enchantmentComponent.enchantmentLevel() - 1 < enchantmentComponent.enchantmentValues()
                    .availableLevel();
        }

        @Nullable
        @Override
        protected Component getTooltipComponent(EnchantmentComponent enchantmentComponent) {
            if (enchantmentComponent.enchantmentLevel() - 1 >= enchantmentComponent.enchantmentValues().availableLevel()
                    && !enchantmentComponent.isNotAvailable()) {
                return EnchantmentTooltipHelper.MODIFY_LEVEL_COMPONENT;
            } else {
                return null;
            }
        }
    }
}
