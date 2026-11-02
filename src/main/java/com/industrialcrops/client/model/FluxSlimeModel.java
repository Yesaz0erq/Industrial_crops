package com.industrialcrops.client.model;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.entity.FluxSlime;
import net.minecraft.resources.ResourceLocation;

public final class FluxSlimeModel extends IndustrialSlimeModel<FluxSlime> {
    public FluxSlimeModel() {
        super(
                new ResourceLocation(IndustrialCrops.MOD_ID, "geo/flux_slime.geo.json"),
                new ResourceLocation(IndustrialCrops.MOD_ID, "textures/entity/flux_slime.png"),
                new ResourceLocation(IndustrialCrops.MOD_ID, "animations/flux_slime.animation.json")
        );
    }
}
