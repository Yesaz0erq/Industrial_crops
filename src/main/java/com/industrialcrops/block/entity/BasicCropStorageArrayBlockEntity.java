package com.industrialcrops.block.entity;

import com.industrialcrops.machine.BulkStorageItemHandler;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class BasicCropStorageArrayBlockEntity extends BlockEntity {
   public static final int SLOT_COUNT = 54;
   /** Copper tier (Basic Storage Component) per-slot item limit. */
   public static final int SLOT_LIMIT = 99;
   private final ItemStackHandler inventory = new BulkStorageItemHandler(54) {
      protected int getStackLimit(int slot, ItemStack stack) {
         return SLOT_LIMIT;
      }

      public int getSlotLimit(int slot) {
         return SLOT_LIMIT;
      }

      protected void onContentsChanged(int slot) {
         BasicCropStorageArrayBlockEntity.this.setChanged();
      }
   };

   public BasicCropStorageArrayBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.INDUSTRIAL_CROP_STORAGE_ARRAY.get(), pos, state);
   }

   public IItemHandler getItemHandler() {
      return this.inventory;
   }

   public int getStoredIndustrialCarrots() {
      return this.getStoredCount((Item)ModItems.INDUSTRIAL_CARROT.get());
   }

   public int getStoredIndustrialPotatoes() {
      return this.getStoredCount((Item)ModItems.INDUSTRIAL_POTATO.get());
   }

   public int storeFromInventory(Inventory playerInventory, Item item) {
      int stored = 0;

      for (int i = 0; i < playerInventory.getContainerSize(); i++) {
         ItemStack stack = playerInventory.getItem(i);
         if (stack.is(item)) {
            int inserted = this.insertStack(stack.copy());
            if (inserted > 0) {
               stack.shrink(inserted);
               stored += inserted;
            }
         }
      }

      return stored;
   }

   public int extractToInventory(Inventory playerInventory, Item item, int amount) {
      int remaining = amount;

      for (int i = 0; i < this.inventory.getSlots() && remaining > 0; i++) {
         ItemStack stack = this.inventory.getStackInSlot(i);
         if (stack.is(item)) {
            ItemStack extracted = this.inventory.extractItem(i, Math.min(remaining, stack.getMaxStackSize()), false);
            int before = extracted.getCount();
            if (!playerInventory.add(extracted)) {
               playerInventory.player.drop(extracted, false);
            }

            remaining -= before;
         }
      }

      return amount - remaining;
   }

   public int insert(Item item, int amount) {
      return this.insertStack(new ItemStack(item, amount));
   }

   public int remove(Item item, int amount) {
      int remaining = amount;

      for (int i = 0; i < this.inventory.getSlots() && remaining > 0; i++) {
         if (this.inventory.getStackInSlot(i).is(item)) {
            remaining -= this.inventory.extractItem(i, remaining, false).getCount();
         }
      }

      return amount - remaining;
   }

   public int getStoredCount(Item item) {
      long total = 0L;

      for (int i = 0; i < this.inventory.getSlots(); i++) {
         if (this.inventory.getStackInSlot(i).is(item)) {
            total += (long)this.inventory.getStackInSlot(i).getCount();
         }
      }

      return (int)Math.min(2147483647L, total);
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
         CompoundTag tag = customData.copyTag();
         if (tag.contains("Items")) {
            this.inventory.deserializeNBT(this.level.registryAccess(), tag);
         } else {
            this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_CARROT.get(), "IndustrialCarrots");
            this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_POTATO.get(), "IndustrialPotatoes");
            this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_WHEAT.get(), "IndustrialWheat");
            this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_MELON.get(), "IndustrialMelons");
            this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_PUMPKIN.get(), "IndustrialPumpkins");
         }
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
      } else {
         this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_CARROT.get(), "IndustrialCarrots");
         this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_POTATO.get(), "IndustrialPotatoes");
         this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_WHEAT.get(), "IndustrialWheat");
         this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_MELON.get(), "IndustrialMelons");
         this.migrateLegacy(tag, (Item)ModItems.INDUSTRIAL_PUMPKIN.get(), "IndustrialPumpkins");
      }
   }

   private void migrateLegacy(CompoundTag tag, Item item, String key) {
      int remaining = Math.max(0, tag.getInt(key));

      while (remaining > 0) {
         int amount = Math.min(remaining, item.getDefaultMaxStackSize());
         if (this.insertStack(new ItemStack(item, amount)) <= 0) {
            break;
         }

         remaining -= amount;
      }
   }

   @Nullable
   public static BasicCropStorageArrayBlockEntity findAttached(Level level, BlockPos controllerPos) {
      List<BasicCropStorageArrayBlockEntity> attached = findAllAttached(level, controllerPos);
      return attached.isEmpty() ? null : attached.getFirst();
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

         for (Direction directionx : Direction.values()) {
            BlockPos neighborPos = currentPos.relative(directionx);
            if (!visited.contains(neighborPos) && level.getBlockEntity(neighborPos) instanceof BasicCropStorageArrayBlockEntity drive) {
               attached.add(drive);
               queue.addLast(neighborPos);
               visited.add(neighborPos);
            }
         }
      }

      return attached;
   }
}
