package com.industrialcrops.block;

import com.industrialcrops.block.entity.ResidueIncineratorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ResidueIncineratorBlock extends BioEnergyMachineBlock {
    public static final MapCodec<ResidueIncineratorBlock> CODEC = simpleCodec(ResidueIncineratorBlock::new);
    public ResidueIncineratorBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BioEnergyMachineBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ResidueIncineratorBlockEntity(pos, state); }
}
