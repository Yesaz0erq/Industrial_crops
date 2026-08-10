package com.industrialcrops.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/**
 * Shared Create-style world outline used by placement previews, target locks and
 * any future in-world selection boxes.
 */
public final class WorldVolumeOutlineRenderer {
    public static final int SKY_BLUE = 0x66CCFF;
    public static final int INVALID_RED = 0xFF4545;

    private static final double LINE_WIDTH = 1.0D / 16.0D;
    private static final double SURFACE_OFFSET = 1.0D / 128.0D;
    private static final float ALPHA = 0.88F;

    public enum Status {
        NORMAL(SKY_BLUE),
        INVALID(INVALID_RED);

        private final int color;

        Status(int color) {
            this.color = color;
        }
    }

    private WorldVolumeOutlineRenderer() {}

    public static RenderType renderType() {
        return RenderType.debugFilledBox();
    }

    public static void renderBlock(
            PoseStack poseStack,
            MultiBufferSource buffers,
            BlockPos pos,
            Status status
    ) {
        renderBox(poseStack, buffers, new AABB(pos), status);
    }

    public static void renderBox(
            PoseStack poseStack,
            MultiBufferSource buffers,
            AABB bounds,
            Status status
    ) {
        VertexConsumer vertices = buffers.getBuffer(renderType());
        AABB box = bounds.inflate(SURFACE_OFFSET);
        float red = ((status.color >> 16) & 0xFF) / 255.0F;
        float green = ((status.color >> 8) & 0xFF) / 255.0F;
        float blue = (status.color & 0xFF) / 255.0F;
        double halfWidth = LINE_WIDTH * 0.5D;

        for (double y : new double[] {box.minY, box.maxY}) {
            for (double z : new double[] {box.minZ, box.maxZ}) {
                addEdge(poseStack, vertices,
                        box.minX - halfWidth, y - halfWidth, z - halfWidth,
                        box.maxX + halfWidth, y + halfWidth, z + halfWidth,
                        red, green, blue);
            }
        }
        for (double x : new double[] {box.minX, box.maxX}) {
            for (double z : new double[] {box.minZ, box.maxZ}) {
                addEdge(poseStack, vertices,
                        x - halfWidth, box.minY - halfWidth, z - halfWidth,
                        x + halfWidth, box.maxY + halfWidth, z + halfWidth,
                        red, green, blue);
            }
        }
        for (double x : new double[] {box.minX, box.maxX}) {
            for (double y : new double[] {box.minY, box.maxY}) {
                addEdge(poseStack, vertices,
                        x - halfWidth, y - halfWidth, box.minZ - halfWidth,
                        x + halfWidth, y + halfWidth, box.maxZ + halfWidth,
                        red, green, blue);
            }
        }
    }

    private static void addEdge(
            PoseStack poseStack,
            VertexConsumer vertices,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            float red,
            float green,
            float blue
    ) {
        LevelRenderer.addChainedFilledBoxVertices(
                poseStack, vertices,
                minX, minY, minZ,
                maxX, maxY, maxZ,
                red, green, blue, ALPHA
        );
    }
}
