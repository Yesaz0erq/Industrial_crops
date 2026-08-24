package com.industrialcrops.block;

import com.industrialcrops.block.entity.EnergyBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class EnergyBatteryBlock extends BioEnergyMachineBlock {
    public EnergyBatteryBlock(Properties properties) { super(properties); }
@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EnergyBatteryBlockEntity(pos, state); }
}
