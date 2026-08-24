package com.industrialcrops.network.payload;

import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import com.industrialcrops.screen.MatterMachineMenu;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record StorageSearchPayload(String query) {
    public static void encode(StorageSearchPayload p, FriendlyByteBuf b) { b.writeUtf(p.query == null ? "" : p.query, 64); }
    public static StorageSearchPayload decode(FriendlyByteBuf b) { return new StorageSearchPayload(b.readUtf(64)); }
    public static void handle(StorageSearchPayload p, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        String query = p.query == null ? "" : p.query.strip().toLowerCase(Locale.ROOT);
        if (query.length() > 64) query = query.substring(0, 64);
        if (player != null && player.containerMenu instanceof AdvancedIndustrialStorageMenu menu) menu.setSearchQuery(query);
        else if (player != null && player.containerMenu instanceof ItemNetworkTerminalMenu menu) menu.setSearchQuery(query);
        else if (player != null && player.containerMenu instanceof MatterMachineMenu menu) menu.setSearchQuery(query);
        supplier.get().setPacketHandled(true);
    }
}
