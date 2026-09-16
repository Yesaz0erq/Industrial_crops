package com.industrialcrops.network.payload;
import com.industrialcrops.screen.CrystalLogisticsMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record LogisticsConfigPayload(int containerId, String channel, boolean output) {
    public static void encode(LogisticsConfigPayload p, FriendlyByteBuf b) { b.writeInt(p.containerId); b.writeUtf(p.channel); b.writeBoolean(p.output); }
    public static LogisticsConfigPayload decode(FriendlyByteBuf b) { return new LogisticsConfigPayload(b.readInt(), b.readUtf(32), b.readBoolean()); }
    public static void handle(LogisticsConfigPayload payload, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get(); context.setPacketHandled(true);
        var player = context.getSender(); if (player == null) return;

        if (player.containerMenu instanceof CrystalLogisticsMenu menu && menu.containerId == payload.containerId)
            menu.configure(player, payload.channel, payload.output);

    }

}
