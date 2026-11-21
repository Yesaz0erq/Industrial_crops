package com.industrialcrops.block.entity;
import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.screen.ApAgricultureMenu;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import com.industrialcrops.block.IndustrialFruitBlock;
import com.industrialcrops.block.IndustrialGourdCropBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;
public final class ApAgricultureBlockEntity extends ApControllerBlockEntity {
   public static final int SEED_SLOTS = 9;
   public static final int OUTPUT_SLOTS = 9;
   public static final int SLOTS = 18;
   /** Farms run cheap: one 10K crop of energy (500,000 FE) buffers a whole season. */
   public static final int ENERGY_CAPACITY = 500_000;
   public static final int INPUT_RATE = 25_000;
   public static final int MAX_BOOST = 4;
   public static final int ENERGY_PER_PLANT = 4;
   public static final int ENERGY_PER_BOOST = 6;
   public static final int BOOSTS_PER_LEVEL = 3;
   public static final int BOOSTS_PER_SPEED_TIER = 2;
   public static final int ENERGY_PER_HARVEST = 8;
   public static final int DATA_COUNT = 12;
   public static final int STATUS_RUNNING = 0;
   public static final int STATUS_STRUCTURE = 1;
   public static final int STATUS_ENERGY = 2;
   public static final int STATUS_NO_SEEDS = 3;
   public static final int STATUS_IDLE = 4;
   public static final int STATUS_OUTPUT_FULL = 5;
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
   private final LinkedHashSet<BlockPos> ripe = new LinkedHashSet<>();
   private int boost = 2;
   private int harvested;
   private int scanIndex;
   private boolean scanNeeded = true;
   private boolean outputBlocked;
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
   @Override public ApStructure.Type structureType() { return ApStructure.Type.FARM; }

   @Override protected void onStructureChanged() {
      this.crops.clear();
      this.ripe.clear();
      this.scanIndex = 0;
      this.scanNeeded = true;
   }

   public static boolean isPlantableSeed(ItemStack var0) {
      return !var0.isEmpty() && var0.getItem() instanceof BlockItem var1 && (var1.getBlock() instanceof CropBlock || var1.getBlock() instanceof StemBlock || var1.getBlock() instanceof PitcherCropBlock);
   }
   @Override
   public Block coreBlock() {
      return (Block)ModBlocks.AUTOMATIC_PLANTER.get();
   }
   public static void tick(Level var0, BlockPos var1, BlockState var2, ApAgricultureBlockEntity var3) {
      if (var0 instanceof ServerLevel var4) {
         PoweredMachineSupport.pullEnergy(var0, var1, var3.energy, INPUT_RATE);
         long var5 = var0.getGameTime();
         var3.tickCommon(var4, var5);
         boolean var7 = false;
         if (!var3.structure.formed()) {
            var3.status = 1;
         } else {
            int var8 = var3.upgrades.speedTier();
            if (var3.scanNeeded || var5 % 20L == 0L) {
               var3.scanCrops(var4);
            }
            boolean didHarvest = var3.harvest(var4, 1 + var8);
            // Let a slow power supply accumulate the cost of a ripe harvest before planting or accelerating.
            boolean waitingForHarvestPower = !var3.ripe.isEmpty()
               && var3.energy.getEnergyStored() < var3.upgrades.cost(ENERGY_PER_HARVEST);
            int var9 = var8 == 0 ? (var5 % 2L == 0L ? 1 : 0) : var8;
            boolean var10 = false;
            for (int var11 = 0; !waitingForHarvestPower && var11 < var9; var11++) {
               var10 |= var3.plantOnce();
            }
            boolean var15 = !waitingForHarvestPower && var3.accelerate(var4, var8);
            if (var3.upgrades.automatic() && var5 % 20L == 10L) {
               var3.exportToNeighbours(var3.inventory, SEED_SLOTS, OUTPUT_SLOTS);
            }
            var7 = var10 || var15 || didHarvest;
            if (var7) {
               var3.status = 0;
            } else if (var3.outputBlocked) {
               var3.status = STATUS_OUTPUT_FULL;
            } else if (waitingForHarvestPower || var3.energy.getEnergyStored() < var3.upgrades.cost(ENERGY_PER_PLANT)) {
               var3.status = 2;
            } else if (var5 % 20L == 0L) {
               var3.status = !var3.hasSeeds() && var3.crops.isEmpty() && var3.ripe.isEmpty() ? STATUS_NO_SEEDS : STATUS_IDLE;
            }
         }
         boolean var14 = var3.structure.formed() && var3.status == 0;
         if ((Boolean)var2.getValue(ApControllerBlock.ACTIVE) != var14) {
            var0.setBlock(var1, (BlockState)var2.setValue(ApControllerBlock.ACTIVE, var14), 3);
         }
      }
   }
   private void scanCrops(ServerLevel level) {
      this.crops.clear();
      // Keep earlier candidates ahead of rescanned, storage-blocked crops.
      this.scanNeeded = false;
      this.outputBlocked = false;
      ApStructure.Layout area = this.layout();
      for (int u = 1; u < area.width() - 1; u++) {
         for (int v = 1; v < area.depth() - 1; v++) {
            BlockPos pos = area.pos(u, 0, v);
            if (!this.workPosLoaded(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (isRipe(level, pos, state)) this.ripe.add(pos);
            else if (state.isRandomlyTicking() && isGrowable(level, pos, state)) this.crops.add(pos);
         }
      }
   }
   private static boolean isRipe(ServerLevel level, BlockPos pos, BlockState state) {
      if (state.getBlock() instanceof IndustrialGourdCropBlock) return false;
      if (state.getBlock() instanceof CropBlock crop) return crop.isMaxAge(state);
      if (state.getBlock() instanceof PitcherCropBlock) {
         return state.getValue(PitcherCropBlock.HALF) == DoubleBlockHalf.LOWER && state.getValue(PitcherCropBlock.AGE) == 4;
      }
      return state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN) || state.getBlock() instanceof IndustrialFruitBlock
         || state.is(Blocks.TORCHFLOWER) && level.getBlockState(pos.below()).getBlock() instanceof FarmBlock;
   }

   private static boolean isGrowable(ServerLevel var0, BlockPos var1, BlockState var2) {
      if (var2.getBlock() instanceof IndustrialGourdCropBlock stem) return !stem.isMaxAge(var2) || !var2.getValue(IndustrialGourdCropBlock.ATTACHED);
      if (var2.getBlock() instanceof CropBlock var5) {
         return !var5.isMaxAge(var2);
      } else if (var2.getBlock() instanceof StemBlock) {
         return true;
      } else {
         if (var2.getBlock() instanceof BonemealableBlock var3 && var3.isValidBonemealTarget(var0, var1, var2, false)) {
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
         int var4 = this.upgrades.cost(ENERGY_PER_BOOST);
         boolean var5 = false;
         for (int var6 = 0; var6 < var3 && this.energy.getEnergyStored() >= var4; var6++) {
            BlockPos var7 = this.crops.get(var1.random.nextInt(this.crops.size()));
            if (!this.workPosLoaded(var7)) continue;
            BlockState var8 = var1.getBlockState(var7);
            if (var8.isRandomlyTicking() && isGrowable(var1, var7, var8)) {
               var8.randomTick(var1, var7, var1.random);
               if (isRipe(var1, var7, var1.getBlockState(var7))) this.ripe.add(var7);
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
   private boolean harvest(ServerLevel level, int workLimit) {
      int cost = this.upgrades.cost(ENERGY_PER_HARVEST);
      boolean worked = false;
      while (workLimit-- > 0 && !ripe.isEmpty() && energy.getEnergyStored() >= cost) {
         var next = ripe.iterator();
         BlockPos pos = next.next();
         next.remove();
         if (!workPosLoaded(pos)) continue;
         BlockState state = level.getBlockState(pos);
         if (!isRipe(level, pos, state)) continue;
         List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos));
         ItemStackHandler preview = new ItemStackHandler(SLOTS);
         for (int slot = 0; slot < SLOTS; slot++) preview.setStackInSlot(slot, inventory.getStackInSlot(slot).copy());
         if (!storeHarvest(preview, drops, null, pos)) {
            this.outputBlocked = true;
            continue;
         }
         // The upper half of a pitcher is the same plant; remove it without another loot roll.
         if (state.getBlock() instanceof PitcherCropBlock) {
            BlockState upper = level.getBlockState(pos.above());
            if (upper.is(state.getBlock()) && upper.getValue(PitcherCropBlock.HALF) == DoubleBlockHalf.UPPER) {
               level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
         }
         if (!level.removeBlock(pos, false)) continue;
         storeHarvest(inventory, drops, level, pos);
         level.levelEvent(2001, pos, Block.getId(state));
         energy.consume(cost);
         harvested++;
         worked = true;
      }
      if (worked) setChanged();
      return worked;
   }

   private static boolean storeHarvest(ItemStackHandler target, List<ItemStack> drops, @Nullable ServerLevel level, BlockPos pos) {
      boolean seedReserved = false;
      for (ItemStack drop : drops) {
         ItemStack rest = drop.copy();
         if (!seedReserved && isPlantableSeed(rest)) {
            ItemStack seed = rest.copyWithCount(1);
            if (insert(target, seed, 0, SEED_SLOTS).isEmpty()) {
               rest.shrink(1);
               seedReserved = true;
            }
         }
         rest = insert(target, rest, SEED_SLOTS, SLOTS);
         if (!rest.isEmpty()) {
            if (level == null) return false;
            // A mod can change the real inventory from a synchronous block-removal callback.
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, rest);
         }
      }
      return true;
   }

   private static ItemStack insert(ItemStackHandler target, ItemStack stack, int from, int to) {
      for (int slot = from; slot < to && !stack.isEmpty(); slot++) stack = target.insertItem(slot, stack, false);
      return stack;
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
      int var1 = this.upgrades.cost(ENERGY_PER_PLANT);
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
         BlockPos neighbour = this.worldPosition.relative(var5);
         if (!this.level.hasChunk(neighbour.getX() >> 4, neighbour.getZ() >> 4)) continue;
         IItemHandler var6 = (IItemHandler)com.industrialcrops.machine.ForgeCapabilityAccess.itemHandler(this.level, this.worldPosition.relative(var5), var5.getOpposite());
         if (var6 != null && this.plantFrom(var6, var1)) {
            return true;
         }
      }
      return false;
   }
   @Nullable
   private BlockPos nextTarget() {
      ApStructure.Layout area = this.layout();
      int interiorWidth = area.width() - 2;
      int count = interiorWidth * (area.depth() - 2);
      int index = Math.floorMod(this.scanIndex, count);
      this.scanIndex = (index + 1) % count;
      BlockPos pos = area.pos(1 + index % interiorWidth, 0, 1 + index / interiorWidth);
      return this.workPosLoaded(pos) ? pos : null;
   }
   private boolean isOpenSoil(BlockPos pos) {
      return this.workPosLoaded(pos) && this.level.getBlockState(pos).isAir()
         && this.level.getBlockState(pos.below()).getBlock() instanceof FarmBlock;
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
   private final IItemHandler itemPort = com.industrialcrops.machine.MachineItemPort.processing(inventory, 9, 9);

   public IItemHandler automation() {
      return itemPort;
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
   protected void saveAdditional(CompoundTag var1) {
      super.saveAdditional(var1);
      var1.put("Inventory", this.inventory.serializeNBT());
      var1.putInt("Energy", this.energy.getEnergyStored());
      var1.putInt("Boost", this.boost);
      var1.putInt("ScanIndex", this.scanIndex);
      var1.putInt("Planted", this.planted);
      var1.putLong("Boosted", this.boosted);
      var1.putInt("Harvested", this.harvested);
   }
   @Override
   public void load(CompoundTag var1) {
      super.load(var1);
      if (var1.contains("Inventory")) {
         this.inventory.deserializeNBT(var1.getCompound("Inventory"));
      }
      this.energy.set(var1.getInt("Energy"));
      this.boost = var1.contains("Boost") ? Math.max(0, Math.min(4, var1.getInt("Boost"))) : 2;
      this.scanIndex = Math.max(0, var1.getInt("ScanIndex"));
      this.scanNeeded = true;
      this.planted = Math.max(0, var1.getInt("Planted"));
      this.boosted = Math.max(0L, var1.getLong("Boosted"));
      this.harvested = Math.max(0, var1.getInt("Harvested"));
   }
   private final class Energy extends EnergyStorage {
      Energy() {
         super(ENERGY_CAPACITY, INPUT_RATE, 0);
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
   private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> automation());
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
      forgeCap0 = net.minecraftforge.common.util.LazyOptional.of(() -> automation());
      forgeCap1 = net.minecraftforge.common.util.LazyOptional.of(() -> getEnergyStorage(null));
   }
}
