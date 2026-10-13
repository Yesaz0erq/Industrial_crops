package com.industrialcrops.block.entity;
import com.industrialcrops.block.SatelliteReceiverBlock;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.SatelliteReceiverMenu;
import com.industrialcrops.world.SatelliteNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;
public final class SatelliteReceiverBlockEntity extends BlockEntity implements MenuProvider, GeoBlockEntity {
   public static final int ENERGY_CAPACITY = 2000000;
   public static final int INPUT_RATE = 100000;
   public static final int DATA_COUNT = 5;
   private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.satellite_receiver.idle");
   private static final RawAnimation WORKING = RawAnimation.begin().thenLoop("animation.satellite_receiver.working");
   private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
   private final SatelliteReceiverBlockEntity.InputEnergyStorage energy = new SatelliteReceiverBlockEntity.InputEnergyStorage();
   private int satellites;
   private final ContainerData data = new ContainerData() {
      public int get(int index) {
         return switch (index) {
            case 0 -> SatelliteReceiverBlockEntity.this.energy.getEnergyStored() & 65535;
            case 1 -> SatelliteReceiverBlockEntity.this.energy.getEnergyStored() >>> 16;
            case 2 -> SatelliteReceiverBlockEntity.this.isWorking() ? 1 : 0;
            case 3 -> SatelliteReceiverBlockEntity.this.energy.getEnergyStored() > 0 && SatelliteReceiverBlockEntity.this.satellites > 0 ? 1 : 0;
            case 4 -> Math.min(32767, SatelliteReceiverBlockEntity.this.satellites);
            default -> 0;
         };
      }
      public void set(int index, int value) {
      }
      public int getCount() {
         return 5;
      }
   };
   public SatelliteReceiverBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.SATELLITE_RECEIVER.get(), pos, state);
   }
   public static void tick(Level level, BlockPos pos, BlockState state, SatelliteReceiverBlockEntity machine) {
      if (machine.energy.getEnergyStored() > 0) {
         machine.energy.extractEnergy(20, false);
      }
      if (level.getGameTime() % 40L == 0L && level.getServer() != null) {
         machine.satellites = SatelliteNetworkData.total(level.getServer());
      }
      boolean active = machine.energy.getEnergyStored() > 0;
      if ((Boolean)state.getValue(SatelliteReceiverBlock.ACTIVE) != active) {
         level.setBlock(pos, (BlockState)state.setValue(SatelliteReceiverBlock.ACTIVE, active), 3);
      }
   }
   public boolean isWorking() {
      return (Boolean)this.getBlockState().getValue(SatelliteReceiverBlock.ACTIVE);
   }
   @Nullable
   public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
      return this.energy;
   }
   public ContainerData getData() {
      return this.data;
   }
   public Component getDisplayName() {
      return Component.translatable("block.industrialcrops.satellite_receiver");
   }
   public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
      return new SatelliteReceiverMenu(id, inventory, this);
   }
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController(this, "operation", 5, state -> state.setAndContinue(this.isWorking() ? WORKING : IDLE)));
   }
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.animationCache;
   }
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt("Energy", this.energy.getEnergyStored());
   }
   public void load(CompoundTag tag) {
      super.load(tag);
      this.energy.setStored(tag.getInt("Energy"));
   }
   private final class InputEnergyStorage extends EnergyStorage {
      InputEnergyStorage() {
         super(2000000, 100000, 0);
      }
      void setStored(int value) {
         this.energy = Math.max(0, Math.min(this.capacity, value));
      }
      public int receiveEnergy(int amount, boolean simulate) {
         int accepted = super.receiveEnergy(amount, simulate);
         if (!simulate && accepted > 0) {
            SatelliteReceiverBlockEntity.this.setChanged();
         }
         return accepted;
      }
   }
   private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.energy.IEnergyStorage> forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> getEnergyStorage(null));
   @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
      if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY) return forgeCap0.cast();
      return super.getCapability(capability, side);
   }
   @Override public void invalidateCaps() { super.invalidateCaps();
      forgeCap0.invalidate();
   }
   @Override public void reviveCaps() { super.reviveCaps();
      forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> getEnergyStorage(null));
   }
}
