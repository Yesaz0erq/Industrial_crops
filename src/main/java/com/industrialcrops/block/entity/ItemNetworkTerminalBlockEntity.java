package com.industrialcrops.block.entity;

import com.industrialcrops.basic_pipe.PipeTransferUtil;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ItemNetworkTerminalBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MAX_NETWORK_NODES = 512;
    public static final int RECONSTRUCTION_BATCH_SIZE = 64;
    private final List<Entry> entries = new ArrayList<>();
    private int selectedIndex = -1;

    public ItemNetworkTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_NETWORK_TERMINAL.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ItemNetworkTerminalBlockEntity terminal) {
        // Uploads are intentionally user-triggered from the digitizer screen.
    }

    public boolean copySelected(int absoluteIndex) {
        if (!validIndex(absoluteIndex)) return false;
        MatterMachineBlockEntity copier = firstMachine(MatterMachineBlockEntity.Kind.COPIER);
        if (copier == null) return false;
        setSelectedIndex(absoluteIndex);
        if (!copier.selectNetworkEntry(absoluteIndex)) return false;
        return copier.requestOperation();
    }

    public void setSelectedIndex(int index) {
        selectedIndex = validIndex(index) ? index : -1;
        setChanged();
    }

    public boolean operateWith(MatterMachineBlockEntity machine, Player player) {
        return machine.requestOperation();
    }

    public boolean canOperateWith(MatterMachineBlockEntity machine) {
        return canOperateWith(machine, selectedIndex);
    }

    public boolean canOperateWith(MatterMachineBlockEntity machine, int index) {
        if (!validIndex(index)) return false;
        if (machine.getKind() == MatterMachineBlockEntity.Kind.COPIER) {
            return isCopyable(entries.get(index).stack);
        }
        if (machine.getKind() == MatterMachineBlockEntity.Kind.RECONSTRUCTOR) {
            return reconstructionAmount(machine, index) > 0;
        }
        return false;
    }

    public boolean completeOperation(MatterMachineBlockEntity machine) {
        return completeOperation(machine, selectedIndex);
    }

    public boolean completeOperation(MatterMachineBlockEntity machine, int index) {
        if (!canOperateWith(machine, index)) return false;
        if (machine.getKind() == MatterMachineBlockEntity.Kind.COPIER) {
            entries.get(index).count++;
            setChanged();
            return true;
        }
        if (machine.getKind() == MatterMachineBlockEntity.Kind.RECONSTRUCTOR) {
            Entry entry = entries.get(index);
            int amount = reconstructionAmount(machine, index);
            if (amount <= 0) return false;
            ItemStack result = entry.stack.copyWithCount(amount);
            if (!machine.insertReconstructed(result)) return false;
            entry.count -= amount;
            if (entry.count <= 0) {
                entries.remove(index);
                selectedIndex = -1;
            }
            setChanged();
            return true;
        }
        return false;
    }

    private int reconstructionAmount(MatterMachineBlockEntity machine, int index) {
        if (!validIndex(index)) return 0;
        Entry entry = entries.get(index);
        return (int) Math.min(RECONSTRUCTION_BATCH_SIZE,
                Math.min(entry.count, machine.reconstructedCapacityFor(entry.stack)));
    }

    public boolean reconstructSelected(int absoluteIndex, Player player) {
        if (!validIndex(absoluteIndex)) return false;
        MatterMachineBlockEntity reconstructor = firstMachine(MatterMachineBlockEntity.Kind.RECONSTRUCTOR);
        if (reconstructor == null) return false;
        setSelectedIndex(absoluteIndex);
        if (!reconstructor.selectNetworkEntry(absoluteIndex)) return false;
        return reconstructor.requestOperation();
    }

    public int size() { return entries.size(); }
    public List<Integer> matchingIndices(String query) {
        List<Integer> matches = new ArrayList<>();
        String normalized = query == null ? "" : query.strip().toLowerCase(java.util.Locale.ROOT);
        for (int index = 0; index < entries.size(); index++) {
            ItemStack stack = entries.get(index).stack;
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(java.util.Locale.ROOT);
            String name = stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT);
            if (normalized.isEmpty() || id.contains(normalized) || name.contains(normalized)) matches.add(index);
        }
        return matches;
    }
    public ItemStack displayStack(int absoluteIndex) {
        return validIndex(absoluteIndex) ? entries.get(absoluteIndex).stack.copyWithCount(1) : ItemStack.EMPTY;
    }
    public long count(int absoluteIndex) { return validIndex(absoluteIndex) ? entries.get(absoluteIndex).count : 0; }

    public void upload(ItemStack stack) {
        add(stack, stack.getCount());
    }

    public void addGenerated(ItemStack stack, long amount) {
        add(stack, amount);
    }

    public ItemStack extractOneMatching(Predicate<ItemStack> predicate) {
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index);
            if (entry.count <= 0 || !predicate.test(entry.stack)) continue;
            ItemStack extracted = entry.stack.copyWithCount(1);
            entry.count--;
            if (entry.count <= 0) {
                entries.remove(index);
                if (selectedIndex == index) selectedIndex = -1;
                else if (selectedIndex > index) selectedIndex--;
            }
            setChanged();
            return extracted;
        }
        return ItemStack.EMPTY;
    }

    private void add(ItemStack stack, long amount) {
        if (stack.isEmpty() || amount <= 0 || !isNetworkMaterial(stack)) return;
        for (Entry entry : entries) {
            if (ItemStack.isSameItemSameTags(entry.stack, stack)) {
                entry.count = Math.min(Long.MAX_VALUE, entry.count + amount);
                setChanged(); return;
            }
        }
        entries.add(new Entry(stack.copyWithCount(1), amount));
        setChanged();
    }

    private boolean validIndex(int index) { return index >= 0 && index < entries.size() && entries.get(index).count > 0; }
    private static boolean isNetworkMaterial(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft")
                || stack.is(com.industrialcrops.registry.CarroteItems.STABLE_MATTER_INGOT.get());
    }

    /** Prevents duplication of nested inventories such as filled shulker boxes and bundles. */
    public static boolean isCopyable(ItemStack stack) {
        if (stack.isEmpty() || !isNetworkMaterial(stack)) return false;
        if (stack.hasTag() && containsStoredItems(stack.getTag())) return false;
        return true;
    }

    private static boolean containsStoredItems(CompoundTag tag) {
        if (tag.contains("Items", Tag.TAG_LIST) && !tag.getList("Items", Tag.TAG_COMPOUND).isEmpty()) return true;
        for (String key : tag.getAllKeys()) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag compound && containsStoredItems(compound)) return true;
            if (child instanceof ListTag list) {
                for (Tag value : list) {
                    if (value instanceof CompoundTag compound && containsStoredItems(compound)) return true;
                }
            }
        }
        return false;
    }

    private @Nullable MatterMachineBlockEntity firstMachine(MatterMachineBlockEntity.Kind kind) {
        List<MatterMachineBlockEntity> machines = findMachines(kind);
        return machines.isEmpty() ? null : machines.get(0);
    }

    private List<MatterMachineBlockEntity> findMachines(MatterMachineBlockEntity.Kind kind) {
        List<MatterMachineBlockEntity> result = new ArrayList<>();
        if (level == null) return result;
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(worldPosition); visited.add(worldPosition);
        while (!queue.isEmpty() && visited.size() <= MAX_NETWORK_NODES) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!level.hasChunkAt(next) || visited.contains(next)) continue;
                BlockEntity entity = level.getBlockEntity(next);
                if (entity instanceof MatterMachineBlockEntity machine) {
                    visited.add(next);
                    if (machine.getKind() == kind) result.add(machine);
                } else if (PipeTransferUtil.isPipe(level.getBlockState(next))) {
                    visited.add(next); queue.addLast(next);
                }
            }
        }
        return result;
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag item = new CompoundTag();
            item.put("Stack", entry.stack.save(new CompoundTag())); item.putLong("Count", entry.count); list.add(item);
        }
        tag.put("Items", list);
        tag.putInt("SelectedIndex", selectedIndex);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); entries.clear();
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i=0;i<list.size();i++) {
            CompoundTag item=list.getCompound(i); ItemStack stack=ItemStack.of(item.getCompound("Stack"));
            long count=item.getLong("Count"); if(!stack.isEmpty()&&count>0&&isNetworkMaterial(stack)) entries.add(new Entry(stack.copyWithCount(1),count));
        }
        selectedIndex = tag.getInt("SelectedIndex");
        if (!validIndex(selectedIndex)) selectedIndex = -1;
    }
    @Override public Component getDisplayName() { return Component.translatable("block.industrialcrops.item_network_management_terminal"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new ItemNetworkTerminalMenu(id, inv, this, worldPosition); }
    private static final class Entry { private final ItemStack stack; private long count; private Entry(ItemStack stack,long count){this.stack=stack;this.count=count;} }
}
