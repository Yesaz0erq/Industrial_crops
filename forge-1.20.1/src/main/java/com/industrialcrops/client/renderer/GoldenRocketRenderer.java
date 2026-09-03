package com.industrialcrops.client.renderer;

import com.industrialcrops.entity.GoldenRocketEntity;
import com.industrialcrops.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the flying explosive potato using the same item model shown in the silo slot. */
public final class GoldenRocketRenderer extends EntityRenderer<GoldenRocketEntity> {
    private final ItemRenderer itemRenderer;

    public GoldenRocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.15F;
    }

    @Override
    public void render(
            GoldenRocketEntity rocket,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rocket.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(rocket.getXRot()));
        poseStack.scale(0.9F, 0.9F, 0.9F);
        itemRenderer.renderStatic(
                new ItemStack(ModItems.EXPLOSIVE_POTATO.get()),
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                rocket.level(),
                rocket.getId()
        );
        poseStack.popPose();
        super.render(rocket, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenRocketEntity rocket) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
