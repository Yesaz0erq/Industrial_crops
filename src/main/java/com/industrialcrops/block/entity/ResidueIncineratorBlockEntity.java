package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class ResidueIncineratorBlockEntity extends BioEnergyMachineBlockEntity {
    public ResidueIncineratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESIDUE_INCINERATOR.get(), Kind.INCINERATOR, pos, state);
    }
}
