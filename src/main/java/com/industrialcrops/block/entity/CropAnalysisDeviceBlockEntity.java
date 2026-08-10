package com.industrialcrops.block.entity;

import com.industrialcrops.crop.CropGenetics;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.CropAnalysisDeviceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class CropAnalysisDeviceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && CropGenetics.isGeneticCrop(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty() && !CropGenetics.hasGenes(stack)) {
                CropGenetics.initializeInitial(stack, level != null ? level.random : net.minecraft.util.RandomSource.create());
            }
            setChanged();
        }
    };

    public CropAnalysisDeviceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CROP_ANALYSIS_DEVICE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.crop_analysis_device");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CropAnalysisDeviceMenu(containerId, playerInventory, this, worldPosition);
    }
}
