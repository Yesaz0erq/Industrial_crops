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
import net.minecraft.core.HolderLookup.Provider;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

public final class ApLivestockBlockEntity extends ApControllerBlockEntity {
   public static final int FEED_SLOTS = 9;
   public static final int OUTPUT_SLOTS = 9;
   public static final int SLOTS = 18;
   /** Ranches run cheap: one 10K crop of energy (500,000 FE) of buffer. */
   public static final int ENERGY_CAPACITY = 500_000;
   public static final int INPUT_RATE = 25_000;
   public static final int[] CAPS = new int[]{8, 16, 32, 64};
   public static final int ENERGY_PER_BIRTH = 80;
   public static final int ENERGY_PER_SHEAR = 20;
   public static final int ENERGY_PER_CULL = 60;
   public static final int ENERGY_PER_GROWTH = 8;
   public static final int GROWTH_STEP = 600;
   public static final int FLAG_BREED = 1;
   public static final int FLAG_SHEAR = 2;
   public static final int FLAG_CULL = 4;
   public static final int FLAG_GROW = 8;
   public static final int[] CYCLE_TICKS = new int[]{20, 15, 10, 7, 5};
   public static final int CHECK_INTERVAL_TICKS = 60;
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
   private int growthCycleRemainder;
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

   @Override public ApStructure.Type structureType() { return ApStructure.Type.RANCH; }

   @Override protected void onStructureChanged() { this.animalCount = 0; }

   private boolean inside(net.minecraft.world.entity.Entity entity) {
      return this.layout().workBounds().contains(entity.position())
         && this.workPosLoaded(entity.blockPosition());
   }

   private AABB area() { return this.layout().workBounds(); }

   private boolean inspectionDue(long time) {
      return Math.floorMod(time + Math.floorMod(this.worldPosition.asLong(), CHECK_INTERVAL_TICKS), CHECK_INTERVAL_TICKS) == 0;
   }

   @Override
   protected boolean shouldCheckStructure(long time) { return this.inspectionDue(time); }


   private static boolean livestock(Animal var0) {
      return var0.isAlive()
         && !var0.hasCustomName()
         && (!(var0 instanceof TamableAnimal var2) || !var2.isTame())
         && (!(var0 instanceof AbstractHorse var1) || !var1.isTamed());
   }

   public static void tick(Level var0, BlockPos var1, BlockState var2, ApLivestockBlockEntity var3) {
      if (var0 instanceof ServerLevel var4) {
         PoweredMachineSupport.pullEnergy(var0, var1, var3.energy, INPUT_RATE);
         long var5 = var0.getGameTime();
         var3.tickCommon(var4, var5);
         if (!var3.structure.formed()) {
            var3.status = 1;
         } else if (var3.inspectionDue(var5)) {
            // Spread expensive entity scans across ticks for ranches at different positions.
            int growthInterval = CYCLE_TICKS[var3.upgrades.speedTier()];
            var3.growthCycleRemainder += CHECK_INTERVAL_TICKS;
            int growthCycles = var3.growthCycleRemainder / growthInterval;
            var3.growthCycleRemainder %= growthInterval;
            var3.work(var4, growthCycles);
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

   private void work(ServerLevel level, int growthCycles) {
      AABB bounds = this.area();
      List<Animal> animals = level.getEntitiesOfClass(Animal.class, bounds,
         animal -> livestock(animal) && this.inside(animal));
      boolean worked = this.collectDrops(level, bounds);
      if (animals.isEmpty()) {
         this.animalCount = 0;
         this.status = worked ? STATUS_RUNNING : STATUS_NO_ANIMALS;
         return;
      }

      // Ready breeding/culling takes priority over optional periodic energy use.
      worked |= this.breedAndCull(level, animals);
      if ((this.flags & FLAG_SHEAR) != 0) worked |= this.shear(level, animals);
      if ((this.flags & FLAG_GROW) != 0 && this.grow(level, animals, growthCycles)) {
         worked = true;
         // A baby that matures during this inspection can join its breeding pass.
         worked |= this.breedAndCull(level, level.getEntitiesOfClass(Animal.class, bounds,
            animal -> livestock(animal) && this.inside(animal)));
      }
      this.animalCount = level.getEntitiesOfClass(Animal.class, bounds,
         animal -> livestock(animal) && this.inside(animal)).size();
      this.status = worked ? STATUS_RUNNING
         : this.energy.getEnergyStored() < this.upgrades.cost(ENERGY_PER_GROWTH) ? STATUS_ENERGY
         : this.outputFull() ? STATUS_FULL : STATUS_IDLE;
      if (worked) this.setChanged();
   }

   private boolean breedAndCull(ServerLevel level, List<Animal> animals) {
      HashMap<net.minecraft.world.entity.EntityType<?>, List<Animal>> groups = new HashMap<>();
      for (Animal animal : animals) {
         if (livestock(animal) && this.inside(animal)) groups.computeIfAbsent(animal.getType(), key -> new ArrayList<>()).add(animal);
      }
      boolean worked = false;
      int cap = CAPS[this.capIndex];
      for (List<Animal> group : groups.values()) {
         if ((this.flags & FLAG_CULL) != 0 && group.size() > cap) worked |= this.cull(level, group, group.size() - cap);
         group.removeIf(animal -> !animal.isAlive());
         if ((this.flags & FLAG_BREED) != 0) {
            int vacancies = cap - group.size();
            for (int attempt = 0; attempt < vacancies; attempt++) {
               if (!this.breed(level, group)) break;
               worked = true;
            }
         }
      }
      return worked;
   }

   private boolean isFeedBag(ItemStack var1) {
      return var1.is((Item)ModItems.FEED_BAG_BASIC.get()) || var1.is((Item)ModItems.FEED_BAG_FAST_BREEDING.get());
   }

   private int feedSlotFor(Animal var1, int var2) {
      for (int var3 = 0; var3 < 9; var3++) {
         ItemStack var4 = this.inventory.getStackInSlot(var3);
         if (!var4.isEmpty() && (var3 != var2 || var4.getCount() >= 2) && (var1.isFood(var4) || this.isFeedBag(var4))
               && (var1.getAge() == 0 || var4.is(ModItems.FEED_BAG_FAST_BREEDING.get()))) {
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

   private boolean breed(ServerLevel level, List<Animal> animals) {
      if (this.energy.getEnergyStored() < this.upgrades.cost(ENERGY_PER_BIRTH)) return false;
      for (int i = 0; i < animals.size(); i++) {
         Animal first = animals.get(i);
         if (!livestock(first) || !this.inside(first) || first.isBaby()) continue;
         int firstSlot = first.isInLove() ? -1 : this.feedSlotFor(first, -1);
         if (!first.isInLove() && firstSlot < 0) continue;
         for (int j = i + 1; j < animals.size(); j++) {
            Animal second = animals.get(j);
            if (!livestock(second) || !this.inside(second) || second.isBaby()) continue;
            int secondSlot = second.isInLove() ? -1 : this.feedSlotFor(second, firstSlot);
            if (!second.isInLove() && secondSlot < 0) continue;
            int firstAge = first.getAge(), secondAge = second.getAge();
            int firstLove = first.getInLoveTime(), secondLove = second.getInLoveTime();
            prepareForBreeding(first, firstSlot);
            prepareForBreeding(second, secondSlot);
            if (!first.canMate(second)) {
               restoreBreedingState(first, firstAge, firstLove);
               restoreBreedingState(second, secondAge, secondLove);
               continue;
            }
            BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(first, second, first.getBreedOffspring(level, second));
            NeoForge.EVENT_BUS.post(event);
            AgeableMob child = event.getChild();
            if (!event.isCanceled() && child != null) {
               child.setBaby(true);
               child.moveTo(first.getX(), first.getY(), first.getZ(), 0.0F, 0.0F);
               if (this.inside(child) && level.addFreshEntity(child)) {
                  if (firstSlot >= 0) this.consumeFeed(firstSlot);
                  if (secondSlot >= 0) this.consumeFeed(secondSlot);
                  first.finalizeSpawnChildFromBreeding(level, second, child);
                  this.energy.consume(this.upgrades.cost(ENERGY_PER_BIRTH));
                  this.births++;
                  return true;
               }
            }
            restoreBreedingState(first, firstAge, firstLove);
            restoreBreedingState(second, secondAge, secondLove);
            // Try other pairs rather than allowing one denied pair to block the whole herd.
         }
      }
      return false;
   }

   private void prepareForBreeding(Animal animal, int slot) {
      if (slot < 0) return; // Already fed: preserve its love state and consume no second ration.
      if (this.inventory.getStackInSlot(slot).is(ModItems.FEED_BAG_FAST_BREEDING.get())) animal.setAge(0);
      animal.setInLove(null);
   }

   private static void restoreBreedingState(Animal animal, int age, int love) {
      animal.setAge(age);
      animal.setInLoveTime(love);
   }

   private boolean shear(ServerLevel var1, List<Animal> var2) {
      boolean var3 = false;

      for (Animal var5 : var2) {
         if (livestock(var5) && this.inside(var5) && var5 instanceof Sheep) {
            Sheep var6 = (Sheep)var5;
            if (var6.readyForShearing()) {
               if (this.energy.getEnergyStored() < this.upgrades.cost(ENERGY_PER_SHEAR)) {
                  break;
               }

               var6.shear(SoundSource.BLOCKS);
               this.energy.consume(this.upgrades.cost(ENERGY_PER_SHEAR));
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
         BlockPos var5 = this.center();
         var4.setPos((double)var5.getX() + 0.5, (double)(var5.getY() + 1), (double)var5.getZ() + 0.5);
         boolean var6 = false;

         for (Animal var8 : var2) {
            if (var3 <= 0 || this.energy.getEnergyStored() < this.upgrades.cost(ENERGY_PER_CULL)) {
               break;
            }

            if (livestock(var8) && this.inside(var8) && !var8.isBaby()) {
               var8.hurt(var1.damageSources().playerAttack(var4), Float.MAX_VALUE);
               if (!var8.isAlive()) {
                  this.energy.consume(this.upgrades.cost(ENERGY_PER_CULL));
                  this.culled++;
                  var3--;
                  var6 = true;
               }
            }
         }

         return var6;
      }
   }

   private boolean grow(ServerLevel var1, List<Animal> var2, int growthCycles) {
      boolean var3 = false;

      for (Animal var5 : var2) {
         if (livestock(var5) && this.inside(var5) && var5.isBaby()) {
            if (this.energy.getEnergyStored() < this.upgrades.cost(ENERGY_PER_GROWTH)) {
               break;
            }

            int cost = this.upgrades.cost(ENERGY_PER_GROWTH);
            int steps = (int)Math.min(growthCycles,
               Math.min((-(long)var5.getAge() + GROWTH_STEP - 1) / GROWTH_STEP, this.energy.getEnergyStored() / cost));
            if (steps <= 0) continue;
            var5.ageUp(30 * steps, false);
            this.energy.consume(cost * steps);
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

      for (ItemEntity var5 : var1.getEntitiesOfClass(ItemEntity.class, var2, drop -> drop.isAlive() && drop.getAge() > 10 && this.inside(drop))) {
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
         BlockPos overflow = this.center();
         Containers.dropItemStack(
            this.level, (double)overflow.getX() + 0.5, (double)overflow.getY() + 0.5, (double)overflow.getZ() + 0.5, var2
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
   protected void saveAdditional(CompoundTag var1, Provider var2) {
      super.saveAdditional(var1, var2);
      var1.put("Inventory", this.inventory.serializeNBT(var2));
      var1.putInt("Energy", this.energy.getEnergyStored());
      var1.putInt("Flags", this.flags);
      var1.putInt("Cap", this.capIndex);
      var1.putInt("Births", this.births);
      var1.putInt("Shorn", this.shorn);
      var1.putInt("Culled", this.culled);
   }

   @Override
   protected void loadAdditional(CompoundTag var1, Provider var2) {
      super.loadAdditional(var1, var2);
      if (var1.contains("Inventory")) {
         this.inventory.deserializeNBT(var2, var1.getCompound("Inventory"));
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
            ApLivestockBlockEntity.this.setChanged();
         }

         return var3;
      }
   }
}
