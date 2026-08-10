package com.industrialcrops.network.payload;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.GoldenLaunchSiloBlockEntity;
import com.industrialcrops.screen.GoldenLaunchSiloMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Updates coordinates atomically with a launch request. The server verifies
 * both the open menu and player distance before accepting the command.
 */
public record GoldenSiloCommandPayload(
        BlockPos siloPos,
        int targetX,
        int targetY,
        int targetZ,
        boolean relativeMode,
        boolean ignoreYMode,
        boolean launch
)
        implements CustomPacketPayload {
    public static final Type<GoldenSiloCommandPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "golden_silo_command")
    );
    public static final StreamCodec<ByteBuf, GoldenSiloCommandPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GoldenSiloCommandPayload decode(ByteBuf buffer) {
            return new GoldenSiloCommandPayload(
                    BlockPos.STREAM_CODEC.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.VAR_INT.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer),
                    ByteBufCodecs.BOOL.decode(buffer)
            );
        }

        @Override
        public void encode(ByteBuf buffer, GoldenSiloCommandPayload payload) {
            BlockPos.STREAM_CODEC.encode(buffer, payload.siloPos);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.targetX);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.targetY);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.targetZ);
            ByteBufCodecs.BOOL.encode(buffer, payload.relativeMode);
            ByteBufCodecs.BOOL.encode(buffer, payload.ignoreYMode);
            ByteBufCodecs.BOOL.encode(buffer, payload.launch);
        }
    };

    public static void handle(GoldenSiloCommandPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.containerMenu instanceof GoldenLaunchSiloMenu menu)
                || !menu.getSiloPos().equals(payload.siloPos)
                || player.distanceToSqr(
                        payload.siloPos.getX() + 0.5D,
                        payload.siloPos.getY() + 0.5D,
                        payload.siloPos.getZ() + 0.5D
                ) > 64.0D
                || !(player.level().getBlockEntity(payload.siloPos) instanceof GoldenLaunchSiloBlockEntity silo)) {
            return;
        }

        if (payload.launch) {
            silo.launch();
        } else {
            silo.configureTarget(
                    payload.targetX,
                    payload.targetY,
                    payload.targetZ,
                    payload.relativeMode,
                    payload.ignoreYMode
            );
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
