package com.industrialcrops.item;

import com.industrialcrops.block.UniversalReplicationDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/** Validates the source machine before consuming and placing the device. */
public final class UniversalReplicationDeviceItem extends BlockItem {
    public UniversalReplicationDeviceItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Direction front = context.getHorizontalDirection().getOpposite();
        BlockPos sourcePos = UniversalReplicationDeviceBlock.sourcePosition(
                context.getClickedPos(),
                front
        );
        if (!UniversalReplicationDeviceBlock.canCopy(context.getLevel(), sourcePos)) {
            Player player = context.getPlayer();
            if (player != null && !context.getLevel().isClientSide()) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.industrialcrops.universal_replication_device.no_source"),
                        true
                );
            }
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }
}
