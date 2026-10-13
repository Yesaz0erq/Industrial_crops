package com.industrialcrops.block.entity;

import com.industrialcrops.block.SatelliteControlDeviceBlock;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.SatelliteControlMenu;
import com.industrialcrops.world.SatelliteNetworkData;
import com.industrialcrops.world.SpaceMiningAreas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public final class SatelliteControlDeviceBlockEntity extends BlockEntity implements MenuProvider {
   public static final int ENERGY_CAPACITY = 1000000;
   public static final int INPUT_RATE = 50000;
   public static final int ENERGY_PER_TICK = 200;
   public static final int RANGE = 8;
   public static final int DATA_COUNT = 24;
   public static final int STATUS_MINING = 0;
   public static final int STATUS_IDLE = 1;
   public static final int STATUS_NO_RECEIVER = 2;
   public static final int STATUS_RECEIVER_OFFLINE = 3;
   public static final int STATUS_NO_TERMINAL = 4;
   public static final int STATUS_NO_SATELLITE = 5;
   public static final int STATUS_NO_ENERGY = 6;
   public static final int FLAG_RECEIVER = 1;
   public static final int FLAG_LINKED = 2;
   public static final int FLAG_TERMINAL = 4;
   public static final int FLAG_RUNNING = 8;
   public static final int FLAG_LOST = 16;
   private final SatelliteControlDeviceBlockEntity.Energy energy = new SatelliteControlDeviceBlockEntity.Energy();
   private int area = 1;
   private boolean running;
   private int progress;
   private long mined;
   private int status = 1;
   private int lostTicks;
   @Nullable
   private BlockPos receiverPos;
   @Nullable
   private BlockPos terminalPos;
   private boolean receiverLinked;
   private final int[] counts = new int[4];
   private final long[] durability = new long[4];
   private final long[] maxDurability = new long[4];
   private int maxReach;
   private final ContainerData data = new ContainerData() {
      public int get(int index) {
         if (index >= 7 && index <= 9) {
            return Math.min(32767, SatelliteControlDeviceBlockEntity.this.counts[index - 6]);
         } else if (index >= 10 && index <= 15) {
            return SatelliteControlDeviceBlockEntity.split(
               SatelliteControlDeviceBlockEntity.clampInt(SatelliteControlDeviceBlockEntity.this.durability[(index - 10) / 2 + 1]), (index - 10) % 2
            );
         } else if (index >= 16 && index <= 21) {
            return SatelliteControlDeviceBlockEntity.split(
               SatelliteControlDeviceBlockEntity.clampInt(SatelliteControlDeviceBlockEntity.this.maxDurability[(index - 16) / 2 + 1]), (index - 16) % 2
            );
         } else {
            return switch (index) {
               case 0 -> SatelliteControlDeviceBlockEntity.split(SatelliteControlDeviceBlockEntity.this.energy.getEnergyStored(), 0);
               case 1 -> SatelliteControlDeviceBlockEntity.split(SatelliteControlDeviceBlockEntity.this.energy.getEnergyStored(), 1);
               case 2 -> SatelliteControlDeviceBlockEntity.this.flags();
               case 3 -> SatelliteControlDeviceBlockEntity.this.area;
               case 4 -> SatelliteControlDeviceBlockEntity.this.maxReach;
               case 5 -> SatelliteControlDeviceBlockEntity.this.progress;
               case 6 -> SatelliteControlDeviceBlockEntity.this.status;
               default -> 0;
               case 22 -> SatelliteControlDeviceBlockEntity.split(SatelliteControlDeviceBlockEntity.clampInt(SatelliteControlDeviceBlockEntity.this.mined), 0);
               case 23 -> SatelliteControlDeviceBlockEntity.split(SatelliteControlDeviceBlockEntity.clampInt(SatelliteControlDeviceBlockEntity.this.mined), 1);
            };
         }
      }

      public void set(int index, int value) {
      }

      public int getCount() {
         return 24;
      }
   };

   private static int clampInt(long value) {
      return (int)Math.max(0L, Math.min(2147483647L, value));
   }

   private static int split(int value, int half) {
      return half == 0 ? value & 65535 : value >>> 16;
   }

   public SatelliteControlDeviceBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.SATELLITE_CONTROL_DEVICE.get(), pos, state);
   }

   private int flags() {
      int flags = 0;
      if (this.receiverPos != null) {
         flags |= 1;
      }

      if (this.receiverLinked) {
         flags |= 2;
      }

      if (this.terminalPos != null) {
         flags |= 4;
      }

      if (this.running) {
         flags |= 8;
      }

      if (this.lostTicks > 0) {
         flags |= 16;
      }

      return flags;
   }

   private void scan(Level level) {
      SatelliteReceiverBlockEntity receiver = null;
      ItemNetworkTerminalBlockEntity terminal = null;
      double receiverDistance = Double.MAX_VALUE;
      double terminalDistance = Double.MAX_VALUE;
      int minChunkX = this.worldPosition.getX() - 8 >> 4;
      int maxChunkX = this.worldPosition.getX() + 8 >> 4;
      int minChunkZ = this.worldPosition.getZ() - 8 >> 4;
      int maxChunkZ = this.worldPosition.getZ() + 8 >> 4;

      for (int cx = minChunkX; cx <= maxChunkX; cx++) {
         for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
            if (level.hasChunk(cx, cz)) {
               LevelChunk chunk = level.getChunk(cx, cz);

               for (BlockEntity entity : chunk.getBlockEntities().values()) {
                  BlockPos pos = entity.getBlockPos();
                  if (!entity.isRemoved() && this.inRange(pos)) {
                     double distance = pos.distSqr(this.worldPosition);
                     if (entity instanceof SatelliteReceiverBlockEntity) {
                        SatelliteReceiverBlockEntity found = (SatelliteReceiverBlockEntity)entity;
                        boolean powered = found.getEnergyStorage(null).getEnergyStored() > 0;
                        boolean currentPowered = receiver != null && receiver.getEnergyStorage(null).getEnergyStored() > 0;
                        if (receiver == null || powered && !currentPowered || powered == currentPowered && distance < receiverDistance) {
                           receiver = found;
                           receiverDistance = distance;
                        }
                     } else if (entity instanceof ItemNetworkTerminalBlockEntity) {
                        ItemNetworkTerminalBlockEntity found = (ItemNetworkTerminalBlockEntity)entity;
                        if (distance < terminalDistance) {
                           terminal = found;
                           terminalDistance = distance;
                        }
                     }
                  }
               }
            }
         }
      }

      this.receiverPos = receiver == null ? null : receiver.getBlockPos();
      this.receiverLinked = receiver != null && receiver.getEnergyStorage(null).getEnergyStored() > 0;
      this.terminalPos = terminal == null ? null : terminal.getBlockPos();
   }

   private boolean inRange(BlockPos pos) {
      return Math.abs(pos.getX() - this.worldPosition.getX()) <= 8
         && Math.abs(pos.getY() - this.worldPosition.getY()) <= 8
         && Math.abs(pos.getZ() - this.worldPosition.getZ()) <= 8;
   }

   private void snapshot(SatelliteNetworkData network) {
      for (int tier = 1; tier <= 3; tier++) {
         this.counts[tier] = network.count(tier);
         this.durability[tier] = network.durability(tier);
         this.maxDurability[tier] = network.maxDurability(tier);
      }

      this.maxReach = network.maxReach();
   }

   public static void tick(Level level, BlockPos pos, BlockState state, SatelliteControlDeviceBlockEntity device) {
      if (level instanceof ServerLevel server) {
         SatelliteNetworkData network = SatelliteNetworkData.get(server.getServer());
         if (Math.floorMod(level.getGameTime() + pos.asLong(), 20L) == 0L) {
            device.scan(level);
            device.snapshot(network);
         }

         if (device.lostTicks > 0) {
            device.lostTicks--;
         }

         int next = device.evaluate(network);
         device.status = next;
         if (next == 0) {
            device.energy.consume(200);
            device.progress++;
            if (device.progress >= SpaceMiningAreas.cycleTicks(device.area)) {
               device.progress = 0;
               device.mineOnce(server, network);
            }

            device.setChanged();
         } else if (next != 6) {
            device.progress = 0;
         }

         boolean active = next == 0;
         if ((Boolean)state.getValue(SatelliteControlDeviceBlock.ACTIVE) != active) {
            level.setBlock(pos, (BlockState)state.setValue(SatelliteControlDeviceBlock.ACTIVE, active), 3);
         }
      }
   }

   private int evaluate(SatelliteNetworkData network) {
      if (!this.running) {
         return 1;
      } else if (this.receiverPos == null) {
         return 2;
      } else if (!this.receiverLinked) {
         return 3;
      } else if (this.terminalPos == null) {
         return 4;
      } else if (network.assign(this.area) == null) {
         return 5;
      } else {
         return this.energy.getEnergyStored() < 200 ? 6 : 0;
      }
   }

   private void mineOnce(ServerLevel level, SatelliteNetworkData network) {
      SatelliteNetworkData.Satellite satellite = network.assign(this.area);
      if (satellite != null && this.terminalPos != null) {
         if (level.getBlockEntity(this.terminalPos) instanceof ItemNetworkTerminalBlockEntity terminal) {
            ItemStack var6 = SpaceMiningAreas.roll(this.area, level.getRandom());
            terminal.uploadFromOrbit(var6);
            this.mined = this.mined + (long)var6.getCount();
            level.playSound(null, this.terminalPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 1.3F);
            if (network.wear(satellite, 1)) {
               this.lostTicks = 100;
               level.playSound(null, this.worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
            }

            this.snapshot(network);
         } else {
            this.terminalPos = null;
         }
      }
   }

   public void toggleRunning() {
      this.running = !this.running;
      this.progress = 0;
      this.setChanged();
   }

   public void selectArea(int requested) {
      if (requested >= 1 && requested <= 3 && requested != this.area) {
         if (this.level == null || this.level.getServer() == null || requested <= SatelliteNetworkData.get(this.level.getServer()).maxReach()) {
            this.area = requested;
            this.progress = 0;
            this.setChanged();
         }
      }
   }

   public IEnergyStorage energy() {
      return this.energy;
   }

   public ContainerData getData() {
      return this.data;
   }

   public Component getDisplayName() {
      return Component.translatable("block.industrialcrops.satellite_control_device");
   }

   public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
      if (this.level != null && !this.level.isClientSide()) {
         this.scan(this.level);
         if (this.level.getServer() != null) {
            this.snapshot(SatelliteNetworkData.get(this.level.getServer()));
         }
      }

      return new SatelliteControlMenu(id, inventory, this);
   }

   protected void saveAdditional(CompoundTag tag, Provider registries) {
      super.saveAdditional(tag, registries);
      tag.putInt("Energy", this.energy.getEnergyStored());
      tag.putInt("Area", this.area);
      tag.putBoolean("Running", this.running);
      tag.putInt("Progress", this.progress);
      tag.putLong("Mined", this.mined);
   }

   protected void loadAdditional(CompoundTag tag, Provider registries) {
      super.loadAdditional(tag, registries);
      this.energy.set(tag.getInt("Energy"));
      this.area = Math.max(1, Math.min(3, tag.getInt("Area")));
      this.running = tag.getBoolean("Running");
      this.progress = tag.getInt("Progress");
      this.mined = tag.getLong("Mined");
   }

   private final class Energy extends EnergyStorage {
      Energy() {
         super(1000000, 50000, 0);
      }

      void set(int value) {
         this.energy = Math.max(0, Math.min(this.capacity, value));
      }

      void consume(int amount) {
         this.energy = Math.max(0, this.energy - amount);
      }

      public int receiveEnergy(int amount, boolean simulate) {
         int accepted = super.receiveEnergy(amount, simulate);
         if (!simulate && accepted > 0) {
            SatelliteControlDeviceBlockEntity.this.setChanged();
         }

         return accepted;
      }
   }
}
