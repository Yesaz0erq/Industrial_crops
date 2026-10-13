package com.industrialcrops.machine;

import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.BasicControlDeviceBlockEntity;
import com.industrialcrops.block.entity.CrystalLogisticsBlockEntity;
import com.industrialcrops.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

public final class DimensionUpgradeHelper {
   public static final double DEFAULT_REMOTE_RADIUS = 128.0;
   private static final double DEFAULT_REMOTE_RADIUS_SQR = 16384.0;
   private static final TicketController TICKETS = new TicketController(
      ResourceLocation.fromNamespaceAndPath("industrialcrops", "dimension_upgrades"), (var0, var1) -> var1.getBlockTickets().keySet().forEach(var2 -> {
            var0.getChunkAt(var2);
            if (installedUpgrade(var0, var2).isEmpty()) {
               var1.removeAllTickets(var2);
            }
         })
   );

   private DimensionUpgradeHelper() {
   }

   public static void registerTicketController(RegisterTicketControllersEvent var0) {
      var0.register(TICKETS);
   }

   public static boolean isDimensionUpgrade(ItemStack var0) {
      return var0.is((Item)ModItems.DIMENSION_UPGRADE_COMPONENT.get()) || var0.is((Item)ModItems.INFINITE_DIMENSION_UPGRADE_COMPONENT.get());
   }

   public static boolean isInfinite(ItemStack var0) {
      return var0.is((Item)ModItems.INFINITE_DIMENSION_UPGRADE_COMPONENT.get());
   }

   public static void forceOwnerChunk(BlockEntity var0, boolean var1) {
      if (var0.getLevel() instanceof ServerLevel var2) {
         BlockPos var4 = var0.getBlockPos();
         TICKETS.forceChunk(var2, var4, var4.getX() >> 4, var4.getZ() >> 4, var1, true);
      }
   }

   public static void forceChunk(BlockEntity var0, int var1, int var2, boolean var3) {
      if (var0.getLevel() instanceof ServerLevel var4) {
         TICKETS.forceChunk(var4, var0.getBlockPos(), var1, var2, var3, true);
      }
   }

   public static boolean canRemoteAccess(Player var0, Level var1, BlockPos var2, ItemStack var3) {
      boolean var4 = var0.level().dimension().equals(var1.dimension());
      if (!var4) {
         return isInfinite(var3);
      } else {
         return isDimensionUpgrade(var3)
            ? true
            : var0.distanceToSqr((double)var2.getX() + 0.5, (double)var2.getY() + 0.5, (double)var2.getZ() + 0.5) <= 16384.0;
      }
   }

   public static ItemStack installedUpgrade(Level var0, BlockPos var1) {
      BlockEntity var2 = var0.getBlockEntity(var1);
      if (var2 instanceof BasicControlDeviceBlockEntity var6) {
         return var6.getDimensionUpgrade();
      } else if (var2 instanceof AdvancedIndustrialStorageBlockEntity var5) {
         return var5.getDimensionUpgrade();
      } else if (var2 instanceof CrystalLogisticsBlockEntity var4) {
         return var4.getDimensionUpgrade();
      } else {
         return var2 instanceof ApUpgrades.Holder var3 ? var3.apUpgrades().dimensionUpgrade() : ItemStack.EMPTY;
      }
   }
}
