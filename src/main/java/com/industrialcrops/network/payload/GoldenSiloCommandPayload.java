package com.industrialcrops.network.payload;

import com.industrialcrops.block.entity.GoldenLaunchSiloBlockEntity;
import com.industrialcrops.screen.GoldenLaunchSiloMenu;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record GoldenSiloCommandPayload(BlockPos siloPos, int targetX, int targetY, int targetZ,
        boolean relativeMode, boolean ignoreYMode, boolean launch) {
    public static void encode(GoldenSiloCommandPayload p, FriendlyByteBuf b) {
        b.writeBlockPos(p.siloPos); b.writeVarInt(p.targetX); b.writeVarInt(p.targetY); b.writeVarInt(p.targetZ);
        b.writeBoolean(p.relativeMode); b.writeBoolean(p.ignoreYMode); b.writeBoolean(p.launch);
    }
    public static GoldenSiloCommandPayload decode(FriendlyByteBuf b) {
        return new GoldenSiloCommandPayload(b.readBlockPos(), b.readVarInt(), b.readVarInt(), b.readVarInt(),
                b.readBoolean(), b.readBoolean(), b.readBoolean());
    }
    public static void handle(GoldenSiloCommandPayload p, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null && player.containerMenu instanceof GoldenLaunchSiloMenu menu
                && menu.getSiloPos().equals(p.siloPos)
                && player.distanceToSqr(p.siloPos.getX() + .5, p.siloPos.getY() + .5, p.siloPos.getZ() + .5) <= 64
                && player.level().getBlockEntity(p.siloPos) instanceof GoldenLaunchSiloBlockEntity silo) {
            if (p.launch) silo.launch();
            else silo.configureTarget(p.targetX, p.targetY, p.targetZ, p.relativeMode, p.ignoreYMode);
        }
        supplier.get().setPacketHandled(true);
    }
}
