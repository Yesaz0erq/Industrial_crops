package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.MachineInventoryHelper;
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
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class CropCompressorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int REQUIRED_COUNT = 64;
    public static final int MAX_PROGRESS = 160;
    public static final int UPGRADE_SLOT_START = 2, UPGRADE_SLOT_COUNT = 4;

    private boolean compressing;
    private int progress;

    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && isCompressible(stack)
                    || slot >= UPGRADE_SLOT_START && SpeedUpgradeHelper.isSpeedUpgrade(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public CropCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CROP_COMPRESSOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CropCompressorBlockEntity blockEntity) {
        if (!level.isClientSide()) {
            blockEntity.tryCompress();
        }
    }

    public static boolean isCompressible(ItemStack stack) {
        return stack.is(ModItems.INDUSTRIAL_CARROT.get())
                || stack.is(ModItems.INDUSTRIAL_POTATO.get())
                || stack.is(ModItems.INDUSTRIAL_WHEAT.get());
    }

    public static ItemStack getCompressionResult(ItemStack input) {
        if (input.is(ModItems.INDUSTRIAL_CARROT.get())) {
            return new ItemStack(ModBlocks.INDUSTRIAL_CARROT_BLOCK.get().asItem());
        }
        if (input.is(ModItems.INDUSTRIAL_POTATO.get())) {
            return new ItemStack(ModBlocks.INDUSTRIAL_POTATO_BLOCK.get().asItem());
        }
        if (input.is(ModItems.INDUSTRIAL_WHEAT.get())) {
            return new ItemStack(ModBlocks.INDUSTRIAL_WHEAT_BLOCK.get().asItem());
        }
        return ItemStack.EMPTY;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getProgress() {
        return progress;
    }

    public int getSlots() {
        return inventory.getSlots();
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public boolean canCompress() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (input.getCount() < REQUIRED_COUNT) {
            return false;
        }
        ItemStack result = getCompressionResult(input);
        return !result.isEmpty() && canOutputAccept(inventory.getStackInSlot(OUTPUT_SLOT), result);
    }

    private void tryCompress() {
        if (compressing || !canCompress()) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }
        progress += SpeedUpgradeHelper.progressStep(inventory, UPGRADE_SLOT_START, UPGRADE_SLOT_COUNT, MAX_PROGRESS);
        if (progress < MAX_PROGRESS) {
            setChanged();
            return;
        }
        compressing = true;
        try {
            ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
            ItemStack result = getCompressionResult(input);
            inventory.extractItem(INPUT_SLOT, REQUIRED_COUNT, false);
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
            } else {
                output.grow(1);
                inventory.setStackInSlot(OUTPUT_SLOT, output);
            }
            progress = 0;
            setChanged();
        } finally {
            compressing = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
            MachineInventoryHelper.ensureSize(inventory, UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT);
        }
        progress = tag.getInt("Progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.crop_compressor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.industrialcrops.screen.CropCompressorMenu(containerId, playerInventory, this, worldPosition);
    }

    private static boolean canOutputAccept(ItemStack outputStack, ItemStack resultStack) {
        if (outputStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(outputStack, resultStack)) {
            return false;
        }
        return outputStack.getCount() + resultStack.getCount() <= outputStack.getMaxStackSize();
    }
}
