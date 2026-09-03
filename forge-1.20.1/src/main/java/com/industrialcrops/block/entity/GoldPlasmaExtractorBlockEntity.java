package com.industrialcrops.block.entity;

import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModFluids;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.GoldPlasmaExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GoldPlasmaExtractorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int PROCESS_TICKS = 160;
    public static final int ENERGY_CAPACITY = 100_000;
    public static final int FLUID_CAPACITY = 8_000;
    private static final int ENERGY_PER_TICK = 40;
    private static final int RECEIVE_RATE = 5_000;
    private int progress;
    private int syncedFluidAmount;
    private final TrackedEnergyStorage energy = new TrackedEnergyStorage();
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.is(ModItems.PLASMA_BERRY.get()); }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final FluidTank outputTank = new FluidTank(FLUID_CAPACITY,
            stack -> stack.getFluid() == ModFluids.CONCENTRATED_PLASMA_JUICE.get()) {
        @Override protected void onContentsChanged() { setChanged(); }
    };
    private final IFluidHandler outputHandler = new IFluidHandler() {
        @Override public int getTanks() { return outputTank.getTanks(); }
        @Override public FluidStack getFluidInTank(int tank) { return outputTank.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return outputTank.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return outputTank.drain(resource, action); }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return outputTank.drain(maxDrain, action); }
    };
    private final LazyOptional<ItemStackHandler> itemCapability = LazyOptional.of(() -> inventory);
    private final LazyOptional<EnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private final LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> outputHandler);
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return switch (index) {
            case 0 -> energy.getEnergyStored() & 0xFFFF;
            case 1 -> energy.getEnergyStored() >>> 16;
            case 2 -> progress;
            case 3 -> PROCESS_TICKS;
            case 4 -> level != null && level.isClientSide() ? syncedFluidAmount : outputTank.getFluidAmount();
            case 5 -> FLUID_CAPACITY;
            default -> 0;
        }; }
        @Override public void set(int index, int value) {
            if (index == 0) energy.setStored((energy.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF));
            else if (index == 1) energy.setStored((energy.getEnergyStored() & 0xFFFF) | ((value & 0xFFFF) << 16));
            else if (index == 2) progress = value;
            else if (index == 4) syncedFluidAmount = Math.max(0, Math.min(FLUID_CAPACITY, value));
        }
        @Override public int getCount() { return 6; }
    };

    public GoldPlasmaExtractorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(), pos, state); }

    public static void tick(Level level, BlockPos pos, BlockState state, GoldPlasmaExtractorBlockEntity machine) {
        if (level.isClientSide()) return;
        PoweredMachineSupport.pullEnergy(level, pos, machine.energy, RECEIVE_RATE);
        if (machine.inventory.getStackInSlot(0).isEmpty() || machine.outputTank.getFluidAmount() + 1_000 > FLUID_CAPACITY) {
            machine.progress = 0;
            return;
        }
        if (machine.energy.getEnergyStored() < ENERGY_PER_TICK) return;
        machine.energy.consume(ENERGY_PER_TICK);
        machine.progress++;
        if (machine.progress >= PROCESS_TICKS) {
            int filled = machine.outputTank.fill(new FluidStack(ModFluids.CONCENTRATED_PLASMA_JUICE.get(), 1_000),
                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            if (filled == 1_000) machine.inventory.extractItem(0, 1, false);
            machine.progress = 0;
        }
        machine.setChanged();
    }

    public int insertInput(ItemStack stack, boolean simulate) {
        ItemStack remainder = inventory.insertItem(0, stack, simulate);
        return stack.getCount() - remainder.getCount();
    }
    public ItemStackHandler getInventory() { return inventory; }
    public EnergyStorage getEnergyStorage(@Nullable Direction side) { return energy; }
    public FluidTank getOutputTank() { return outputTank; }
    public IFluidHandler getOutputHandler() { return outputHandler; }
    public ContainerData getData() { return data; }
    @Override public Component getDisplayName() { return Component.translatable("block.industrialcrops.gold_plasma_extractor"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new GoldPlasmaExtractorMenu(id, inv, this, worldPosition); }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put("Inventory", inventory.serializeNBT());
        tag.put("Tank", outputTank.writeToNBT(new CompoundTag())); tag.putInt("Energy", energy.getEnergyStored()); tag.putInt("Progress", progress);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
        if (tag.contains("Tank")) outputTank.readFromNBT(tag.getCompound("Tank")); energy.setStored(tag.getInt("Energy")); progress = Math.max(0, Math.min(PROCESS_TICKS, tag.getInt("Progress")));
    }
    @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemCapability.cast();
        if (cap == ForgeCapabilities.ENERGY) return energyCapability.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return fluidCapability.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps(); itemCapability.invalidate(); energyCapability.invalidate(); fluidCapability.invalidate();
    }
    private final class TrackedEnergyStorage extends EnergyStorage {
        private TrackedEnergyStorage() { super(ENERGY_CAPACITY, RECEIVE_RATE, 0); }
        private void setStored(int value) { energy = Math.max(0, Math.min(capacity, value)); }
        private void consume(int value) { energy = Math.max(0, energy - value); }
        @Override public int receiveEnergy(int amount, boolean simulate) { int accepted = super.receiveEnergy(amount, simulate); if (accepted > 0 && !simulate) setChanged(); return accepted; }
    }
}
