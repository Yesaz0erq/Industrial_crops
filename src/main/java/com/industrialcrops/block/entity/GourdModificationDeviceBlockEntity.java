package com.industrialcrops.block.entity;

import com.industrialcrops.crop.CropGenetics;
import com.industrialcrops.crop.CropQuality;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.GourdModificationDeviceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class GourdModificationDeviceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MELON_INPUT_SLOT = 0;
    public static final int PUMPKIN_INPUT_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int MAX_PROGRESS = 100;

    private int progress;

    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case MELON_INPUT_SLOT -> isSuperMelon(stack);
                case PUMPKIN_INPUT_SLOT -> isSuperPumpkin(stack);
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public GourdModificationDeviceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOURD_MODIFICATION_DEVICE.get(), pos, state);
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            GourdModificationDeviceBlockEntity blockEntity
    ) {
        if (!level.isClientSide()) {
            blockEntity.processTick();
        }
    }

    public static boolean isSuperMelon(ItemStack stack) {
        return stack.is(ModItems.INDUSTRIAL_MELON.get()) && hasSuperDominantGene(stack);
    }

    public static boolean isSuperPumpkin(ItemStack stack) {
        return stack.is(ModItems.INDUSTRIAL_PUMPKIN.get()) && hasSuperDominantGene(stack);
    }

    private static boolean hasSuperDominantGene(ItemStack stack) {
        CropGenetics.Genes genes = CropGenetics.read(stack);
        return genes != null && genes.dominantQuality() == CropQuality.SUPER;
    }

    private void processTick() {
        if (!canProcess()) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }

        progress++;
        if (progress < MAX_PROGRESS) {
            setChanged();
            return;
        }

        inventory.extractItem(MELON_INPUT_SLOT, 1, false);
        inventory.extractItem(PUMPKIN_INPUT_SLOT, 1, false);
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.FUSION_MELON.get()));
        } else {
            output.grow(1);
            inventory.setStackInSlot(OUTPUT_SLOT, output);
        }
        progress = 0;
        setChanged();
    }

    private boolean canProcess() {
        ItemStack melon = inventory.getStackInSlot(MELON_INPUT_SLOT);
        ItemStack pumpkin = inventory.getStackInSlot(PUMPKIN_INPUT_SLOT);
        if (!isSuperMelon(melon) || !isSuperPumpkin(pumpkin)) {
            return false;
        }

        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return output.is(ModItems.FUSION_MELON.get())
                && output.getCount() < output.getMaxStackSize();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        progress = tag.getInt("Progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.gourd_modification_device");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        return new GourdModificationDeviceMenu(containerId, playerInventory, this, worldPosition);
    }
}
