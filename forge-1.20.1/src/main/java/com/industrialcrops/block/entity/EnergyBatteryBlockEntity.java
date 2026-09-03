package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class EnergyBatteryBlockEntity extends BioEnergyMachineBlockEntity {
    public EnergyBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_BATTERY.get(), Kind.BATTERY, pos, state);
    }
}
