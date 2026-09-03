package com.industrialcrops.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class FertileFarmlandBlock extends FarmBlock {
    public FertileFarmlandBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MOISTURE, MAX_MOISTURE));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(MOISTURE) != MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
        }
    }

    public static boolean isFertile(BlockState state) {
        return state.is(com.industrialcrops.registry.ModBlocks.FERTILE_FARMLAND.get());
    }
}
