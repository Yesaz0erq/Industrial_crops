package com.industrialcrops.client.model;
import com.industrialcrops.block.entity.SatelliteReceiverBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
public final class SatelliteReceiverModel extends GeoModel<SatelliteReceiverBlockEntity> {
   public ResourceLocation getModelResource(SatelliteReceiverBlockEntity machine) {
      return new ResourceLocation("industrialcrops", "geo/block/satellite_receiver.geo.json");
   }
   public ResourceLocation getTextureResource(SatelliteReceiverBlockEntity machine) {
      return new ResourceLocation("industrialcrops", "textures/block/satellite_receiver.png");
   }
   public ResourceLocation getAnimationResource(SatelliteReceiverBlockEntity machine) {
      return new ResourceLocation("industrialcrops", "animations/block/satellite_receiver.animation.json");
   }
}
