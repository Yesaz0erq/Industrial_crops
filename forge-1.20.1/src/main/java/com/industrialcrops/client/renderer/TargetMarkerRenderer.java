package com.industrialcrops.client.renderer;

import com.industrialcrops.entity.TargetMarkerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/** Draws the shared sky-blue volume outline around the locked impact block. */
public final class TargetMarkerRenderer extends EntityRenderer<TargetMarkerEntity> {
    public TargetMarkerRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(
            TargetMarkerEntity marker,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        WorldVolumeOutlineRenderer.renderBox(
                poseStack,
                buffer,
                new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D),
                WorldVolumeOutlineRenderer.Status.NORMAL
        );
        super.render(marker, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TargetMarkerEntity marker) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
