package com.industrialcrops.entity;

import com.industrialcrops.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class SatelliteRocketEntity extends Entity {
   private static final EntityDataAccessor<Integer> TIER = SynchedEntityData.defineId(SatelliteRocketEntity.class, EntityDataSerializers.INT);
   private static final int LIFETIME = 240;

   public SatelliteRocketEntity(EntityType<? extends SatelliteRocketEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public SatelliteRocketEntity(Level level, double x, double y, double z, int tier) {
      this((EntityType<? extends SatelliteRocketEntity>)ModEntities.SATELLITE_ROCKET.get(), level);
      this.setPos(x, y, z);
      this.entityData.set(TIER, tier);
   }

   public int tier() {
      return (Integer)this.entityData.get(TIER);
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(TIER, 1);
   }

   public void tick() {
      super.tick();
      int age = this.tickCount;
      double speed = age < 20 ? 0.01 * (double)age : Math.min(1.8, 0.2 + (double)((age - 20) * (age - 20)) * 9.0E-4);
      this.setDeltaMovement(0.0, speed, 0.0);
      this.setPos(this.getX(), this.getY() + speed, this.getZ());
      Level level = this.level();
      if (level.isClientSide()) {
         int plumes = 2 + this.tier();

         for (int i = 0; i < plumes; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 0.35;
            double oz = (this.random.nextDouble() - 0.5) * 0.35;
            level.addParticle(
               ParticleTypes.FLAME, this.getX() + ox, this.getY() - 0.1, this.getZ() + oz, ox * 0.2, -0.25 - this.random.nextDouble() * 0.2, oz * 0.2
            );
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX() + ox, this.getY() - 0.4, this.getZ() + oz, ox * 0.05, 0.02, oz * 0.05);
         }

         if (age % 2 == 0) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() - 0.2, this.getZ(), 0.0, -0.3, 0.0);
         }
      } else if (age > 240 || this.getY() > (double)(level.getMaxBuildHeight() + 96)) {
         this.discard();
      }
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 262144.0;
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(TIER, tag.getInt("Tier"));
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putInt("Tier", this.tier());
   }
}
