package com.industrialcrops.block.entity;

import com.industrialcrops.block.SatelliteRocketBlock;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.world.SatelliteNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class SatelliteRocketBlockEntity extends BlockEntity {
   public static final String DURABILITY = "SatelliteDurability";
   public static final String MAX_DURABILITY = "SatelliteMaxDurability";
   private int durability = -1;
   private int maxDurability = -1;

   public SatelliteRocketBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.SATELLITE_ROCKET.get(), pos, state);
   }

   private int tier() {
      return this.getBlockState().getBlock() instanceof SatelliteRocketBlock rocket ? rocket.tier() : 0;
   }

   public int durability() {
      return this.durability >= 0 ? this.durability : SatelliteNetworkData.defaultDurability(this.tier());
   }

   public int maxDurability() {
      return this.maxDurability > 0 ? this.maxDurability : Math.max(1, SatelliteNetworkData.defaultDurability(this.tier()));
   }

   public void set(int durability, int maxDurability) {
      this.durability = Math.max(0, durability);
      this.maxDurability = Math.max(1, maxDurability);
      this.setChanged();
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.putInt("SatelliteDurability", this.durability());
      tag.putInt("SatelliteMaxDurability", this.maxDurability());
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.durability = tag.contains("SatelliteDurability") ? tag.getInt("SatelliteDurability") : -1;
      this.maxDurability = tag.contains("SatelliteMaxDurability") ? tag.getInt("SatelliteMaxDurability") : -1;
   }
}
