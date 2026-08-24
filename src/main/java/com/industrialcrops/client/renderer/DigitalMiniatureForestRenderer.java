package com.industrialcrops.client.renderer;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class DigitalMiniatureForestRenderer implements BlockEntityRenderer<DigitalMiniatureForestBlockEntity> {
    private static final ResourceLocation LEAF_TEXTURE = new ResourceLocation(
            IndustrialCrops.MOD_ID,
            "textures/block/digital_miniature_forest_falling_leaf.png"
    );
    private static final float[][] LEAVES = {
            {0.31F, 0.36F, 0.00F},
            {0.67F, 0.44F, 0.17F},
            {0.39F, 0.68F, 0.34F},
            {0.58F, 0.30F, 0.49F},
            {0.25F, 0.55F, 0.63F},
            {0.72F, 0.63F, 0.78F},
            {0.49F, 0.22F, 0.91F}
    };

    public DigitalMiniatureForestRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            DigitalMiniatureForestBlockEntity forest,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (forest.getLevel() == null) {
            return;
        }
        float cycle = (forest.getLevel().getGameTime() + partialTick) / 80.0F;
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(LEAF_TEXTURE));
        for (int i = 0; i < LEAVES.length; i++) {
            renderLeaf(poseStack, vertices, packedLight, packedOverlay, cycle, i);
        }
    }

    private static void renderLeaf(
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay,
            float cycle,
            int index
    ) {
        float[] leaf = LEAVES[index];
        float progress = cycle + leaf[2];
        progress -= (float) Math.floor(progress);
        float angle = (progress * 360.0F) + index * 47.0F;
        float sway = (float) Math.sin((progress * Math.PI * 2.0) + index) * 0.045F;
        float x = leaf[0] + sway;
        float y = 0.91F - progress * 0.64F;
        float z = leaf[1] + (float) Math.cos((progress * Math.PI * 2.0) + index * 0.7F) * 0.035F;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle * 1.4F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.mulPose(Axis.XP.rotationDegrees(28.0F + angle * 0.45F));
        poseStack.scale(0.055F, 0.038F, 0.055F);
        addDoubleSidedQuad(vertices, poseStack.last(), packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void addDoubleSidedQuad(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            int packedLight,
            int packedOverlay
    ) {
        addVertex(vertices, pose, -0.5F, -0.5F, 0.0F, 0.0F, 1.0F, packedLight, packedOverlay, 1.0F);
        addVertex(vertices, pose, 0.5F, -0.5F, 0.0F, 1.0F, 1.0F, packedLight, packedOverlay, 1.0F);
        addVertex(vertices, pose, 0.5F, 0.5F, 0.0F, 1.0F, 0.0F, packedLight, packedOverlay, 1.0F);
        addVertex(vertices, pose, -0.5F, 0.5F, 0.0F, 0.0F, 0.0F, packedLight, packedOverlay, 1.0F);

        addVertex(vertices, pose, -0.5F, 0.5F, 0.0F, 0.0F, 0.0F, packedLight, packedOverlay, -1.0F);
        addVertex(vertices, pose, 0.5F, 0.5F, 0.0F, 1.0F, 0.0F, packedLight, packedOverlay, -1.0F);
        addVertex(vertices, pose, 0.5F, -0.5F, 0.0F, 1.0F, 1.0F, packedLight, packedOverlay, -1.0F);
        addVertex(vertices, pose, -0.5F, -0.5F, 0.0F, 0.0F, 1.0F, packedLight, packedOverlay, -1.0F);
    }

    private static void addVertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int packedLight,
            int packedOverlay,
            float normalZ
    ) {
        vertices.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 0.0F, normalZ)
                .endVertex();
    }
}
