package com.industrialcrops.screen;

import com.industrialcrops.block.BioEnergyMachineBlock;
import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class BioEnergyMenu extends AbstractContainerMenu implements UpgradeableMenu {
   public static final int UPGRADE_X = -58;
   public static final int UPGRADE_Y = 24;
   public static final int UPGRADE_SPACING = 18;
   public static final int BUTTON_SIDE_BASE = 10;
   public static final int BUTTON_ALL_SIDES_ON = 20;
   public static final int BUTTON_ALL_SIDES_OFF = 21;
   public static final int RELATIVE_SIDE_COUNT = 6;
   private final BioEnergyMachineBlockEntity blockEntity;
   private final BlockPos pos;
   private final ContainerData data;
   private final int machineSlots;
   private final int playerInventoryStart;
   private boolean upgradeSlotsVisible;

   public BioEnergyMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
      this(id, inventory, read(inventory, buffer));
   }

   private BioEnergyMenu(int id, Inventory inventory, BioEnergyMachineBlockEntity entity) {
      this(id, inventory, entity, entity.getBlockPos());
   }

   public BioEnergyMenu(int id, Inventory inventory, final BioEnergyMachineBlockEntity entity, BlockPos pos) {
      super((MenuType)ModMenus.BIO_ENERGY_MACHINE.get(), id);
      this.blockEntity = entity;
      this.pos = pos;
      this.data = entity.getData();
      this.machineSlots = entity.getKind() == BioEnergyMachineBlockEntity.Kind.BATTERY
         ? 0
         : (entity.getKind() == BioEnergyMachineBlockEntity.Kind.GENERATOR ? 5 : 1);
      if (this.machineSlots > 0) {
         this.addSlot(new SlotItemHandler(entity.getInventory(), 0, 18, 35) {
            public boolean mayPlace(ItemStack stack) {
               return entity.getInventory().isItemValid(0, stack);
            }
         });
      }

      if (entity.getKind() == BioEnergyMachineBlockEntity.Kind.GENERATOR) {
         for (int i = 0; i < 4; i++) {
            int x = -58 + i % 2 * 18;
            int y = 24 + i / 2 * 18;
            this.addSlot(new SlotItemHandler(entity.getInventory(), 1 + i, x, y) {
               public boolean mayPlace(ItemStack stack) {
                  return SpeedUpgradeHelper.isSpeedUpgrade(stack);
               }

               public boolean isActive() {
                  return BioEnergyMenu.this.upgradeSlotsVisible;
               }
            });
         }
      }

      this.playerInventoryStart = this.machineSlots;

      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
         }
      }

      for (int column = 0; column < 9; column++) {
         this.addSlot(new Slot(inventory, column, 8 + column * 18, 142));
      }

      this.addDataSlots(this.data);
   }

   public BioEnergyMachineBlockEntity.Kind kind() {
      int ordinal = Math.max(0, Math.min(BioEnergyMachineBlockEntity.Kind.values().length - 1, this.data.get(10)));
      return BioEnergyMachineBlockEntity.Kind.values()[ordinal];
   }

   public boolean isReactor() {
      return this.blockEntity.isReactor();
   }

   public int energy() {
      return this.data.get(0) & 65535 | (this.data.get(1) & 65535) << 16;
   }

   public int energyCapacity() {
      return this.data.get(2) & 65535 | (this.data.get(3) & 65535) << 16;
   }

   public int progress() {
      return this.data.get(4);
   }

   public int maxProgress() {
      return this.data.get(5);
   }

   public int residue() {
      return this.data.get(6);
   }

   public int residueCapacity() {
      return this.data.get(7);
   }

   public int burnTime() {
      return this.data.get(8);
   }

   public int burnTimeTotal() {
      return this.data.get(9);
   }

   public int currentYield() {
      return this.data.get(11);
   }

   public boolean isEnergySideEnabled(Direction direction) {
      return (this.data.get(12) & 1 << direction.ordinal()) != 0;
   }

   public boolean isRelativeEnergySideEnabled(int relativeSide) {
      return this.isEnergySideEnabled(this.toWorldDirection(relativeSide));
   }

   public BioEnergyMachineBlockEntity.EnergySideMode relativeEnergySideMode(int relativeSide) {
      Direction direction = this.toWorldDirection(relativeSide);
      int bit = 1 << direction.ordinal();
      if ((this.data.get(12) & bit) != 0) {
         return BioEnergyMachineBlockEntity.EnergySideMode.OUTPUT;
      } else {
         return (this.data.get(13) & bit) != 0 ? BioEnergyMachineBlockEntity.EnergySideMode.INPUT : BioEnergyMachineBlockEntity.EnergySideMode.NONE;
      }
   }

   public Direction worldDirectionForRelative(int relativeSide) {
      return this.toWorldDirection(relativeSide);
   }

   public int scaledProgress(int width) {
      return this.maxProgress() <= 0 ? 0 : Math.max(0, Math.min(width, this.progress() * width / this.maxProgress()));
   }

   @Override
   public void setUpgradeSlotsVisible(boolean visible) {
      this.upgradeSlotsVisible = visible;
   }

   public ItemStack quickMoveStack(Player player, int index) {
      if (index >= 0 && index < this.slots.size()) {
         Slot slot = (Slot)this.slots.get(index);
         if (!slot.hasItem()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack stack = slot.getItem();
            ItemStack copy = stack.copy();
            int inventoryEnd = this.playerInventoryStart + 27;
            int hotbarEnd = inventoryEnd + 9;
            if (index < this.machineSlots) {
               if (!this.moveItemStackTo(stack, this.playerInventoryStart, hotbarEnd, true)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.blockEntity.getKind() == BioEnergyMachineBlockEntity.Kind.GENERATOR && SpeedUpgradeHelper.isSpeedUpgrade(stack)) {
               if (!this.moveItemStackTo(stack, 1, 5, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (this.machineSlots > 0 && this.blockEntity.getInventory().isItemValid(0, stack)) {
               if (!this.moveItemStackTo(stack, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index < inventoryEnd) {
               if (!this.moveItemStackTo(stack, inventoryEnd, hotbarEnd, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(stack, this.playerInventoryStart, inventoryEnd, false)) {
               return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
               slot.set(ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }

            if (stack.getCount() == copy.getCount()) {
               return ItemStack.EMPTY;
            } else {
               slot.onTake(player, stack);
               return copy;
            }
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public boolean stillValid(Player player) {
      BioEnergyMachineBlockEntity.Kind kind = this.blockEntity.getKind();
      Block expected;
      if (kind == BioEnergyMachineBlockEntity.Kind.GENERATOR) {
         expected = this.blockEntity.isReactor() ? (Block)ModBlocks.BIO_ENERGY_REACTOR.get() : (Block)ModBlocks.BIO_ENERGY_GENERATOR.get();
      } else if (kind == BioEnergyMachineBlockEntity.Kind.BATTERY) {
         expected = (Block)ModBlocks.ENERGY_BATTERY.get();
      } else {
         expected = (Block)ModBlocks.RESIDUE_INCINERATOR.get();
      }

      return stillValid(ContainerLevelAccess.create(player.level(), this.pos), player, expected);
   }

   public boolean clickMenuButton(Player player, int id) {
      if (this.blockEntity.getKind() != BioEnergyMachineBlockEntity.Kind.BATTERY) {
         return false;
      } else if (id >= 10 && id < 16) {
         this.blockEntity.cycleEnergySide(this.toWorldDirection(id - 10));
         this.broadcastChanges();
         return true;
      } else if (id != 20 && id != 21) {
         return false;
      } else {
         this.blockEntity.setAllEnergyOutputs(id == 20);
         this.broadcastChanges();
         return true;
      }
   }

   private Direction toWorldDirection(int relativeSide) {
      Direction front = (Direction)this.blockEntity.getBlockState().getValue(BioEnergyMachineBlock.FACING);

      return switch (relativeSide) {
         case 0 -> Direction.UP;
         case 1 -> Direction.DOWN;
         case 2 -> front;
         case 3 -> front.getOpposite();
         case 4 -> front.getCounterClockWise();
         case 5 -> front.getClockWise();
         default -> front;
      };
   }

   private static BioEnergyMachineBlockEntity read(Inventory inventory, RegistryFriendlyByteBuf buffer) {
      BlockPos pos = buffer.readBlockPos();
      BlockEntity entity = inventory.player.level().getBlockEntity(pos);
      if (entity instanceof BioEnergyMachineBlockEntity) {
         return (BioEnergyMachineBlockEntity)entity;
      } else {
         throw new IllegalStateException("Missing bio-energy machine at " + pos);
      }
   }
}
