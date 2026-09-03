package com.industrialcrops.block.entity;
import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public final class MatterDigitizerBlockEntity extends MatterMachineBlockEntity {
    public MatterDigitizerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.MATTER_DIGITIZER.get(), Kind.DIGITIZER, pos, state); }
}
