package com.industrialcrops.block;

import com.industrialcrops.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Purple comet soil that can sprout a comet sapling when bone-mealed. */
public final class CometSoilBlock extends Block implements BonemealableBlock {
    private static final float SAPLING_CHANCE = 0.35F;

    public CometSoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        BlockPos above = pos.above();
        return level.getBlockState(above).canBeReplaced()
                && level.getFluidState(above).isEmpty();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < SAPLING_CHANCE;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos above = pos.above();
        BlockState sapling = ModBlocks.COMET_SAPLING.get().defaultBlockState();
        if (sapling.canSurvive(level, above)) {
            level.setBlock(above, sapling, 3);
        }
    }
}
