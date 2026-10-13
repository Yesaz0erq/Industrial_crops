package com.industrialcrops.screen;
import com.industrialcrops.block.entity.LaunchSiteBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
public final class LaunchSiteMenu extends AbstractContainerMenu {
   public static final int BUTTON_LAUNCH = 0;
   private final BlockPos pos;
   private final ContainerData data;
   public LaunchSiteMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
      this(id, inventory, buffer.readBlockPos(), new SimpleContainerData(9));
   }
   public LaunchSiteMenu(int id, Inventory inventory, LaunchSiteBlockEntity site) {
      this(id, inventory, site.getBlockPos(), site.getData());
   }
   private LaunchSiteMenu(int id, Inventory inventory, BlockPos pos, ContainerData data) {
      super((MenuType)ModMenus.LAUNCH_SITE.get(), id);
      this.pos = pos;
      this.data = data;
      this.addDataSlots(data);
   }
   public int energy() {
      return this.data.get(0) & 65535 | (this.data.get(1) & 65535) << 16;
   }
   public int fuel() {
      return this.data.get(2) & 65535;
   }
   public int pads() {
      return this.data.get(3) & 15;
   }
   public int frames() {
      return this.data.get(3) >> 4 & 15;
   }
   public int rocketTier() {
      return this.data.get(4);
   }
   public int status() {
      return this.data.get(5);
   }
   public int countdown() {
      return this.data.get(6);
   }
   public int satellites() {
      return this.data.get(7);
   }
   public boolean skyClear() {
      return this.data.get(8) != 0;
   }
   public boolean clickMenuButton(Player player, int id) {
      if (id == 0 && player.level().getBlockEntity(this.pos) instanceof LaunchSiteBlockEntity site) {
         site.requestLaunch(player);
         return true;
      } else {
         return false;
      }
   }
   public boolean stillValid(Player player) {
      return stillValid(ContainerLevelAccess.create(player.level(), this.pos), player, (Block)ModBlocks.LAUNCH_SITE.get());
   }
   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }
}
