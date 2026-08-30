package com.industrialcrops.block.entity;

import com.industrialcrops.basic_pipe.PipeTransferUtil;
import com.industrialcrops.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class PipeSorterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int FILTER_SLOT_COUNT = 18;
    private static final int BUFFER_SLOT_COUNT = 9;
    private static final int TRANSFER_RATE = 16;

    private boolean blacklist;
    private final ItemStackHandler filters = new ItemStackHandler(FILTER_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndUpdate();
        }
    };
    private final ItemStackHandler buffer = new ItemStackHandler(BUFFER_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndUpdate();
        }
    };
    private final IItemHandler inputHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return buffer.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return buffer.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!allows(stack)) return stack;
            return buffer.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // The sorter controls extraction so items cannot bypass its routing rules.
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return buffer.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return allows(stack) && buffer.isItemValid(slot, stack);
        }
    };

    public PipeSorterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIPE_SORTER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PipeSorterBlockEntity sorter) {
        if (!level.isClientSide()) sorter.distributeItems();
    }

    public ItemStackHandler getFilters() {
        return filters;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public void setBlacklist(boolean blacklist) {
        if (this.blacklist == blacklist) return;
        this.blacklist = blacklist;
        setChangedAndUpdate();
    }

    public void setFilter(int slot, ItemStack stack) {
        if (slot < 0 || slot >= FILTER_SLOT_COUNT) return;
        filters.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
    }

    public boolean allows(ItemStack stack) {
        if (stack.isEmpty()) return false;
        boolean hasFilters = false;
        boolean matched = false;
        for (int slot = 0; slot < FILTER_SLOT_COUNT; slot++) {
            ItemStack filter = filters.getStackInSlot(slot);
            if (filter.isEmpty()) continue;
            hasFilters = true;
            if (filter.getItem() == stack.getItem()) {
                matched = true;
                break;
            }
        }
        if (!hasFilters) return true;
        return blacklist ? !matched : matched;
    }

    private void distributeItems() {
        if (level == null) return;
        List<HandlerEndpoint> targets = collectTargets();
        if (targets.isEmpty()) return;

        for (int slot = 0; slot < buffer.getSlots(); slot++) {
            ItemStack stored = buffer.getStackInSlot(slot);
            if (stored.isEmpty()) continue;
            int amount = Math.min(TRANSFER_RATE, stored.getCount());
            for (HandlerEndpoint target : targets) {
                int accepted = getInsertableAmount(target.handler(), stored.copyWithCount(amount));
                if (accepted <= 0) continue;

                ItemStack extracted = buffer.extractItem(slot, accepted, false);
                ItemStack remainder = insert(target.handler(), extracted);
                if (!remainder.isEmpty()) buffer.insertItem(slot, remainder, false);
                if (remainder.getCount() < extracted.getCount()) return;
            }
        }
    }

    private List<HandlerEndpoint> collectTargets() {
        List<HandlerEndpoint> targets = new ArrayList<>();
        if (level == null) return targets;
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = worldPosition.relative(direction);
            if (PipeTransferUtil.isPipe(level.getBlockState(targetPos))) continue;
            IItemHandler handler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK, targetPos, direction.getOpposite());
            if (handler != null) targets.add(new HandlerEndpoint(targetPos, handler));
        }
        return targets;
    }

    private static int getInsertableAmount(IItemHandler handler, ItemStack stack) {
        int original = stack.getCount();
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, true);
        }
        return original - remaining.getCount();
    }

    private static ItemStack insert(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Filters", filters.serializeNBT(registries));
        tag.put("Buffer", buffer.serializeNBT(registries));
        tag.putBoolean("Blacklist", blacklist);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Filters")) filters.deserializeNBT(registries, tag.getCompound("Filters"));
        if (tag.contains("Buffer")) buffer.deserializeNBT(registries, tag.getCompound("Buffer"));
        blacklist = tag.getBoolean("Blacklist");
    }

    public IItemHandler getInputHandler() {
        return inputHandler;
    }

    public ItemStackHandler getBuffer() {
        return buffer;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.pipe_sorter");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.industrialcrops.screen.PipeSorterMenu(containerId, playerInventory, this, worldPosition);
    }

    private record HandlerEndpoint(BlockPos pos, IItemHandler handler) {
    }
}
