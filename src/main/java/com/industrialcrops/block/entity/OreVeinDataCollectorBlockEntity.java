package com.industrialcrops.block.entity;

import com.industrialcrops.block.OreVeinDataCollectorBlock;
import com.industrialcrops.item.FluxOreGelItem;
import com.industrialcrops.machine.FluxOreIndex;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.OreVeinDataCollectorMenu;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Ore Vein Data Collector: spends FE to survey the natural metal ores of the current world, 30 seconds per
 * ore type, then imprints the slimeball in its input slot into Flux Ore Gel.
 */
public final class OreVeinDataCollectorBlockEntity extends BlockEntity implements MenuProvider {
   public static final int INPUT_SLOT = 0;
   public static final int OUTPUT_SLOT = 1;
   public static final int SCAN_TICKS = 30 * 20;
   public static final int ENERGY_CAPACITY = 1_000_000;
   public static final int ENERGY_INPUT = 50_000;
   public static final int ENERGY_PER_TICK = 200;
   public static final int DATA_COUNT = 6;
   public static final int STATUS_SCANNING = 0;
   public static final int STATUS_NO_SLIMEBALL = 1;
   public static final int STATUS_NO_ENERGY = 2;
   public static final int STATUS_OUTPUT_BLOCKED = 3;
   public static final int STATUS_NO_ORES = 4;

   private final ItemStackHandler inventory = new ItemStackHandler(2) {
      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
         return slot == INPUT_SLOT && stack.is(Items.SLIME_BALL);
      }

      @Override
      protected void onContentsChanged(int slot) {
         setChanged();
      }
   };
   private final Energy energy = new Energy();
   private int progress;
   private int scanned;
   private int status = STATUS_NO_SLIMEBALL;
   private final ContainerData data = new ContainerData() {
      @Override
      public int get(int index) {
         return switch (index) {
            case 0 -> energy.getEnergyStored() & 0xFFFF;
            case 1 -> energy.getEnergyStored() >>> 16;
            case 2 -> progress;
            case 3 -> scanned;
            case 4 -> level instanceof ServerLevel server ? FluxOreIndex.types(server).size() : 0;
            case 5 -> status;
            default -> 0;
         };
      }

      @Override
      public void set(int index, int value) {
      }

      @Override
      public int getCount() {
         return DATA_COUNT;
      }
   };

   public OreVeinDataCollectorBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.ORE_VEIN_DATA_COLLECTOR.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, OreVeinDataCollectorBlockEntity collector) {
      if (!(level instanceof ServerLevel server)) return;
      PoweredMachineSupport.pullEnergy(level, pos, collector.energy, ENERGY_INPUT);
      boolean active = collector.work(server);
      if (state.getValue(OreVeinDataCollectorBlock.LIT) != active) {
         level.setBlock(pos, state.setValue(OreVeinDataCollectorBlock.LIT, active), 3);
      }
   }

   private boolean work(ServerLevel server) {
      List<FluxOreIndex.OreType> types = FluxOreIndex.types(server);
      ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
      if (!input.is(Items.SLIME_BALL)) {
         setStatus(STATUS_NO_SLIMEBALL);
         if (progress != 0 || scanned != 0) {
            progress = 0;
            scanned = 0;
            setChanged();
         }
         return false;
      }
      if (types.isEmpty()) {
         setStatus(STATUS_NO_ORES);
         return false;
      }
      if (scanned >= types.size()) {
         ItemStack result = gel(server, types);
         // Survey complete: wait until the output has room for the gel.
         if (!fits(result)) {
            setStatus(STATUS_OUTPUT_BLOCKED);
            return false;
         }
         finish(server, result);
         return false;
      }
      if (energy.getEnergyStored() < ENERGY_PER_TICK) {
         setStatus(STATUS_NO_ENERGY);
         return false;
      }
      energy.consume(ENERGY_PER_TICK);
      setStatus(STATUS_SCANNING);
      if (++progress >= SCAN_TICKS) {
         progress = 0;
         scanned++;
         server.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.2F + 0.05F * scanned);
         if (scanned >= types.size()) {
            ItemStack result = gel(server, types);
            if (fits(result)) finish(server, result);
         }
      }
      setChanged();
      return true;
   }

   private static ItemStack gel(ServerLevel server, List<FluxOreIndex.OreType> types) {
      return FluxOreGelItem.create(FluxOreIndex.ingotIds(types), server.dimension().location());
   }

   private boolean fits(ItemStack result) {
      ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
      return output.isEmpty()
         || ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize();
   }

   private void finish(ServerLevel server, ItemStack result) {
      inventory.extractItem(INPUT_SLOT, 1, false);
      ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
      if (output.isEmpty()) inventory.setStackInSlot(OUTPUT_SLOT, result);
      else {
         output.grow(1);
         inventory.setStackInSlot(OUTPUT_SLOT, output);
      }
      scanned = 0;
      progress = 0;
      server.playSound(null, worldPosition, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 0.7F);
      server.playSound(null, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.5F, 1.8F);
      setChanged();
   }

   private void setStatus(int value) {
      status = value;
   }

   public void openMenu(ServerPlayer player) {
      List<FluxOreIndex.OreType> types = FluxOreIndex.types((ServerLevel) player.level());
      player.openMenu(this, buffer -> writeMenuData(buffer, worldPosition, types));
   }

   public static void writeMenuData(RegistryFriendlyByteBuf buffer, BlockPos pos, List<FluxOreIndex.OreType> types) {
      buffer.writeBlockPos(pos);
      buffer.writeVarInt(types.size());
      for (FluxOreIndex.OreType type : types) {
         buffer.writeResourceLocation(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(type.icon()));
         buffer.writeResourceLocation(type.ingotId());
      }
   }

   public ItemStackHandler getInventory() {
      return inventory;
   }

   public ContainerData getData() {
      return data;
   }

   public EnergyStorage getEnergyStorage(@Nullable Direction side) {
      return energy;
   }

   public IItemHandler getAutomationHandler() {
      return new RangedWrapper(inventory, 0, 2) {
         @Override
         public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot == INPUT_SLOT ? super.insertItem(slot, stack, simulate) : stack;
         }

         @Override
         public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == OUTPUT_SLOT ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
         }
      };
   }

   public List<ItemStack> collectDrops() {
      return List.of(inventory.getStackInSlot(INPUT_SLOT).copy(), inventory.getStackInSlot(OUTPUT_SLOT).copy());
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("block.industrialcrops.ore_vein_data_collector");
   }

   @Override
   public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
      List<FluxOreIndex.OreType> types = level instanceof ServerLevel server ? FluxOreIndex.types(server) : List.of();
      return new OreVeinDataCollectorMenu(containerId, playerInventory, this, types);
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      tag.put("Inventory", inventory.serializeNBT(registries));
      tag.putInt("Energy", energy.getEnergyStored());
      tag.putInt("Progress", progress);
      tag.putInt("Scanned", scanned);
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      if (tag.contains("Inventory")) {
         inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
         MachineInventoryHelper.ensureSize(inventory, 2);
      }
      energy.set(tag.getInt("Energy"));
      progress = Math.max(0, tag.getInt("Progress"));
      scanned = Math.max(0, tag.getInt("Scanned"));
   }

   private final class Energy extends EnergyStorage {
      Energy() {
         super(ENERGY_CAPACITY, ENERGY_INPUT, 0);
      }

      void set(int value) {
         this.energy = Math.max(0, Math.min(capacity, value));
      }

      void consume(int value) {
         this.energy = Math.max(0, this.energy - value);
      }

      @Override
      public int receiveEnergy(int amount, boolean simulate) {
         int received = super.receiveEnergy(amount, simulate);
         if (!simulate && received > 0) setChanged();
         return received;
      }
   }
}
