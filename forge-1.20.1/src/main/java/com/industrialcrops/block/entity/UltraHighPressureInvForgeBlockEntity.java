package com.industrialcrops.block.entity;
import com.industrialcrops.block.UltraHighPressureInvForgeBlock;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.recipe.UltraHighPressureInvForgeRecipes;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.UltraHighPressureInvForgeMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
public final class UltraHighPressureInvForgeBlockEntity extends BlockEntity implements MenuProvider {
   public static final int INPUT_SLOT = 0;
   public static final int INPUT_SLOTS = 4;
   public static final int OUTPUT_SLOT = 4;
   public static final int SLOT_COUNT = 5;
   public static final int ENERGY_CAPACITY = 100000;
   private static final int RECEIVE_RATE = 5000;
   private int progress;
   private int selectedRecipe;
   private boolean syncedHeat;
   private final UltraHighPressureInvForgeBlockEntity.TrackedEnergyStorage energy = new UltraHighPressureInvForgeBlockEntity.TrackedEnergyStorage();
   private final ItemStackHandler inventory = new ItemStackHandler(5) {
      public boolean isItemValid(int slot, ItemStack stack) {
         return slot < 4 && UltraHighPressureInvForgeBlockEntity.isSupportedIngredient(stack);
      }
      protected void onContentsChanged(int slot) {
         UltraHighPressureInvForgeBlockEntity.this.setChanged();
      }
   };
   private final ContainerData data = new ContainerData() {
      public int get(int index) {
         return switch (index) {
            case 0 -> UltraHighPressureInvForgeBlockEntity.this.energy.getEnergyStored() & 65535;
            case 1 -> UltraHighPressureInvForgeBlockEntity.this.energy.getEnergyStored() >>> 16;
            case 2 -> UltraHighPressureInvForgeBlockEntity.this.progress;
            case 3 -> UltraHighPressureInvForgeBlockEntity.this.recipe().ticks();
            case 4 -> UltraHighPressureInvForgeBlockEntity.this.selectedRecipe;
            case 5 -> UltraHighPressureInvForgeBlockEntity.this.level != null && UltraHighPressureInvForgeBlockEntity.this.level.isClientSide()
            ? (UltraHighPressureInvForgeBlockEntity.this.syncedHeat ? 1 : 0)
            : (UltraHighPressureInvForgeBlockEntity.this.hasHeatSource() ? 1 : 0);
            default -> 0;
         };
      }
      public void set(int index, int value) {
         if (index == 0) {
            UltraHighPressureInvForgeBlockEntity.this.energy.setStored(UltraHighPressureInvForgeBlockEntity.this.energy.getEnergyStored() & -65536 | value & 65535);
         } else if (index == 1) {
            UltraHighPressureInvForgeBlockEntity.this.energy
               .setStored(UltraHighPressureInvForgeBlockEntity.this.energy.getEnergyStored() & 65535 | (value & 65535) << 16);
         } else if (index == 2) {
            UltraHighPressureInvForgeBlockEntity.this.progress = Math.max(0, value);
         } else if (index == 4) {
            UltraHighPressureInvForgeBlockEntity.this.selectedRecipe = Math.floorMod(value, UltraHighPressureInvForgeRecipes.count());
         } else if (index == 5) {
            UltraHighPressureInvForgeBlockEntity.this.syncedHeat = value != 0;
         }
      }
      public int getCount() {
         return 6;
      }
   };
   public UltraHighPressureInvForgeBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.ULTRA_HIGH_PRESSURE_INV_FORGE.get(), pos, state);
   }
   public static void tick(Level level, BlockPos pos, BlockState state, UltraHighPressureInvForgeBlockEntity furnace) {
      if (!level.isClientSide()) {
         PoweredMachineSupport.pullEnergy(level, pos, furnace.energy, 5000);
         boolean heat = furnace.hasHeatSource();
         furnace.syncedHeat = heat;
         UltraHighPressureInvForgeRecipes.Recipe recipe = furnace.recipe();
         ItemStack result = recipe.output();
         boolean ready = heat && recipe.matches(furnace.inventory, 0, 4) && furnace.canOutput(result);
         boolean working = ready && furnace.energy.getEnergyStored() >= recipe.energyPerTick();
         if (working) {
            furnace.energy.consume(recipe.energyPerTick());
            furnace.progress++;
            if (furnace.progress >= recipe.ticks()) {
               recipe.consume(furnace.inventory, 0, 4);
               furnace.insertOutput(result);
               furnace.progress = 0;
            }
            furnace.setChanged();
         } else if (furnace.progress != 0) {
            furnace.progress = 0;
            furnace.setChanged();
         }
         if ((Boolean)state.getValue(UltraHighPressureInvForgeBlock.LIT) != working) {
            level.setBlock(pos, (BlockState)state.setValue(UltraHighPressureInvForgeBlock.LIT, working), 3);
         }
      }
   }
   public static boolean isSupportedIngredient(ItemStack stack) {
      return UltraHighPressureInvForgeRecipes.isIngredient(stack);
   }
   public UltraHighPressureInvForgeRecipes.Recipe recipe() {
      return UltraHighPressureInvForgeRecipes.get(this.selectedRecipe);
   }
   public void selectRecipeIndex(int index) {
      int target = Math.floorMod(index, UltraHighPressureInvForgeRecipes.count());
      if (target != this.selectedRecipe) {
         this.selectedRecipe = target;
         this.progress = 0;
         this.setChanged();
      }
   }
   public void selectRecipe(int delta) {
      this.selectedRecipe = Math.floorMod(this.selectedRecipe + delta, UltraHighPressureInvForgeRecipes.count());
      this.progress = 0;
      this.setChanged();
   }
   public int selectedRecipe() {
      return this.selectedRecipe;
   }
   public boolean hasHeatSource() {
      if (this.level == null) {
         return false;
      } else {
         for (Direction direction : Direction.values()) {
            BlockState adjacent = this.level.getBlockState(this.worldPosition.relative(direction));
            if (adjacent.is(Blocks.FIRE)
               || adjacent.is(Blocks.SOUL_FIRE)
               || adjacent.is(Blocks.LAVA)
               || adjacent.is(Blocks.MAGMA_BLOCK)
               || adjacent.is(Blocks.CAMPFIRE)
               || adjacent.is(Blocks.SOUL_CAMPFIRE)) {
               return true;
            }
         }
         return false;
      }
   }
   public ItemStackHandler getInventory() {
      return this.inventory;
   }
   public EnergyStorage getEnergyStorage(@Nullable Direction side) {
      return this.energy;
   }
   public ContainerData getData() {
      return this.data;
   }
   public Component getDisplayName() {
      return Component.translatable("block.industrialcrops.ultra_high_pressure_inv_forge");
   }
   @Nullable
   public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
      return new UltraHighPressureInvForgeMenu(id, inventory, this, this.worldPosition);
   }
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.put("Inventory", this.inventory.serializeNBT());
      tag.putInt("Energy", this.energy.getEnergyStored());
      tag.putInt("Progress", this.progress);
      tag.putInt("SelectedRecipe", this.selectedRecipe);
   }
   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains("Inventory")) {
         this.inventory.deserializeNBT(tag.getCompound("Inventory"));
      }
      if (this.inventory.getSlots() == 2) {
         ItemStack oldOutput = this.inventory.getStackInSlot(1).copy();
         MachineInventoryHelper.ensureSize(this.inventory, 5);
         this.inventory.setStackInSlot(1, ItemStack.EMPTY);
         this.inventory.setStackInSlot(4, oldOutput);
      }
      MachineInventoryHelper.ensureSize(this.inventory, 5);
      this.energy.setStored(tag.getInt("Energy"));
      this.selectedRecipe = Math.floorMod(tag.getInt("SelectedRecipe"), UltraHighPressureInvForgeRecipes.count());
      this.progress = Math.max(0, Math.min(this.recipe().ticks(), tag.getInt("Progress")));
   }
   private boolean canOutput(ItemStack result) {
      ItemStack output = this.inventory.getStackInSlot(4);
      return output.isEmpty() || ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize();
   }
   private void insertOutput(ItemStack result) {
      ItemStack output = this.inventory.getStackInSlot(4);
      if (output.isEmpty()) {
         this.inventory.setStackInSlot(4, result.copy());
      } else {
         output.grow(result.getCount());
         this.inventory.setStackInSlot(4, output);
      }
   }
   private final class TrackedEnergyStorage extends EnergyStorage {
      private TrackedEnergyStorage() {
         super(100000, 5000, 0);
      }
      private void setStored(int value) {
         this.energy = Math.max(0, Math.min(this.capacity, value));
      }
      private void consume(int value) {
         this.energy = Math.max(0, this.energy - value);
      }
      public int receiveEnergy(int amount, boolean simulate) {
         int accepted = super.receiveEnergy(amount, simulate);
         if (accepted > 0 && !simulate) {
            UltraHighPressureInvForgeBlockEntity.this.setChanged();
         }
         return accepted;
      }
   }
   private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> getInventory());
   private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.energy.IEnergyStorage> forgeCap1 = net.minecraftforge.common.util.LazyOptional.of(() -> getEnergyStorage(null));
   @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
      if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) return forgeCap0.cast();
      if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY) return forgeCap1.cast();
      return super.getCapability(capability, side);
   }
   @Override public void invalidateCaps() { super.invalidateCaps();
      forgeCap0.invalidate();
      forgeCap1.invalidate();
   }
   @Override public void reviveCaps() { super.reviveCaps();
      forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> getInventory());
      forgeCap1 = net.minecraftforge.common.util.LazyOptional.of(() -> getEnergyStorage(null));
   }
}
