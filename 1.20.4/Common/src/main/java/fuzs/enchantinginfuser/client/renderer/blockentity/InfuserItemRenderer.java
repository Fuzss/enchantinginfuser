package fuzs.enchantinginfuser.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class InfuserItemRenderer extends InfuserRenderer {

    public InfuserItemRenderer(BlockEntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(InfuserBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, multiBufferSource, packedLight, packedOverlay);
        if (blockEntity.open == 0.0F && blockEntity.oOpen == 0.0F) return;
        ItemStack itemToEnchant = ((Container) blockEntity).getItem(0);
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemToEnchant, blockEntity.getLevel(), null, 0);
        float hoverOffset = Mth.sin((blockEntity.time + partialTick) / 10.0F) * 0.1F + 0.1F;
        float modelYScale = model.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        float openness = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        poseStack.translate(0.0, hoverOffset + 0.25F * modelYScale * openness - 0.15F * (1.0F - openness), 0.0);
        final float scale = openness * 0.8F + 0.2F;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotation((blockEntity.time + partialTick) / 20.0F));
        Minecraft.getInstance().getItemRenderer().render(itemToEnchant, ItemDisplayContext.GROUND, false, poseStack, multiBufferSource, packedLight, OverlayTexture.NO_OVERLAY, model);
        poseStack.popPose();
    }

}
