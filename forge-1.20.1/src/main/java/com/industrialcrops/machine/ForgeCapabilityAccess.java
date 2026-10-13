package com.industrialcrops.machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
/** Resolve adjacent item providers without assuming that a block entity exists. */
public final class ForgeCapabilityAccess {
    private ForgeCapabilityAccess() {}
    public static IItemHandler itemHandler(Level level, BlockPos pos, Direction side) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity == null ? null : entity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
    }
}
