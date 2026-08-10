package com.industrialcrops.network.payload;

import com.industrialcrops.Carrote;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.replication.UniversalReplicaData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Replaces the client placeholder with the copied machine's exact BE type. */
public record UniversalReplicaSyncPayload(BlockPos pos, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<UniversalReplicaSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Carrote.MOD_ID, "universal_replica_sync"));
    public static final StreamCodec<ByteBuf, UniversalReplicaSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public UniversalReplicaSyncPayload decode(ByteBuf buffer) {
            return new UniversalReplicaSyncPayload(
                    BlockPos.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.COMPOUND_TAG.decode(buffer)
            );
        }

        @Override
        public void encode(ByteBuf buffer, UniversalReplicaSyncPayload payload) {
            BlockPos.STREAM_CODEC.encode(buffer, payload.pos);
            ByteBufCodecs.COMPOUND_TAG.encode(buffer, payload.tag);
        }
    };

    public static UniversalReplicaSyncPayload of(BlockEntity entity, ServerLevel level) {
        return new UniversalReplicaSyncPayload(
                entity.getBlockPos(),
                entity.saveWithFullMetadata(level.registryAccess())
        );
    }

    public static void sendToTracking(ServerLevel level, BlockEntity entity) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(entity.getBlockPos()), of(entity, level));
    }

    public static void handle(UniversalReplicaSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            if (!level.hasChunkAt(payload.pos)
                    || !level.getBlockState(payload.pos).is(com.industrialcrops.registry.CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get())) {
                return;
            }
            BlockEntity current = level.getBlockEntity(payload.pos);
            if (current == null) {
                return;
            }
            if (payload.tag.isEmpty()) {
                level.removeBlockEntity(payload.pos);
                level.setBlockEntity(new com.industrialcrops.block.entity.UniversalReplicationDeviceBlockEntity(
                        payload.pos, level.getBlockState(payload.pos)));
                level.invalidateCapabilities(payload.pos);
                return;
            }
            BlockState mirrored = UniversalReplicaData.getMirroredStateFromTag(level, payload.tag);
            ResourceLocation savedType = ResourceLocation.tryParse(payload.tag.getString("id"));
            BlockState constructionState = savedType != null
                    && savedType.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(
                            com.industrialcrops.registry.CarroteBlockEntities.UNIVERSAL_REPLICATION_DEVICE.get()))
                    ? level.getBlockState(payload.pos)
                    : mirrored;
            BlockEntity loaded = BlockEntity.loadStatic(
                    payload.pos, constructionState, payload.tag, level.registryAccess());
            if (loaded == null) {
                return;
            }
            level.removeBlockEntity(payload.pos);
            level.setBlockEntity(loaded);
            UniversalReplicaData.setMirroredState(loaded, mirrored);
            UniversalReplicaData.markFeValidated(loaded);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void sendClearToTracking(ServerLevel level, BlockPos pos) {
        PacketDistributor.sendToPlayersTrackingChunk(
                level, new ChunkPos(pos), new UniversalReplicaSyncPayload(pos, new CompoundTag()));
    }
}
