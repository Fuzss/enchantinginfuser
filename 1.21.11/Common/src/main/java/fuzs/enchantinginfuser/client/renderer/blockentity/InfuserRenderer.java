package fuzs.enchantinginfuser.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.client.renderer.blockentity.state.InfuserRenderState;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class InfuserRenderer implements BlockEntityRenderer<InfuserBlockEntity, InfuserRenderState> {
    public static final Material BOOK_LOCATION = Sheets.BLOCK_ENTITIES_MAPPER.apply(EnchantingInfuser.id(
            "enchanting_infuser_book"));

    public final ItemModelResolver itemModelResolver;
    private final MaterialSet materials;
    private final BookModel bookModel;
    private final EnchantTableRenderer enchantTableRenderer;

    public InfuserRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.materials = context.materials();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.enchantTableRenderer = new EnchantTableRenderer(context);
    }

    @Override
    public InfuserRenderState createRenderState() {
        return new InfuserRenderState();
    }

    @Override
    public void extractRenderState(InfuserBlockEntity blockEntity, InfuserRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        this.enchantTableRenderer.extractRenderState(blockEntity,
                renderState,
                partialTick,
                cameraPosition,
                crumblingOverlay);
        renderState.item.clear();
        this.itemModelResolver.updateForTopItem(renderState.item,
                blockEntity.getItem(0),
                ItemDisplayContext.GROUND,
                blockEntity.getLevel(),
                null,
                0);
    }

    /**
     * @see net.minecraft.client.renderer.blockentity.EnchantTableRenderer
     */
    @Override
    public void submit(InfuserRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.submitBook(renderState, poseStack, submitNodeCollector);
        this.submitItem(renderState, poseStack, submitNodeCollector);
    }

    /**
     * Exact copy, but we replace the book model and material.
     *
     * @see net.minecraft.client.renderer.blockentity.EnchantTableRenderer#submit(EnchantTableRenderState, PoseStack,
     *         SubmitNodeCollector, CameraRenderState)
     */
    private void submitBook(EnchantTableRenderState enchantTableRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        poseStack.translate(0.0F, 0.1F + Mth.sin(enchantTableRenderState.time * 0.1F) * 0.01F, 0.0F);
        float f = enchantTableRenderState.yRot;
        poseStack.mulPose(Axis.YP.rotation(-f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float g = Mth.frac(enchantTableRenderState.flip + 0.25F) * 1.6F - 0.3F;
        float h = Mth.frac(enchantTableRenderState.flip + 0.75F) * 1.6F - 0.3F;
        BookModel.State state = new BookModel.State(enchantTableRenderState.time,
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(h, 0.0F, 1.0F),
                enchantTableRenderState.open);
        submitNodeCollector.submitModel(this.bookModel,
                state,
                poseStack,
                BOOK_LOCATION.renderType(RenderTypes::entitySolid),
                enchantTableRenderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.materials.get(BOOK_LOCATION),
                0,
                enchantTableRenderState.breakProgress);
        poseStack.popPose();
    }

    /**
     * @see net.minecraft.client.renderer.entity.ItemEntityRenderer#submit(ItemEntityRenderState, PoseStack,
     *         SubmitNodeCollector, CameraRenderState)
     */
    private void submitItem(InfuserRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        if (renderState.open > 0.0F) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.0F, 0.5F);
            float hoverOffset = Mth.sin(renderState.time / 10.0F) * 0.1F + 0.1F;
            AABB aABB = renderState.item.getModelBoundingBox();
            float modelYScale = -((float) aABB.minY) + 0.0625F;
            poseStack.translate(0.0,
                    hoverOffset + modelYScale * renderState.open - 0.15F * (1.0F - renderState.open),
                    0.0);
            float scale = renderState.open * 0.8F + 0.2F;
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.YP.rotation(renderState.time / 20.0F));
            renderState.item.submit(poseStack,
                    submitNodeCollector,
                    renderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);
            poseStack.popPose();
        }
    }
}
