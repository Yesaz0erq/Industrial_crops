package com.industrialcrops.client.model;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.entity.GoldenRedstoneLampSlime;
import net.minecraft.resources.ResourceLocation;

public final class GoldenRedstoneLampSlimeModel extends IndustrialSlimeModel<GoldenRedstoneLampSlime> {
    public GoldenRedstoneLampSlimeModel() {
        super(
                ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "geo/golden_gear_slime_v2.geo.json"),
                ResourceLocation.fromNamespaceAndPath(
                        IndustrialCrops.MOD_ID,
                        "textures/entity/golden_redstone_lamp_slime/golden_redstone_lamp_slime_atlas.png"
                ),
                ResourceLocation.fromNamespaceAndPath(
                        IndustrialCrops.MOD_ID,
                        "animations/golden_gear_slime_v2.animation.json"
                )
        );
    }
}
