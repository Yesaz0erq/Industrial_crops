package com.industrialcrops.block.entity;

import com.industrialcrops.crop.CropGenetics;
import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CropGeneticsBlockEntity extends BlockEntity {
    private CropGenetics.Genes genes = new CropGenetics.Genes(0, 1);
    private CropGenetics.Genes seedGenes = genes;
    private boolean initialized;
    private boolean inheritanceResolved;

    public CropGeneticsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CROP_GENETICS.get(), pos, state);
    }

    public void initialize(CropGenetics.Genes plantedGenes, RandomSource random) {
        genes = plantedGenes == null ? CropGenetics.createInitial(random) : plantedGenes;
        genes = CropGenetics.normalize(genes.dominantTier(), genes.recessiveTier());
        seedGenes = genes;
        initialized = true;
        inheritanceResolved = false;
        setChanged();
    }

    public CropGenetics.Genes getOrCreateGenes(RandomSource random) {
        if (!initialized) {
            initialize(null, random);
        }
        return genes;
    }

    public CropGenetics.Genes getGenes() {
        return genes;
    }

    public CropGenetics.Genes getSeedGenes() {
        return inheritanceResolved ? seedGenes : genes;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isInheritanceResolved() {
        return inheritanceResolved;
    }

    public void resolveSeeds(CropGenetics.Genes inheritedGenes) {
        seedGenes = CropGenetics.normalize(
                inheritedGenes.dominantTier(), inheritedGenes.recessiveTier());
        inheritanceResolved = true;
        setChanged();
    }

    public void resetForRegrowth() {
        seedGenes = genes;
        inheritanceResolved = false;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Initialized", initialized);
        tag.putInt("DominantQuality", genes.dominantTier());
        tag.putInt("RecessiveQuality", genes.recessiveTier());
        tag.putInt("SeedDominantQuality", seedGenes.dominantTier());
        tag.putInt("SeedRecessiveQuality", seedGenes.recessiveTier());
        tag.putBoolean("InheritanceResolved", inheritanceResolved);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        initialized = tag.getBoolean("Initialized");
        genes = CropGenetics.normalize(
                tag.getInt("DominantQuality"), tag.getInt("RecessiveQuality"));
        seedGenes = CropGenetics.normalize(
                tag.getInt("SeedDominantQuality"), tag.getInt("SeedRecessiveQuality"));
        inheritanceResolved = tag.getBoolean("InheritanceResolved");
        if (!initialized) {
            seedGenes = genes;
        }
    }
}
