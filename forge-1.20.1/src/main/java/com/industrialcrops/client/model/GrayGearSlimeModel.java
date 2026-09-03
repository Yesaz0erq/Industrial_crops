package com.industrialcrops.client.model;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.entity.GrayGearSlime;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class GrayGearSlimeModel extends IndustrialSlimeModel<GrayGearSlime> {
    public GrayGearSlimeModel() {
        super(
                new ResourceLocation(IndustrialCrops.MOD_ID, "geo/gray_gear_slime_v2.geo.json"),
                new ResourceLocation(
                        IndustrialCrops.MOD_ID,
                        "textures/entity/gray_gear_slime/gray_gear_slime_create_atlas.png"
                ),
                new ResourceLocation(
                        IndustrialCrops.MOD_ID,
                        "animations/gray_gear_slime_v2.animation.json"
                )
        );
    }

    @Override
    public RenderType getRenderType(GrayGearSlime animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
