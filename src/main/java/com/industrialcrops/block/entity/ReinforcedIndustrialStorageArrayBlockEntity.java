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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import com.industrialcrops.compat.LegacyItemData.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
public final class ReinforcedIndustrialStorageArrayBlockEntity extends BlockEntity {
   public static final int SLOT_COUNT = 54;
   private final ItemStackHandler inventory = new BulkStorageItemHandler(54) {
      protected int getStackLimit(int slot, ItemStack stack) {
         return Integer.MAX_VALUE;
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
      CompoundTag storageData = this.inventory.serializeNBT();
      if (!storageData.isEmpty()) {
         com.industrialcrops.compat.LegacyItemData.set(stack, CustomData.of(storageData));
      }
      return stack;
   }
   public void readStorageFromStack(ItemStack stack) {
      CustomData customData = com.industrialcrops.compat.LegacyItemData.get(stack);
      if (customData != null && this.level != null) {
         this.inventory.deserializeNBT( customData.copyTag());
      }
      this.setChanged();
   }
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("Inventory", this.inventory.serializeNBT());
   }
   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("Inventory")) {
         this.inventory.deserializeNBT(tag.getCompound("Inventory"));
         MachineInventoryHelper.ensureSize(this.inventory, 54);
      }
   }
   @Nullable
   public static ReinforcedIndustrialStorageArrayBlockEntity findAttached(Level level, BlockPos controllerPos) {
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
   private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> getInventory());
   @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
      if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) return forgeCap0.cast();
      return super.getCapability(capability, side);
   }
   @Override public void invalidateCaps() { super.invalidateCaps();
      forgeCap0.invalidate();
   }
   @Override public void reviveCaps() { super.reviveCaps();
      forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> getInventory());
   }
}
