package com.industrialcrops.block.entity;

import com.industrialcrops.block.MaterialHardeningDeviceBlock;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.screen.MaterialHardeningDeviceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class MaterialHardeningDeviceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int PROCESS_TICKS = 100;

    private int progress;
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && !stack.isEmpty();
        }

        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return index == 0 ? progress : PROCESS_TICKS;
        }

        @Override public void set(int index, int value) {
            if (index == 0) progress = value;
        }

        @Override public int getCount() { return 2; }
    };

    public MaterialHardeningDeviceBlockEntity(BlockPos pos, BlockState state) {
        super(com.industrialcrops.registry.CarroteBlockEntities.MATERIAL_HARDENING_DEVICE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MaterialHardeningDeviceBlockEntity device) {
        if (level.isClientSide()) return;

        ItemStack input = device.inventory.getStackInSlot(INPUT_SLOT);
        ItemStack result = input.isEmpty() ? ItemStack.EMPTY : makeUnbreakable(input);
        boolean active = !input.isEmpty() && device.canOutput(result);
        if (state.hasProperty(MaterialHardeningDeviceBlock.LIT) && state.getValue(MaterialHardeningDeviceBlock.LIT) != active) {
            level.setBlock(pos, state.setValue(MaterialHardeningDeviceBlock.LIT, active), Block.UPDATE_CLIENTS);
        }
        if (!active) {
            if (device.progress != 0) {
                device.progress = 0;
                device.setChanged();
            }
            return;
        }

        if (++device.progress < PROCESS_TICKS) {
            device.setChanged();
            return;
        }
        device.inventory.extractItem(INPUT_SLOT, 1, false);
        device.insertOutput(result);
        device.progress = 0;
        device.setChanged();
    }

    /** Adds the real 1.21 unbreakable component and a legacy-friendly NBT marker. */
    public static ItemStack makeUnbreakable(ItemStack input) {
        ItemStack result = input.copyWithCount(1);
        result.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        CustomData.update(DataComponents.CUSTOM_DATA, result,
                tag -> tag.putBoolean("Unbreakable", true));
        return result;
    }

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty();
    }

    private boolean canOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty()
                || ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void insertOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
        else {
            output.grow(result.getCount());
            inventory.setStackInSlot(OUTPUT_SLOT, output);
        }
    }

    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return data; }

    @Override public Component getDisplayName() {
        return Component.translatable("block.carrote.material_hardening_device");
    }

    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MaterialHardeningDeviceMenu(id, inventory, this, worldPosition);
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
