package com.industrialcrops.machine;
import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.BasicControlDeviceBlockEntity;
import com.industrialcrops.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.world.ForgeChunkManager;
/** Chunk-loading and remote-access rules supplied by dimension components. */
public final class DimensionUpgradeHelper {
    public static final double DEFAULT_REMOTE_RADIUS = 128.0D;
    private static final double DEFAULT_REMOTE_RADIUS_SQR = DEFAULT_REMOTE_RADIUS * DEFAULT_REMOTE_RADIUS;
    private DimensionUpgradeHelper() { }
    public static void registerTicketValidation() {
        ForgeChunkManager.setForcedChunkLoadingCallback(IndustrialCrops.MOD_ID, (level, tickets) -> {
            tickets.getBlockTickets().keySet().forEach(pos -> {
                level.getChunkAt(pos);
                if (installedUpgrade(level, pos).isEmpty()) tickets.removeAllTickets(pos);
            });
        });
    }
    public static boolean isDimensionUpgrade(ItemStack stack) {
        return stack.is(ModItems.DIMENSION_UPGRADE_COMPONENT.get())
                || stack.is(ModItems.INFINITE_DIMENSION_UPGRADE_COMPONENT.get());
    }
    public static boolean isInfinite(ItemStack stack) {
        return stack.is(ModItems.INFINITE_DIMENSION_UPGRADE_COMPONENT.get());
    }
    public static void forceOwnerChunk(BlockEntity owner, boolean force) {
        if (!(owner.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = owner.getBlockPos();
        ForgeChunkManager.forceChunk(level, IndustrialCrops.MOD_ID, pos,
                pos.getX() >> 4, pos.getZ() >> 4, force, true);
    }
    public static void forceChunk(BlockEntity owner, int x, int z, boolean force) {
        if (owner.getLevel() instanceof ServerLevel level) ForgeChunkManager.forceChunk(level, IndustrialCrops.MOD_ID, owner.getBlockPos(), x, z, force, true);
    }
    public static boolean canRemoteAccess(Player player, Level targetLevel, BlockPos targetPos, ItemStack upgrade) {
        boolean sameDimension = player.level().dimension().equals(targetLevel.dimension());
        if (!sameDimension) return isInfinite(upgrade);
        if (isDimensionUpgrade(upgrade)) return true;
        return player.distanceToSqr(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D) <= DEFAULT_REMOTE_RADIUS_SQR;
    }
    public static ItemStack installedUpgrade(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BasicControlDeviceBlockEntity basic) return basic.getDimensionUpgrade();
        if (blockEntity instanceof AdvancedIndustrialStorageBlockEntity advanced) return advanced.getDimensionUpgrade();
        if (blockEntity instanceof com.industrialcrops.block.entity.CrystalLogisticsBlockEntity logistics) return logistics.getDimensionUpgrade();
        if (blockEntity instanceof ApUpgrades.Holder ap) return ap.apUpgrades().dimensionUpgrade();
        return ItemStack.EMPTY;
    }
}
