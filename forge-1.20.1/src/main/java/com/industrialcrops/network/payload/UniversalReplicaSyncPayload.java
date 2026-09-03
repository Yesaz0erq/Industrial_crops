package com.industrialcrops.network.payload;

import com.industrialcrops.client.network.UniversalReplicaClientHandler;
import com.industrialcrops.network.ModNetworking;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public record UniversalReplicaSyncPayload(BlockPos pos, CompoundTag tag) {
    public static void encode(UniversalReplicaSyncPayload p, FriendlyByteBuf b) { b.writeBlockPos(p.pos); b.writeNbt(p.tag); }
    public static UniversalReplicaSyncPayload decode(FriendlyByteBuf b) {
        BlockPos pos = b.readBlockPos();
        CompoundTag tag = b.readNbt();
        return new UniversalReplicaSyncPayload(pos, tag == null ? new CompoundTag() : tag);
    }
    public static UniversalReplicaSyncPayload of(BlockEntity entity, ServerLevel level) {
        return new UniversalReplicaSyncPayload(entity.getBlockPos(), entity.saveWithFullMetadata());
    }
    public static void sendToTracking(ServerLevel level, BlockEntity entity) {
        ModNetworking.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(entity.getBlockPos())), of(entity, level));
    }
    public static void sendClearToTracking(ServerLevel level, BlockPos pos) {
        ModNetworking.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)),
                new UniversalReplicaSyncPayload(pos, new CompoundTag()));
    }
    public static void handle(UniversalReplicaSyncPayload p, Supplier<NetworkEvent.Context> supplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> UniversalReplicaClientHandler.handle(p));
        supplier.get().setPacketHandled(true);
    }
}
