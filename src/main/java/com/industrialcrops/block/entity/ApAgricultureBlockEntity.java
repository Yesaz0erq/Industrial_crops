package com.industrialcrops.block.entity;

import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.screen.ApAgricultureMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

public final class ApAgricultureBlockEntity extends ApControllerBlockEntity {
   public static final int SEED_SLOTS = 9;
   public static final int OUTPUT_SLOTS = 9;
   public static final int SLOTS = 18;
   public static final int ENERGY_CAPACITY = 1000000;
   public static final int INPUT_RATE = 20000;
   public static final int MAX_BOOST = 4;
   public static final int ENERGY_PER_PLANT = 20;
   public static final int ENERGY_PER_BOOST = 30;
   public static final int BOOSTS_PER_LEVEL = 3;
   public static final int BOOSTS_PER_SPEED_TIER = 2;
   public static final int ENERGY_PER_HARVEST = 40;
   public static final int DATA_COUNT = 12;
   public static final int STATUS_RUNNING = 0;
   public static final int STATUS_STRUCTURE = 1;
   public static final int STATUS_ENERGY = 2;
   public static final int STATUS_NO_SEEDS = 3;
   public static final int STATUS_IDLE = 4;
   private static final int SIDE = 25;
   private final ItemStackHandler inventory = new ItemStackHandler(18) {
      public boolean isItemValid(int var1, ItemStack var2) {
         return var1 >= 9 || ApAgricultureBlockEntity.isPlantableSeed(var2);
      }

      protected void onContentsChanged(int var1) {
         ApAgricultureBlockEntity.this.setChanged();
      }
   };
   private final IItemHandler seeds = new RangedWrapper(this.inventory, 0, 9);
   private final ApAgricultureBlockEntity.Energy energy = new ApAgricultureBlockEntity.Energy();
   private final List<BlockPos> crops = new ArrayList<>();
   private final List<BlockPos> ripe = new ArrayList<>();
   private int boost = 2;
   private int harvested;
   private int scanIndex;
   private int planted;
   private int status = 1;
   private long boosted;
   private final ContainerData data = new ContainerData() {
      public int get(int var1) {
         return switch (var1) {
            case 0 -> ApAgricultureBlockEntity.this.energy.getEnergyStored() & 65535;
            case 1 -> ApAgricultureBlockEntity.this.energy.getEnergyStored() >>> 16;
            case 2 -> ApAgricultureBlockEntity.this.structure.pack();
            case 3 -> ApAgricultureBlockEntity.this.boost;
            case 4 -> ApAgricultureBlockEntity.this.status;
            case 5 -> Math.min(32767, ApAgricultureBlockEntity.this.crops.size());
            case 6 -> ApAgricultureBlockEntity.this.planted & 65535;
            case 7 -> ApAgricultureBlockEntity.this.planted >>> 16;
            case 8 -> (int)Math.min(32767L, ApAgricultureBlockEntity.this.boosted / 1000L);
            case 9 -> ApAgricultureBlockEntity.this.projection() ? 1 : 0;
            case 10 -> ApAgricultureBlockEntity.this.harvested & 65535;
            case 11 -> ApAgricultureBlockEntity.this.harvested >>> 16;
            default -> 0;
         };
      }

      public void set(int var1, int var2) {
      }

      public int getCount() {
         return 12;
      }
   };

   public ApAgricultureBlockEntity(BlockPos var1, BlockState var2) {
      super((BlockEntityType<?>)ModBlockEntities.AP_AGRICULTURE.get(), var1, var2);
   }

   public static boolean isPlantableSeed(ItemStack var0) {
      return !var0.isEmpty() && var0.getItem() instanceof BlockItem var1 && (var1.getBlock() instanceof CropBlock || var1.getBlock() instanceof StemBlock);
   }

   @Override
   public Block coreBlock() {
      return (Block)ModBlocks.AUTOMATIC_PLANTER.get();
   }

   public static void tick(Level var0, BlockPos var1, BlockState var2, ApAgricultureBlockEntity var3) {
      if (var0 instanceof ServerLevel var4) {
         PoweredMachineSupport.pullEnergy(var0, var1, var3.energy, 20000);
         long var5 = var0.getGameTime();
         var3.tickCommon(var4, var5);
         boolean var7 = false;
         if (!var3.structure.formed()) {
            var3.status = 1;
         } else {
            int var8 = var3.upgrades.speedTier();
            if (var5 % 40L == 0L) {
               var3.scanCrops(var4);
            }

            int var9 = var8 == 0 ? (var5 % 2L == 0L ? 1 : 0) : var8;
            boolean var10 = false;

            for (int var11 = 0; var11 < var9; var11++) {
               var10 |= var3.plantOnce();
            }

            boolean var15 = var3.accelerate(var4, var8);
            boolean var12 = false;
            if (var3.upgrades.automatic()) {
               if (var5 % 4L == 0L) {
                  var12 = var3.harvest(var4, 1 + var8);
               }

               if (var5 % 20L == 10L) {
                  var3.exportToNeighbours(var3.inventory, 9, 9);
               }
            }

            var7 = var10 || var15 || var12;
            if (var7) {
               var3.status = 0;
            } else if (var3.energy.getEnergyStored() < 20) {
               var3.status = 2;
            } else if (var5 % 20L == 0L) {
               var3.status = !var3.hasSeeds() && var3.crops.isEmpty() ? 3 : 4;
            }
         }

         boolean var14 = var3.structure.formed() && var3.status == 0;
         if ((Boolean)var2.getValue(ApControllerBlock.ACTIVE) != var14) {
            var0.setBlock(var1, (BlockState)var2.setValue(ApControllerBlock.ACTIVE, var14), 3);
         }
      }
   }

   private void scanCrops(ServerLevel var1) {
      this.crops.clear();
      this.ripe.clear();
      BlockPos var2 = this.center();

      for (int var3 = -12; var3 <= 12; var3++) {
         for (int var4 = -12; var4 <= 12; var4++) {
            int var5 = var2.getX() + var3;
            int var6 = var2.getZ() + var4;
            if (var1.hasChunk(var5 >> 4, var6 >> 4)) {
               for (int var7 = -3; var7 <= 3; var7++) {
                  BlockPos var8 = new BlockPos(var5, var2.getY() + var7, var6);
                  BlockState var9 = var1.getBlockState(var8);
                  if (var9.getBlock() instanceof CropBlock var10 && var10.isMaxAge(var9)) {
                     this.ripe.add(var8);
                     break;
                  }

                  if (var9.isRandomlyTicking() && isGrowable(var1, var8, var9)) {
                     this.crops.add(var8);
                     break;
                  }
               }
            }
         }
      }
   }

   private static boolean isGrowable(ServerLevel var0, BlockPos var1, BlockState var2) {
      if (var2.getBlock() instanceof CropBlock var5) {
         return !var5.isMaxAge(var2);
      } else if (var2.getBlock() instanceof StemBlock) {
         return true;
      } else {
         if (var2.getBlock() instanceof BonemealableBlock var3 && var3.isValidBonemealTarget(var0, var1, var2)) {
            return true;
         }

         return false;
      }
   }

   public static int ticksPerLevel(int var0) {
      return 3 + 2 * var0;
   }

   private boolean accelerate(ServerLevel var1, int var2) {
      if (this.boost > 0 && !this.crops.isEmpty()) {
         int var3 = this.boost * ticksPerLevel(var2);
         int var4 = this.upgrades.cost(30);
         boolean var5 = false;

         for (int var6 = 0; var6 < var3 && this.energy.getEnergyStored() >= var4; var6++) {
            BlockPos var7 = this.crops.get(var1.random.nextInt(this.crops.size()));
            BlockState var8 = var1.getBlockState(var7);
            if (var8.isRandomlyTicking() && isGrowable(var1, var7, var8)) {
               var8.randomTick(var1, var7, var1.random);
               this.energy.consume(var4);
               this.boosted++;
               var5 = true;
               if (var1.random.nextInt(24) == 0) {
                  var1.sendParticles(
                     ParticleTypes.HAPPY_VILLAGER, (double)var7.getX() + 0.5, (double)var7.getY() + 0.5, (double)var7.getZ() + 0.5, 2, 0.3, 0.2, 0.3, 0.0
                  );
               }
            }
         }

         if (var5) {
            this.setChanged();
         }

         return var5;
      } else {
         return false;
      }
   }

   private boolean harvest(ServerLevel var1, int var2) {
      if (!this.ripe.isEmpty() && !this.slotsFull(this.inventory, 9, 9)) {
         int var3 = this.upgrades.cost(40);
         boolean var4 = false;

         while (var2 > 0 && !this.ripe.isEmpty() && this.energy.getEnergyStored() >= var3) {
            BlockPos var5 = this.ripe.remove(this.ripe.size() - 1);
            BlockState var6 = var1.getBlockState(var5);
            if (var6.getBlock() instanceof CropBlock var7 && var7.isMaxAge(var6)) {
               List<ItemStack> var12 = Block.getDrops(var6, var1, var5, null);
               var1.levelEvent(2001, var5, Block.getId(var6));
               var1.removeBlock(var5, false);

               for (ItemStack var10 : var12) {
                  ItemStack var11 = isPlantableSeed(var10) ? this.insert(var10, 0, 9) : var10;
                  var11 = this.insert(var11, 9, 18);
                  if (!var11.isEmpty()) {
                     Containers.dropItemStack(var1, (double)var5.getX() + 0.5, (double)var5.getY() + 0.5, (double)var5.getZ() + 0.5, var11);
                  }
               }

               this.energy.consume(var3);
               this.harvested++;
               var2--;
               var4 = true;
            }
         }

         if (var4) {
            this.setChanged();
         }

         return var4;
      } else {
         return false;
      }
   }

   private ItemStack insert(ItemStack var1, int var2, int var3) {
      ItemStack var4 = var1;

      for (int var5 = var2; var5 < var3 && !var4.isEmpty(); var5++) {
         var4 = this.inventory.insertItem(var5, var4, false);
      }

      return var4;
   }

   private boolean hasSeeds() {
      for (int var1 = 0; var1 < 9; var1++) {
         if (isPlantableSeed(this.inventory.getStackInSlot(var1))) {
            return true;
         }
      }

      return false;
   }

   private boolean plantOnce() {
      int var1 = this.upgrades.cost(20);
      if (this.level != null && this.energy.getEnergyStored() >= var1) {
         for (int var2 = 0; var2 < 48; var2++) {
            BlockPos var3 = this.nextTarget();
            if (var3 != null && this.isOpenSoil(var3)) {
               if (!this.plantFrom(this.seeds, var3) && !this.plantFromNeighbours(var3)) {
                  return false;
               }

               this.energy.consume(var1);
               this.planted++;
               this.setChanged();
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean plantFromNeighbours(BlockPos var1) {
      for (Direction var5 : Direction.values()) {
         IItemHandler var6 = (IItemHandler)this.level.getCapability(ItemHandler.BLOCK, this.worldPosition.relative(var5), var5.getOpposite());
         if (var6 != null && this.plantFrom(var6, var1)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   private BlockPos nextTarget() {
      int var1 = this.scanIndex++;
      if (this.scanIndex >= 625) {
         this.scanIndex = 0;
      }

      BlockPos var2 = this.center();
      int var3 = var2.getX() - 12 + var1 % 25;
      int var4 = var2.getZ() - 12 + var1 / 25;
      if (this.level != null && this.level.hasChunk(var3 >> 4, var4 >> 4)) {
         int var5 = this.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, var3, var4);
         return Math.abs(var5 - var2.getY()) > 4 ? null : new BlockPos(var3, var5, var4);
      } else {
         return null;
      }
   }

   private boolean isOpenSoil(BlockPos var1) {
      return this.level.getBlockState(var1).isAir() && !this.level.getBlockState(var1.below()).isAir();
   }

   private boolean plantFrom(IItemHandler var1, BlockPos var2) {
      for (int var3 = 0; var3 < var1.getSlots(); var3++) {
         ItemStack var4 = var1.extractItem(var3, 1, true);
         if (isPlantableSeed(var4)) {
            BlockPos var5 = var2.below();
            BlockHitResult var6 = new BlockHitResult(Vec3.atCenterOf(var5), Direction.UP, var5, false);
            if (var4.getItem().useOn(new UseOnContext(this.level, null, InteractionHand.MAIN_HAND, var4, var6)).consumesAction()) {
               var1.extractItem(var3, 1, false);
               return true;
            }
         }
      }

      return false;
   }

   public void changeBoost(int var1) {
      this.boost = Math.max(0, Math.min(4, this.boost + var1));
      this.setChanged();
   }

   public ItemStackHandler getInventory() {
      return this.inventory;
   }

   public IItemHandler automation() {
      return new RangedWrapper(this.inventory, 0, 18) {
         public ItemStack extractItem(int var1, int var2, boolean var3) {
            return var1 < 9 ? ItemStack.EMPTY : super.extractItem(var1, var2, var3);
         }

         public ItemStack insertItem(int var1, ItemStack var2, boolean var3) {
            return var1 < 9 ? super.insertItem(var1, var2, var3) : var2;
         }
      };
   }

   public EnergyStorage getEnergyStorage(@Nullable Direction var1) {
      return this.energy;
   }

   public ContainerData getData() {
      return this.data;
   }

   public Component getDisplayName() {
      return Component.translatable("block.industrialcrops.ap_agricultural_complex");
   }

   public AbstractContainerMenu createMenu(int var1, Inventory var2, Player var3) {
      this.checkStructure();
      return new ApAgricultureMenu(var1, var2, this);
   }

   @Override
   protected void saveAdditional(CompoundTag var1, Provider var2) {
      super.saveAdditional(var1, var2);
      var1.put("Inventory", this.inventory.serializeNBT(var2));
      var1.putInt("Energy", this.energy.getEnergyStored());
      var1.putInt("Boost", this.boost);
      var1.putInt("ScanIndex", this.scanIndex);
      var1.putInt("Planted", this.planted);
      var1.putLong("Boosted", this.boosted);
      var1.putInt("Harvested", this.harvested);
   }

   @Override
   protected void loadAdditional(CompoundTag var1, Provider var2) {
      super.loadAdditional(var1, var2);
      if (var1.contains("Inventory")) {
         this.inventory.deserializeNBT(var2, var1.getCompound("Inventory"));
      }

      this.energy.set(var1.getInt("Energy"));
      this.boost = var1.contains("Boost") ? Math.max(0, Math.min(4, var1.getInt("Boost"))) : 2;
      this.scanIndex = Math.floorMod(var1.getInt("ScanIndex"), 625);
      this.planted = Math.max(0, var1.getInt("Planted"));
      this.boosted = Math.max(0L, var1.getLong("Boosted"));
      this.harvested = Math.max(0, var1.getInt("Harvested"));
   }

   private final class Energy extends EnergyStorage {
      Energy() {
         super(1000000, 20000, 0);
      }

      void set(int var1) {
         this.energy = Math.max(0, Math.min(this.capacity, var1));
      }

      void consume(int var1) {
         this.energy = Math.max(0, this.energy - var1);
      }

      public int receiveEnergy(int var1, boolean var2) {
         int var3 = super.receiveEnergy(var1, var2);
         if (!var2 && var3 > 0) {
            ApAgricultureBlockEntity.this.setChanged();
         }

         return var3;
      }
   }
}
