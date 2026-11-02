package com.industrialcrops.block.entity;

import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.entity.FluxSlime;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.FluxOreIndex;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.screen.ApSlimeFarmMenu;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * AP Slime Farm: every Flux Slime inside the validated pen converts a batch of raw ore / ore blocks from the
 * input row into ingots each work cycle. Each slime only handles metals its scan data covers.
 */
public final class ApSlimeFarmBlockEntity extends ApControllerBlockEntity {
   public static final int INPUT_SLOTS = 9;
   public static final int OUTPUT_SLOTS = 9;
   public static final int SLOTS = 18;
   /** Full AP machine: four 10K crops of energy (2,000,000 FE) of buffer. */
   public static final int ENERGY_CAPACITY = 2_000_000;
   public static final int INPUT_RATE = 100_000;
   public static final int PER_SLIME = 8;
   public static final int ENERGY_PER_ITEM = 400;
   public static final int[] CYCLE_TICKS = {40, 30, 20, 14, 10};
   public static final int DATA_COUNT = 11;
   public static final int STATUS_RUNNING = 0;
   public static final int STATUS_STRUCTURE = 1;
   public static final int STATUS_ENERGY = 2;
   public static final int STATUS_NO_SLIMES = 3;
   public static final int STATUS_FULL = 4;
   public static final int STATUS_IDLE = 5;
   public static final int STATUS_UNKNOWN_ORE = 6;

   private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
      @Override
      public boolean isItemValid(int slot, ItemStack stack) {
         if (slot >= INPUT_SLOTS) return true;
         Level current = ApSlimeFarmBlockEntity.this.level;
         return current != null ? FluxOreIndex.ingotFor(current, stack) != null : FluxOreIndex.isOreInput(stack);
      }

      @Override
      protected void onContentsChanged(int slot) {
         ApSlimeFarmBlockEntity.this.setChanged();
      }
   };
   private final Energy energy = new Energy();
   private int status = STATUS_STRUCTURE;
   private int slimeCount;
   private int knownCount;
   private long converted;
   private int lastProduct;
   private int cycleTicks;
   private final ContainerData data = new ContainerData() {
      @Override
      public int get(int index) {
         return switch (index) {
            case 0 -> energy.getEnergyStored() & 0xFFFF;
            case 1 -> energy.getEnergyStored() >>> 16;
            case 2 -> structure.pack();
            case 3 -> status;
            case 4 -> Math.min(32767, slimeCount);
            case 5 -> (int) (Math.min(converted, 0x3FFFFFFFL) & 0x7FFF);
            case 6 -> (int) (Math.min(converted, 0x3FFFFFFFL) >>> 15);
            case 7 -> Math.min(32767, knownCount);
            case 8 -> projection() ? 1 : 0;
            case 9 -> lastProduct;
            case 10 -> CYCLE_TICKS[upgrades.speedTier()];
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

   public ApSlimeFarmBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.AP_SLIME_FARM.get(), pos, state);
   }

   @Override
   public Block coreBlock() {
      return ModBlocks.SLIME_INCUBATOR.get();
   }

   @Override
   public ApStructure.Type structureType() {
      return ApStructure.Type.SLIME;
   }

   @Override
   protected void onStructureChanged() {
      slimeCount = 0;
   }

   public static void tick(Level level, BlockPos pos, BlockState state, ApSlimeFarmBlockEntity farm) {
      if (!(level instanceof ServerLevel server)) return;
      PoweredMachineSupport.pullEnergy(level, pos, farm.energy, INPUT_RATE);
      long time = level.getGameTime();
      farm.tickCommon(server, time);
      if (!farm.structure.formed()) {
         farm.status = STATUS_STRUCTURE;
         farm.cycleTicks = 0;
      } else if (++farm.cycleTicks >= CYCLE_TICKS[farm.upgrades.speedTier()]) {
         farm.cycleTicks = 0;
         farm.work(server);
      }
      if (farm.structure.formed() && time % 20L == 10L && farm.upgrades.automatic()) {
         farm.exportToNeighbours(farm.inventory, INPUT_SLOTS, OUTPUT_SLOTS);
      }
      boolean active = farm.structure.formed() && farm.status == STATUS_RUNNING;
      if (state.getValue(ApControllerBlock.ACTIVE) != active) {
         level.setBlock(pos, state.setValue(ApControllerBlock.ACTIVE, active), 3);
      }
   }

   private boolean inside(FluxSlime slime) {
      return slime.isAlive() && layout().workBounds().contains(slime.position()) && workPosLoaded(slime.blockPosition());
   }

   private void work(ServerLevel level) {
      AABB area = layout().workBounds();
      List<FluxSlime> slimes = level.getEntitiesOfClass(FluxSlime.class, area, this::inside);
      slimeCount = slimes.size();
      Set<ResourceLocation> union = new HashSet<>();
      for (FluxSlime slime : slimes) union.addAll(slime.knownIngots());
      knownCount = union.size();
      if (slimes.isEmpty()) {
         status = STATUS_NO_SLIMES;
         return;
      }
      if (!hasInput()) {
         status = STATUS_IDLE;
         return;
      }
      int perItem = upgrades.cost(ENERGY_PER_ITEM);
      if (energy.getEnergyStored() < perItem) {
         status = STATUS_ENERGY;
         return;
      }
      // Each slime contributes a batch of PER_SLIME conversions for every metal it knows.
      Map<Item, Integer> capacity = new IdentityHashMap<>();
      Map<Item, FluxSlime> worker = new IdentityHashMap<>();
      for (FluxSlime slime : slimes) {
         for (ResourceLocation id : slime.knownIngots()) {
            Item ingot = FluxOreIndex.item(id);
            capacity.merge(ingot, PER_SLIME, Integer::sum);
            worker.putIfAbsent(ingot, slime);
         }
      }
      boolean anyKnown = false;
      boolean blocked = false;
      int made = 0;
      Set<FluxSlime> pulsed = new HashSet<>();
      for (int slot = 0; slot < INPUT_SLOTS; slot++) {
         ItemStack input = inventory.getStackInSlot(slot);
         Item ingot = FluxOreIndex.ingotFor(level, input);
         if (ingot == null) continue;
         Integer left = capacity.get(ingot);
         if (left == null) continue;
         anyKnown = true;
         int amount = Math.min(left, input.getCount());
         amount = Math.min(amount, energy.getEnergyStored() / perItem);
         if (amount <= 0) continue;
         ItemStack remainder = insertOutput(new ItemStack(ingot, amount), true);
         int accepted = amount - remainder.getCount();
         if (accepted <= 0) {
            blocked = true;
            continue;
         }
         insertOutput(new ItemStack(ingot, accepted), false);
         inventory.extractItem(slot, accepted, false);
         energy.consume(accepted * perItem);
         capacity.put(ingot, left - accepted);
         made += accepted;
         lastProduct = BuiltInRegistries.ITEM.getId(ingot);
         FluxSlime slime = worker.get(ingot);
         if (slime != null && pulsed.add(slime)) slime.pulse();
      }
      if (made > 0) {
         converted += made;
         status = STATUS_RUNNING;
         level.playSound(null, center(), SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 0.6F, 0.6F + level.random.nextFloat() * 0.3F);
         setChanged();
      } else if (blocked) {
         status = STATUS_FULL;
      } else if (!anyKnown) {
         status = STATUS_UNKNOWN_ORE;
      } else {
         status = energy.getEnergyStored() < perItem ? STATUS_ENERGY : STATUS_IDLE;
      }
   }

   private boolean hasInput() {
      for (int slot = 0; slot < INPUT_SLOTS; slot++) {
         if (!inventory.getStackInSlot(slot).isEmpty()) return true;
      }
      return false;
   }

   private ItemStack insertOutput(ItemStack stack, boolean simulate) {
      ItemStack remainder = stack;
      for (int slot = INPUT_SLOTS; slot < SLOTS && !remainder.isEmpty(); slot++) {
         remainder = inventory.insertItem(slot, remainder, simulate);
      }
      return remainder;
   }

   public ItemStackHandler getInventory() {
      return inventory;
   }

   public IItemHandler automation() {
      return new RangedWrapper(inventory, 0, SLOTS) {
         @Override
         public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot < INPUT_SLOTS ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
         }

         @Override
         public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot < INPUT_SLOTS ? super.insertItem(slot, stack, simulate) : stack;
         }
      };
   }

   public EnergyStorage getEnergyStorage(@Nullable Direction side) {
      return energy;
   }

   public ContainerData getData() {
      return data;
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("block.industrialcrops.ap_slime_farm");
   }

   @Override
   public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
      checkStructure();
      return new ApSlimeFarmMenu(id, playerInventory, this);
   }

   @Override
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("Inventory", inventory.serializeNBT());
      tag.putInt("Energy", energy.getEnergyStored());
      tag.putLong("Converted", converted);
      tag.putInt("LastProduct", lastProduct);
   }

   @Override
   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
      energy.set(tag.getInt("Energy"));
      converted = tag.getLong("Converted");
      lastProduct = tag.getInt("LastProduct");
   }

   private final class Energy extends EnergyStorage {
      Energy() {
         super(ENERGY_CAPACITY, INPUT_RATE, 0);
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
