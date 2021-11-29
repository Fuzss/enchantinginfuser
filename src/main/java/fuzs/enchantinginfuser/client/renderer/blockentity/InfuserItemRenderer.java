package fuzs.enchantinginfuser.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

public class InfuserItemRenderer extends InfuserRenderer {
    public InfuserItemRenderer(BlockEntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(InfuserBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        if (tileEntityIn.open == 0.0F && tileEntityIn.oOpen == 0.0F) return;
        ItemStack itemToEnchant = ((Container) tileEntityIn).getItem(0);
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 1.0F, 0.5F);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemToEnchant, tileEntityIn.getLevel(), null, 0);
        float hoverOffset = Mth.sin((tileEntityIn.time + partialTicks) / 10.0F) * 0.1F + 0.1F;
        float modelYScale = model.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
        float openness = Mth.lerp(partialTicks, tileEntityIn.oOpen, tileEntityIn.open);
        matrixStackIn.translate(0.0, hoverOffset + 0.25F * modelYScale * openness - 0.15F * (1.0F - openness), 0.0);
        final float scale = openness * 0.8F + 0.2F;
        matrixStackIn.scale(scale, scale, scale);
        matrixStackIn.mulPose(Vector3f.YP.rotation((tileEntityIn.time + partialTicks) / 20.0F));
        Minecraft.getInstance().getItemRenderer().render(itemToEnchant, ItemTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, combinedLightIn, OverlayTexture.NO_OVERLAY, model);
        matrixStackIn.popPose();
    }

}
