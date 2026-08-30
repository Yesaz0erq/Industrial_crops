package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CopperFluidStorageCabinetBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16_000;
    private final FluidTank tank = new FluidTank(CAPACITY,
            stack -> stack.getFluid() == ModFluids.CONCENTRATED_PLASMA_JUICE.get()) {
        @Override protected void onContentsChanged() { setChanged(); }
    };
    private final LazyOptional<FluidTank> fluidCapability = LazyOptional.of(() -> tank);

    public CopperFluidStorageCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COPPER_FLUID_STORAGE_CABINET.get(), pos, state);
    }

    public FluidTank getTank() { return tank; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Tank")) tank.readFromNBT(tag.getCompound("Tank"));
    }

    @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.FLUID_HANDLER ? fluidCapability.cast() : super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); fluidCapability.invalidate(); }
}
