package com.industrialcrops.network;

import com.industrialcrops.Carrote;
import com.industrialcrops.network.payload.UniversalReplicaSyncPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Carrote.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class CarroteNetworking {
    private CarroteNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Carrote.MOD_ID).versioned("1");
        registrar.playToClient(UniversalReplicaSyncPayload.TYPE, UniversalReplicaSyncPayload.STREAM_CODEC,
                UniversalReplicaSyncPayload::handle);
    }
}
