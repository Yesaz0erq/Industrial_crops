package com.industrialcrops.client.renderer;

import com.industrialcrops.block.CrystalSteelWorkbenchBlock;
import com.industrialcrops.block.entity.CrystalSteelWorkbenchBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import net.minecraft.resources.ResourceLocation;

/** The front gauge is fed by the same synchronized tank as the menu and pipes. */
public final class CrystalSteelWorkbenchRenderer implements BlockEntityRenderer<CrystalSteelWorkbenchBlockEntity> {
    public CrystalSteelWorkbenchRenderer(BlockEntityRendererProvider.Context context) {}

    @Override public void render(CrystalSteelWorkbenchBlockEntity entity, float partialTick, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        var fluid = entity.getTank().getFluid();
        if (fluid.isEmpty()) return;
        float fraction = net.minecraft.util.Mth.clamp(fluid.getAmount() / (float) entity.getTank().getCapacity(), 0, 1);
        int color = 0xFFFFFFFF;
        poses.pushPose();
        poses.translate(0.5, 0, 0.5);
        poses.mulPose(Axis.YP.rotationDegrees(180 - entity.getBlockState().getValue(CrystalSteelWorkbenchBlock.FACING).toYRot()));
        poses.translate(-0.5, 0, -0.5);
        var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation("industrialcrops", "textures/block/workbench_gauge_blue.png")));
        float left = 10 / 32F, right = 22 / 32F, bottom = 6 / 32F;
        float top = bottom + Math.max(1 / 32F, 20 / 32F * fraction);
        float u0 = 0, u1 = 1, v1 = 1;
        float v0 = 1 - fraction;
        // Opaque machine blocks can receive zero block-entity light. This is a luminous indicator.
        light = LightTexture.FULL_BRIGHT;
        vertex(vertices, poses.last(), right, bottom, u0, v1, color, light, overlay);
        vertex(vertices, poses.last(), left, bottom, u1, v1, color, light, overlay);
        vertex(vertices, poses.last(), left, top, u1, v0, color, light, overlay);
        vertex(vertices, poses.last(), right, top, u0, v0, color, light, overlay);
        // The ruler covers the fill, instead of occupying a separate black column.
        poses.translate(0, 0, -0.001F);
        var ticks = buffers.getBuffer(RenderType.entityCutoutNoCull(new ResourceLocation(
                "industrialcrops", "textures/block/workbench_gauge_ticks.png")));
        vertex(ticks, poses.last(), 1, 0, 0, 1, color, light, overlay);
        vertex(ticks, poses.last(), 0, 0, 1, 1, color, light, overlay);
        vertex(ticks, poses.last(), 0, 1, 1, 0, color, light, overlay);
        vertex(ticks, poses.last(), 1, 1, 0, 0, color, light, overlay);
        poses.popPose();
    }

    private static void vertex(VertexConsumer v, PoseStack.Pose pose, float x, float y, float u, float t,
                               int color, int light, int overlay) {
        v.vertex(pose.pose(), x, y, -0.001F).color(color).uv(u, t).overlayCoords(overlay)
                .uv2(light).normal(pose.normal(), 0, 0, -1).endVertex();
    }
}
