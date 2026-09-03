package com.industrialcrops.client.renderer;

import com.industrialcrops.client.model.GoldenRedstoneLampSlimeModel;
import com.industrialcrops.entity.GoldenRedstoneLampSlime;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;

public final class GoldenRedstoneLampSlimeRenderer extends IndustrialSlimeRenderer<GoldenRedstoneLampSlime> {
    public GoldenRedstoneLampSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new GoldenRedstoneLampSlimeModel(), 0.35F, "industrialcrops_golden_slime_shell", false);
    }

    @Override
    protected @Nullable RenderType getRenderTypeOverrideForExtraBone(GeoBone bone, ResourceLocation texture) {
        if ("core".equals(bone.getName())) {
            return RenderType.entityTranslucentEmissive(texture);
        }
        return null;
    }
}
