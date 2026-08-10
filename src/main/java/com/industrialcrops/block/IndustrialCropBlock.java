package com.industrialcrops.block;

import com.industrialcrops.block.entity.CropGeneticsBlockEntity;
import com.industrialcrops.crop.CropGenetics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class IndustrialCropBlock extends CropBlock implements EntityBlock {
    private static final Direction[] CROSS_DIRECTIONS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };
    public static final BooleanProperty SEEDS_EXTRACTED = BooleanProperty.create("seeds_extracted");
    private final Supplier<? extends ItemLike> seed;
    private final Supplier<? extends ItemLike> harvest;
    private final int matureDropMinimum;
    private final int matureDropRange;
    private final boolean resetsAfterBagHarvest;

    public IndustrialCropBlock(BlockBehaviour.Properties properties, Supplier<? extends ItemLike> seed) {
        this(properties, seed, seed, 4, 2);
    }

    public IndustrialCropBlock(BlockBehaviour.Properties properties,
            Supplier<? extends ItemLike> seed, Supplier<? extends ItemLike> harvest,
            int matureDropMinimum, int matureDropRange) {
        this(properties, seed, harvest, matureDropMinimum, matureDropRange, false);
    }

    public IndustrialCropBlock(BlockBehaviour.Properties properties,
            Supplier<? extends ItemLike> seed, Supplier<? extends ItemLike> harvest,
            int matureDropMinimum, int matureDropRange, boolean resetsAfterBagHarvest) {
        super(properties);
        this.seed = seed;
        this.harvest = harvest;
        this.matureDropMinimum = matureDropMinimum;
        this.matureDropRange = matureDropRange;
        this.resetsAfterBagHarvest = resetsAfterBagHarvest;
        registerDefaultState(defaultBlockState().setValue(SEEDS_EXTRACTED, false));
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }

    public ItemLike getSeedItem() {
        return seed.get();
    }

    public boolean wereSeedsExtracted(BlockState state) {
        return state.getValue(SEEDS_EXTRACTED);
    }

    public BlockState markSeedsExtracted(BlockState state) {
        return state.setValue(SEEDS_EXTRACTED, true);
    }

    public boolean resetsAfterBagHarvest() {
        return resetsAfterBagHarvest;
    }

    public BlockState resetAfterBagHarvest() {
        return getStateForAge(0).setValue(SEEDS_EXTRACTED, false);
    }

    public void finishGrowth(ServerLevel level, BlockPos pos, BlockState state) {
        if (!isMaxAge(state)) {
            level.setBlock(pos, getStateForAge(getMaxAge()), 3);
            resolveMaturity(level, pos);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockPos pos = BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN));
        if (isMaxAge(state)) {
            resolveMaturity(builder.getLevel(), pos);
        }

        CropGenetics.Genes growthGenes = null;
        CropGenetics.Genes offspringGenes = null;
        BlockEntity droppedBlockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (droppedBlockEntity instanceof CropGeneticsBlockEntity genetics) {
            growthGenes = genetics.getOrCreateGenes(builder.getLevel().random);
            offspringGenes = isMaxAge(state) ? genetics.getSeedGenes() : growthGenes;
        }
        if (growthGenes == null) {
            growthGenes = getGenes(builder.getLevel(), pos);
            offspringGenes = growthGenes;
        }
        int count = 1;
        if (isMaxAge(state)) {
            double multiplier = growthGenes == null
                    ? 1.0D : growthGenes.dominantQuality().yieldMultiplier();
            int minimum = (int) Math.round(matureDropMinimum * multiplier);
            int maximum = (int) Math.round((matureDropMinimum + matureDropRange) * multiplier);
            count = minimum + (maximum > minimum
                    ? builder.getLevel().random.nextInt(maximum - minimum + 1) : 0);
        }
        if (builder.getLevel().getBlockState(pos.below()).is(com.industrialcrops.registry.ModBlocks.FERTILE_FARMLAND.get())) {
            count *= 2;
        }
        ItemStack result = new ItemStack(harvest.get().asItem(), count);
        if (offspringGenes != null) {
            CropGenetics.write(result, offspringGenes);
        }
        return List.of(result);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isMaxAge(state)) {
            resolveMaturity(level, pos);
            return;
        }

        CropGenetics.Genes genes = getGenes(level, pos);
        double durationMultiplier = genes == null
                ? 1.0D : genes.dominantQuality().growthDurationMultiplier();
        int baseAttempts = level.getBlockState(pos.below())
                .is(com.industrialcrops.registry.ModBlocks.FERTILE_FARMLAND.get()) ? 3 : 2;
        double desiredAttempts = baseAttempts / durationMultiplier;
        int attempts = (int) Math.floor(desiredAttempts);
        if (random.nextDouble() < desiredAttempts - attempts) {
            attempts++;
        }

        for (int attempt = 0; attempt < attempts; attempt++) {
            BlockState current = level.getBlockState(pos);
            if (!current.is(this) || isMaxAge(current)) {
                break;
            }
            super.randomTick(current, level, pos, random);
            BlockState grown = level.getBlockState(pos);
            if (grown.is(this) && isMaxAge(grown)) {
                resolveMaturity(level, pos);
                break;
            }
        }
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        super.growCrops(level, pos, state);
        if (level instanceof ServerLevel serverLevel && isMaxAge(level.getBlockState(pos))) {
            resolveMaturity(serverLevel, pos);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics) {
            genetics.initialize(CropGenetics.read(stack), level.random);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack result = new ItemStack(getBaseSeedId());
        if (level.getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics
                && genetics.isInitialized()) {
            CropGenetics.write(result, genetics.getSeedGenes());
        }
        return result;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CropGeneticsBlockEntity(pos, state);
    }

    public ItemStack createSeedStack(Level level, BlockPos pos, int count) {
        ItemStack seedStack = new ItemStack(getSeedItem().asItem(), count);
        if (level.getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics) {
            CropGenetics.write(seedStack, genetics.getOrCreateGenes(level.random));
            if (genetics.isInheritanceResolved()) {
                CropGenetics.write(seedStack, genetics.getSeedGenes());
            }
        } else {
            CropGenetics.initializeInitial(seedStack, level.random);
        }
        return seedStack;
    }

    public void resetGeneticsForRegrowth(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics) {
            genetics.resetForRegrowth();
        }
    }

    protected final CropGenetics.Genes getGenes(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics) {
            return genetics.getOrCreateGenes(level.random);
        }
        return null;
    }

    protected final void resolveMaturity(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics)
                || genetics.isInheritanceResolved()) {
            return;
        }

        CropGenetics.Genes ownGenes = genetics.getOrCreateGenes(level.random);
        List<CropGeneticsBlockEntity> neighbors = new ArrayList<>(4);
        for (Direction direction : CROSS_DIRECTIONS) {
            BlockPos neighborPos = pos.relative(direction);
            if (level.getBlockState(neighborPos).is(this)
                    && level.getBlockEntity(neighborPos) instanceof CropGeneticsBlockEntity neighbor) {
                neighbors.add(neighbor);
            }
        }

        if (neighbors.isEmpty()) {
            genetics.resolveSeeds(ownGenes);
            return;
        }

        CropGeneticsBlockEntity neighbor = neighbors.get(level.random.nextInt(neighbors.size()));
        CropGenetics.Genes neighborGenes = neighbor.getOrCreateGenes(level.random);
        CropGenetics.Genes ownInheritedGenes = CropGenetics.cross(
                ownGenes, neighborGenes, level.random);
        genetics.resolveSeeds(ownInheritedGenes);
        if (!neighbor.isInheritanceResolved()) {
            neighbor.resolveSeeds(CropGenetics.cross(
                    ownGenes, neighborGenes, level.random));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SEEDS_EXTRACTED);
    }
}
