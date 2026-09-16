package com.industrialcrops.network.payload;

import com.industrialcrops.screen.CrystalLogisticsMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LogisticsConfigPayload(int containerId, String channel, boolean output) implements CustomPacketPayload {
    public static final Type<LogisticsConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("industrialcrops", "logistics_config"));
    public static final StreamCodec<ByteBuf, LogisticsConfigPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, LogisticsConfigPayload::containerId,
        ByteBufCodecs.stringUtf8(32), LogisticsConfigPayload::channel,
        ByteBufCodecs.BOOL, LogisticsConfigPayload::output, LogisticsConfigPayload::new);
    public static void handle(LogisticsConfigPayload payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof CrystalLogisticsMenu menu && menu.containerId == payload.containerId)
            menu.configure(context.player(), payload.channel, payload.output);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
