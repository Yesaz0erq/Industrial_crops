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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;

public final class IndustrialGourdCropBlock extends IndustrialCropBlock {
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    };
    /** The mature stem points at its currently attached fruit, like vanilla gourds. */
    public static final DirectionProperty FRUIT_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
    private final Supplier<? extends Block> fruitBlock;

    public IndustrialGourdCropBlock(
            BlockBehaviour.Properties properties,
            Supplier<? extends ItemLike> seed,
            Supplier<? extends Block> fruitBlock
    ) {
        super(properties, seed, () -> fruitBlock.get().asItem(), 1, 1);
        this.fruitBlock = fruitBlock;
        registerDefaultState(defaultBlockState()
                .setValue(FRUIT_FACING, Direction.NORTH)
                .setValue(ATTACHED, false));
    }

    @Override
    public void finishGrowth(ServerLevel level, BlockPos pos, BlockState state) {
        super.finishGrowth(level, pos, state);
        tryGrowFruit(level, pos, level.random);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
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
        if (syncFruitConnection(level, pos, state)) {
            return;
        }
        if (level.getRawBrightness(pos, 0) >= 9) {
            tryGrowFruit(level, pos, random);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state,
            boolean isClient) {
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
                if (!syncFruitConnection(level, pos, level.getBlockState(pos))) {
                    tryGrowFruit(level, pos, random);
                }
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
        FruitTarget grassTarget = findTarget(level, pos, random, true);
        FruitTarget target = grassTarget != null ? grassTarget : findTarget(level, pos, random, false);
        if (target != null) {
            level.setBlockAndUpdate(target.pos(), fruitBlock.get().defaultBlockState());
            CropGenetics.Genes genes = getGenes(level, pos);
            if (genes != null
                    && level.getBlockEntity(target.pos()) instanceof CropGeneticsBlockEntity fruitGenetics) {
                fruitGenetics.initialize(genes, random);
            }
            connectStem(level, pos, target.direction());
        }
    }

    private FruitTarget findTarget(ServerLevel level, BlockPos pos, RandomSource random, boolean grassOnly) {
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
                    return new FruitTarget(target, direction);
                }
            } else if (canSupportFruit(support)) {
                return new FruitTarget(target, direction);
            }
        }
        return null;
    }

    private boolean syncFruitConnection(Level level, BlockPos pos, BlockState state) {
        Block fruit = fruitBlock.get();
        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            if (level.getBlockState(pos.relative(direction)).is(fruit)) {
                connectStem(level, pos, direction);
                return true;
            }
        }
        if (state.getValue(ATTACHED)) {
            level.setBlock(pos, state.setValue(ATTACHED, false), 2);
        }
        return false;
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

    private static void connectStem(Level level, BlockPos pos, Direction direction) {
        BlockState stem = level.getBlockState(pos);
        if (stem.hasProperty(FRUIT_FACING)
                && (!stem.getValue(ATTACHED) || stem.getValue(FRUIT_FACING) != direction)) {
            level.setBlock(pos, stem.setValue(FRUIT_FACING, direction).setValue(ATTACHED, true), 2);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && isMaxAge(state)) {
            syncFruitConnection(level, pos, state);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FRUIT_FACING, ATTACHED);
    }

    private record FruitTarget(BlockPos pos, Direction direction) {
    }

    private static boolean canSupportFruit(BlockState support) {
        return support.is(Blocks.GRASS_BLOCK)
                || support.is(Blocks.FARMLAND)
                || support.is(com.industrialcrops.registry.ModBlocks.FERTILE_FARMLAND.get())
                || support.is(BlockTags.DIRT);
    }
}
