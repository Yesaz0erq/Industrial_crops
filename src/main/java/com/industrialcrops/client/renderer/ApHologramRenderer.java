package com.industrialcrops.client.renderer;
import com.industrialcrops.block.entity.ApControllerBlockEntity;
import com.industrialcrops.machine.ApStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
public final class ApHologramRenderer<T extends ApControllerBlockEntity> implements BlockEntityRenderer<T> {
   private static final int FULL_BRIGHT = 15728880;
   private final BlockRenderDispatcher blocks;
   public ApHologramRenderer(Context var1) {
      this.blocks = var1.getBlockRenderDispatcher();
   }
   public void render(T var1, float var2, PoseStack var3, MultiBufferSource var4, int var5, int var6) {
      Level var7 = var1.getLevel();
      if (var7 != null && var1.projection()) {
         BlockPos var8 = var1.getBlockPos();
         Direction var9 = var1.facing();
         List<ApStructure.Part> var10 = ApStructure.parts(var8, var9, var1.coreBlock());
         float var11 = (float)var7.getGameTime() + var2;
         float var12 = 0.5F + 0.5F * Mth.sin(var11 * 0.12F);
         int var13 = (int)(95.0F + 60.0F * var12);
         MultiBufferSource var14 = var2x -> new ApHologramRenderer.GhostConsumer(var4.getBuffer(Sheets.translucentCullBlockSheet()), var13);
         boolean var15 = false;
         for (ApStructure.Part var17 : var10) {
            BlockState var18 = var7.getBlockState(var17.pos());
            if (!var18.is(var17.block())) {
               var15 = true;
               double var19 = (double)(var17.pos().getX() - var8.getX());
               double var21 = (double)(var17.pos().getY() - var8.getY());
               double var23 = (double)(var17.pos().getZ() - var8.getZ());
               if (!var18.isAir() && !var18.canBeReplaced()) {
                  LevelRenderer.renderLineBox(
                     var3,
                     var4.getBuffer(RenderType.lines()),
                     var19 - 0.01,
                     var21 - 0.01,
                     var23 - 0.01,
                     var19 + 1.01,
                     var21 + 1.01,
                     var23 + 1.01,
                     1.0F,
                     0.25F,
                     0.25F,
                     1.0F
                  );
               } else {
                  BlockState var25 = var17.block().defaultBlockState();
                  if (var25.hasProperty(HorizontalDirectionalBlock.FACING)) {
                     var25 = (BlockState)var25.setValue(HorizontalDirectionalBlock.FACING, var9);
                  }
                  if (var25.getRenderShape() == RenderShape.MODEL) {
                     var3.pushPose();
                     var3.translate(var19 + 0.5, var21 + 0.5, var23 + 0.5);
                     float var26 = 0.86F + 0.04F * var12;
                     var3.scale(var26, var26, var26);
                     var3.translate(-0.5, -0.5, -0.5);
                     this.blocks.renderSingleBlock(var25, var3, var14, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
                     var3.popPose();
                  }
                  LevelRenderer.renderLineBox(
                     var3,
                     var4.getBuffer(RenderType.lines()),
                     var19 + 0.04,
                     var21 + 0.04,
                     var23 + 0.04,
                     var19 + 0.96,
                     var21 + 0.96,
                     var23 + 0.96,
                     0.37F,
                     0.89F,
                     1.0F,
                     0.55F + 0.35F * var12
                  );
               }
            }
         }
         if (var15) {
            BlockPos var27 = var1.center().subtract(var8);
            LevelRenderer.renderLineBox(
               var3,
               var4.getBuffer(RenderType.lines()),
               (double)var27.getX() - 1.02,
               -0.02,
               (double)var27.getZ() - 1.02,
               (double)var27.getX() + 2.02,
               3.02,
               (double)var27.getZ() + 2.02,
               0.91F,
               0.66F,
               0.23F,
               0.6F
            );
         }
      }
   }
   public AABB getRenderBoundingBox(T var1) {
      BlockPos var2 = var1.center();
      return new AABB(
            (double)(var2.getX() - 1),
            (double)var2.getY(),
            (double)(var2.getZ() - 1),
            (double)(var2.getX() + 2),
            (double)(var2.getY() + 3),
            (double)(var2.getZ() + 2)
         )
         .inflate(0.5);
   }
   public int getViewDistance() {
      return 96;
   }
   private static record GhostConsumer(VertexConsumer delegate, int alpha) implements VertexConsumer {
      public VertexConsumer vertex(double x, double y, double z) { delegate.vertex(x, y, z); return this; }
      public VertexConsumer color(int r, int g, int b, int a) { delegate.color(r*3/4, g*15/16, Math.min(255,b+24),alpha); return this; }
      public VertexConsumer uv(float u, float v) { delegate.uv(u,v); return this; }
      public VertexConsumer overlayCoords(int u, int v) { delegate.overlayCoords(u,v); return this; }
      public VertexConsumer uv2(int u, int v) { delegate.uv2(u,v); return this; }
      public VertexConsumer normal(float x, float y, float z) { delegate.normal(x,y,z); return this; }
      public void endVertex() { delegate.endVertex(); }
      public void defaultColor(int r,int g,int b,int a) { delegate.defaultColor(r*3/4,g*15/16,Math.min(255,b+24),alpha); }
      public void unsetDefaultColor() { delegate.unsetDefaultColor(); }
   }
}
