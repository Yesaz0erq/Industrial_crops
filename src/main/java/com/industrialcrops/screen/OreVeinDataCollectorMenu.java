package com.industrialcrops.screen;

import com.industrialcrops.block.entity.OreVeinDataCollectorBlockEntity;
import com.industrialcrops.machine.FluxOreIndex;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class OreVeinDataCollectorMenu extends AbstractContainerMenu {
   public static final int INPUT_X = 13, INPUT_Y = 23;
   public static final int OUTPUT_X = 13, OUTPUT_Y = 73;
   public static final int INVENTORY_Y = 140, HOTBAR_Y = 198;

   /** One ore type of this world, as shown in the survey ledger. */
   public record Entry(Item icon, Item ingot) { }

   private final BlockPos pos;
   private final ContainerData data;
   private final List<Entry> entries;

   public OreVeinDataCollectorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
      this(id, inventory, buffer.readBlockPos(), new ItemStackHandler(2), new SimpleContainerData(OreVeinDataCollectorBlockEntity.DATA_COUNT), readEntries(buffer));
   }

   public OreVeinDataCollectorMenu(int id, Inventory inventory, OreVeinDataCollectorBlockEntity collector, List<FluxOreIndex.OreType> types) {
      this(id, inventory, collector.getBlockPos(), collector.getInventory(), collector.getData(), toEntries(types));
   }

   private OreVeinDataCollectorMenu(int id, Inventory inventory, BlockPos pos, IItemHandler handler, ContainerData data, List<Entry> entries) {
      super(ModMenus.ORE_VEIN_DATA_COLLECTOR.get(), id);
      this.pos = pos;
      this.data = data;
      this.entries = entries;
      addSlot(new SlotItemHandler(handler, OreVeinDataCollectorBlockEntity.INPUT_SLOT, INPUT_X, INPUT_Y) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.SLIME_BALL);
         }
      });
      addSlot(new SlotItemHandler(handler, OreVeinDataCollectorBlockEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y) {
         @Override
         public boolean mayPlace(ItemStack stack) {
            return false;
         }
      });
      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
         }
      }
      for (int column = 0; column < 9; column++) {
         addSlot(new Slot(inventory, column, 8 + column * 18, HOTBAR_Y));
      }
      addDataSlots(data);
   }

   private static List<Entry> readEntries(RegistryFriendlyByteBuf buffer) {
      int count = buffer.readVarInt();
      List<Entry> list = new ArrayList<>(count);
      for (int index = 0; index < count; index++) {
         Item icon = FluxOreIndex.item(buffer.readResourceLocation());
         Item ingot = FluxOreIndex.item(buffer.readResourceLocation());
         list.add(new Entry(icon, ingot));
      }
      return List.copyOf(list);
   }

   private static List<Entry> toEntries(List<FluxOreIndex.OreType> types) {
      List<Entry> list = new ArrayList<>(types.size());
      for (FluxOreIndex.OreType type : types) list.add(new Entry(type.icon(), type.ingot()));
      return List.copyOf(list);
   }

   public List<Entry> entries() {
      return entries;
   }

   public int energy() {
      return data.get(0) & 0xFFFF | (data.get(1) & 0xFFFF) << 16;
   }

   public int progress() {
      return data.get(2);
   }

   public int scanned() {
      return data.get(3);
   }

   public int total() {
      int synced = data.get(4);
      return synced > 0 ? synced : entries.size();
   }

   public int status() {
      return data.get(5);
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      Slot slot = slots.get(index);
      if (!slot.hasItem()) return ItemStack.EMPTY;
      ItemStack stack = slot.getItem();
      ItemStack original = stack.copy();
      if (index < 2) {
         if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
      } else if (stack.is(Items.SLIME_BALL)) {
         if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
      if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
      else slot.setChanged();
      return original;
   }

   @Override
   public boolean stillValid(Player player) {
      return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.ORE_VEIN_DATA_COLLECTOR.get());
   }
}
