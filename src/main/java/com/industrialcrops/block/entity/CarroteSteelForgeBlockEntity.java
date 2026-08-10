package com.industrialcrops.block.entity;

import com.industrialcrops.block.CarroteSteelForgeBlock;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.machine.MachineInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class CarroteSteelForgeBlockEntity extends BlockEntity {
    public static final int CARROTE_SLOT = 0;
    public static final int ALLOY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int PROCESS_TICKS = 100;
    public static final TagKey<Item> ALLOY_INGOTS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("carrote", "carrote_steel_alloy_ingots"));

    private int progress;
    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == CARROTE_SLOT ? stack.is(com.industrialcrops.registry.CarroteItems.CARROTE.get())
                    : slot == ALLOY_SLOT && stack.is(ALLOY_INGOTS);
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final IItemHandler automation = new IItemHandler() {
        @Override public int getSlots() { return inventory.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == CARROTE_SLOT || slot == ALLOY_SLOT
                    ? inventory.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == OUTPUT_SLOT ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return inventory.isItemValid(slot, stack); }
    };

    public CarroteSteelForgeBlockEntity(BlockPos pos, BlockState state) {
        super(com.industrialcrops.registry.CarroteBlockEntities.CARROTE_STEEL_FORGE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CarroteSteelForgeBlockEntity forge) {
        if (level.isClientSide()) return;
        forge.absorbDroppedInputs(level, pos);
        boolean active = forge.canForge();
        if (state.hasProperty(CarroteSteelForgeBlock.LIT) && state.getValue(CarroteSteelForgeBlock.LIT) != active) {
            level.setBlock(pos, state.setValue(CarroteSteelForgeBlock.LIT, active), Block.UPDATE_CLIENTS);
        }
        if (!active) {
            if (forge.progress != 0) { forge.progress = 0; forge.setChanged(); }
            return;
        }
        if (++forge.progress < PROCESS_TICKS) { forge.setChanged(); return; }
        forge.inventory.extractItem(CARROTE_SLOT, 1, false);
        forge.inventory.extractItem(ALLOY_SLOT, 1, false);
        ItemStack output = forge.inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) forge.inventory.setStackInSlot(OUTPUT_SLOT, new ItemStack(com.industrialcrops.registry.CarroteItems.CARROTE_STEEL_INGOT.get()));
        else { output.grow(1); forge.inventory.setStackInSlot(OUTPUT_SLOT, output); }
        forge.progress = 0;
        forge.setChanged();
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.85F, 1.15F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5D, pos.getY() + 1.02D,
                    pos.getZ() + 0.5D, 10, 0.28D, 0.10D, 0.28D, 0.04D);
            server.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 0.98D,
                    pos.getZ() + 0.5D, 5, 0.24D, 0.06D, 0.24D, 0.04D);
            server.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.96D,
                    pos.getZ() + 0.5D, 8, 0.28D, 0.08D, 0.28D, 0.03D);
        }
    }

    public static boolean isAcceptedInput(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(com.industrialcrops.registry.CarroteItems.CARROTE.get()) || stack.is(ALLOY_INGOTS));
    }
    public ItemStack insertInput(ItemStack stack, boolean simulate) {
        if (stack.is(com.industrialcrops.registry.CarroteItems.CARROTE.get())) return inventory.insertItem(CARROTE_SLOT, stack, simulate);
        if (stack.is(ALLOY_INGOTS)) return inventory.insertItem(ALLOY_SLOT, stack, simulate);
        return stack;
    }
    public ItemStack extractOutput(int amount, boolean simulate) {
        return inventory.extractItem(OUTPUT_SLOT, amount, simulate);
    }
    public ItemStackHandler getInventory() { return inventory; }
    public IItemHandler getAutomationHandler() { return automation; }

    private boolean canForge() {
        if (!inventory.getStackInSlot(CARROTE_SLOT).is(com.industrialcrops.registry.CarroteItems.CARROTE.get())
                || !inventory.getStackInSlot(ALLOY_SLOT).is(ALLOY_INGOTS)) return false;
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || output.is(com.industrialcrops.registry.CarroteItems.CARROTE_STEEL_INGOT.get()) && output.getCount() < output.getMaxStackSize();
    }
    private void absorbDroppedInputs(Level level, BlockPos pos) {
        AABB intake = new AABB(pos).inflate(0.12D).expandTowards(0.0D, 1.15D, 0.0D);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, intake, ItemEntity::isAlive)) {
            ItemStack stack = entity.getItem();
            if (!isAcceptedInput(stack)) continue;
            ItemStack remainder = insertInput(stack.copy(), false);
            if (remainder.getCount() == stack.getCount()) continue;
            if (remainder.isEmpty()) entity.discard(); else entity.setItem(remainder);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Progress", progress);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        MachineInventoryHelper.ensureSize(inventory, OUTPUT_SLOT + 1);
        progress = tag.getInt("Progress");
    }
}
