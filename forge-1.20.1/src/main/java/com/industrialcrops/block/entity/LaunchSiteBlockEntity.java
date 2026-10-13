package com.industrialcrops.block.entity;
import com.industrialcrops.block.LaunchSiteBlock;
import com.industrialcrops.block.SatelliteRocketBlock;
import com.industrialcrops.entity.SatelliteRocketEntity;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModFluids;
import com.industrialcrops.screen.LaunchSiteMenu;
import com.industrialcrops.world.SatelliteNetworkData;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
public final class LaunchSiteBlockEntity extends BlockEntity implements MenuProvider {
   public static final int ENERGY_CAPACITY = 500000;
   public static final int INPUT_RATE = 25000;
   public static final int TANK_CAPACITY = 8000;
   public static final int COUNTDOWN_TICKS = 100;
   public static final int DATA_COUNT = 9;
   public static final int STATUS_READY = 0;
   public static final int STATUS_STRUCTURE = 1;
   public static final int STATUS_NO_ROCKET = 2;
   public static final int STATUS_BLOCKED = 3;
   public static final int STATUS_ENERGY = 4;
   public static final int STATUS_FUEL = 5;
   public static final int STATUS_COUNTDOWN = 6;
   public static final int STATUS_LAUNCHED = 7;
   public static final int STATUS_ABORTED = 8;
   public static final int[] ENERGY_COST = new int[]{0, 40000, 120000, 300000};
   public static final int[] FUEL_COST = new int[]{0, 1000, 2000, 4000};
   private final LaunchSiteBlockEntity.Energy energy = new LaunchSiteBlockEntity.Energy();
   private final FluidTank tank = new FluidTank(8000, stack -> stack.getFluid() == ModFluids.CONCENTRATED_PLASMA_JUICE.get()) {
      protected void onContentsChanged() {
         LaunchSiteBlockEntity.this.setChanged();
      }
   };
   private int pads;
   private int frames;
   private int rocketTier;
   private boolean skyClear;
   private int status = 1;
   private int countdown;
   private int messageTicks;
   private int satellites;
   private final ContainerData data = new ContainerData() {
      public int get(int index) {
         return switch (index) {
            case 0 -> LaunchSiteBlockEntity.this.energy.getEnergyStored() & 65535;
            case 1 -> LaunchSiteBlockEntity.this.energy.getEnergyStored() >>> 16;
            case 2 -> LaunchSiteBlockEntity.this.tank.getFluidAmount();
            case 3 -> LaunchSiteBlockEntity.this.pads | LaunchSiteBlockEntity.this.frames << 4;
            case 4 -> LaunchSiteBlockEntity.this.rocketTier;
            case 5 -> LaunchSiteBlockEntity.this.status;
            case 6 -> LaunchSiteBlockEntity.this.countdown;
            case 7 -> Math.min(32767, LaunchSiteBlockEntity.this.satellites);
            case 8 -> LaunchSiteBlockEntity.this.skyClear ? 1 : 0;
            default -> 0;
         };
      }
      public void set(int index, int value) {
      }
      public int getCount() {
         return 9;
      }
   };
   public LaunchSiteBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.LAUNCH_SITE.get(), pos, state);
   }
   private Direction facing() {
      return (Direction)this.getBlockState().getValue(LaunchSiteBlock.FACING);
   }
   public BlockPos padCenter() {
      return this.worldPosition.relative(this.facing().getOpposite());
   }
   private void scan(Level level) {
      BlockPos center = this.padCenter();
      int padCount = 0;
      int frameCount = 0;
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            BlockPos base = center.offset(dx, 0, dz);
            if (!base.equals(this.worldPosition) && level.getBlockState(base).is((Block)ModBlocks.LAUNCH_PAD.get())) {
               padCount++;
            }
            if (dx != 0 && dz != 0) {
               for (int dy = 1; dy <= 2; dy++) {
                  if (level.getBlockState(base.above(dy)).is((Block)ModBlocks.LAUNCH_GANTRY_FRAME.get())) {
                     frameCount++;
                  }
               }
            }
         }
      }
      this.pads = padCount;
      this.frames = frameCount;
      BlockState rocket = level.getBlockState(center.above());
      if (rocket.getBlock() instanceof SatelliteRocketBlock block && (Integer)rocket.getValue(SatelliteRocketBlock.PART) == 0) {
         this.rocketTier = block.tier();
         int top = center.getY() + block.height();
         this.skyClear = level.getHeight(Types.MOTION_BLOCKING, center.getX(), center.getZ()) <= top + 1;
         return;
      }
      this.rocketTier = 0;
      this.skyClear = false;
   }
   public boolean structureComplete() {
      return this.pads == 8 && this.frames == 8;
   }
   private int readiness() {
      if (!this.structureComplete()) {
         return 1;
      } else if (this.rocketTier == 0) {
         return 2;
      } else if (!this.skyClear) {
         return 3;
      } else if (this.energy.getEnergyStored() < ENERGY_COST[this.rocketTier]) {
         return 4;
      } else {
         return this.tank.getFluidAmount() < FUEL_COST[this.rocketTier] ? 5 : 0;
      }
   }
   public static void tick(Level level, BlockPos pos, BlockState state, LaunchSiteBlockEntity site) {
      if (level instanceof ServerLevel server) {
         if (site.countdown > 0 || level.getGameTime() % 10L == 0L) {
            site.scan(level);
         }
         if (level.getGameTime() % 40L == 0L) {
            site.satellites = SatelliteNetworkData.total(server.getServer());
         }
         if (site.countdown > 0) {
            int ready = site.readiness();
            if (ready != 0) {
               site.countdown = 0;
               site.status = 8;
               site.messageTicks = 60;
               level.playSound(null, pos, (SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
            } else {
               site.countdown--;
               site.status = 6;
               BlockPos base = site.padCenter().above();
               if (site.countdown % 20 == 0 && site.countdown > 0) {
                  level.playSound(null, base, (SoundEvent)SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.BLOCKS, 1.0F, 1.4F);
               }
               if (site.countdown < 40) {
                  server.sendParticles(
                     ParticleTypes.CAMPFIRE_COSY_SMOKE,
                     (double)base.getX() + 0.5,
                     (double)base.getY() + 0.1,
                     (double)base.getZ() + 0.5,
                     3,
                     0.6,
                     0.05,
                     0.6,
                     0.01
                  );
               }
               if (site.countdown == 0) {
                  site.launch(server);
               }
            }
         } else if (site.messageTicks > 0) {
            site.messageTicks--;
         } else {
            site.status = site.readiness();
         }
      }
   }
   public void requestLaunch(Player player) {
      if (this.level != null && !this.level.isClientSide() && this.countdown <= 0) {
         this.scan(this.level);
         int ready = this.readiness();
         if (ready != 0) {
            this.status = ready;
         } else {
            this.countdown = 100;
            this.status = 6;
            this.messageTicks = 0;
            this.level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.2F);
            this.setChanged();
         }
      }
   }
   private void launch(ServerLevel server) {
      int tier = this.rocketTier;
      this.energy.extractInternal(ENERGY_COST[tier]);
      this.tank.drain(FUEL_COST[tier], FluidAction.EXECUTE);
      BlockPos base = this.padCenter().above();
      int durability = SatelliteNetworkData.defaultDurability(tier);
      int maxDurability = Math.max(1, durability);
      if (server.getBlockEntity(base) instanceof SatelliteRocketBlockEntity standing) {
         durability = standing.durability();
         maxDurability = standing.maxDurability();
      }
      SatelliteRocketBlock.removeWithoutDrops(server, base);
      SatelliteRocketEntity rocket = new SatelliteRocketEntity(server, (double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, tier);
      server.addFreshEntity(rocket);
      server.playSound(null, base, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 4.0F, 0.5F);
      server.playSound(null, base, (SoundEvent)SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.5F, 1.6F);
      server.sendParticles(ParticleTypes.EXPLOSION, (double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, 4, 0.6, 0.1, 0.6, 0.0);
      server.sendParticles(ParticleTypes.CLOUD, (double)base.getX() + 0.5, (double)base.getY(), (double)base.getZ() + 0.5, 40, 1.0, 0.1, 1.0, 0.05);
      SatelliteNetworkData network = SatelliteNetworkData.get(server.getServer());
      network.launch(tier, durability, maxDurability);
      this.satellites = network.total();
      this.rocketTier = 0;
      this.status = 7;
      this.messageTicks = 80;
      this.setChanged();
   }
   public IEnergyStorage energy() {
      return this.energy;
   }
   public FluidTank tank() {
      return this.tank;
   }
   public ContainerData getData() {
      return this.data;
   }
   public Component getDisplayName() {
      return Component.translatable("block.industrialcrops.rocket_launch_site");
   }
   public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
      return new LaunchSiteMenu(id, inventory, this);
   }
   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt("Energy", this.energy.getEnergyStored());
      tag.put("Plasma", this.tank.writeToNBT( new CompoundTag()));
   }
   public void load(CompoundTag tag) {
      super.load(tag);
      this.energy.set(tag.getInt("Energy"));
      if (tag.contains("Plasma")) {
         this.tank.readFromNBT( tag.getCompound("Plasma"));
      }
   }
   private final class Energy extends EnergyStorage {
      Energy() {
         super(500000, 25000, 0);
      }
      void set(int value) {
         this.energy = Math.max(0, Math.min(this.capacity, value));
      }
      void extractInternal(int amount) {
         this.energy = Math.max(0, this.energy - amount);
         LaunchSiteBlockEntity.this.setChanged();
      }
      public int receiveEnergy(int amount, boolean simulate) {
         int accepted = super.receiveEnergy(amount, simulate);
         if (!simulate && accepted > 0) {
            LaunchSiteBlockEntity.this.setChanged();
         }
         return accepted;
      }
   }
   private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.energy.IEnergyStorage> forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> energy());
   private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.fluids.capability.IFluidHandler> forgeCap1 = net.minecraftforge.common.util.LazyOptional.of(() -> tank());
   @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
      if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY) return forgeCap0.cast();
      if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER) return forgeCap1.cast();
      return super.getCapability(capability, side);
   }
   @Override public void invalidateCaps() { super.invalidateCaps();
      forgeCap0.invalidate();
      forgeCap1.invalidate();
   }
   @Override public void reviveCaps() { super.reviveCaps();
      forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> energy());
      forgeCap1 = net.minecraftforge.common.util.LazyOptional.of(() -> tank());
   }
}
