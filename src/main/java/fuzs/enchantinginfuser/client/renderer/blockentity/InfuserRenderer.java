package fuzs.enchantinginfuser.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import fuzs.enchantinginfuser.EnchantingInfuser;
import fuzs.enchantinginfuser.world.level.block.entity.InfuserBlockEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

/**
 * copied from enchanting table renderer so we can replace book texture
 * also book texture needs to be stitched on atlas
 */
public class InfuserRenderer implements BlockEntityRenderer<InfuserBlockEntity> {
   /** The texture for the book above the enchantment table. */
   public static final Material BOOK_LOCATION = new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(EnchantingInfuser.MOD_ID, "entity/enchanting_infuser_book"));
   private final BookModel bookModel;

   public InfuserRenderer(BlockEntityRendererProvider.Context pContext) {
      this.bookModel = new BookModel(pContext.bakeLayer(ModelLayers.BOOK));
   }

   @Override
   public void render(InfuserBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
      pPoseStack.pushPose();
      pPoseStack.translate(0.5D, 0.75D, 0.5D);
      float f = (float)pBlockEntity.time + pPartialTick;
      pPoseStack.translate(0.0D, (double)(0.1F + Mth.sin(f * 0.1F) * 0.01F), 0.0D);

      float f1;
      for(f1 = pBlockEntity.rot - pBlockEntity.oRot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
      }

      while(f1 < -(float)Math.PI) {
         f1 += ((float)Math.PI * 2F);
      }

      float f2 = pBlockEntity.oRot + f1 * pPartialTick;
      pPoseStack.mulPose(Vector3f.YP.rotation(-f2));
      pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
      float f3 = Mth.lerp(pPartialTick, pBlockEntity.oFlip, pBlockEntity.flip);
      float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
      float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
      float f6 = Mth.lerp(pPartialTick, pBlockEntity.oOpen, pBlockEntity.open);
      this.bookModel.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
      VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(pBufferSource, RenderType::entitySolid);
      this.bookModel.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
      pPoseStack.popPose();
   }
}