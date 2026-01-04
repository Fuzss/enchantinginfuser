package fuzs.enchantinginfuser.client.gui.components;

import fuzs.enchantinginfuser.client.gui.screens.inventory.EnchantmentComponent;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipBuilder;
import fuzs.puzzleslib.api.util.v1.CommonHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class EnchantingOperationButton extends ImageButton {
    private final boolean isPowerTooLow;

    public EnchantingOperationButton(EnchantmentComponent enchantmentComponent, int x, int y, WidgetSprites widgetSprites, OnPress onPress) {
        super(x, y, 18, 18, widgetSprites, onPress);
        this.visible = !enchantmentComponent.isNotAvailable() && this.getVisibleValue(enchantmentComponent);
        this.active = this.getActiveValue(enchantmentComponent);
        Component component = this.getTooltipComponent(enchantmentComponent);
        this.isPowerTooLow = component != null;
        if (this.isPowerTooLow) {
            TooltipBuilder.create(enchantmentComponent.getWeakPowerTooltip(component)).splitLines().build(this);
        }
    }

    protected abstract boolean getVisibleValue(EnchantmentComponent enchantmentComponent);

    protected abstract boolean getActiveValue(EnchantmentComponent enchantmentComponent);

    @Nullable
    protected abstract Component getTooltipComponent(EnchantmentComponent enchantmentComponent);

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isActive() && CommonHelper.hasShiftDown()) {
            Identifier identifier = this.sprites.get(true, this.isHoveredOrFocused());
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    identifier,
                    this.getX() - 3,
                    this.getY(),
                    this.width,
                    this.height);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    identifier,
                    this.getX() + 3,
                    this.getY(),
                    this.width,
                    this.height);
        } else {
            super.renderContents(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (this.isPowerTooLow && this.isHoveredOrFocused()) {
            InfuserScreen.setIsPowerTooLow(true);
        }
    }

    public static class Add extends EnchantingOperationButton {

        public Add(EnchantmentComponent enchantmentComponent, int x, int y, OnPress onPress) {
            super(enchantmentComponent, x, y, InfuserScreen.ADD_BUTTON_SPRITES, onPress);
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
            super(enchantmentComponent, x, y, InfuserScreen.REMOVE_BUTTON_SPRITES, onPress);
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
