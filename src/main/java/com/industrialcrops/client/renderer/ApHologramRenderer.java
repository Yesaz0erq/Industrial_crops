package com.industrialcrops.client.renderer;

import com.industrialcrops.block.entity.ApControllerBlockEntity;
import com.industrialcrops.machine.ApStructure;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Incremental preview for large structures. Never loads chunks or draws every part at once. */
public final class ApHologramRenderer<T extends ApControllerBlockEntity> implements BlockEntityRenderer<T> {
   private static final int FULL_BRIGHT = 15728880;
   private static final int MAX_GHOSTS = 256;
   private static final int CHECKS_PER_TICK = 256;
   private final BlockRenderDispatcher blocks;
   private final Map<T, Preview> previews = new WeakHashMap<>();

   public ApHologramRenderer(Context context) {
      this.blocks = context.getBlockRenderDispatcher();
   }

   @Override
   public void render(T controller, float partialTick, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
      Level level = controller.getLevel();
      if (level == null || !controller.projection()) {
         this.previews.remove(controller);
         return;
      }
      ApStructure.Layout layout = controller.layout();
      Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      Preview preview = this.previews.get(controller);
      if (preview == null || preview.layout != layout) {
         preview = new Preview(layout, camera);
         this.previews.put(controller, preview);
      }
      preview.update(level, camera);
      BlockPos origin = controller.getBlockPos();
      float pulse = 0.5F + 0.5F * Mth.sin((level.getGameTime() + partialTick) * 0.12F);
      int alpha = (int)(95.0F + 60.0F * pulse);
      MultiBufferSource ghosts = ignored -> new GhostConsumer(buffers.getBuffer(Sheets.translucentCullBlockSheet()), alpha);

      // Selection is cached and bounded. Recheck only visible entries, so newly placed blocks disappear immediately.
      int drawn = 0;
      for (ApStructure.Part part : preview.visible) {
         if (drawn >= MAX_GHOSTS) break;
         if (!level.hasChunkAt(part.pos()) || part.matches(level)) continue;
         drawn++;
         BlockState current = level.getBlockState(part.pos());
         double x = part.pos().getX() - origin.getX();
         double y = part.pos().getY() - origin.getY();
         double z = part.pos().getZ() - origin.getZ();
         boolean obstructed = part.category() == ApStructure.Category.CLEARANCE || (!current.isAir() && !current.canBeReplaced());
         if (!obstructed) {
            BlockState example = part.example();
            if (example.getRenderShape() == RenderShape.MODEL) {
               poses.pushPose();
               poses.translate(x + 0.5, y + 0.5, z + 0.5);
               float scale = 0.86F + 0.04F * pulse;
               poses.scale(scale, scale, scale);
               poses.translate(-0.5, -0.5, -0.5);
               this.blocks.renderSingleBlock(example, poses, ghosts, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
               poses.popPose();
            }
         }
         LevelRenderer.renderLineBox(poses, buffers.getBuffer(RenderType.lines()),
            x + 0.02, y + 0.02, z + 0.02, x + 0.98, y + 0.98, z + 0.98,
            obstructed ? 1.0F : 0.37F, obstructed ? 0.25F : 0.89F, obstructed ? 0.25F : 1.0F, 0.55F + 0.35F * pulse);
      }
      AABB bounds = layout.bounds().move(-origin.getX(), -origin.getY(), -origin.getZ()).inflate(0.02);
      LevelRenderer.renderLineBox(poses, buffers.getBuffer(RenderType.lines()), bounds, 0.91F, 0.66F, 0.23F, 0.6F);
   }

   public AABB getRenderBoundingBox(T controller) {
      return controller.layout().bounds().inflate(0.5);
   }

   @Override
   public int getViewDistance() {
      return 128;
   }

   private static double distance(ApStructure.Part part, Vec3 camera) {
      return camera.distanceToSqr(part.pos().getX() + 0.5, part.pos().getY() + 0.5, part.pos().getZ() + 0.5);
   }

   private static final class Preview {
      final ApStructure.Layout layout;
      final List<ApStructure.Part> parts;
      final Map<BlockPos, ApStructure.Part> missing = new HashMap<>();
      List<ApStructure.Part> visible = List.of();
      int cursor;
      long lastScan = Long.MIN_VALUE;
      long lastSelect = Long.MIN_VALUE;
      Vec3 selectionCamera;

      Preview(ApStructure.Layout layout, Vec3 camera) {
         this.layout = layout;
         ArrayList<ApStructure.Part> sorted = new ArrayList<>(layout.parts());
         sorted.sort(Comparator.comparingDouble(part -> distance(part, camera)));
         this.parts = List.copyOf(sorted);
         this.selectionCamera = camera;
      }

      void update(Level level, Vec3 camera) {
         long now = level.getGameTime();
         if (this.lastScan != now) {
            this.lastScan = now;
            for (int n = 0; n < Math.min(CHECKS_PER_TICK, this.parts.size()); n++) {
               ApStructure.Part part = this.parts.get(this.cursor++);
               if (this.cursor >= this.parts.size()) this.cursor = 0;
               if (!level.hasChunkAt(part.pos()) || part.matches(level)) this.missing.remove(part.pos());
               else this.missing.put(part.pos(), part);
            }
         }
         if (this.lastSelect == Long.MIN_VALUE || now - this.lastSelect >= 10 || camera.distanceToSqr(this.selectionCamera) >= 4.0) {
            this.lastSelect = now;
            this.selectionCamera = camera;
            this.visible = this.missing.values().stream()
               .sorted(Comparator.comparingDouble(part -> distance(part, camera))).limit(MAX_GHOSTS).toList();
         }
      }
   }

   private static record GhostConsumer(VertexConsumer delegate, int alpha) implements VertexConsumer {
      public VertexConsumer addVertex(float var1, float var2, float var3) {
         this.delegate.addVertex(var1, var2, var3);
         return this;
      }

      public VertexConsumer setColor(int var1, int var2, int var3, int var4) {
         this.delegate.setColor(var1 * 3 / 4, var2 * 15 / 16, Math.min(255, var3 + 24), this.alpha);
         return this;
      }

      public VertexConsumer setUv(float var1, float var2) {
         this.delegate.setUv(var1, var2);
         return this;
      }

      public VertexConsumer setUv1(int var1, int var2) {
         this.delegate.setUv1(var1, var2);
         return this;
      }

      public VertexConsumer setUv2(int var1, int var2) {
         this.delegate.setUv2(var1, var2);
         return this;
      }

      public VertexConsumer setNormal(float var1, float var2, float var3) {
         this.delegate.setNormal(var1, var2, var3);
         return this;
      }

      public void addVertex(float var1, float var2, float var3, int var4, float var5, float var6, int var7, int var8, float var9, float var10, float var11) {
         int var12 = var4 >> 16 & 0xFF;
         int var13 = var4 >> 8 & 0xFF;
         int var14 = var4 & 0xFF;
         int var15 = this.alpha << 24 | var12 * 3 / 4 << 16 | var13 * 15 / 16 << 8 | Math.min(255, var14 + 24);
         this.delegate.addVertex(var1, var2, var3, var15, var5, var6, var7, var8, var9, var10, var11);
      }
   }
}
