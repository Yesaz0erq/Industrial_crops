package com.industrialcrops.network.payload;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import com.industrialcrops.screen.MatterMachineMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StorageSearchPayload(String query) implements CustomPacketPayload {
    public static final Type<StorageSearchPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "storage_search"));
    public static final StreamCodec<ByteBuf, StorageSearchPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(64), StorageSearchPayload::query, StorageSearchPayload::new);
    public static void handle(StorageSearchPayload payload, IPayloadContext context) {
        String query = payload.query == null ? "" : payload.query.strip().toLowerCase(java.util.Locale.ROOT);
        if (query.length() > 64) query = query.substring(0, 64);
        if (context.player().containerMenu instanceof AdvancedIndustrialStorageMenu menu) menu.setSearchQuery(query);
        else if (context.player().containerMenu instanceof ItemNetworkTerminalMenu menu) menu.setSearchQuery(query);
        else if (context.player().containerMenu instanceof MatterMachineMenu menu) menu.setSearchQuery(query);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
