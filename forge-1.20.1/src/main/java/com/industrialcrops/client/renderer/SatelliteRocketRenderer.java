package com.industrialcrops.client.renderer;
import com.industrialcrops.block.SatelliteRocketBlock;
import com.industrialcrops.entity.SatelliteRocketEntity;
import com.industrialcrops.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
public final class SatelliteRocketRenderer extends EntityRenderer<SatelliteRocketEntity> {
   private final BlockRenderDispatcher blocks;
   public SatelliteRocketRenderer(Context context) {
      super(context);
      this.blocks = context.getBlockRenderDispatcher();
      this.shadowRadius = 0.0F;
   }
   public void render(SatelliteRocketEntity rocket, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      if (switch (rocket.tier()) {
         case 2 -> (Block)ModBlocks.MEDIUM_SATELLITE_ROCKET.get();
         case 3 -> (Block)ModBlocks.LARGE_SATELLITE_ROCKET.get();
         default -> (Block)ModBlocks.SMALL_SATELLITE_ROCKET.get();
      } instanceof SatelliteRocketBlock rocketBlock) {
         poseStack.pushPose();
         poseStack.translate(-0.5, 0.0, -0.5);
         for (int part = 0; part < rocketBlock.height(); part++) {
            this.blocks.renderSingleBlock(rocketBlock.segment(part), poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY);
            poseStack.translate(0.0, 1.0, 0.0);
         }
         poseStack.popPose();
         super.render(rocket, entityYaw, partialTick, poseStack, buffer, packedLight);
      }
   }
   public ResourceLocation getTextureLocation(SatelliteRocketEntity rocket) {
      return InventoryMenu.BLOCK_ATLAS;
   }
}
