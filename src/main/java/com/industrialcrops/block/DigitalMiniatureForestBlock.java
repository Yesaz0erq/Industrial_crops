package com.industrialcrops.block;

import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class DigitalMiniatureForestBlock extends GoldPoweredMachineBlock {
    public static final MapCodec<DigitalMiniatureForestBlock> CODEC = simpleCodec(DigitalMiniatureForestBlock::new);
    public DigitalMiniatureForestBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends GoldPoweredMachineBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DigitalMiniatureForestBlockEntity(pos, state); }
}
