package com.industrialcrops.block.entity;
import com.industrialcrops.basic_pipe.PipeTransferUtil;
import com.industrialcrops.registry.CarroteItems;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.industrialcrops.compat.LegacyItemData.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
public final class ItemNetworkTerminalBlockEntity extends BlockEntity implements MenuProvider {
   private static final int MAX_NETWORK_NODES = 512;
   public static final int RECONSTRUCTION_BATCH_SIZE = 64;
   private final List<ItemNetworkTerminalBlockEntity.Entry> entries = new ArrayList<>();
   private int selectedIndex = -1;
   public ItemNetworkTerminalBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ITEM_NETWORK_TERMINAL.get(), pos, state);
   }
   public static void tick(Level level, BlockPos pos, BlockState state, ItemNetworkTerminalBlockEntity terminal) {
   }
   public boolean copySelected(int absoluteIndex) {
      if (!this.validIndex(absoluteIndex)) {
         return false;
      } else {
         MatterMachineBlockEntity copier = this.firstMachine(MatterMachineBlockEntity.Kind.COPIER);
         if (copier == null) {
            return false;
         } else {
            this.setSelectedIndex(absoluteIndex);
            return !copier.selectNetworkEntry(absoluteIndex) ? false : copier.requestOperation();
         }
      }
   }
   public void setSelectedIndex(int index) {
      this.selectedIndex = this.validIndex(index) ? index : -1;
      this.setChanged();
   }
   public boolean operateWith(MatterMachineBlockEntity machine, Player player) {
      return machine.requestOperation();
   }
   public boolean canOperateWith(MatterMachineBlockEntity machine) {
      return this.canOperateWith(machine, this.selectedIndex);
   }
   public boolean canOperateWith(MatterMachineBlockEntity machine, int index) {
      if (!this.validIndex(index)) {
         return false;
      } else if (machine.getKind() == MatterMachineBlockEntity.Kind.COPIER) {
         return isCopyable(this.entries.get(index).stack);
      } else {
         return machine.getKind() == MatterMachineBlockEntity.Kind.RECONSTRUCTOR ? this.reconstructionAmount(machine, index) > 0 : false;
      }
   }
   public boolean completeOperation(MatterMachineBlockEntity machine) {
      return this.completeOperation(machine, this.selectedIndex);
   }
   public boolean completeOperation(MatterMachineBlockEntity machine, int index) {
      if (!this.canOperateWith(machine, index)) {
         return false;
      } else if (machine.getKind() == MatterMachineBlockEntity.Kind.COPIER) {
         this.entries.get(index).count++;
         this.setChanged();
         return true;
      } else if (machine.getKind() == MatterMachineBlockEntity.Kind.RECONSTRUCTOR) {
         ItemNetworkTerminalBlockEntity.Entry entry = this.entries.get(index);
         int amount = this.reconstructionAmount(machine, index);
         if (amount <= 0) {
            return false;
         } else {
            ItemStack result = entry.stack.copyWithCount(amount);
            if (!machine.insertReconstructed(result)) {
               return false;
            } else {
               entry.count -= (long)amount;
               if (entry.count <= 0L) {
                  this.entries.remove(index);
                  this.selectedIndex = -1;
               }
               this.setChanged();
               return true;
            }
         }
      } else {
         return false;
      }
   }
   private int reconstructionAmount(MatterMachineBlockEntity machine, int index) {
      if (!this.validIndex(index)) {
         return 0;
      } else {
         ItemNetworkTerminalBlockEntity.Entry entry = this.entries.get(index);
         return (int)Math.min(64L, Math.min(entry.count, (long)machine.reconstructedCapacityFor(entry.stack)));
      }
   }
   public boolean reconstructSelected(int absoluteIndex, Player player) {
      if (!this.validIndex(absoluteIndex)) {
         return false;
      } else {
         MatterMachineBlockEntity reconstructor = this.firstMachine(MatterMachineBlockEntity.Kind.RECONSTRUCTOR);
         if (reconstructor == null) {
            return false;
         } else {
            this.setSelectedIndex(absoluteIndex);
            return !reconstructor.selectNetworkEntry(absoluteIndex) ? false : reconstructor.requestOperation();
         }
      }
   }
   public int size() {
      return this.entries.size();
   }
   public List<Integer> matchingIndices(String query) {
      List<Integer> matches = new ArrayList<>();
      String normalized = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
      for (int index = 0; index < this.entries.size(); index++) {
         ItemStack stack = this.entries.get(index).stack;
         String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
         String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
         if (normalized.isEmpty() || id.contains(normalized) || name.contains(normalized)) {
            matches.add(index);
         }
      }
      return matches;
   }
   public ItemStack displayStack(int absoluteIndex) {
      return this.validIndex(absoluteIndex) ? this.entries.get(absoluteIndex).stack.copyWithCount(1) : ItemStack.EMPTY;
   }
   public long count(int absoluteIndex) {
      return this.validIndex(absoluteIndex) ? this.entries.get(absoluteIndex).count : 0L;
   }
   public void upload(ItemStack stack) {
      this.add(stack, (long)stack.getCount());
   }
   public void addGenerated(ItemStack stack, long amount) {
      this.add(stack, amount);
   }
   public ItemStack craftingStored(ItemStack key) {
      for (ItemNetworkTerminalBlockEntity.Entry entry : this.entries) {
         if (ItemStack.isSameItemSameTags(entry.stack, key)) {
            return key.copyWithCount((int)Math.min(1000000L, entry.count));
         }
      }
      return ItemStack.EMPTY;
   }
   public ItemStack craftingExtract(ItemStack key, int amount) {
      for (int i = 0; i < this.entries.size(); i++) {
         ItemNetworkTerminalBlockEntity.Entry entry = this.entries.get(i);
         if (ItemStack.isSameItemSameTags(entry.stack, key)) {
            int taken = (int)Math.min(entry.count, (long)Math.max(0, amount));
            entry.count -= (long)taken;
            if (entry.count == 0L) {
               this.entries.remove(i);
               this.selectedIndex = -1;
            }
            this.setChanged();
            return key.copyWithCount(taken);
         }
      }
      return ItemStack.EMPTY;
   }
   public ItemStack extractOneMatching(Predicate<ItemStack> predicate) {
      for (int index = 0; index < this.entries.size(); index++) {
         ItemNetworkTerminalBlockEntity.Entry entry = this.entries.get(index);
         if (entry.count > 0L && predicate.test(entry.stack)) {
            ItemStack extracted = entry.stack.copyWithCount(1);
            entry.count--;
            if (entry.count <= 0L) {
               this.entries.remove(index);
               if (this.selectedIndex == index) {
                  this.selectedIndex = -1;
               } else if (this.selectedIndex > index) {
                  this.selectedIndex--;
               }
            }
            this.setChanged();
            return extracted;
         }
      }
      return ItemStack.EMPTY;
   }
   public void uploadFromOrbit(ItemStack stack) {
      this.store(stack, (long)stack.getCount());
   }
   private void add(ItemStack stack, long amount) {
      if (!stack.isEmpty() && amount > 0L && isNetworkMaterial(stack)) {
         this.store(stack, amount);
      }
   }
   private void store(ItemStack stack, long amount) {
      if (!stack.isEmpty() && amount > 0L) {
         for (ItemNetworkTerminalBlockEntity.Entry entry : this.entries) {
            if (ItemStack.isSameItemSameTags(entry.stack, stack)) {
               entry.count = Math.min(Long.MAX_VALUE, entry.count + amount);
               this.setChanged();
               return;
            }
         }
         this.entries.add(new ItemNetworkTerminalBlockEntity.Entry(stack.copyWithCount(1), amount));
         this.setChanged();
      }
   }
   private boolean validIndex(int index) {
      return index >= 0 && index < this.entries.size() && this.entries.get(index).count > 0L;
   }
   private static boolean isNetworkMaterial(ItemStack stack) {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("minecraft") || stack.is((Item)CarroteItems.STABLE_MATTER_INGOT.get());
   }
   public static boolean isCopyable(ItemStack stack) {
      if (stack.isEmpty() || !isNetworkMaterial(stack)) return false;
      CompoundTag tag = stack.getTag();
      return tag == null || !containsStoredItems(tag);
   }
   private static boolean containsStoredItems(CompoundTag tag) {
      if (tag.contains("Items", 9) && !tag.getList("Items", 10).isEmpty()) {
         return true;
      } else {
         for (String key : tag.getAllKeys()) {
            Tag child = tag.get(key);
            if (child instanceof CompoundTag compound && containsStoredItems(compound)) {
               return true;
            }
            if (child instanceof ListTag) {
               for (Tag value : (ListTag)child) {
                  if (value instanceof CompoundTag compound && containsStoredItems(compound)) {
                     return true;
                  }
               }
            }
         }
         return false;
      }
   }
   @Nullable
   private MatterMachineBlockEntity firstMachine(MatterMachineBlockEntity.Kind kind) {
      List<MatterMachineBlockEntity> machines = this.findMachines(kind);
      return machines.isEmpty() ? null : machines.get(0);
   }
   private List<MatterMachineBlockEntity> findMachines(MatterMachineBlockEntity.Kind kind) {
      List<MatterMachineBlockEntity> result = new ArrayList<>();
      if (this.level == null) {
         return result;
      } else {
         ArrayDeque<BlockPos> queue = new ArrayDeque<>();
         Set<BlockPos> visited = new HashSet<>();
         queue.add(this.worldPosition);
         visited.add(this.worldPosition);
         while (!queue.isEmpty() && visited.size() <= 512) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
               BlockPos next = current.relative(direction);
               if (this.level.hasChunkAt(next) && !visited.contains(next)) {
                  BlockEntity entity = this.level.getBlockEntity(next);
                  if (entity instanceof MatterMachineBlockEntity) {
                     MatterMachineBlockEntity machine = (MatterMachineBlockEntity)entity;
                     visited.add(next);
                     if (machine.getKind() == kind) {
                        result.add(machine);
                     }
                  } else if (PipeTransferUtil.isPipe(this.level.getBlockState(next))) {
                     visited.add(next);
                     queue.addLast(next);
                  }
               }
            }
         }
         return result;
      }
   }
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      ListTag list = new ListTag();
      for (ItemNetworkTerminalBlockEntity.Entry entry : this.entries) {
         CompoundTag item = new CompoundTag();
         item.put("Stack", entry.stack.save(new CompoundTag()));
         item.putLong("Count", entry.count);
         list.add(item);
      }
      tag.put("Items", list);
      tag.putInt("SelectedIndex", this.selectedIndex);
   }
   public void load(CompoundTag tag) {
      super.load(tag);
      this.entries.clear();
      ListTag list = tag.getList("Items", 10);
      for (int i = 0; i < list.size(); i++) {
         CompoundTag item = list.getCompound(i);
         ItemStack stack = ItemStack.of(item.getCompound("Stack"));
         long count = item.getLong("Count");
         if (!stack.isEmpty() && count > 0L && isNetworkMaterial(stack)) {
            this.entries.add(new ItemNetworkTerminalBlockEntity.Entry(stack.copyWithCount(1), count));
         }
      }
      this.selectedIndex = tag.getInt("SelectedIndex");
      if (!this.validIndex(this.selectedIndex)) {
         this.selectedIndex = -1;
      }
   }
   public Component getDisplayName() {
      return Component.translatable("block.industrialcrops.item_network_management_terminal");
   }
   @Nullable
   public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
      return new ItemNetworkTerminalMenu(id, inv, this, this.worldPosition);
   }
   private static final class Entry {
      private final ItemStack stack;
      private long count;
      private Entry(ItemStack stack, long count) {
         this.stack = stack;
         this.count = count;
      }
   }
}
