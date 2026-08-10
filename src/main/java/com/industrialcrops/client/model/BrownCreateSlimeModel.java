package com.industrialcrops.client.model;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.entity.BrownCreateSlime;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class BrownCreateSlimeModel extends IndustrialSlimeModel<BrownCreateSlime> {
    public BrownCreateSlimeModel() {
        super(
                ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "geo/copper_gear_slime_v2.geo.json"),
                ResourceLocation.fromNamespaceAndPath(
                        IndustrialCrops.MOD_ID,
                        "textures/entity/copper_gear_slime/copper_gear_slime_create_atlas.png"
                ),
                ResourceLocation.fromNamespaceAndPath(
                        IndustrialCrops.MOD_ID,
                        "animations/copper_gear_slime_v2.animation.json"
                )
        );
    }

    @Override
    public RenderType getRenderType(BrownCreateSlime animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
