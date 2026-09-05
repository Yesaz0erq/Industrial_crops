package com.industrialcrops.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Placeholder used until the device has copied a block without its own block
 * entity. Machines with a block entity replace this instance with an
 * independent instance of the copied machine's real type.
 */
public final class UniversalReplicationDeviceBlockEntity extends BlockEntity {
    public UniversalReplicationDeviceBlockEntity(BlockPos pos, BlockState state) {
        super(com.industrialcrops.registry.CarroteBlockEntities.UNIVERSAL_REPLICATION_DEVICE.get(), pos, state);
    }
}
