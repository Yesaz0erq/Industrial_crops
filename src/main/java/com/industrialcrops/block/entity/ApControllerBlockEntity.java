package com.industrialcrops.block.entity;

import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.ApUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public abstract class ApControllerBlockEntity extends BlockEntity implements MenuProvider, ApUpgrades.Holder {
   protected final ApUpgrades upgrades = new ApUpgrades(this::upgradesChanged);
   protected ApStructure.Status structure = new ApStructure.Status(0, 0, false, false);
   private ApStructure.Layout cachedLayout;
   private boolean structureDirty = true;
   private boolean projection = true;
   private int clientExpansionTier;

   protected ApControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public abstract Block coreBlock();
   public abstract ApStructure.Type structureType();
   public Direction facing() { return this.getBlockState().getValue(ApControllerBlock.FACING); }
   public int expansionTier() {
      return this.level != null && this.level.isClientSide() ? this.clientExpansionTier : this.upgrades.expansionTier();
   }

   public ApStructure.Layout layout() {
      int tier = this.expansionTier();
      Direction direction = this.facing();
      if (this.cachedLayout == null || this.cachedLayout.tier() != tier || this.cachedLayout.facing() != direction) {
         this.cachedLayout = ApStructure.layout(this.worldPosition, direction, this.structureType(), tier);
         this.structureDirty = true;
         this.structure = ApStructure.Status.empty(this.structureType(), tier);
         this.onStructureChanged();
      }
      return this.cachedLayout;
   }

   public BlockPos center() { return this.layout().center(); }
   public ApStructure.Status structureStatus() { this.layout(); return this.structure; }
   @Override public ApUpgrades apUpgrades() { return this.upgrades; }
   public ItemStackHandler getUpgradeSlots() { return this.upgrades.items(); }

   private void upgradesChanged() {
      this.setChanged();
      this.structureDirty = true;
      this.cachedLayout = null;
      this.structure = ApStructure.Status.empty(this.structureType(), this.upgrades.expansionTier());
      this.onStructureChanged();
      this.syncAppearance();
   }

   /** Subclasses drop every cached operation target when size or validity changes. */
   protected void onStructureChanged() { }

   protected void checkStructure() {
      if (this.level != null && !this.level.isClientSide()) {
         this.setStructureStatus(this.layout().check(this.level));
         this.structureDirty = false;
      }
   }

   private void setStructureStatus(ApStructure.Status status) {
      if (!this.structure.equals(status)) {
         this.structure = status;
         this.onStructureChanged();
         this.syncAppearance();
      }
   }

   protected boolean workAreaLoaded() {
      if (this.level == null) return false;
      for (long key : ApUpgrades.footprintChunks(this.layout().bounds())) {
         ChunkPos chunk = new ChunkPos(key);
         if (!this.level.hasChunk(chunk.x, chunk.z)) return false;
      }
      return true;
   }

   protected boolean workPosLoaded(BlockPos pos) {
      return this.level != null && this.layout().containsWorkPos(pos)
         && this.level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }

   protected void tickCommon(ServerLevel level, long time) {
      ApStructure.Layout current = this.layout();
      if (this.structureDirty || time % 20L == 0L) {
         this.upgrades.updateChunks(level, this, current);
         this.checkStructure();
      }
      // A neighbouring chunk may unload between the regular blueprint checks.
      if (this.structure.formed() && !this.workAreaLoaded()) {
         this.setStructureStatus(ApStructure.Status.empty(this.structureType(), this.expansionTier()));
         this.structureDirty = true;
      }
   }

   public void releaseChunks() { this.upgrades.release(this, this.layout()); }
   @Override public void setRemoved() { this.releaseChunks(); super.setRemoved(); }
   @Override public void onChunkUnloaded() { this.releaseChunks(); super.onChunkUnloaded(); }
   public boolean projection() { return this.projection; }
   public void toggleProjection() {
      this.projection = !this.projection;
      this.setChanged();
      this.syncAppearance();
   }

   private void syncAppearance() {
      if (this.level != null && !this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public CompoundTag getUpdateTag(Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.putBoolean("Projection", this.projection);
      tag.putInt("ExpansionTier", this.expansionTier());
      tag.putInt("StructureStatus", this.structure.pack());
      return tag;
   }
   public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
   public void handleUpdateTag(CompoundTag tag, Provider provider) {
      if (tag.contains("Projection")) this.projection = tag.getBoolean("Projection");
      int tier = Math.max(0, Math.min(3, tag.getInt("ExpansionTier")));
      if (tier != this.clientExpansionTier) { this.clientExpansionTier = tier; this.cachedLayout = null; }
      this.layout();
      if (tag.contains("StructureStatus")) this.structure = ApStructure.Status.unpack(tag.getInt("StructureStatus"));
   }
   public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, Provider provider) {
      this.handleUpdateTag(packet.getTag(), provider);
   }

   protected boolean exportToNeighbours(ItemStackHandler var1, int var2, int var3) {
      if (this.level == null) {
         return false;
      } else {
         boolean var4 = false;

         for (Direction var8 : Direction.values()) {
            BlockPos var9 = this.worldPosition.relative(var8);
            if (!this.level.hasChunk(var9.getX() >> 4, var9.getZ() >> 4)) continue;
            if (!(this.level.getBlockEntity(var9) instanceof ApControllerBlockEntity)) {
               IItemHandler var10 = (IItemHandler)this.level.getCapability(ItemHandler.BLOCK, var9, var8.getOpposite());
               if (var10 != null) {
                  for (int var11 = var2; var11 < var2 + var3; var11++) {
                     ItemStack var12 = var1.getStackInSlot(var11);
                     if (!var12.isEmpty()) {
                        ItemStack var13 = ItemHandlerHelper.insertItemStacked(var10, var12.copy(), false);
                        if (var13.getCount() != var12.getCount()) {
                           var1.setStackInSlot(var11, var13);
                           var4 = true;
                        }
                     }
                  }
               }
            }
         }

         return var4;
      }
   }

   protected boolean slotsFull(ItemStackHandler var1, int var2, int var3) {
      for (int var4 = var2; var4 < var2 + var3; var4++) {
         ItemStack var5 = var1.getStackInSlot(var4);
         if (var5.isEmpty() || var5.getCount() < var5.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   @Override protected void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      this.upgrades.save(tag, provider);
      tag.putBoolean("Projection", this.projection);
      // Diagnostic snapshot only; load always validates the live structure again.
      tag.putInt("StructureStatus", this.structure.pack());
   }
   @Override protected void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.upgrades.load(tag, provider);
      this.projection = !tag.contains("Projection") || tag.getBoolean("Projection");
      this.cachedLayout = null;
      this.structureDirty = true;
   }
}
