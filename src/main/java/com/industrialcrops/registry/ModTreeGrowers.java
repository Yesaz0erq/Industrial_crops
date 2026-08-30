package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class ModTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> COMET_TREE_FEATURE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "comet_tree"));

    public static final TreeGrower COMET = new TreeGrower(
            "industrialcrops:comet",
            Optional.empty(),
            Optional.of(COMET_TREE_FEATURE),
            Optional.empty());

    private ModTreeGrowers() {
    }
}
