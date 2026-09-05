package com.industrialcrops.client.renderer;

import com.industrialcrops.block.IncubatorBlock;
import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.IndustrialCrops;
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
import software.bernie.geckolib.animatable.GeoEntity;

public final class IncubatorBlockEntityRenderer implements BlockEntityRenderer<IncubatorBlockEntity> {
    private static final ResourceLocation GLASS_TEXTURE = new ResourceLocation(
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
                displaySlime.yRotO = 0.0F;
                displaySlime.setYHeadRot(0.0F);
                displaySlime.yHeadRotO = 0.0F;
                displaySlime.yBodyRot = 0.0F;
                displaySlime.yBodyRotO = 0.0F;
                displaySlime.setXRot(0.0F);
                displaySlime.xRotO = 0.0F;
                // This is a render-only entity: never tick its AI or spawn jump particles.
                // Keep GeckoLib's idle/core animation and animate the entire model together.
                displaySlime.setOnGround(true);
                float phase = ((slime_converter.getLevel().getGameTime()
                        + Math.floorMod(slime_converter.getBlockPos().asLong(), 48L)) % 48L + partialTick) / 48.0F;
                float airborne = Math.max(0.0F, (phase - 0.2F) / 0.6F);
                float hop = airborne < 1.0F ? 4.0F * airborne * (1.0F - airborne) : 0.0F;
                float squash = phase < 0.2F ? (float) Math.sin(phase / 0.2F * Math.PI)
                        : phase > 0.8F ? (float) Math.sin((phase - 0.8F) / 0.2F * Math.PI) : 0.0F;
                poseStack.pushPose();
                poseStack.translate(0.5D, 0.14D + hop * 0.12D, 0.5D);
                // Entity renderers already rotate a zero-yaw model by 180 degrees to face +Z.
                // Apply only the block rotation, so the face always points through the glass.
                poseStack.mulPose(Axis.YP.rotationDegrees(facingRotation));
                // Industrial geo models are fixed-size, unlike vanilla slimes' size-scaled renderer.
                float scale = displaySlime instanceof GeoEntity ? 0.62F : 0.95F / displaySlime.getSize();
                poseStack.scale(scale * (1.0F + squash * 0.08F),
                        scale * (1.0F - squash * 0.12F), scale * (1.0F + squash * 0.08F));
                ContainedSlimeRenderContext.setActive(true);
                try {
                    Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(displaySlime).render(
                            displaySlime,
                            0.0F,
                            partialTick,
                            poseStack,
                            buffers,
                            packedLight
                    );
                } finally {
                    ContainedSlimeRenderContext.setActive(false);
                    poseStack.popPose();
                }
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
        vertices.vertex(pose.pose(), x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    private void refreshEntity(IncubatorBlockEntity slime_converter) {
        if (displaySlime != null && displaySlime.level() == slime_converter.getLevel()
                && displayedType == slime_converter.getSlimeType() && displayedSize == slime_converter.getSlimeSize()) {
            return;
        }
        EntityType<? extends Slime> type = IncubatorBlockEntity.getSlimeEntityType(slime_converter.getSlimeType());
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
