package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class ModTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> COMET_TREE_FEATURE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            new ResourceLocation(IndustrialCrops.MOD_ID, "comet_tree"));

    public static final AbstractTreeGrower COMET = new AbstractTreeGrower() {
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            return COMET_TREE_FEATURE;
        }
    };

    private ModTreeGrowers() { }
}
