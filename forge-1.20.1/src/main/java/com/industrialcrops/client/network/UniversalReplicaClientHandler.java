package com.industrialcrops.client.network;

import com.industrialcrops.block.entity.UniversalReplicationDeviceBlockEntity;
import com.industrialcrops.network.payload.UniversalReplicaSyncPayload;
import com.industrialcrops.registry.CarroteBlockEntities;
import com.industrialcrops.registry.CarroteBlocks;
import com.industrialcrops.replication.UniversalReplicaData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class UniversalReplicaClientHandler {
    private UniversalReplicaClientHandler() {
    }

    public static void handle(UniversalReplicaSyncPayload payload) {
        var level = Minecraft.getInstance().level;
        var pos = payload.pos();
        var tag = payload.tag();
        if (level == null || !level.hasChunkAt(pos)
                || !level.getBlockState(pos).is(CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())) {
            return;
        }
        if (tag.isEmpty()) {
            level.removeBlockEntity(pos);
            level.setBlockEntity(new UniversalReplicationDeviceBlockEntity(pos, level.getBlockState(pos)));
            return;
        }

        BlockState mirrored = UniversalReplicaData.getMirroredStateFromTag(level, tag);
        ResourceLocation savedType = ResourceLocation.tryParse(tag.getString("id"));
        BlockState constructionState = savedType != null && savedType.equals(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CarroteBlockEntities.UNIVERSAL_REPLICATION_DEVICE.get()))
                ? level.getBlockState(pos) : mirrored;
        BlockEntity loaded = BlockEntity.loadStatic(pos, constructionState, tag);
        if (loaded != null) {
            level.removeBlockEntity(pos);
            level.setBlockEntity(loaded);
            UniversalReplicaData.setMirroredState(loaded, mirrored);
            UniversalReplicaData.markFeValidated(loaded);
        }
    }
}
