package com.industrialcrops.screen;

import com.industrialcrops.block.entity.SatelliteControlDeviceBlockEntity;
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

public final class SatelliteControlMenu extends AbstractContainerMenu {
   public static final int BUTTON_TOGGLE = 0;
   private final BlockPos pos;
   private final ContainerData data;

   public SatelliteControlMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
      this(id, inventory, buffer.readBlockPos(), new SimpleContainerData(24));
   }

   public SatelliteControlMenu(int id, Inventory inventory, SatelliteControlDeviceBlockEntity device) {
      this(id, inventory, device.getBlockPos(), device.getData());
   }

   private SatelliteControlMenu(int id, Inventory inventory, BlockPos pos, ContainerData data) {
      super((MenuType)ModMenus.SATELLITE_CONTROL_DEVICE.get(), id);
      this.pos = pos;
      this.data = data;
      this.addDataSlots(data);
   }

   private int wide(int low) {
      return this.data.get(low) & 65535 | (this.data.get(low + 1) & 65535) << 16;
   }

   public int energy() {
      return this.wide(0);
   }

   private int flags() {
      return this.data.get(2);
   }

   public boolean receiverFound() {
      return (this.flags() & 1) != 0;
   }

   public boolean receiverLinked() {
      return (this.flags() & 2) != 0;
   }

   public boolean terminalFound() {
      return (this.flags() & 4) != 0;
   }

   public boolean running() {
      return (this.flags() & 8) != 0;
   }

   public boolean satelliteLost() {
      return (this.flags() & 16) != 0;
   }

   public int area() {
      return Math.max(1, this.data.get(3));
   }

   public int maxReach() {
      return this.data.get(4);
   }

   public int progress() {
      return this.data.get(5);
   }

   public int status() {
      return this.data.get(6);
   }

   public int count(int tier) {
      return tier >= 1 && tier <= 3 ? this.data.get(6 + tier) : 0;
   }

   public int durability(int tier) {
      return tier >= 1 && tier <= 3 ? this.wide(10 + (tier - 1) * 2) : 0;
   }

   public int maxDurability(int tier) {
      return tier >= 1 && tier <= 3 ? this.wide(16 + (tier - 1) * 2) : 0;
   }

   public int mined() {
      return this.wide(22);
   }

   public boolean clickMenuButton(Player player, int id) {
      if (player.level().getBlockEntity(this.pos) instanceof SatelliteControlDeviceBlockEntity device) {
         if (id == 0) {
            device.toggleRunning();
            return true;
         } else if (id >= 1 && id <= 3) {
            device.selectArea(id);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean stillValid(Player player) {
      return stillValid(ContainerLevelAccess.create(player.level(), this.pos), player, (Block)ModBlocks.SATELLITE_CONTROL_DEVICE.get());
   }

   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }
}
