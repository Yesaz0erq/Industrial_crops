package com.industrialcrops.block.entity;

import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.SuperCompressorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Super Compressor: packs 10,000 Industrial Potatoes into one 10K Compressed Potato.
 *
 * <p>Potatoes (1 each) and Industrial Potato Blocks (64 each) put into the input slot are drained
 * into an internal buffer every tick, so a pipe or a player can keep feeding normal stacks. Once the
 * buffer holds 10,000 potatoes the machine compresses them; speed components shorten the press time.
 */
public final class SuperCompressorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int UPGRADE_SLOT_START = 2;
    public static final int UPGRADE_SLOT_COUNT = 4;
    public static final int REQUIRED_POTATOES = 10_000;
    /** Two batches, so a whole potato block always fits while the previous batch is being pressed. */
    public static final int BUFFER_CAPACITY = 2 * REQUIRED_POTATOES;
    public static final int MAX_PROGRESS = 200;
    public static final int POTATO_BLOCK_VALUE = 64;

    private int storedPotatoes;
    private int progress;

    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && potatoValue(stack) > 0
                    || slot >= UPGRADE_SLOT_START && SpeedUpgradeHelper.isSpeedUpgrade(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** Automation view: slot 0 accepts potatoes / potato blocks, slot 1 hands out 10K Compressed Potatoes. */
    private final IItemHandler automationHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? inventory.getStackInSlot(INPUT_SLOT) : slot == 1 ? inventory.getStackInSlot(OUTPUT_SLOT) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == 0 ? inventory.insertItem(INPUT_SLOT, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? inventory.extractItem(OUTPUT_SLOT, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && potatoValue(stack) > 0;
        }
    };

    public SuperCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUPER_COMPRESSOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SuperCompressorBlockEntity blockEntity) {
        if (!level.isClientSide()) {
            blockEntity.absorbInput();
            blockEntity.tryCompress();
        }
    }

    /** Potatoes represented by one item: 1 for an Industrial Potato, 64 for an Industrial Potato Block. */
    public static int potatoValue(ItemStack stack) {
        if (stack.is(ModItems.INDUSTRIAL_POTATO.get())) return 1;
        if (stack.is(ModItems.INDUSTRIAL_POTATO_BLOCK.get())) return POTATO_BLOCK_VALUE;
        return 0;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public IItemHandler getAutomationHandler() {
        return automationHandler;
    }

    public int getStoredPotatoes() {
        return storedPotatoes;
    }

    public int getProgress() {
        return progress;
    }

    private void absorbInput() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        int value = potatoValue(input);
        if (value <= 0) return;
        int fits = (BUFFER_CAPACITY - storedPotatoes) / value;
        int taken = Math.min(fits, input.getCount());
        if (taken <= 0) return;
        inventory.extractItem(INPUT_SLOT, taken, false);
        storedPotatoes += taken * value;
        setChanged();
    }

    private boolean canCompress() {
        if (storedPotatoes < REQUIRED_POTATOES) return false;
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty()
                || output.is(ModItems.COMPRESSED_POTATO_10K.get()) && output.getCount() < output.getMaxStackSize();
    }

    private void tryCompress() {
        if (!canCompress()) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }
        progress += SpeedUpgradeHelper.progressStep(inventory, UPGRADE_SLOT_START, UPGRADE_SLOT_COUNT, MAX_PROGRESS);
        if (progress >= MAX_PROGRESS) {
            storedPotatoes -= REQUIRED_POTATOES;
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                inventory.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.COMPRESSED_POTATO_10K.get()));
            } else {
                output.grow(1);
                inventory.setStackInSlot(OUTPUT_SLOT, output);
            }
            progress = 0;
        }
        setChanged();
    }

    /**
     * Items to drop when the machine is broken: inventory contents plus the buffered potatoes, returned
     * as Industrial Potato Blocks and loose Industrial Potatoes.
     */
    public java.util.List<ItemStack> collectDrops() {
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) drops.add(stack.copy());
        }
        int blocks = storedPotatoes / POTATO_BLOCK_VALUE;
        int loose = storedPotatoes % POTATO_BLOCK_VALUE;
        while (blocks > 0) {
            int count = Math.min(64, blocks);
            drops.add(new ItemStack(ModItems.INDUSTRIAL_POTATO_BLOCK.get(), count));
            blocks -= count;
        }
        if (loose > 0) drops.add(new ItemStack(ModItems.INDUSTRIAL_POTATO.get(), loose));
        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("StoredPotatoes", storedPotatoes);
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
            MachineInventoryHelper.ensureSize(inventory, UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT);
        }
        storedPotatoes = Math.max(0, Math.min(BUFFER_CAPACITY, tag.getInt("StoredPotatoes")));
        progress = tag.getInt("Progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.super_compressor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SuperCompressorMenu(containerId, playerInventory, this, worldPosition);
    }
}
