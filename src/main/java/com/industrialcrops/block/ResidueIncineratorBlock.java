package com.industrialcrops.block;

import com.industrialcrops.block.entity.ResidueIncineratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ResidueIncineratorBlock extends BioEnergyMachineBlock {
    public ResidueIncineratorBlock(Properties properties) { super(properties); }
@Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ResidueIncineratorBlockEntity(pos, state); }
}
