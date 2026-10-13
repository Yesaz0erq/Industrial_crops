package com.industrialcrops.block.entity;
import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.ApUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
public abstract class ApControllerBlockEntity extends BlockEntity implements MenuProvider, ApUpgrades.Holder {
   public static final int RADIUS = 12;
   protected final ApUpgrades upgrades = new ApUpgrades(this::setChanged);
   protected ApStructure.Status structure = new ApStructure.Status(0, 0, false, false);
   private boolean projection = true;
   protected ApControllerBlockEntity(BlockEntityType<?> var1, BlockPos var2, BlockState var3) {
      super(var1, var2, var3);
   }
   public abstract Block coreBlock();
   public Direction facing() {
      return (Direction)this.getBlockState().getValue(ApControllerBlock.FACING);
   }
   public BlockPos center() {
      return ApStructure.center(this.worldPosition, this.facing());
   }
   @Override
   public ApUpgrades apUpgrades() {
      return this.upgrades;
   }
   public ItemStackHandler getUpgradeSlots() {
      return this.upgrades.items();
   }
   protected void checkStructure() {
      if (this.level != null) {
         this.structure = ApStructure.check(this.level, this.worldPosition, this.facing(), this.coreBlock());
      }
   }
   protected void tickCommon(ServerLevel var1, long var2) {
      if (var2 % 20L == 0L) {
         this.checkStructure();
         this.upgrades.updateChunks(var1, this, this.center(), 12);
      }
   }
   public void releaseChunks() {
      this.upgrades.release(this, this.center(), 12);
   }
   public boolean projection() {
      return this.projection;
   }
   public void toggleProjection() {
      this.projection = !this.projection;
      this.setChanged();
      if (this.level != null && !this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }
   public CompoundTag getUpdateTag() {
      CompoundTag var2 = new CompoundTag();
      var2.putBoolean("Projection", this.projection);
      return var2;
   }
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
   public void handleUpdateTag(CompoundTag var1) {
      if (var1.contains("Projection")) {
         this.projection = var1.getBoolean("Projection");
      }
   }
   public void onDataPacket(Connection var1, ClientboundBlockEntityDataPacket var2) {
      this.handleUpdateTag(var2.getTag());
   }
   protected boolean exportToNeighbours(ItemStackHandler var1, int var2, int var3) {
      if (this.level == null) {
         return false;
      } else {
         boolean var4 = false;
         for (Direction var8 : Direction.values()) {
            BlockPos var9 = this.worldPosition.relative(var8);
            if (!(this.level.getBlockEntity(var9) instanceof ApControllerBlockEntity)) {
               IItemHandler var10 = (IItemHandler)com.industrialcrops.machine.ForgeCapabilityAccess.itemHandler(this.level, var9, var8.getOpposite());
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
   protected void saveAdditional(CompoundTag var1) {
      super.saveAdditional(var1);
      this.upgrades.save(var1);
      var1.putBoolean("Projection", this.projection);
   }
   public void load(CompoundTag var1) {
      super.load(var1);
      this.upgrades.load(var1);
      this.projection = !var1.contains("Projection") || var1.getBoolean("Projection");
   }
}
