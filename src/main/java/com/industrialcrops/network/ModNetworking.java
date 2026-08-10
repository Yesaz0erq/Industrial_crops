package com.industrialcrops.network;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.network.payload.GoldenSiloCommandPayload;
import com.industrialcrops.network.payload.MatterMachineSelectionPayload;
import com.industrialcrops.network.payload.ResizeStorageMenuPayload;
import com.industrialcrops.network.payload.StorageSearchPayload;
import com.industrialcrops.network.payload.StorageCraftingTransferPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registers client-to-server commands used by coordinate entry fields. */
@EventBusSubscriber(modid = IndustrialCrops.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetworking {
    private ModNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(IndustrialCrops.MOD_ID).versioned("2");
        registrar.playToServer(
                GoldenSiloCommandPayload.TYPE,
                GoldenSiloCommandPayload.STREAM_CODEC,
                GoldenSiloCommandPayload::handle
        );
        registrar.playToServer(
                ResizeStorageMenuPayload.TYPE,
                ResizeStorageMenuPayload.STREAM_CODEC,
                ResizeStorageMenuPayload::handle
        );
        registrar.playToServer(StorageSearchPayload.TYPE, StorageSearchPayload.STREAM_CODEC, StorageSearchPayload::handle);
        registrar.playToServer(StorageCraftingTransferPayload.TYPE, StorageCraftingTransferPayload.STREAM_CODEC,
                StorageCraftingTransferPayload::handle);
        registrar.playToServer(MatterMachineSelectionPayload.TYPE, MatterMachineSelectionPayload.STREAM_CODEC,
                MatterMachineSelectionPayload::handle);
    }
}
