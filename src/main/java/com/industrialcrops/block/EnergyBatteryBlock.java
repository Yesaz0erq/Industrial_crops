package com.industrialcrops.block;

import com.industrialcrops.block.entity.EnergyBatteryBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class EnergyBatteryBlock extends BioEnergyMachineBlock {
    public static final MapCodec<EnergyBatteryBlock> CODEC = simpleCodec(EnergyBatteryBlock::new);
    public EnergyBatteryBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BioEnergyMachineBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EnergyBatteryBlockEntity(pos, state); }
}
