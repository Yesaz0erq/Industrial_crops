package com.industrialcrops.block;

import com.industrialcrops.block.entity.ElectricFurnaceBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ElectricFurnaceBlock extends GoldPoweredMachineBlock {
    public static final MapCodec<ElectricFurnaceBlock> CODEC = simpleCodec(ElectricFurnaceBlock::new);
    public ElectricFurnaceBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends GoldPoweredMachineBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ElectricFurnaceBlockEntity(pos, state); }
}
