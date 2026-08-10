package com.industrialcrops.block;

import java.util.List;
import java.util.function.Supplier;
import com.industrialcrops.block.entity.CropGeneticsBlockEntity;
import com.industrialcrops.crop.CropGenetics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public final class IndustrialGourdCropBlock extends IndustrialCropBlock {
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    };
    private final Supplier<? extends Block> fruitBlock;

    public IndustrialGourdCropBlock(
            BlockBehaviour.Properties properties,
            Supplier<? extends ItemLike> seed,
            Supplier<? extends Block> fruitBlock
    ) {
        super(properties, seed, () -> fruitBlock.get().asItem(), 1, 1);
        this.fruitBlock = fruitBlock;
    }

    @Override
    public void finishGrowth(ServerLevel level, BlockPos pos, BlockState state) {
        super.finishGrowth(level, pos, state);
        tryGrowFruit(level, pos, level.random);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) {
            return;
        }

        if (!isMaxAge(state)) {
            growStemFaster(state, level, pos, random);
            return;
        }

        resolveMaturity(level, pos);
        if (level.getRawBrightness(pos, 0) >= 9 && !hasAdjacentFruit(level, pos)) {
            tryGrowFruit(level, pos, random);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        return !isMaxAge(state) || !hasAdjacentFruit(level, pos);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (!isMaxAge(state)) {
            int age = Math.min(getMaxAge(), getAge(state) + Mth.nextInt(random, 2, 5));
            BlockState grownState = getStateForAge(age);
            level.setBlock(pos, grownState, 2);
            if (age >= getMaxAge()) {
                resolveMaturity(level, pos);
                tryGrowFruit(level, pos, random);
            }
            return;
        }

        tryGrowFruit(level, pos, random);
    }

    private void growStemFaster(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        for (int attempt = 0; attempt < 2; attempt++) {
            BlockState currentState = level.getBlockState(pos);
            if (!currentState.is(this) || isMaxAge(currentState)) {
                if (currentState.is(this) && isMaxAge(currentState)) {
                    tryGrowFruit(level, pos, random);
                }
                return;
            }
            super.randomTick(currentState, level, pos, random);
        }
    }

    private void tryGrowFruit(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos grassTarget = findTarget(level, pos, random, true);
        BlockPos target = grassTarget != null ? grassTarget : findTarget(level, pos, random, false);
        if (target != null) {
            level.setBlockAndUpdate(target, fruitBlock.get().defaultBlockState());
            CropGenetics.Genes genes = getGenes(level, pos);
            if (genes != null
                    && level.getBlockEntity(target) instanceof CropGeneticsBlockEntity fruitGenetics) {
                fruitGenetics.initialize(genes, random);
            }
        }
    }

    private BlockPos findTarget(ServerLevel level, BlockPos pos, RandomSource random, boolean grassOnly) {
        int start = random.nextInt(HORIZONTAL_DIRECTIONS.length);
        for (int index = 0; index < HORIZONTAL_DIRECTIONS.length; index++) {
            Direction direction = HORIZONTAL_DIRECTIONS[(start + index) % HORIZONTAL_DIRECTIONS.length];
            BlockPos target = pos.relative(direction);
            if (!level.isEmptyBlock(target)) {
                continue;
            }

            BlockState support = level.getBlockState(target.below());
            if (grassOnly) {
                if (support.is(Blocks.GRASS_BLOCK)) {
                    return target;
                }
            } else if (canSupportFruit(support)) {
                return target;
            }
        }
        return null;
    }

    private boolean hasAdjacentFruit(net.minecraft.world.level.LevelReader level, BlockPos pos) {
        Block fruit = fruitBlock.get();
        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            if (level.getBlockState(pos.relative(direction)).is(fruit)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canSupportFruit(BlockState support) {
        return support.is(Blocks.GRASS_BLOCK)
                || support.is(Blocks.FARMLAND)
                || support.is(com.industrialcrops.registry.ModBlocks.FERTILE_FARMLAND.get())
                || support.is(BlockTags.DIRT);
    }
}
