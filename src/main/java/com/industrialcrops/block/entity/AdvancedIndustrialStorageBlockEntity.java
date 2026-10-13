package com.industrialcrops.block.entity;

import com.industrialcrops.item.IndustrialStorageComponentItem;
import com.industrialcrops.machine.BulkStorageItemHandler;
import com.industrialcrops.machine.DimensionUpgradeHelper;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class AdvancedIndustrialStorageBlockEntity extends BlockEntity implements MenuProvider {
   public static final int CELL_SLOT_COUNT = 6;
   public static final int SLOTS_PER_CELL = 54;
   public static final int MAX_CELL_MULTIPLIER = 16;
   public static final int STORAGE_SLOT_COUNT = 5184;
   public static final int FLUID_CAPACITY = 64000;
   private final FluidTank tank = new FluidTank(64000) {
      protected void onContentsChanged() {
         AdvancedIndustrialStorageBlockEntity.this.setChanged();
      }
   };
   private boolean dimensionTicketRequested;
   private final ItemStackHandler dimensionUpgrade = new ItemStackHandler(1) {
      public int getSlotLimit(int slot) {
         return 1;
      }

      public boolean isItemValid(int slot, ItemStack stack) {
         return DimensionUpgradeHelper.isDimensionUpgrade(stack);
      }

      protected void onContentsChanged(int slot) {
         AdvancedIndustrialStorageBlockEntity.this.setChanged();
         AdvancedIndustrialStorageBlockEntity.this.refreshDimensionTicket();
      }
   };
   private final ItemStackHandler cellInventory = new ItemStackHandler(6) {
      public int getSlotLimit(int slot) {
         return 1;
      }

      public boolean isItemValid(int slot, ItemStack stack) {
         return AdvancedIndustrialStorageBlockEntity.isStorageCell(stack);
      }

      protected void onContentsChanged(int slot) {
         AdvancedIndustrialStorageBlockEntity.this.setChanged();
      }
   };
   private final ItemStackHandler storageInventory = new BulkStorageItemHandler(5184) {
      protected int getStackLimit(int slot, ItemStack stack) {
         return Integer.MAX_VALUE;
      }

      public boolean isItemValid(int slot, ItemStack stack) {
         return AdvancedIndustrialStorageBlockEntity.this.isStorageSlotUnlocked(slot);
      }

      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
         return !AdvancedIndustrialStorageBlockEntity.this.isStorageSlotUnlocked(slot) ? stack : super.insertItem(slot, stack, simulate);
      }

      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         return !AdvancedIndustrialStorageBlockEntity.this.isStorageSlotUnlocked(slot) ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
      }

      protected void onContentsChanged(int slot) {
         AdvancedIndustrialStorageBlockEntity.this.setChanged();
      }
   };
   private final IItemHandler basic_pipeItemHandler = new IItemHandler() {
      public int getSlots() {
         return 5184;
      }

      public ItemStack getStackInSlot(int slot) {
         return AdvancedIndustrialStorageBlockEntity.isValidStorageSlot(slot)
            ? AdvancedIndustrialStorageBlockEntity.this.storageInventory.getStackInSlot(slot)
            : ItemStack.EMPTY;
      }

      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
         return AdvancedIndustrialStorageBlockEntity.isValidStorageSlot(slot) && AdvancedIndustrialStorageBlockEntity.this.isStorageSlotUnlocked(slot)
            ? AdvancedIndustrialStorageBlockEntity.this.storageInventory.insertItem(slot, stack, simulate)
            : stack;
      }

      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         return AdvancedIndustrialStorageBlockEntity.isValidStorageSlot(slot) && AdvancedIndustrialStorageBlockEntity.this.isStorageSlotUnlocked(slot)
            ? AdvancedIndustrialStorageBlockEntity.this.storageInventory.extractItem(slot, amount, simulate)
            : ItemStack.EMPTY;
      }

      public int getSlotLimit(int slot) {
         return AdvancedIndustrialStorageBlockEntity.this.isStorageSlotUnlocked(slot) ? Integer.MAX_VALUE : 0;
      }

      public boolean isItemValid(int slot, ItemStack stack) {
         return AdvancedIndustrialStorageBlockEntity.this.isStorageSlotUnlocked(slot);
      }
   };

   public boolean hasFluidStorage() {
      return this.getType() == ModBlockEntities.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get();
   }

   public FluidTank getTank() {
      return this.tank;
   }

   public AdvancedIndustrialStorageBlockEntity(BlockPos pos, BlockState state) {
      this((BlockEntityType<?>)ModBlockEntities.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), pos, state);
   }

   protected AdvancedIndustrialStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public static boolean isStorageCell(ItemStack stack) {
      return stack.getItem() instanceof IndustrialStorageComponentItem;
   }

   public static int getStorageCellMultiplier(ItemStack stack) {
      return stack.getItem() instanceof IndustrialStorageComponentItem component ? component.capacityMultiplier() : 0;
   }

   public ItemStackHandler getCellInventory() {
      return this.cellInventory;
   }

   public ItemStack getDimensionUpgrade() {
      return this.dimensionUpgrade.getStackInSlot(0);
   }

   public boolean installDimensionUpgrade(ItemStack stack) {
      if (this.getDimensionUpgrade().isEmpty() && DimensionUpgradeHelper.isDimensionUpgrade(stack)) {
         this.dimensionUpgrade.setStackInSlot(0, stack.copyWithCount(1));
         return true;
      } else {
         return false;
      }
   }

   public ItemStack removeDimensionUpgrade() {
      return this.dimensionUpgrade.extractItem(0, 1, false);
   }

   public void releaseDimensionTicket() {
      if (this.dimensionTicketRequested || !this.getDimensionUpgrade().isEmpty()) {
         DimensionUpgradeHelper.forceOwnerChunk(this, false);
         this.dimensionTicketRequested = false;
      }
   }

   public void onLoad() {
      super.onLoad();
      this.refreshDimensionTicket();
   }

   private void refreshDimensionTicket() {
      boolean shouldForce = !this.getDimensionUpgrade().isEmpty();
      if (this.level != null && shouldForce != this.dimensionTicketRequested) {
         DimensionUpgradeHelper.forceOwnerChunk(this, shouldForce);
         this.dimensionTicketRequested = shouldForce;
      }
   }

   public IItemHandler getPipeItemHandler() {
      return this.basic_pipeItemHandler;
   }

   public int getUnlockedStorageSlots() {
      int capacityUnits = 0;

      for (int slot = 0; slot < 6; slot++) {
         capacityUnits += getStorageCellMultiplier(this.cellInventory.getStackInSlot(slot));
      }

      return Math.min(5184, capacityUnits * 54);
   }

   public boolean isStorageSlotUnlocked(int slot) {
      return isValidStorageSlot(slot) && slot < this.getUnlockedStorageSlots();
   }

   public boolean canRemoveStorageCell(int cellSlot) {
      if (cellSlot >= 0 && cellSlot < 6) {
         int removedSlots = getStorageCellMultiplier(this.cellInventory.getStackInSlot(cellSlot)) * 54;
         int unlockedAfterRemove = Math.max(0, this.getUnlockedStorageSlots() - removedSlots);

         for (int slot = unlockedAfterRemove; slot < 5184; slot++) {
            if (!this.storageInventory.getStackInSlot(slot).isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public ItemStack getStorageStack(int slot) {
      return isValidStorageSlot(slot) ? this.storageInventory.getStackInSlot(slot) : ItemStack.EMPTY;
   }

   public void setStorageStack(int slot, ItemStack stack) {
      if (this.isStorageSlotUnlocked(slot)) {
         this.storageInventory.setStackInSlot(slot, stack.copy());
      }
   }

   public ItemStack extractFromStorageSlot(int slot, int amount) {
      return !this.isStorageSlotUnlocked(slot) ? ItemStack.EMPTY : this.storageInventory.extractItem(slot, amount, false);
   }

   public int insertIntoStorageSlot(int slot, ItemStack stack) {
      if (this.isStorageSlotUnlocked(slot) && !stack.isEmpty()) {
         int original = stack.getCount();
         ItemStack remainder = this.storageInventory.insertItem(slot, stack, false);
         return original - remainder.getCount();
      } else {
         return 0;
      }
   }

   public int insertIntoStorage(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         int original = stack.getCount();
         ItemStack remaining = stack.copy();
         int unlocked = this.getUnlockedStorageSlots();

         for (int slot = 0; slot < unlocked && !remaining.isEmpty(); slot++) {
            remaining = this.storageInventory.insertItem(slot, remaining, false);
         }

         return original - remaining.getCount();
      }
   }

   public ItemStack createDroppedStack(Block block) {
      ItemStack stack = new ItemStack(block.asItem());
      CompoundTag storageData = this.writeInventories(this.level == null ? null : this.level.registryAccess());
      if (!storageData.isEmpty()) {
         stack.set(DataComponents.CUSTOM_DATA, CustomData.of(storageData));
      }

      return stack;
   }

   public void readStorageFromStack(ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null && this.level != null) {
         this.readInventories(customData.copyTag(), this.level.registryAccess());
      }

      this.setChanged();
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.put("StorageData", this.writeInventories(registries));
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      if (tag.contains("StorageData")) {
         this.readInventories(tag.getCompound("StorageData"), registries);
      }
   }

   public Component getDisplayName() {
      return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
      return new AdvancedIndustrialStorageMenu(containerId, inventory, this, this.worldPosition);
   }

   private CompoundTag writeInventories(@Nullable Provider registries) {
      CompoundTag tag = new CompoundTag();
      if (this.hasFluidStorage() && registries != null) {
         tag.put("Fluid", this.tank.writeToNBT(registries, new CompoundTag()));
      }

      tag.put("Cells", this.cellInventory.serializeNBT(registries));
      tag.put("Storage", this.storageInventory.serializeNBT(registries));
      tag.put("DimensionUpgrade", this.dimensionUpgrade.serializeNBT(registries));
      return tag;
   }

   private void readInventories(CompoundTag tag, Provider registries) {
      if (this.hasFluidStorage()) {
         this.tank.setFluid(FluidStack.EMPTY);
         if (tag.contains("Fluid")) {
            this.tank.readFromNBT(registries, tag.getCompound("Fluid"));
         }

         if (this.tank.getFluidAmount() > 64000) {
            this.tank.getFluid().setAmount(64000);
         }
      }

      if (tag.contains("Cells")) {
         readFixedSizeInventory(this.cellInventory, 6, tag.getCompound("Cells"), registries);
      }

      if (tag.contains("Storage")) {
         readFixedSizeInventory(this.storageInventory, 5184, tag.getCompound("Storage"), registries);
      }

      if (tag.contains("DimensionUpgrade")) {
         readFixedSizeInventory(this.dimensionUpgrade, 1, tag.getCompound("DimensionUpgrade"), registries);
      }
   }

   private static void readFixedSizeInventory(ItemStackHandler target, int expectedSize, CompoundTag tag, Provider registries) {
      ItemStackHandler temporary = new BulkStorageItemHandler(0);
      temporary.deserializeNBT(registries, tag);
      target.setSize(expectedSize);
      int slots = Math.min(expectedSize, temporary.getSlots());

      for (int slot = 0; slot < slots; slot++) {
         ItemStack stack = temporary.getStackInSlot(slot);
         if (!stack.isEmpty()) {
            target.setStackInSlot(slot, stack);
         }
      }
   }

   private static boolean isValidStorageSlot(int slot) {
      return slot >= 0 && slot < 5184;
   }
}
