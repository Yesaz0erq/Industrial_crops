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
 * Super Compressor: packs 10,000 Industrial Potatoes, Carrots or Wheat into one 10K compressed item.
 *
 * <p>Crops (1 each) and their Industrial crop blocks (64 each) put into the input slot are drained
 * into an internal buffer every tick, so a pipe or a player can keep feeding normal stacks. The buffer
 * holds one crop at a time; once it holds 10,000 the machine compresses them into that crop's 10K item.
 * Speed components shorten the press time.
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

    public static final int CROP_NONE = -1, CROP_POTATO = 0, CROP_CARROT = 1, CROP_WHEAT = 2;

    private int storedPotatoes;
    /** Crop currently in the buffer; CROP_NONE once it is empty. */
    private int storedCrop = CROP_NONE;
    private int progress;

    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && accepts(stack)
                    || slot >= UPGRADE_SLOT_START && SpeedUpgradeHelper.isSpeedUpgrade(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** Automation view: slot 0 accepts crops / crop blocks, slot 1 hands out the 10K compressed items. */
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
            return slot == 0 && accepts(stack);
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

    /** Crops represented by one item: 1 for an Industrial crop, 64 for its Industrial crop block. */
    public static int potatoValue(ItemStack stack) {
        if (stack.is(ModItems.INDUSTRIAL_POTATO.get()) || stack.is(ModItems.INDUSTRIAL_CARROT.get())
                || stack.is(ModItems.INDUSTRIAL_WHEAT.get())) return 1;
        if (stack.is(ModItems.INDUSTRIAL_POTATO_BLOCK.get()) || stack.is(ModItems.INDUSTRIAL_CARROT_BLOCK.get())
                || stack.is(ModItems.INDUSTRIAL_WHEAT_BLOCK.get())) return POTATO_BLOCK_VALUE;
        return 0;
    }

    /** Which crop an item feeds, or CROP_NONE. */
    public static int cropOf(ItemStack stack) {
        if (stack.is(ModItems.INDUSTRIAL_POTATO.get()) || stack.is(ModItems.INDUSTRIAL_POTATO_BLOCK.get())) return CROP_POTATO;
        if (stack.is(ModItems.INDUSTRIAL_CARROT.get()) || stack.is(ModItems.INDUSTRIAL_CARROT_BLOCK.get())) return CROP_CARROT;
        if (stack.is(ModItems.INDUSTRIAL_WHEAT.get()) || stack.is(ModItems.INDUSTRIAL_WHEAT_BLOCK.get())) return CROP_WHEAT;
        return CROP_NONE;
    }

    public static ItemStack product(int crop) {
        return switch (crop) {
            case CROP_POTATO -> new ItemStack(ModItems.COMPRESSED_POTATO_10K.get());
            case CROP_CARROT -> new ItemStack(ModItems.COMPRESSED_CARROT_10K.get());
            case CROP_WHEAT -> new ItemStack(ModItems.COMPRESSED_WHEAT_10K.get());
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack loose(int crop, int count) {
        return switch (crop) {
            case CROP_CARROT -> new ItemStack(ModItems.INDUSTRIAL_CARROT.get(), count);
            case CROP_WHEAT -> new ItemStack(ModItems.INDUSTRIAL_WHEAT.get(), count);
            default -> new ItemStack(ModItems.INDUSTRIAL_POTATO.get(), count);
        };
    }

    private static ItemStack block(int crop, int count) {
        return switch (crop) {
            case CROP_CARROT -> new ItemStack(ModItems.INDUSTRIAL_CARROT_BLOCK.get(), count);
            case CROP_WHEAT -> new ItemStack(ModItems.INDUSTRIAL_WHEAT_BLOCK.get(), count);
            default -> new ItemStack(ModItems.INDUSTRIAL_POTATO_BLOCK.get(), count);
        };
    }

    /** A crop is accepted while the buffer is empty or already holds that same crop. */
    public boolean accepts(ItemStack stack) {
        int crop = cropOf(stack);
        return crop != CROP_NONE && (storedPotatoes <= 0 || storedCrop == CROP_NONE || storedCrop == crop);
    }

    public int getStoredCrop() {
        return storedPotatoes > 0 ? storedCrop : CROP_NONE;
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
        int crop = cropOf(input);
        if (storedPotatoes > 0 && storedCrop != CROP_NONE && storedCrop != crop) return;
        storedCrop = crop;
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
        ItemStack product = product(storedCrop == CROP_NONE ? CROP_POTATO : storedCrop);
        return output.isEmpty()
                || output.is(product.getItem()) && output.getCount() < output.getMaxStackSize();
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
            ItemStack product = product(storedCrop == CROP_NONE ? CROP_POTATO : storedCrop);
            if (storedPotatoes <= 0) storedCrop = CROP_NONE;
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                inventory.setStackInSlot(OUTPUT_SLOT, product);
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
        int crop = storedCrop == CROP_NONE ? CROP_POTATO : storedCrop;
        while (blocks > 0) {
            int count = Math.min(64, blocks);
            drops.add(block(crop, count));
            blocks -= count;
        }
        if (loose > 0) drops.add(loose(crop, loose));
        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("StoredPotatoes", storedPotatoes);
        tag.putInt("StoredCrop", storedCrop);
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
        // Older saves only ever buffered potatoes.
        storedCrop = tag.contains("StoredCrop") ? Math.max(CROP_NONE, Math.min(CROP_WHEAT, tag.getInt("StoredCrop")))
                : (storedPotatoes > 0 ? CROP_POTATO : CROP_NONE);
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
