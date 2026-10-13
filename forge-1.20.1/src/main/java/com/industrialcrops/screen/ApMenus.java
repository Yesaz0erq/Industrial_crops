package com.industrialcrops.screen;
import com.industrialcrops.machine.ApUpgrades;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
final class ApMenus {
   private ApMenus() {
   }
   static void addPlayerInventory(Consumer<Slot> var0, Inventory var1, int var2, int var3, int var4) {
      for (int var5 = 0; var5 < 3; var5++) {
         for (int var6 = 0; var6 < 9; var6++) {
            var0.accept(new Slot(var1, var6 + var5 * 9 + 9, var2 + var6 * 18, var3 + var5 * 18));
         }
      }
      for (int var7 = 0; var7 < 9; var7++) {
         var0.accept(new Slot(var1, var7, var2 + var7 * 18, var4));
      }
   }
   static void addUpgradeSlots(Consumer<Slot> var0, IItemHandler var1, final BooleanSupplier var2) {
      for (int var3 = 0; var3 < 4; var3++) {
         var0.accept(new SlotItemHandler(var1, var3, -84 + var3 % 2 * 18, 48 + var3 / 2 * 18) {
            public boolean mayPlace(ItemStack var1) {
               return ApUpgrades.isUpgrade(var1);
            }
            public int getMaxStackSize() {
               return 1;
            }
            public int getMaxStackSize(ItemStack var1) {
               return 1;
            }
            public boolean isActive() {
               return var2.getAsBoolean();
            }
         });
      }
   }
   static ItemStack quickMove(AbstractContainerMenu var0, Player var1, int var2, int var3, int var4, int var5, int var6) {
      if (var2 >= 0 && var2 < var0.slots.size()) {
         Slot var7 = (Slot)var0.slots.get(var2);
         if (!var7.hasItem()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack var8 = var7.getItem();
            ItemStack var9 = var8.copy();
            boolean var10;
            if (var2 < var3) {
               var10 = move(var0, var8, var3, var0.slots.size(), true);
            } else {
               var10 = ApUpgrades.isUpgrade(var8) && move(var0, var8, var6, var6 + 4, false);
               if (!var8.isEmpty()) {
                  var10 |= move(var0, var8, var4, var5, false);
               }
            }
            if (!var10) {
               return ItemStack.EMPTY;
            } else {
               if (var8.isEmpty()) {
                  var7.set(ItemStack.EMPTY);
               } else {
                  var7.setChanged();
               }
               return var8.getCount() == var9.getCount() ? ItemStack.EMPTY : var9;
            }
         }
      } else {
         return ItemStack.EMPTY;
      }
   }
   private static boolean move(AbstractContainerMenu var0, ItemStack var1, int var2, int var3, boolean var4) {
      if (var0 instanceof ApAgricultureMenu var6) {
         return var6.move(var1, var2, var3, var4);
      } else {
         return var0 instanceof ApLivestockMenu var5 ? var5.move(var1, var2, var3, var4) : false;
      }
   }
}
