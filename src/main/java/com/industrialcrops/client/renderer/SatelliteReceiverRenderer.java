package com.industrialcrops.client.renderer;

import com.industrialcrops.block.entity.SatelliteReceiverBlockEntity;
import com.industrialcrops.client.model.SatelliteReceiverModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class SatelliteReceiverRenderer extends GeoBlockRenderer<SatelliteReceiverBlockEntity> {
   public SatelliteReceiverRenderer(Context context) {
      super(new SatelliteReceiverModel());
   }

   public RenderType getRenderType(SatelliteReceiverBlockEntity machine, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
      return RenderType.entityTranslucent(texture);
   }
}
