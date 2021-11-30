package fuzs.enchantinginfuser.client.renderer.blockentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class InfuserItemRenderer extends InfuserRenderer {
    public InfuserItemRenderer(TileEntityRendererDispatcher pContext) {
        super(pContext);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(InfuserBlockEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        if (tileEntityIn.open == 0.0F && tileEntityIn.oOpen == 0.0F) return;
        ItemStack itemToEnchant = ((IInventory) tileEntityIn).getItem(0);
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.0F, 0.5F);
        IBakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemToEnchant, tileEntityIn.getLevel(), null);
        float hoverOffset = MathHelper.sin((tileEntityIn.time + partialTicks) / 10.0F) * 0.1F + 0.1F;
        float modelYScale = model.getTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y();
        float openness = MathHelper.lerp(partialTicks, tileEntityIn.oOpen, tileEntityIn.open);
        matrixStackIn.translate(0.0, hoverOffset + 0.25F * modelYScale * openness - 0.15F * (1.0F - openness), 0.0);
        final float scale = openness * 0.8F + 0.2F;
        matrixStackIn.scale(scale, scale, scale);
        matrixStackIn.mulPose(Vector3f.YP.rotation((tileEntityIn.time + partialTicks) / 20.0F));
        Minecraft.getInstance().getItemRenderer().render(itemToEnchant, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, combinedLightIn, OverlayTexture.NO_OVERLAY, model);
        matrixStackIn.popPose();
    }

}
