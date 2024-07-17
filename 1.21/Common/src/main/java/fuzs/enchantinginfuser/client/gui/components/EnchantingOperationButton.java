package fuzs.enchantinginfuser.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.util.EnchantmentTooltipHelper;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

public class EnchantingOperationButton extends SpritelessImageButton {

    public EnchantingOperationButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, OnPress onPress) {
        super(x, y, width, height, xTexStart, yTexStart, resourceLocation, onPress);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (this.active && Screen.hasShiftDown()) {
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            int index = !this.active ? 0 : this.isHoveredOrFocused() ? 2 : 1;
            guiGraphics.blit(this.resourceLocation, this.getX() + 2, this.getY(), this.xTexStart,
                    this.yTexStart + index * this.yDiffTex, this.width, this.height, this.textureWidth,
                    this.textureHeight
            );
            guiGraphics.blit(this.resourceLocation, this.getX() - 4, this.getY(), this.xTexStart,
                    this.yTexStart + index * this.yDiffTex, this.width, this.height, this.textureWidth,
                    this.textureHeight
            );
            this.refreshTooltip();
        } else {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
            this.refreshTooltip();
        }
    }

    private void refreshTooltip() {
        if (this.isHoveredOrFocused()) {
            if (InfuserScreen.EnchantmentListEntry.this.enchantmentLevel - 1 >= InfuserScreen.EnchantmentListEntry.this.maxLevel && !InfuserScreen.EnchantmentListEntry.this.isObfuscated()) {
                InfuserScreen.EnchantmentListEntry.this.setWeakPowerTooltip(
                        EnchantmentTooltipHelper.MODIFY_LEVEL_COMPONENT);
            }
        }
    }
}
