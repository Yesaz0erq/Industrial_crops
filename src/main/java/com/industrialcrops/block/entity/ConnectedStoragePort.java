package com.industrialcrops.block.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

/** A stable capability reference backed by the same loaded storage pages as the controller menu. */
public final class ConnectedStoragePort<T extends BlockEntity> implements IItemHandler {
    private static final AtomicLong TOPOLOGY = new AtomicLong();
    private final BlockEntity owner;
    private final int pageSize;
    private final Supplier<List<T>> findDrives;
    private final Function<T, IItemHandler> inventory;
    private final IItemHandler fallback;
    private final IntSupplier fallbackSlots;
    private long version = -1;
    private long lastChunkCheck = Long.MIN_VALUE;
    private final Map<ChunkPos, Boolean> watchedChunks = new HashMap<>();
    private List<T> drives = List.of();
    private final Set<IItemHandler> backings = Collections.newSetFromMap(new IdentityHashMap<>());

    ConnectedStoragePort(BlockEntity owner, int pageSize, Supplier<List<T>> findDrives,
                         Function<T, IItemHandler> inventory, IItemHandler fallback, IntSupplier fallbackSlots) {
        this.owner = owner;
        this.pageSize = pageSize;
        this.findDrives = findDrives;
        this.inventory = inventory;
        this.fallback = fallback;
        this.fallbackSlots = fallbackSlots;
    }

    // A drive added or removed anywhere in a connected chain can change its available pages.
    static void topologyChanged() { TOPOLOGY.incrementAndGet(); }

    private boolean accessible() {
        Level level = owner.getLevel();
        return !owner.isRemoved() && level != null && level.hasChunkAt(owner.getBlockPos());
    }

    private boolean chunkAvailabilityChanged(Level level) {
        long tick = level.getGameTime();
        if (tick == lastChunkCheck) return false;
        lastChunkCheck = tick;
        for (var entry : watchedChunks.entrySet()) {
            ChunkPos chunk = entry.getKey();
            if (level.hasChunk(chunk.x, chunk.z) != entry.getValue()) return true;
        }
        return false;
    }

    private void watchAdjacentChunks(Level level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            ChunkPos chunk = new ChunkPos(pos.relative(side));
            watchedChunks.computeIfAbsent(chunk, key -> level.hasChunk(key.x, key.z));
        }
    }

    List<T> components() {
        if (!accessible()) return List.of();
        Level level = owner.getLevel();
        long current = TOPOLOGY.get();
        if (version != current || chunkAvailabilityChanged(level)) {
            drives = findDrives.get();
            watchedChunks.clear();
            watchAdjacentChunks(level, owner.getBlockPos());
            for (T drive : drives) watchAdjacentChunks(level, drive.getBlockPos());
            lastChunkCheck = level.getGameTime();
            backings.clear();
            for (T drive : drives) backings.add(inventory.apply(drive));
            if (drives.isEmpty() && fallback != null) backings.add(fallback);
            version = current;
        }
        return drives;
    }

    private Set<IItemHandler> backingHandlers() {
        if (!accessible()) return Set.of();
        components();
        return backings;
    }

    /** Controllers can expose the same physical slots as another controller or one of their drives. */
    public static boolean sharesStorage(IItemHandler first, IItemHandler second) {
        if (first == second) return true;
        if (first instanceof ConnectedStoragePort<?> a) {
            Set<IItemHandler> aBackings = a.backingHandlers();
            if (second instanceof ConnectedStoragePort<?> b) {
                Set<IItemHandler> bBackings = b.backingHandlers();
                return !Collections.disjoint(aBackings, bBackings);
            }
            return aBackings.contains(second);
        }
        return second instanceof ConnectedStoragePort<?> b && b.backingHandlers().contains(first);
    }

    @Override public int getSlots() {
        if (!accessible()) return 0;
        List<T> pages = components();
        return pages.isEmpty() ? fallbackSlots.getAsInt() : pages.size() * pageSize;
    }

    private record Slot(IItemHandler handler, int index) {}

    private Slot resolve(int index) {
        if (index < 0 || index >= getSlots()) return null;
        List<T> pages = components();
        if (pages.isEmpty()) return new Slot(fallback, index);
        return new Slot(inventory.apply(pages.get(index / pageSize)), index % pageSize);
    }

    @Override public ItemStack getStackInSlot(int slot) {
        Slot target = resolve(slot);
        return target == null ? ItemStack.EMPTY : target.handler.getStackInSlot(target.index);
    }

    @Override public int getSlotLimit(int slot) {
        Slot target = resolve(slot);
        return target == null ? 0 : target.handler.getSlotLimit(target.index);
    }

    @Override public boolean isItemValid(int slot, ItemStack stack) {
        Slot target = resolve(slot);
        return target != null && target.handler.isItemValid(target.index, stack);
    }

    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        Slot target = resolve(slot);
        return target == null ? stack : target.handler.insertItem(target.index, stack, simulate);
    }

    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        Slot target = resolve(slot);
        return target == null ? ItemStack.EMPTY : target.handler.extractItem(target.index, amount, simulate);
    }
}
