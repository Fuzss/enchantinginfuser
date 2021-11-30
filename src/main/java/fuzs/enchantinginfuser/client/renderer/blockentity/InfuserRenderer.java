package fuzs.enchantinginfuser.client.renderer.blockentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

/**
 * copied from enchanting table renderer so we can replace book texture
 * also book texture needs to be stitched on atlas
 */
public class InfuserRenderer extends TileEntityRenderer<InfuserBlockEntity> {
   /** The texture for the book above the enchantment table. */
   public static final RenderMaterial BOOK_LOCATION = new RenderMaterial(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(EnchantingInfuser.MOD_ID, "entity/enchanting_infuser_book"));
   private final BookModel bookModel = new BookModel();

   public InfuserRenderer(TileEntityRendererDispatcher pContext) {
      super(pContext);
   }

   @Override
   public void render(InfuserBlockEntity pBlockEntity, float pPartialTick, MatrixStack pPoseStack, IRenderTypeBuffer pBufferSource, int pPackedLight, int pPackedOverlay) {
      pPoseStack.pushPose();
      pPoseStack.translate(0.5D, 0.75D, 0.5D);
      float f = (float)pBlockEntity.time + pPartialTick;
      pPoseStack.translate(0.0D, (double)(0.1F + MathHelper.sin(f * 0.1F) * 0.01F), 0.0D);

      float f1;
      for(f1 = pBlockEntity.rot - pBlockEntity.oRot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
      }

      while(f1 < -(float)Math.PI) {
         f1 += ((float)Math.PI * 2F);
      }

      float f2 = pBlockEntity.oRot + f1 * pPartialTick;
      pPoseStack.mulPose(Vector3f.YP.rotation(-f2));
      pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
      float f3 = MathHelper.lerp(pPartialTick, pBlockEntity.oFlip, pBlockEntity.flip);
      float f4 = MathHelper.frac(f3 + 0.25F) * 1.6F - 0.3F;
      float f5 = MathHelper.frac(f3 + 0.75F) * 1.6F - 0.3F;
      float f6 = MathHelper.lerp(pPartialTick, pBlockEntity.oOpen, pBlockEntity.open);
      this.bookModel.setupAnim(f, MathHelper.clamp(f4, 0.0F, 1.0F), MathHelper.clamp(f5, 0.0F, 1.0F), f6);
      IVertexBuilder vertexconsumer = BOOK_LOCATION.buffer(pBufferSource, RenderType::entitySolid);
      this.bookModel.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
      pPoseStack.popPose();
   }
}