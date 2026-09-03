package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class BioEnergyGeneratorBlockEntity extends BioEnergyMachineBlockEntity {
    public BioEnergyGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BIO_ENERGY_GENERATOR.get(), Kind.GENERATOR, pos, state);
    }
}
