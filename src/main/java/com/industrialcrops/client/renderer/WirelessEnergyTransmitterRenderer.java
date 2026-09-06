package com.industrialcrops.client.renderer;

import com.industrialcrops.block.entity.WirelessEnergyTransmitterBlockEntity;
import com.industrialcrops.client.model.WirelessEnergyTransmitterModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class WirelessEnergyTransmitterRenderer extends GeoBlockRenderer<WirelessEnergyTransmitterBlockEntity> {
    public WirelessEnergyTransmitterRenderer(BlockEntityRendererProvider.Context context) { super(new WirelessEnergyTransmitterModel()); }
    @Override public RenderType getRenderType(WirelessEnergyTransmitterBlockEntity machine, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
