package com.industrialcrops.client.renderer;

import com.industrialcrops.client.model.FluxSlimeModel;
import com.industrialcrops.entity.FluxSlime;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;

public final class FluxSlimeRenderer extends IndustrialSlimeRenderer<FluxSlime> {
    public FluxSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new FluxSlimeModel(), 0.5F, "industrialcrops_flux_slime_shell", true);
    }

    @Override
    protected @Nullable RenderType getRenderTypeOverrideForExtraBone(GeoBone bone, ResourceLocation texture) {
        // The ore-crystal core and its orbiting shards glow through the gel.
        if ("ore_core".equals(bone.getName()) || "shards".equals(bone.getName())) {
            return RenderType.entityTranslucentEmissive(texture);
        }
        return null;
    }
}
