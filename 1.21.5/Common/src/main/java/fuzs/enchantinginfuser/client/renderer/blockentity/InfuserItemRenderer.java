package fuzs.enchantinginfuser.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class InfuserItemRenderer extends InfuserRenderer {
    private final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    private final ItemRenderer itemRenderer;
    public final ItemModelResolver itemModelResolver;

    public InfuserItemRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void render(InfuserBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 cameraPosition) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay, cameraPosition);
        Level level = blockEntity.getLevel();
        if (level != null && (blockEntity.open != 0.0F || blockEntity.oOpen != 0.0F)) {
            ItemStack itemStack = blockEntity.getItem(0);
            this.itemModelResolver.updateForTopItem(this.itemStackRenderState,
                    itemStack,
                    ItemDisplayContext.GROUND,
                    level,
                    null,
                    0);
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.0F, 0.5F);
            float hoverOffset = Mth.sin((blockEntity.time + partialTick) / 10.0F) * 0.1F + 0.1F;
            AABB aABB = ItemEntityRenderer.calculateModelBoundingBox(this.itemStackRenderState);
            float modelYScale = -((float) aABB.minY) + 0.0625F;
            float openness = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
            poseStack.translate(0.0, hoverOffset + modelYScale * openness - 0.15F * (1.0F - openness), 0.0);
            final float scale = openness * 0.8F + 0.2F;
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.YP.rotation((blockEntity.time + partialTick) / 20.0F));
            this.itemStackRenderState.render(poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }
}
