package com.industrialcrops.block;

import com.industrialcrops.block.entity.BioEnergyGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BioEnergyGeneratorBlock extends BioEnergyMachineBlock {
    public BioEnergyGeneratorBlock(Properties properties) { super(properties); }
@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BioEnergyGeneratorBlockEntity(pos, state); }
}
