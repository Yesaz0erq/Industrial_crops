package com.industrialcrops.block.entity;
import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public final class MatterReconstructorBlockEntity extends MatterMachineBlockEntity {
    public MatterReconstructorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.MATTER_RECONSTRUCTOR.get(), Kind.RECONSTRUCTOR, pos, state); }
}
