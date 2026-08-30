package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class CopperFluidStorageCabinetBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16_000;
    private final FluidTank tank = new FluidTank(CAPACITY,
            stack -> stack.getFluid() == ModFluids.CONCENTRATED_PLASMA_JUICE.get()) {
        @Override protected void onContentsChanged() { setChanged(); }
    };

    public CopperFluidStorageCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COPPER_FLUID_STORAGE_CABINET.get(), pos, state);
    }

    public FluidTank getTank() { return tank; }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Tank")) tank.readFromNBT(registries, tag.getCompound("Tank"));
    }
}
