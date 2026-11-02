package com.industrialcrops.screen;

import com.industrialcrops.block.entity.ApSlimeFarmBlockEntity;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.FluxOreIndex;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ApSlimeFarmMenu extends AbstractContainerMenu implements ApMachineMenu {
   public static final int SLOTS_X = 34;
   public static final int ROW1_Y = 104;
   public static final int ROW2_Y = 122;
   public static final int MACHINE_SLOTS = ApSlimeFarmBlockEntity.SLOTS + 6;
   private final BlockPos pos;
   private final ContainerData data;
   private final IItemHandler upgrades;
   private boolean upgradesVisible;

   public ApSlimeFarmMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
      this(id, inventory, buffer.readBlockPos(), new ItemStackHandler(ApSlimeFarmBlockEntity.SLOTS), new ItemStackHandler(6),
         new SimpleContainerData(ApSlimeFarmBlockEntity.DATA_COUNT));
   }

   public ApSlimeFarmMenu(int id, Inventory inventory, ApSlimeFarmBlockEntity farm) {
      this(id, inventory, farm.getBlockPos(), farm.getInventory(), farm.getUpgradeSlots(), farm.getData());
   }

   private ApSlimeFarmMenu(int id, Inventory inventory, BlockPos pos, ItemStackHandler handler, IItemHandler upgrades, ContainerData data) {
      super(ModMenus.AP_SLIME_FARM.get(), id);
      this.pos = pos;
      this.data = data;
      this.upgrades = upgrades;
      for (int index = 0; index < ApSlimeFarmBlockEntity.INPUT_SLOTS; index++) {
         addSlot(new SlotItemHandler(handler, index, SLOTS_X + index * 18, ROW1_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
               return FluxOreIndex.isOreInput(stack) && super.mayPlace(stack);
            }
         });
      }
      for (int index = 0; index < ApSlimeFarmBlockEntity.OUTPUT_SLOTS; index++) {
         addSlot(new SlotItemHandler(handler, ApSlimeFarmBlockEntity.INPUT_SLOTS + index, SLOTS_X + index * 18, ROW2_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
               return false;
            }
         });
      }
      ApMenus.addUpgradeSlots(this::addSlot, upgrades, () -> this.upgradesVisible);
      ApMenus.addPlayerInventory(this::addSlot, inventory, SLOTS_X, 152, 210);
      addDataSlots(data);
   }

   public int energy() {
      return data.get(0) & 0xFFFF | (data.get(1) & 0xFFFF) << 16;
   }

   @Override
   public ApStructure.Status structure() {
      return ApStructure.Status.unpack(data.get(2));
   }

   public int status() {
      return data.get(3);
   }

   public int slimes() {
      return data.get(4);
   }

   public long converted() {
      return (long) (data.get(5) & 0x7FFF) | (long) (data.get(6) & 0x7FFF) << 15;
   }

   public int knownTypes() {
      return data.get(7);
   }

   @Override
   public boolean projection() {
      return data.get(8) != 0;
   }

   public Item lastProduct() {
      int id = data.get(9);
      return id <= 0 ? Items.AIR : BuiltInRegistries.ITEM.byId(id);
   }

   public int cycleTicks() {
      return Math.max(1, data.get(10));
   }

   @Override
   public IItemHandler upgrades() {
      return upgrades;
   }

   @Override
   public void setUpgradesVisible(boolean visible) {
      this.upgradesVisible = visible;
   }

   @Override
   public boolean clickMenuButton(Player player, int button) {
      if (!(player.level().getBlockEntity(pos) instanceof ApSlimeFarmBlockEntity farm)) return false;
      if (button == ApMachineMenu.BUTTON_PROJECTION) {
         farm.toggleProjection();
         return true;
      }
      return false;
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      return ApMenus.quickMove(this, player, index, MACHINE_SLOTS, 0, ApSlimeFarmBlockEntity.INPUT_SLOTS, ApSlimeFarmBlockEntity.SLOTS);
   }

   @Override
   public boolean stillValid(Player player) {
      return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.AP_SLIME_FARM.get());
   }

   public boolean move(ItemStack stack, int start, int end, boolean reverse) {
      return moveItemStackTo(stack, start, end, reverse);
   }
}
