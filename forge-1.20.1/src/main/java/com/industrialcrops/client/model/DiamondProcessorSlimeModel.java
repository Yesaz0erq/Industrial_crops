package com.industrialcrops.client.model;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.entity.DiamondProcessorSlime;
import net.minecraft.resources.ResourceLocation;

public final class DiamondProcessorSlimeModel extends IndustrialSlimeModel<DiamondProcessorSlime> {
    public DiamondProcessorSlimeModel() {
        super(
                new ResourceLocation(
                        IndustrialCrops.MOD_ID,
                        "geo/blue_processor_slime.geo.json"
                ),
                new ResourceLocation(
                        IndustrialCrops.MOD_ID,
                        "textures/entity/blue_processor_slime.png"
                ),
                new ResourceLocation(
                        IndustrialCrops.MOD_ID,
                        "animations/blue_processor_slime.animation.json"
                )
        );
    }
}
