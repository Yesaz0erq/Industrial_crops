package com.industrialcrops.network;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.network.payload.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Forge 1.20.1 SimpleChannel registration for the migrated packets. */
public final class ModNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(IndustrialCrops.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();
    private static boolean initialized;

    private ModNetworking() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        int id = 0;
        CHANNEL.messageBuilder(GoldenSiloCommandPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(GoldenSiloCommandPayload::encode).decoder(GoldenSiloCommandPayload::decode)
                .consumerMainThread(GoldenSiloCommandPayload::handle).add();
        CHANNEL.messageBuilder(ResizeStorageMenuPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ResizeStorageMenuPayload::encode).decoder(ResizeStorageMenuPayload::decode)
                .consumerMainThread(ResizeStorageMenuPayload::handle).add();
        CHANNEL.messageBuilder(StorageSearchPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(StorageSearchPayload::encode).decoder(StorageSearchPayload::decode)
                .consumerMainThread(StorageSearchPayload::handle).add();
        CHANNEL.messageBuilder(StorageCraftingTransferPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(StorageCraftingTransferPayload::encode).decoder(StorageCraftingTransferPayload::decode)
                .consumerMainThread(StorageCraftingTransferPayload::handle).add();
        CHANNEL.messageBuilder(MatterMachineSelectionPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MatterMachineSelectionPayload::encode).decoder(MatterMachineSelectionPayload::decode)
                .consumerMainThread(MatterMachineSelectionPayload::handle).add();
        CHANNEL.messageBuilder(UniversalReplicaSyncPayload.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UniversalReplicaSyncPayload::encode).decoder(UniversalReplicaSyncPayload::decode)
                .consumerMainThread(UniversalReplicaSyncPayload::handle).add();
    }

    public static void sendToServer(Object message) { CHANNEL.sendToServer(message); }
}
