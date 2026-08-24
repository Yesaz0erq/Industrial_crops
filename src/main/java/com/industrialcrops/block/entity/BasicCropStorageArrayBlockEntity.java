package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.machine.MachineInventoryHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.items.IItemHandler;
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

public final class BasicCropStorageArrayBlockEntity extends BlockEntity {
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

    public BasicCropStorageArrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIAL_CROP_STORAGE_ARRAY.get(), pos, state);
    }


    public IItemHandler getItemHandler() {
        return inventory;
    }

    public int getStoredIndustrialCarrots() { return getStoredCount(ModItems.INDUSTRIAL_CARROT.get()); }
    public int getStoredIndustrialPotatoes() { return getStoredCount(ModItems.INDUSTRIAL_POTATO.get()); }

    public int storeFromInventory(net.minecraft.world.entity.player.Inventory playerInventory, Item item) {
        int stored = 0;
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (!stack.is(item)) continue;
            int inserted = insertStack(stack.copy());
            if (inserted > 0) { stack.shrink(inserted); stored += inserted; }
        }
        return stored;
    }

    public int extractToInventory(net.minecraft.world.entity.player.Inventory playerInventory, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getSlots() && remaining > 0; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.is(item)) continue;
            ItemStack extracted = inventory.extractItem(i, Math.min(remaining, stack.getMaxStackSize()), false);
            int before = extracted.getCount();
            if (!playerInventory.add(extracted)) playerInventory.player.drop(extracted, false);
            remaining -= before;
        }
        return amount - remaining;
    }

    public int insert(Item item, int amount) { return insertStack(new ItemStack(item, amount)); }
    public int remove(Item item, int amount) {
        int remaining = amount;
        for (int i=0; i<inventory.getSlots() && remaining>0; i++) {
            if (inventory.getStackInSlot(i).is(item)) remaining -= inventory.extractItem(i, remaining, false).getCount();
        }
        return amount - remaining;
    }
    public int getStoredCount(Item item) {
        long total=0;
        for(int i=0;i<inventory.getSlots();i++) if(inventory.getStackInSlot(i).is(item)) total += inventory.getStackInSlot(i).getCount();
        return (int)Math.min(Integer.MAX_VALUE,total);
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
            CompoundTag tag = customData;
            if (tag.contains("Items")) inventory.deserializeNBT(tag);
            else {
                migrateLegacy(tag, ModItems.INDUSTRIAL_CARROT.get(), "IndustrialCarrots");
                migrateLegacy(tag, ModItems.INDUSTRIAL_POTATO.get(), "IndustrialPotatoes");
                migrateLegacy(tag, ModItems.INDUSTRIAL_WHEAT.get(), "IndustrialWheat");
                migrateLegacy(tag, ModItems.INDUSTRIAL_MELON.get(), "IndustrialMelons");
                migrateLegacy(tag, ModItems.INDUSTRIAL_PUMPKIN.get(), "IndustrialPumpkins");
            }
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
        } else {
            migrateLegacy(tag, ModItems.INDUSTRIAL_CARROT.get(), "IndustrialCarrots");
            migrateLegacy(tag, ModItems.INDUSTRIAL_POTATO.get(), "IndustrialPotatoes");
            migrateLegacy(tag, ModItems.INDUSTRIAL_WHEAT.get(), "IndustrialWheat");
            migrateLegacy(tag, ModItems.INDUSTRIAL_MELON.get(), "IndustrialMelons");
            migrateLegacy(tag, ModItems.INDUSTRIAL_PUMPKIN.get(), "IndustrialPumpkins");
        }
    }

    private void migrateLegacy(CompoundTag tag, Item item, String key) {
        int remaining = Math.max(0, tag.getInt(key));
        while (remaining > 0) {
            int amount = Math.min(remaining, item.getMaxStackSize());
            if (insertStack(new ItemStack(item, amount)) <= 0) break;
            remaining -= amount;
        }
    }

    public static @Nullable BasicCropStorageArrayBlockEntity findAttached(Level level, BlockPos controllerPos) {
        List<BasicCropStorageArrayBlockEntity> attached = findAllAttached(level, controllerPos);
        return attached.isEmpty() ? null : attached.get(0);
    }

    public static List<BasicCropStorageArrayBlockEntity> findAllAttached(Level level, BlockPos controllerPos) {
        List<BasicCropStorageArrayBlockEntity> attached = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = controllerPos.relative(direction);
            if (level.getBlockEntity(adjacentPos) instanceof BasicCropStorageArrayBlockEntity drive) {
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
                if (level.getBlockEntity(neighborPos) instanceof BasicCropStorageArrayBlockEntity drive) {
                    attached.add(drive);
                    queue.addLast(neighborPos);
                    visited.add(neighborPos);
                }
            }
        }

        return attached;
    }
}


