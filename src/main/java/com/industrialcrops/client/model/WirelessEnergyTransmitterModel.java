package com.industrialcrops.client.model;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.WirelessEnergyTransmitterBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class WirelessEnergyTransmitterModel extends GeoModel<WirelessEnergyTransmitterBlockEntity> {
    @Override public ResourceLocation getModelResource(WirelessEnergyTransmitterBlockEntity machine) {
        return ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "geo/block/wireless_crystal_transmitter.geo.json");
    }
    @Override public ResourceLocation getTextureResource(WirelessEnergyTransmitterBlockEntity machine) {
        return ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "textures/block/wireless_crystal_transmitter.png");
    }
    @Override public ResourceLocation getAnimationResource(WirelessEnergyTransmitterBlockEntity machine) {
        return ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "animations/block/wireless_crystal_transmitter.animation.json");
    }
}
