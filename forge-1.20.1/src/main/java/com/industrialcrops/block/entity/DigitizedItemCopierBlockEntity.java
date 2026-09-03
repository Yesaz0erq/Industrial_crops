package com.industrialcrops.block.entity;
import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public final class DigitizedItemCopierBlockEntity extends MatterMachineBlockEntity {
    public DigitizedItemCopierBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.DIGITIZED_ITEM_COPIER.get(), Kind.COPIER, pos, state); }
}
