package com.industrialcrops.replication;

import com.industrialcrops.Carrote;
import com.industrialcrops.network.payload.UniversalReplicaSyncPayload;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.block.UniversalReplicationDeviceBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Sends full replica data after the normal chunk packet has reached a client. */
@EventBusSubscriber(modid = Carrote.MOD_ID)
public final class UniversalReplicaEvents {
    private UniversalReplicaEvents() {
    }

    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        for (BlockEntity entity : event.getChunk().getBlockEntities().values()) {
            if (event.getLevel().getBlockState(entity.getBlockPos())
                    .is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())
                    && UniversalReplicaData.isReplica(entity)) {
                PacketDistributor.sendToPlayer(
                        event.getPlayer(),
                        UniversalReplicaSyncPayload.of(entity, event.getLevel())
                );
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        for (BlockEntity entity : java.util.List.copyOf(chunk.getBlockEntities().values())) {
            if (level.getBlockState(entity.getBlockPos())
                    .is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())
                    && UniversalReplicaData.hasUnvalidatedReplicaMarker(entity)) {
                UniversalReplicationDeviceBlock.restoreShellBlockEntity(level, entity.getBlockPos());
                UniversalReplicaSyncPayload.sendClearToTracking(level, entity.getBlockPos());
            }
        }
    }
}
