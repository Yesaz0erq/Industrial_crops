package com.industrialcrops.block.entity;
import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.ApLivestockMenu;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;
public final class ApLivestockBlockEntity extends ApControllerBlockEntity {
   public static final int FEED_SLOTS = 9;
   public static final int OUTPUT_SLOTS = 9;
   public static final int SLOTS = 18;
   public static final int ENERGY_CAPACITY = 1000000;
   public static final int INPUT_RATE = 20000;
   public static final int[] CAPS = new int[]{8, 16, 32, 64};
   public static final int ENERGY_PER_BIRTH = 400;
   public static final int ENERGY_PER_SHEAR = 100;
   public static final int ENERGY_PER_CULL = 300;
   public static final int ENERGY_PER_GROWTH = 40;
   public static final int GROWTH_STEP = 600;
   public static final int FLAG_BREED = 1;
   public static final int FLAG_SHEAR = 2;
   public static final int FLAG_CULL = 4;
   public static final int FLAG_GROW = 8;
   public static final int[] CYCLE_TICKS = new int[]{20, 15, 10, 7, 5};
   public static final int BREED_EVERY = 5;
   public static final int DATA_COUNT = 11;
   public static final int STATUS_RUNNING = 0;
   public static final int STATUS_STRUCTURE = 1;
   public static final int STATUS_ENERGY = 2;
   public static final int STATUS_NO_ANIMALS = 3;
   public static final int STATUS_FULL = 4;
   public static final int STATUS_IDLE = 5;
   private static final GameProfile PROFILE = new GameProfile(UUID.fromString("5f0d3d36-8f1d-4bd8-9d7b-4ad0a3f2c0a1"), "[AP Livestock Ranch]");
   private final ItemStackHandler inventory = new ItemStackHandler(18) {
      public boolean isItemValid(int var1, ItemStack var2) {
         return true;
      }
      protected void onContentsChanged(int var1) {
         ApLivestockBlockEntity.this.setChanged();
      }
   };
   private final ApLivestockBlockEntity.Energy energy = new ApLivestockBlockEntity.Energy();
   private int cycles;
   private int flags = 11;
   private int capIndex = 1;
   private int status = 1;
   private int animalCount;
   private int births;
   private int shorn;
   private int culled;
   private final ContainerData data = new ContainerData() {
      public int get(int var1) {
         return switch (var1) {
            case 0 -> ApLivestockBlockEntity.this.energy.getEnergyStored() & 65535;
            case 1 -> ApLivestockBlockEntity.this.energy.getEnergyStored() >>> 16;
            case 2 -> ApLivestockBlockEntity.this.structure.pack();
            case 3 -> ApLivestockBlockEntity.this.flags;
            case 4 -> ApLivestockBlockEntity.this.capIndex;
            case 5 -> ApLivestockBlockEntity.this.status;
            case 6 -> Math.min(32767, ApLivestockBlockEntity.this.animalCount);
            case 7 -> Math.min(32767, ApLivestockBlockEntity.this.births);
            case 8 -> Math.min(32767, ApLivestockBlockEntity.this.shorn);
            case 9 -> Math.min(32767, ApLivestockBlockEntity.this.culled);
            case 10 -> ApLivestockBlockEntity.this.projection() ? 1 : 0;
            default -> 0;
         };
      }
      public void set(int var1, int var2) {
      }
      public int getCount() {
         return 11;
      }
   };
   public ApLivestockBlockEntity(BlockPos var1, BlockState var2) {
      super((BlockEntityType<?>)ModBlockEntities.AP_LIVESTOCK.get(), var1, var2);
   }
   @Override
   public Block coreBlock() {
      return (Block)ModBlocks.ANIMAL_BREEDER.get();
   }
   private AABB area() {
      BlockPos var1 = ApStructure.center(this.worldPosition, this.facing());
      return new AABB(
         (double)(var1.getX() - 12),
         (double)(var1.getY() - 3),
         (double)(var1.getZ() - 12),
         (double)(var1.getX() + 12 + 1),
         (double)(var1.getY() + 6),
         (double)(var1.getZ() + 12 + 1)
      );
   }
   private static boolean livestock(Animal var0) {
      return var0.isAlive()
         && !var0.hasCustomName()
         && (!(var0 instanceof TamableAnimal var2) || !var2.isTame())
         && (!(var0 instanceof AbstractHorse var1) || !var1.isTamed());
   }
   public static void tick(Level var0, BlockPos var1, BlockState var2, ApLivestockBlockEntity var3) {
      if (var0 instanceof ServerLevel var4) {
         PoweredMachineSupport.pullEnergy(var0, var1, var3.energy, 20000);
         long var5 = var0.getGameTime();
         var3.tickCommon(var4, var5);
         if (!var3.structure.formed()) {
            var3.status = 1;
         } else if (var5 % (long)CYCLE_TICKS[var3.upgrades.speedTier()] == 0L) {
            var3.work(var4);
         }
         if (var3.structure.formed() && var5 % 20L == 10L && var3.upgrades.automatic()) {
            var3.exportToNeighbours(var3.inventory, 9, 9);
         }
         boolean var7 = var3.structure.formed() && var3.status == 0;
         if ((Boolean)var2.getValue(ApControllerBlock.ACTIVE) != var7) {
            var0.setBlock(var1, (BlockState)var2.setValue(ApControllerBlock.ACTIVE, var7), 3);
         }
      }
   }
   private void work(ServerLevel var1) {
      boolean var2 = this.cycles++ % 5 == 0;
      AABB var3 = this.area();
      List<Animal> var4 = var1.getEntitiesOfClass(Animal.class, var3, ApLivestockBlockEntity::livestock);
      this.animalCount = var4.size();
      boolean var5 = this.collectDrops(var1, var3);
      if (var4.isEmpty()) {
         this.status = var5 ? 0 : 3;
      } else if (this.energy.getEnergyStored() < this.upgrades.cost(40)) {
         this.status = 2;
      } else {
         HashMap<net.minecraft.world.entity.EntityType<?>, List<Animal>> var6 = new HashMap<>();
         for (Animal var8 : var4) {
            var6.computeIfAbsent(var8.getType(), var0 -> new ArrayList<>()).add(var8);
         }
         int var10 = CAPS[this.capIndex];
         for (List<Animal> var9 : var6.values()) {
            if ((this.flags & 4) != 0 && var9.size() > var10) {
               var5 |= this.cull(var1, var9, var9.size() - var10);
            }
            if ((this.flags & 1) != 0 && var2 && var9.size() < var10) {
               var5 |= this.breed(var1, var9);
            }
         }
         if ((this.flags & 2) != 0) {
            var5 |= this.shear(var1, var4);
         }
         if ((this.flags & 8) != 0) {
            var5 |= this.grow(var1, var4);
         }
         this.status = var5 ? 0 : (this.outputFull() ? 4 : 5);
         if (var5) {
            this.setChanged();
         }
      }
   }
   private boolean isFeedBag(ItemStack var1) {
      return var1.is((Item)ModItems.FEED_BAG_BASIC.get()) || var1.is((Item)ModItems.FEED_BAG_FAST_BREEDING.get());
   }
   private int feedSlotFor(Animal var1, int var2) {
      for (int var3 = 0; var3 < 9; var3++) {
         ItemStack var4 = this.inventory.getStackInSlot(var3);
         if (!var4.isEmpty() && (var3 != var2 || var4.getCount() >= 2) && (var1.isFood(var4) || this.isFeedBag(var4))) {
            return var3;
         }
      }
      return -1;
   }
   private void consumeFeed(int var1) {
      ItemStack var2 = this.inventory.extractItem(var1, 1, false);
      if (this.isFeedBag(var2)) {
         this.output(new ItemStack((ItemLike)ModItems.EMPTY_BAG.get()));
      } else if (var2.hasCraftingRemainingItem()) {
         this.output(var2.getCraftingRemainingItem());
      }
   }
   private boolean breed(ServerLevel var1, List<Animal> var2) {
      if (this.energy.getEnergyStored() < this.upgrades.cost(400)) {
         return false;
      } else {
         Animal var3 = null;
         for (Animal var5 : var2) {
            if (var5.getAge() == 0 && !var5.isInLove()) {
               if (var3 != null) {
                  int var6 = this.feedSlotFor(var3, -1);
                  if (var6 < 0) {
                     return false;
                  }
                  int var7 = this.feedSlotFor(var5, var6);
                  if (var7 < 0) {
                     return false;
                  }
                  var3.setInLove(null);
                  var5.setInLove(null);
                  if (!var3.canMate(var5)) {
                     var3.resetLove();
                     var5.resetLove();
                     return false;
                  }
                  BabyEntitySpawnEvent var8 = new BabyEntitySpawnEvent(var3, var5, var3.getBreedOffspring(var1, var5));
                  MinecraftForge.EVENT_BUS.post(var8);
                  AgeableMob var9 = var8.getChild();
                  if (!var8.isCanceled() && var9 != null) {
                     var9.setBaby(true);
                     var9.moveTo(var3.getX(), var3.getY(), var3.getZ(), 0.0F, 0.0F);
                     if (!var1.addFreshEntity(var9)) {
                        var3.resetLove();
                        var5.resetLove();
                        return false;
                     }
                     this.consumeFeed(var6);
                     this.consumeFeed(var7);
                     var3.finalizeSpawnChildFromBreeding(var1, var5, var9);
                     this.energy.consume(this.upgrades.cost(400));
                     this.births++;
                     return true;
                  }
                  var3.resetLove();
                  var5.resetLove();
                  return false;
               }
               var3 = var5;
            }
         }
         return false;
      }
   }
   private boolean shear(ServerLevel var1, List<Animal> var2) {
      boolean var3 = false;
      for (Animal var5 : var2) {
         if (var5 instanceof Sheep) {
            Sheep var6 = (Sheep)var5;
            if (var6.readyForShearing()) {
               if (this.energy.getEnergyStored() < this.upgrades.cost(100)) {
                  break;
               }
               var6.shear(SoundSource.BLOCKS);
               this.energy.consume(this.upgrades.cost(100));
               this.shorn++;
               var3 = true;
            }
         }
      }
      return var3;
   }
   private boolean cull(ServerLevel var1, List<Animal> var2, int var3) {
      if (this.outputFull()) {
         return false;
      } else {
         FakePlayer var4 = FakePlayerFactory.get(var1, PROFILE);
         BlockPos var5 = ApStructure.center(this.worldPosition, this.facing());
         var4.setPos((double)var5.getX() + 0.5, (double)(var5.getY() + 1), (double)var5.getZ() + 0.5);
         boolean var6 = false;
         for (Animal var8 : var2) {
            if (var3 <= 0 || this.energy.getEnergyStored() < this.upgrades.cost(300)) {
               break;
            }
            if (!var8.isBaby() && var8.isAlive()) {
               var8.hurt(var1.damageSources().playerAttack(var4), Float.MAX_VALUE);
               if (!var8.isAlive()) {
                  this.energy.consume(this.upgrades.cost(300));
                  this.culled++;
                  var3--;
                  var6 = true;
               }
            }
         }
         return var6;
      }
   }
   private boolean grow(ServerLevel var1, List<Animal> var2) {
      boolean var3 = false;
      for (Animal var5 : var2) {
         if (var5.isBaby()) {
            if (this.energy.getEnergyStored() < this.upgrades.cost(40)) {
               break;
            }
            var5.ageUp(30, false);
            this.energy.consume(this.upgrades.cost(40));
            var3 = true;
            if (var1.random.nextInt(6) == 0) {
               var1.sendParticles(ParticleTypes.HAPPY_VILLAGER, var5.getX(), var5.getY() + (double)var5.getBbHeight(), var5.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
            }
         }
      }
      return var3;
   }
   private boolean collectDrops(ServerLevel var1, AABB var2) {
      boolean var3 = false;
      for (ItemEntity var5 : var1.getEntitiesOfClass(ItemEntity.class, var2, var0 -> var0.isAlive() && var0.getAge() > 10)) {
         ItemStack var6 = this.insertOutput(var5.getItem().copy());
         if (var6.getCount() != var5.getItem().getCount()) {
            var3 = true;
         }
         if (var6.isEmpty()) {
            var5.discard();
         } else {
            var5.setItem(var6);
         }
      }
      return var3;
   }
   private ItemStack insertOutput(ItemStack var1) {
      ItemStack var2 = var1;
      for (int var3 = 9; var3 < 18 && !var2.isEmpty(); var3++) {
         var2 = this.inventory.insertItem(var3, var2, false);
      }
      return var2;
   }
   private void output(ItemStack var1) {
      ItemStack var2 = this.insertOutput(var1);
      if (!var2.isEmpty() && this.level != null) {
         Containers.dropItemStack(
            this.level, (double)this.worldPosition.getX() + 0.5, (double)(this.worldPosition.getY() + 1), (double)this.worldPosition.getZ() + 0.5, var2
         );
      }
   }
   private boolean outputFull() {
      for (int var1 = 9; var1 < 18; var1++) {
         ItemStack var2 = this.inventory.getStackInSlot(var1);
         if (var2.isEmpty() || var2.getCount() < var2.getMaxStackSize()) {
            return false;
         }
      }
      return true;
   }
   public void toggle(int var1) {
      this.flags ^= var1;
      this.setChanged();
   }
   public void cycleCap() {
      this.capIndex = (this.capIndex + 1) % CAPS.length;
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
      return Component.translatable("block.industrialcrops.ap_livestock_ranch");
   }
   public AbstractContainerMenu createMenu(int var1, Inventory var2, Player var3) {
      this.checkStructure();
      return new ApLivestockMenu(var1, var2, this);
   }
   @Override
   protected void saveAdditional(CompoundTag var1) {
      super.saveAdditional(var1);
      var1.put("Inventory", this.inventory.serializeNBT());
      var1.putInt("Energy", this.energy.getEnergyStored());
      var1.putInt("Flags", this.flags);
      var1.putInt("Cap", this.capIndex);
      var1.putInt("Births", this.births);
      var1.putInt("Shorn", this.shorn);
      var1.putInt("Culled", this.culled);
   }
   @Override
   public void load(CompoundTag var1) {
      super.load(var1);
      if (var1.contains("Inventory")) {
         this.inventory.deserializeNBT(var1.getCompound("Inventory"));
      }
      this.energy.set(var1.getInt("Energy"));
      this.flags = var1.contains("Flags") ? var1.getInt("Flags") : 11;
      this.capIndex = Math.floorMod(var1.getInt("Cap"), CAPS.length);
      this.births = var1.getInt("Births");
      this.shorn = var1.getInt("Shorn");
      this.culled = var1.getInt("Culled");
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
            ApLivestockBlockEntity.this.setChanged();
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
