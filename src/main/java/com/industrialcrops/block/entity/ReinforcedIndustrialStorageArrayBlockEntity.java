package com.industrialcrops.block.entity;

import com.industrialcrops.machine.BulkStorageItemHandler;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.registry.ModBlockEntities;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class ReinforcedIndustrialStorageArrayBlockEntity extends BlockEntity {
   public static final int SLOT_COUNT = 54;
   /** Iron tier (Reinforced Storage Component) per-slot item limit. */
   public static final int SLOT_LIMIT = 99;
   private final ItemStackHandler inventory = new BulkStorageItemHandler(54) {
      protected int getStackLimit(int slot, ItemStack stack) {
         return SLOT_LIMIT;
      }

      public int getSlotLimit(int slot) {
         return SLOT_LIMIT;
      }

      protected void onContentsChanged(int slot) {
         ReinforcedIndustrialStorageArrayBlockEntity.this.setChanged();
      }
   };

   public ReinforcedIndustrialStorageArrayBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get(), pos, state);
   }

   public int getSlots() {
      return this.inventory.getSlots();
   }

   public ItemStackHandler getInventory() {
      return this.inventory;
   }

   public ItemStack getStackInSlot(int slot) {
      return this.inventory.getStackInSlot(slot);
   }

   public int insertStack(ItemStack stack) {
      int original = stack.getCount();
      ItemStack remaining = stack.copy();

      for (int slot = 0; slot < this.inventory.getSlots() && !remaining.isEmpty(); slot++) {
         remaining = this.inventory.insertItem(slot, remaining, false);
      }

      return original - remaining.getCount();
   }

   public ItemStack extractFromSlot(int slot, int amount) {
      return this.inventory.extractItem(slot, amount, false);
   }

   public int insertIntoSlot(int slot, ItemStack stack) {
      if (slot >= 0 && slot < this.inventory.getSlots() && !stack.isEmpty()) {
         int original = stack.getCount();
         ItemStack remainder = this.inventory.insertItem(slot, stack, false);
         return original - remainder.getCount();
      } else {
         return 0;
      }
   }

   public void setStackInSlot(int slot, ItemStack stack) {
      if (slot >= 0 && slot < this.inventory.getSlots()) {
         this.inventory.setStackInSlot(slot, stack);
      }
   }

   public boolean isEmpty() {
      for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
         if (!this.inventory.getStackInSlot(slot).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void clear() {
      for (int slot = 0; slot < this.inventory.getSlots(); slot++) {
         this.inventory.setStackInSlot(slot, ItemStack.EMPTY);
      }
   }

   public ItemStack createDroppedStack(Block block) {
      ItemStack stack = new ItemStack(block.asItem());
      CompoundTag storageData = this.inventory.serializeNBT(this.level == null ? null : this.level.registryAccess());
      if (!storageData.isEmpty()) {
         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(storageData));
      }

      return stack;
   }

   public void readStorageFromStack(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null && this.level != null) {
         this.inventory.deserializeNBT(this.level.registryAccess(), customData.copyTag());
      }

      this.setChanged();
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.put("Inventory", this.inventory.serializeNBT(registries));
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      if (tag.contains("Inventory")) {
         this.inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
         MachineInventoryHelper.ensureSize(this.inventory, 54);
      }
   }

   @Nullable
   public static ReinforcedIndustrialStorageArrayBlockEntity findAttached(Level level, BlockPos controllerPos) {
      List<ReinforcedIndustrialStorageArrayBlockEntity> attached = findAllAttached(level, controllerPos);
      return attached.isEmpty() ? null : attached.getFirst();
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

         for (Direction directionx : Direction.values()) {
            BlockPos neighborPos = currentPos.relative(directionx);
            if (!visited.contains(neighborPos) && level.getBlockEntity(neighborPos) instanceof ReinforcedIndustrialStorageArrayBlockEntity drive) {
               attached.add(drive);
               queue.addLast(neighborPos);
               visited.add(neighborPos);
            }
         }
      }

      return attached;
   }
}
