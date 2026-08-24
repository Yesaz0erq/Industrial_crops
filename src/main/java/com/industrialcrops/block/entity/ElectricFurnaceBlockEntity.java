package com.industrialcrops.block.entity;

import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.ElectricFurnaceMenu;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class ElectricFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CHANNELS = 3;
    public static final int INPUT_START = 0;
    public static final int OUTPUT_START = 3;
    public static final int UPGRADE_START = 6;
    public static final int UPGRADE_COUNT = 4;
    public static final int PROCESS_TICKS = 100;
    public static final int ENERGY_CAPACITY = 100_000;
    private static final int ENERGY_PER_BASE_TICK = 20;
    private static final int RECEIVE_RATE = 5_000;

    private final int[] progress = new int[CHANNELS];
    private final TrackedEnergyStorage energy = new TrackedEnergyStorage();
    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_START + UPGRADE_COUNT) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= UPGRADE_START) return SpeedUpgradeHelper.isSpeedUpgrade(stack);
            return slot >= INPUT_START && slot < OUTPUT_START;
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> energy.getEnergyStored() >>> 16;
                case 2, 3, 4 -> progress[index - 2];
                case 5 -> PROCESS_TICKS;
                case 6 -> SpeedUpgradeHelper.tier(inventory, UPGRADE_START, UPGRADE_COUNT);
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) energy.setStored((energy.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF));
            else if (index == 1) energy.setStored((energy.getEnergyStored() & 0xFFFF) | ((value & 0xFFFF) << 16));
            else if (index >= 2 && index <= 4) progress[index - 2] = value;
        }
        @Override public int getCount() { return 7; }
    };

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FURNACE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity furnace) {
        if (level.isClientSide()) return;
        PoweredMachineSupport.pullEnergy(level, pos, furnace.energy, RECEIVE_RATE);
        int step = SpeedUpgradeHelper.progressStep(furnace.inventory, UPGRADE_START, UPGRADE_COUNT, PROCESS_TICKS);
        boolean changed = false;
        for (int channel = 0; channel < CHANNELS; channel++) {
            RecipeResult recipe = furnace.recipe(channel);
            if (recipe == null || !furnace.canOutput(channel, recipe.result())) {
                if (furnace.progress[channel] != 0) { furnace.progress[channel] = 0; changed = true; }
                continue;
            }
            int advance = Math.min(step, PROCESS_TICKS - furnace.progress[channel]);
            int cost = ENERGY_PER_BASE_TICK * advance;
            if (furnace.energy.getEnergyStored() < cost) continue;
            furnace.energy.consume(cost);
            furnace.progress[channel] += advance;
            changed = true;
            if (furnace.progress[channel] >= PROCESS_TICKS) {
                furnace.inventory.extractItem(INPUT_START + channel, 1, false);
                furnace.insertOutput(channel, recipe.result());
                furnace.progress[channel] = 0;
            }
        }
        if (changed) furnace.setChanged();
    }

    private @Nullable RecipeResult recipe(int channel) {
        if (level == null) return null;
        ItemStack input = inventory.getStackInSlot(INPUT_START + channel);
        if (input.isEmpty()) return null;
        SimpleContainer recipeInput = new SimpleContainer(input);
        SmeltingRecipe recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, recipeInput, level).orElse(null);
        if (recipe == null) return null;
        ItemStack result = recipe.assemble(recipeInput, level.registryAccess());
        return result.isEmpty() ? null : new RecipeResult(result);
    }

    private boolean canOutput(int channel, ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_START + channel);
        return output.isEmpty() || ItemStack.isSameItemSameTags(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }
    private void insertOutput(int channel, ItemStack result) {
        int slot = OUTPUT_START + channel;
        ItemStack output = inventory.getStackInSlot(slot);
        if (output.isEmpty()) inventory.setStackInSlot(slot, result.copy());
        else { output.grow(result.getCount()); inventory.setStackInSlot(slot, output); }
    }

    public ItemStackHandler getInventory() { return inventory; }
    public EnergyStorage getEnergyStorage(@Nullable Direction side) { return energy; }
    public ContainerData getData() { return data; }
    @Override public Component getDisplayName() { return Component.translatable("block.industrialcrops.electric_furnace"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ElectricFurnaceMenu(id, inv, this, worldPosition);
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Energy", energy.getEnergyStored()); tag.putIntArray("Progress", progress);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
        MachineInventoryHelper.ensureSize(inventory, UPGRADE_START + UPGRADE_COUNT);
        energy.setStored(tag.getInt("Energy")); int[] loaded = tag.getIntArray("Progress");
        System.arraycopy(loaded, 0, progress, 0, Math.min(progress.length, loaded.length));
    }
    private record RecipeResult(ItemStack result) { }
    private final class TrackedEnergyStorage extends EnergyStorage {
        private TrackedEnergyStorage() { super(ENERGY_CAPACITY, RECEIVE_RATE, 0); }
        private void setStored(int value) { energy = Math.max(0, Math.min(capacity, value)); }
        private void consume(int value) { energy = Math.max(0, energy - value); }
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int accepted = super.receiveEnergy(amount, simulate); if (accepted > 0 && !simulate) setChanged(); return accepted;
        }
    }
}
