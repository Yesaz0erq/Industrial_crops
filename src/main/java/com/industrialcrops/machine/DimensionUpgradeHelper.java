package com.industrialcrops.machine;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.BasicControlDeviceBlockEntity;
import com.industrialcrops.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

/** Chunk-loading and remote-access rules supplied by dimension components. */
public final class DimensionUpgradeHelper {
    public static final double DEFAULT_REMOTE_RADIUS = 128.0D;
    private static final double DEFAULT_REMOTE_RADIUS_SQR = DEFAULT_REMOTE_RADIUS * DEFAULT_REMOTE_RADIUS;
    private static final TicketController TICKETS = new TicketController(
            ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "dimension_upgrades"),
            (level, helper) -> helper.getBlockTickets().keySet().forEach(owner -> {
                level.getChunkAt(owner);
                if (installedUpgrade(level, owner).isEmpty()) helper.removeAllTickets(owner);
            }));

    private DimensionUpgradeHelper() {
    }

    public static void registerTicketController(RegisterTicketControllersEvent event) {
        event.register(TICKETS);
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
        TICKETS.forceChunk(level, pos, pos.getX() >> 4, pos.getZ() >> 4, force, true);
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
        return ItemStack.EMPTY;
    }
}
