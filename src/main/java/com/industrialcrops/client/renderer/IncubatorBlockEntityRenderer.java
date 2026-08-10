package com.industrialcrops.client.renderer;

import com.industrialcrops.block.IncubatorBlock;
import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.registry.ModEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;

public final class IncubatorBlockEntityRenderer implements BlockEntityRenderer<IncubatorBlockEntity> {
    private static final ResourceLocation GLASS_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IndustrialCrops.MOD_ID,
            "textures/block/slime_converter_front.png"
    );
    private Slime displaySlime;
    private int displayedType = -1;
    private int displayedSize = -1;

    public IncubatorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            IncubatorBlockEntity slime_converter,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (slime_converter.getLevel() == null) {
            return;
        }

        // JSON block-model Y rotations and PoseStack rotations use opposite signs.
        // Negating here keeps the rendered glass/slime aligned with the rotated frame on every axis.
        float facingRotation = -slime_converter.getBlockState().getValue(IncubatorBlock.FACING).toYRot();
        if (slime_converter.hasSlime()) {
            refreshEntity(slime_converter);
            if (displaySlime != null) {
                displaySlime.tickCount = (int) (slime_converter.getLevel().getGameTime() % Integer.MAX_VALUE);
                displaySlime.setYRot(0.0F);
                displaySlime.setYHeadRot(0.0F);
                displaySlime.yBodyRot = 0.0F;
                displaySlime.yBodyRotO = 0.0F;
                poseStack.pushPose();
                poseStack.translate(0.5D, 0.08D, 0.5D);
                // Slime models face local -Z, while the slime_converter window is local +Z.
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F + facingRotation));
                float scale = 0.38F / Math.max(1, slime_converter.getSlimeSize());
                poseStack.scale(scale, scale, scale);
                ContainedSlimeRenderContext.setActive(true);
                try {
                    Minecraft.getInstance().getEntityRenderDispatcher().render(
                            displaySlime,
                            0.0D,
                            0.0D,
                            0.0D,
                            0.0F,
                            partialTick,
                            poseStack,
                            buffers,
                            packedLight
                    );
                } finally {
                    ContainedSlimeRenderContext.setActive(false);
                }
                poseStack.popPose();
            }
        }

        renderGlass(poseStack, buffers, packedLight, packedOverlay, facingRotation);
    }

    private static void renderGlass(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            float facingRotation
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingRotation));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(GLASS_TEXTURE));
        float min = 0.125F;
        float max = 0.875F;
        float z = 0.94F;
        float minU = 3.0F / 16.0F;
        float maxU = 13.0F / 16.0F;
        float minV = 4.0F / 16.0F;
        float maxV = 12.0F / 16.0F;

        addVertex(vertices, pose, min, min, z, minU, maxV, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
        addVertex(vertices, pose, max, min, z, maxU, maxV, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
        addVertex(vertices, pose, max, max, z, maxU, minV, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
        addVertex(vertices, pose, min, max, z, minU, minV, packedLight, packedOverlay, 0.0F, 0.0F, 1.0F);
        poseStack.popPose();
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
            float normalX,
            float normalY,
            float normalZ
    ) {
        vertices.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, normalX, normalY, normalZ);
    }

    private void refreshEntity(IncubatorBlockEntity slime_converter) {
        if (displayedType == slime_converter.getSlimeType() && displayedSize == slime_converter.getSlimeSize()) {
            return;
        }
        EntityType<? extends Slime> type = switch (slime_converter.getSlimeType()) {
            case IncubatorBlockEntity.SLIME_COPPER -> ModEntities.BROWN_CREATE_SLIME.get();
            case IncubatorBlockEntity.SLIME_IRON -> ModEntities.GRAY_GEAR_SLIME.get();
            case IncubatorBlockEntity.SLIME_GOLD -> ModEntities.GOLDEN_REDSTONE_LAMP_SLIME.get();
            default -> EntityType.SLIME;
        };
        displaySlime = type.create(slime_converter.getLevel());
        if (displaySlime != null) {
            displaySlime.setSize(slime_converter.getSlimeSize(), true);
            displaySlime.setYRot(0.0F);
            displaySlime.setYHeadRot(0.0F);
        }
        displayedType = slime_converter.getSlimeType();
        displayedSize = slime_converter.getSlimeSize();
    }
}
