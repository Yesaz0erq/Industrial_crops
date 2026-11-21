package com.industrialcrops.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

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

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction face, IPlantable plant) {
        // Forge 1.20 checks the vanilla farmland block by identity for crops and stems.
        return super.canSustainPlant(state, level, pos, face, plant)
            || face == Direction.UP && Blocks.FARMLAND.defaultBlockState().setValue(MOISTURE, state.getValue(MOISTURE))
                .canSustainPlant(level, pos, face, plant);
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(MOISTURE) > 0;
    }

    public static boolean isFertile(BlockState state) {
        return state.is(com.industrialcrops.registry.ModBlocks.FERTILE_FARMLAND.get());
    }
}
