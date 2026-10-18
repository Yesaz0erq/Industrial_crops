package com.industrialcrops.machine;

import com.industrialcrops.registry.ModItems;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public final class ApUpgrades {
   public static final int SLOTS = 6;
   public static final int MAX_POWER = 3;
   private final ItemStackHandler items;
   private final Set<Long> forcedChunks = new LinkedHashSet<>();
   private boolean ticketsInitialized;
   private boolean cleanLegacyTickets;

   public ApUpgrades(final Runnable changed) {
      this.items = new ItemStackHandler(SLOTS) {
         @Override public int getSlotLimit(int slot) { return 1; }
         @Override public boolean isItemValid(int slot, ItemStack stack) { return isUpgrade(stack); }
         @Override protected void onContentsChanged(int slot) { changed.run(); }
      };
   }

   public ItemStackHandler items() { return this.items; }

   public static boolean isUpgrade(ItemStack stack) {
      return SpeedUpgradeHelper.isSpeedUpgrade(stack)
         || stack.is(ModItems.POWER_COMPONENT.get())
         || stack.is(ModItems.AUTOMATIC_COMPONENT.get())
         || DimensionUpgradeHelper.isDimensionUpgrade(stack)
         || stack.is(ModItems.AP_EXPANSION_COMPONENT_1.get())
         || stack.is(ModItems.AP_EXPANSION_COMPONENT_2.get())
         || stack.is(ModItems.AP_EXPANSION_COMPONENT_3.get());
   }

   public static int expansionTier(IItemHandler handler) {
      int tier = 0;
      for (int slot = 0; slot < handler.getSlots(); slot++) {
         ItemStack stack = handler.getStackInSlot(slot);
         if (stack.is(ModItems.AP_EXPANSION_COMPONENT_3.get())) return 3;
         if (stack.is(ModItems.AP_EXPANSION_COMPONENT_2.get())) tier = Math.max(tier, 2);
         else if (stack.is(ModItems.AP_EXPANSION_COMPONENT_1.get())) tier = Math.max(tier, 1);
      }
      return tier;
   }

   public static int speedTier(IItemHandler handler) {
      int tier = 0;
      for (int slot = 0; slot < handler.getSlots(); slot++) {
         ItemStack stack = handler.getStackInSlot(slot);
         if (stack.is(ModItems.SPEED_COMPONENT_4.get())) return 4;
         if (stack.is(ModItems.SPEED_COMPONENT_3.get())) tier = Math.max(tier, 3);
         else if (stack.is(ModItems.SPEED_COMPONENT_2.get())) tier = Math.max(tier, 2);
         else if (stack.is(ModItems.SPEED_COMPONENT_1.get())) tier = Math.max(tier, 1);
      }
      return tier;
   }

   public static int powerCount(IItemHandler handler) {
      int count = 0;
      for (int slot = 0; slot < handler.getSlots(); slot++) {
         if (handler.getStackInSlot(slot).is(ModItems.POWER_COMPONENT.get())) count++;
      }
      return Math.min(MAX_POWER, count);
   }

   public static boolean automatic(IItemHandler handler) {
      for (int slot = 0; slot < handler.getSlots(); slot++) {
         if (handler.getStackInSlot(slot).is(ModItems.AUTOMATIC_COMPONENT.get())) return true;
      }
      return false;
   }

   public static ItemStack dimension(IItemHandler handler) {
      for (int slot = 0; slot < handler.getSlots(); slot++) {
         ItemStack stack = handler.getStackInSlot(slot);
         if (DimensionUpgradeHelper.isDimensionUpgrade(stack)) return stack;
      }
      return ItemStack.EMPTY;
   }

   public static int savingPercent(IItemHandler handler) { return powerCount(handler) * 25; }
   public int expansionTier() { return expansionTier(this.items); }
   public int speedTier() { return speedTier(this.items); }
   public boolean automatic() { return automatic(this.items); }
   public ItemStack dimensionUpgrade() { return dimension(this.items); }
   public int cost(int amount) { return Math.max(1, amount * (4 - powerCount(this.items)) / 4); }

   /** The AABB is half open; an exact chunk edge must not request the next chunk. */
   public static Set<Long> footprintChunks(AABB bounds) {
      Set<Long> result = new LinkedHashSet<>();
      int minX = ((int)Math.floor(bounds.minX)) >> 4;
      int maxX = ((int)Math.ceil(bounds.maxX) - 1) >> 4;
      int minZ = ((int)Math.floor(bounds.minZ)) >> 4;
      int maxZ = ((int)Math.ceil(bounds.maxZ) - 1) >> 4;
      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) result.add(ChunkPos.asLong(x, z));
      }
      return result;
   }

   public void updateChunks(ServerLevel level, BlockEntity owner, ApStructure.Layout layout) {
      if (this.cleanLegacyTickets) this.clearLegacyTickets(owner, layout);
      Set<Long> wanted = this.dimensionUpgrade().isEmpty() ? Set.of() : footprintChunks(layout.bounds());
      boolean changed = !wanted.equals(this.forcedChunks);
      for (long key : this.forcedChunks) {
         if (!wanted.contains(key)) force(owner, key, false);
      }
      for (long key : wanted) {
         if (!this.ticketsInitialized || !this.forcedChunks.contains(key)) force(owner, key, true);
      }
      this.forcedChunks.clear();
      this.forcedChunks.addAll(wanted);
      this.ticketsInitialized = true;
      if (changed) owner.setChanged();
   }

   public void release(BlockEntity owner, ApStructure.Layout layout) {
      if (!(owner.getLevel() instanceof ServerLevel)) return;
      if (this.cleanLegacyTickets) this.clearLegacyTickets(owner, layout);
      for (long key : this.forcedChunks) force(owner, key, false);
      if (!this.forcedChunks.isEmpty()) owner.setChanged();
      this.forcedChunks.clear();
      this.ticketsInitialized = false;
   }

   private static void force(BlockEntity owner, long key, boolean force) {
      ChunkPos chunk = new ChunkPos(key);
      DimensionUpgradeHelper.forceChunk(owner, chunk.x, chunk.z, force);
   }

   /** Only a migration cleanup for pre-rebuild saves, never an operation radius. */
   private void clearLegacyTickets(BlockEntity owner, ApStructure.Layout layout) {
      BlockPos oldCenter = owner.getBlockPos().relative(layout.facing().getOpposite());
      AABB legacy = new AABB(oldCenter.getX() - 12, oldCenter.getY(), oldCenter.getZ() - 12,
         oldCenter.getX() + 13, oldCenter.getY() + 1, oldCenter.getZ() + 13);
      for (long key : footprintChunks(legacy)) force(owner, key, false);
      this.cleanLegacyTickets = false;
      owner.setChanged();
   }

   public void save(CompoundTag tag) {
      tag.put("Upgrades", this.items.serializeNBT());
      tag.putInt("ApTicketVersion", this.cleanLegacyTickets ? 0 : 1);
      tag.putLongArray("ApForcedChunks", this.forcedChunks.stream().mapToLong(Long::longValue).toArray());
   }

   public void load(CompoundTag tag) {
      if (tag.contains("Upgrades")) {
         CompoundTag saved = tag.getCompound("Upgrades").copy();
         // ItemStackHandler otherwise restores the obsolete four-slot size from old saves.
         saved.putInt("Size", SLOTS);
         this.items.deserializeNBT(saved);
      }
      this.forcedChunks.clear();
      for (long key : tag.getLongArray("ApForcedChunks")) this.forcedChunks.add(key);
      this.cleanLegacyTickets = tag.getInt("ApTicketVersion") < 1;
      this.ticketsInitialized = false;
   }

   public interface Holder { ApUpgrades apUpgrades(); }
}
