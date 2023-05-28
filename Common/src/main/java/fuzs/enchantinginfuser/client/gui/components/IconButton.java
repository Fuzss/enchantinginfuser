package fuzs.enchantinginfuser.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends ImageButton {
    protected int xTexStart;
    protected int yTexStart;

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, ResourceLocation resourceLocation, OnPress onPress) {
        super(x, y, width, height, xTexStart, yTexStart, resourceLocation, onPress);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation resourceLocation, OnPress onPress) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, onPress);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
    }

    public IconButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex, ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress) {
        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onPress);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
    }

    public void setTexture(int textureX, int textureY) {
        this.xTexStart = textureX;
        this.yTexStart = textureY;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderTexture(poseStack, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
    }

    @Override
    public void renderTexture(PoseStack poseStack, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, int n, int o, int p, int q) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        int r = l + m;
        if (!this.isActive()) {
            r = l;
        } else if (this.isHoveredOrFocused()) {
            r = l + m * 2;
        }

        RenderSystem.enableDepthTest();
        blit(poseStack, i, j, (float)k, (float)r, n, o, p, q);
    }

    protected int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }
        return 46 + i * 20;
    }
}
