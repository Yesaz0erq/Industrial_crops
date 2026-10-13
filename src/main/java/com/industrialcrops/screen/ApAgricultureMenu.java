package com.industrialcrops.screen;

import com.industrialcrops.block.entity.ApAgricultureBlockEntity;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ApAgricultureMenu extends AbstractContainerMenu implements ApMachineMenu {
   public static final int SLOTS_X = 34;
   public static final int ROW1_Y = 104;
   public static final int ROW2_Y = 122;
   public static final int INVENTORY_Y = 152;
   public static final int HOTBAR_Y = 210;
   public static final int BUTTON_LESS = 0;
   public static final int BUTTON_MORE = 1;
   public static final int UPGRADE_SLOT = 18;
   public static final int MACHINE_SLOTS = 22;
   private final BlockPos pos;
   private final ContainerData data;
   private final IItemHandler upgrades;
   private boolean upgradesVisible;

   public ApAgricultureMenu(int var1, Inventory var2, RegistryFriendlyByteBuf var3) {
      this(var1, var2, var3.readBlockPos(), new ItemStackHandler(18), new ItemStackHandler(4), new SimpleContainerData(12));
   }

   public ApAgricultureMenu(int var1, Inventory var2, ApAgricultureBlockEntity var3) {
      this(var1, var2, var3.getBlockPos(), var3.getInventory(), var3.getUpgradeSlots(), var3.getData());
   }

   private ApAgricultureMenu(int var1, Inventory var2, BlockPos var3, ItemStackHandler var4, IItemHandler var5, ContainerData var6) {
      super((MenuType)ModMenus.AP_AGRICULTURE.get(), var1);
      this.pos = var3;
      this.data = var6;
      this.upgrades = var5;

      for (int var7 = 0; var7 < 9; var7++) {
         this.addSlot(new SlotItemHandler(var4, var7, 34 + var7 * 18, 104) {
            public boolean mayPlace(ItemStack var1) {
               return ApAgricultureBlockEntity.isPlantableSeed(var1);
            }
         });
      }

      for (int var8 = 0; var8 < 9; var8++) {
         this.addSlot(new SlotItemHandler(var4, 9 + var8, 34 + var8 * 18, 122) {
            public boolean mayPlace(ItemStack var1) {
               return false;
            }
         });
      }

      ApMenus.addUpgradeSlots(var1x -> this.addSlot(var1x), var5, () -> this.upgradesVisible);
      ApMenus.addPlayerInventory(var1x -> this.addSlot(var1x), var2, 34, 152, 210);
      this.addDataSlots(var6);
   }

   public int energy() {
      return this.data.get(0) & 65535 | (this.data.get(1) & 65535) << 16;
   }

   @Override
   public ApStructure.Status structure() {
      return ApStructure.Status.unpack(this.data.get(2));
   }

   public int boost() {
      return this.data.get(3);
   }

   public int status() {
      return this.data.get(4);
   }

   public int crops() {
      return this.data.get(5);
   }

   public int planted() {
      return this.data.get(6) & 65535 | (this.data.get(7) & 65535) << 16;
   }

   public int boostedThousands() {
      return this.data.get(8);
   }

   @Override
   public boolean projection() {
      return this.data.get(9) != 0;
   }

   public int harvested() {
      return this.data.get(10) & 65535 | (this.data.get(11) & 65535) << 16;
   }

   @Override
   public IItemHandler upgrades() {
      return this.upgrades;
   }

   @Override
   public void setUpgradesVisible(boolean var1) {
      this.upgradesVisible = var1;
   }

   public boolean clickMenuButton(Player var1, int var2) {
      if (var1.level().getBlockEntity(this.pos) instanceof ApAgricultureBlockEntity var3) {
         if (var2 == 0) {
            var3.changeBoost(-1);
         } else if (var2 == 1) {
            var3.changeBoost(1);
         } else {
            if (var2 != 20) {
               return false;
            }

            var3.toggleProjection();
         }

         return true;
      } else {
         return false;
      }
   }

   public ItemStack quickMoveStack(Player var1, int var2) {
      return ApMenus.quickMove(this, var1, var2, 22, 0, 9, 18);
   }

   public boolean stillValid(Player var1) {
      return stillValid(ContainerLevelAccess.create(var1.level(), this.pos), var1, (Block)ModBlocks.AP_AGRICULTURE_CONTROLLER.get());
   }

   public boolean move(ItemStack var1, int var2, int var3, boolean var4) {
      return this.moveItemStackTo(var1, var2, var3, var4);
   }
}
