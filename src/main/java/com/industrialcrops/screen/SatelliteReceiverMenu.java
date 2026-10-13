package com.industrialcrops.screen;

import com.industrialcrops.block.entity.SatelliteReceiverBlockEntity;
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

public final class SatelliteReceiverMenu extends AbstractContainerMenu {
   private final BlockPos pos;
   private final ContainerData data;

   public SatelliteReceiverMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
      this(id, inventory, buffer.readBlockPos(), new SimpleContainerData(5));
   }

   public SatelliteReceiverMenu(int id, Inventory inventory, SatelliteReceiverBlockEntity machine) {
      this(id, inventory, machine.getBlockPos(), machine.getData());
   }

   private SatelliteReceiverMenu(int id, Inventory inventory, BlockPos pos, ContainerData data) {
      super((MenuType)ModMenus.SATELLITE_RECEIVER.get(), id);
      this.pos = pos;
      this.data = data;
      this.addDataSlots(data);
   }

   public int energy() {
      return this.data.get(0) & 65535 | (this.data.get(1) & 65535) << 16;
   }

   public boolean active() {
      return this.data.get(2) != 0;
   }

   public boolean linked() {
      return this.data.get(3) != 0;
   }

   public int satellites() {
      return this.data.get(4);
   }

   public boolean stillValid(Player player) {
      return stillValid(ContainerLevelAccess.create(player.level(), this.pos), player, (Block)ModBlocks.SATELLITE_RECEIVER.get());
   }

   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }
}
