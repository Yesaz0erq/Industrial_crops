package com.industrialcrops.machine;

import com.industrialcrops.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class ApUpgrades {
   public static final int SLOTS = 4;
   public static final int MAX_POWER = 3;
   private final ItemStackHandler items;
   @Nullable
   private Boolean chunksForced;

   public ApUpgrades(final Runnable var1) {
      this.items = new ItemStackHandler(4) {
         public int getSlotLimit(int var1x) {
            return 1;
         }

         public boolean isItemValid(int var1x, ItemStack var2) {
            return ApUpgrades.isUpgrade(var2);
         }

         protected void onContentsChanged(int var1x) {
            var1.run();
         }
      };
   }

   public ItemStackHandler items() {
      return this.items;
   }

   public static boolean isUpgrade(ItemStack var0) {
      return SpeedUpgradeHelper.isSpeedUpgrade(var0)
         || var0.is((Item)ModItems.POWER_COMPONENT.get())
         || var0.is((Item)ModItems.AUTOMATIC_COMPONENT.get())
         || DimensionUpgradeHelper.isDimensionUpgrade(var0);
   }

   public static int speedTier(IItemHandler var0) {
      int var1 = 0;

      for (int var2 = 0; var2 < var0.getSlots(); var2++) {
         ItemStack var3 = var0.getStackInSlot(var2);
         if (var3.is((Item)ModItems.SPEED_COMPONENT_4.get())) {
            return 4;
         }

         if (var3.is((Item)ModItems.SPEED_COMPONENT_3.get())) {
            var1 = Math.max(var1, 3);
         } else if (var3.is((Item)ModItems.SPEED_COMPONENT_2.get())) {
            var1 = Math.max(var1, 2);
         } else if (var3.is((Item)ModItems.SPEED_COMPONENT_1.get())) {
            var1 = Math.max(var1, 1);
         }
      }

      return var1;
   }

   public static int powerCount(IItemHandler var0) {
      int var1 = 0;

      for (int var2 = 0; var2 < var0.getSlots(); var2++) {
         if (var0.getStackInSlot(var2).is((Item)ModItems.POWER_COMPONENT.get())) {
            var1++;
         }
      }

      return Math.min(3, var1);
   }

   public static boolean automatic(IItemHandler var0) {
      for (int var1 = 0; var1 < var0.getSlots(); var1++) {
         if (var0.getStackInSlot(var1).is((Item)ModItems.AUTOMATIC_COMPONENT.get())) {
            return true;
         }
      }

      return false;
   }

   public static ItemStack dimension(IItemHandler var0) {
      for (int var1 = 0; var1 < var0.getSlots(); var1++) {
         ItemStack var2 = var0.getStackInSlot(var1);
         if (DimensionUpgradeHelper.isDimensionUpgrade(var2)) {
            return var2;
         }
      }

      return ItemStack.EMPTY;
   }

   public static int savingPercent(IItemHandler var0) {
      return powerCount(var0) * 25;
   }

   public int speedTier() {
      return speedTier(this.items);
   }

   public boolean automatic() {
      return automatic(this.items);
   }

   public ItemStack dimensionUpgrade() {
      return dimension(this.items);
   }

   public int cost(int var1) {
      return Math.max(1, var1 * (4 - powerCount(this.items)) / 4);
   }

   public void updateChunks(ServerLevel var1, BlockEntity var2, BlockPos var3, int var4) {
      boolean var5 = !this.dimensionUpgrade().isEmpty();
      if (this.chunksForced == null || this.chunksForced != var5) {
         forceArea(var2, var3, var4, var5);
         this.chunksForced = var5;
      }
   }

   public void release(BlockEntity var1, BlockPos var2, int var3) {
      forceArea(var1, var2, var3, false);
      this.chunksForced = false;
   }

   private static void forceArea(BlockEntity var0, BlockPos var1, int var2, boolean var3) {
      for (int var4 = var1.getX() - var2 >> 4; var4 <= var1.getX() + var2 >> 4; var4++) {
         for (int var5 = var1.getZ() - var2 >> 4; var5 <= var1.getZ() + var2 >> 4; var5++) {
            DimensionUpgradeHelper.forceChunk(var0, var4, var5, var3);
         }
      }
   }

   public void save(CompoundTag var1, Provider var2) {
      var1.put("Upgrades", this.items.serializeNBT(var2));
   }

   public void load(CompoundTag var1, Provider var2) {
      if (var1.contains("Upgrades")) {
         this.items.deserializeNBT(var2, var1.getCompound("Upgrades"));
      }
   }

   public interface Holder {
      ApUpgrades apUpgrades();
   }
}
