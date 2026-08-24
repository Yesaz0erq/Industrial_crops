package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.machine.MachineInventoryHelper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class ReinforcedIndustrialStorageArrayBlockEntity extends BlockEntity {
    public static final int SLOT_COUNT = 54;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return Integer.MAX_VALUE;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ReinforcedIndustrialStorageArrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get(), pos, state);
    }

    public int getSlots() {
        return inventory.getSlots();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public int insertStack(ItemStack stack) {
        int original = stack.getCount();
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < inventory.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = inventory.insertItem(slot, remaining, false);
        }
        return original - remaining.getCount();
    }

    public ItemStack extractFromSlot(int slot, int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    public int insertIntoSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= inventory.getSlots() || stack.isEmpty()) {
            return 0;
        }

        int original = stack.getCount();
        ItemStack remainder = inventory.insertItem(slot, stack, false);
        return original - remainder.getCount();
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.getSlots()) {
            inventory.setStackInSlot(slot, stack);
        }
    }

    public boolean isEmpty() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public ItemStack createDroppedStack(Block block) {
        ItemStack stack = new ItemStack(block.asItem());
        CompoundTag storageData = inventory.serializeNBT();
        if (!storageData.isEmpty()) {
            stack.setTag(storageData.copy());
        }
        return stack;
    }

    public void readStorageFromStack(ItemStack stack) {
        var customData = com.industrialcrops.util.ItemStackNbt.copyTag(stack);
        if (customData != null && level != null) {
            inventory.deserializeNBT(customData);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
            MachineInventoryHelper.ensureSize(inventory, SLOT_COUNT);
        }
    }

    public static @Nullable ReinforcedIndustrialStorageArrayBlockEntity findAttached(Level level, BlockPos controllerPos) {
        List<ReinforcedIndustrialStorageArrayBlockEntity> attached = findAllAttached(level, controllerPos);
        return attached.isEmpty() ? null : attached.get(0);
    }

    public static List<ReinforcedIndustrialStorageArrayBlockEntity> findAllAttached(Level level, BlockPos controllerPos) {
        List<ReinforcedIndustrialStorageArrayBlockEntity> attached = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = controllerPos.relative(direction);
            if (level.getBlockEntity(adjacentPos) instanceof ReinforcedIndustrialStorageArrayBlockEntity drive) {
                attached.add(drive);
                queue.add(adjacentPos);
                visited.add(adjacentPos);
            }
        }

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);
                if (visited.contains(neighborPos)) {
                    continue;
                }
                if (level.getBlockEntity(neighborPos) instanceof ReinforcedIndustrialStorageArrayBlockEntity drive) {
                    attached.add(drive);
                    queue.addLast(neighborPos);
                    visited.add(neighborPos);
                }
            }
        }

        return attached;
    }
}
