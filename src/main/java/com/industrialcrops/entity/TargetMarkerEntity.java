package com.industrialcrops.entity;

import com.industrialcrops.block.entity.GoldenLaunchSiloBlockEntity;
import com.industrialcrops.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Persistent, non-interactive anchor used to render the selected impact block. */
public final class TargetMarkerEntity extends Entity {
    private static final EntityDataAccessor<BlockPos> SILO_POS = SynchedEntityData.defineId(
            TargetMarkerEntity.class,
            EntityDataSerializers.BLOCK_POS
    );

    public TargetMarkerEntity(EntityType<TargetMarkerEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public TargetMarkerEntity(Level level, BlockPos siloPos, BlockPos target) {
        this(ModEntities.TARGET_MARKER.get(), level);
        entityData.set(SILO_POS, siloPos);
        setPos(target.getX(), target.getY(), target.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SILO_POS, BlockPos.ZERO);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        if (!level().isClientSide() && tickCount % 40 == 0 && level().hasChunkAt(getSiloPos())) {
            if (!(level().getBlockEntity(getSiloPos()) instanceof GoldenLaunchSiloBlockEntity silo)
                    || !silo.hasTarget()
                    || !silo.getTarget().equals(blockPosition())) {
                discard();
            }
        }
    }

    public BlockPos getSiloPos() {
        return entityData.get(SILO_POS);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(SILO_POS, new BlockPos(tag.getInt("SiloX"), tag.getInt("SiloY"), tag.getInt("SiloZ")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        BlockPos siloPos = getSiloPos();
        tag.putInt("SiloX", siloPos.getX());
        tag.putInt("SiloY", siloPos.getY());
        tag.putInt("SiloZ", siloPos.getZ());
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
