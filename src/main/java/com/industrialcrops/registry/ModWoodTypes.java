package com.industrialcrops.registry;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class ModWoodTypes {
    public static final BlockSetType COMET_SET = BlockSetType.register(
            new BlockSetType("industrialcrops:comet"));
    public static final WoodType COMET = WoodType.register(
            new WoodType("industrialcrops:comet", COMET_SET));

    private ModWoodTypes() {
    }
}
