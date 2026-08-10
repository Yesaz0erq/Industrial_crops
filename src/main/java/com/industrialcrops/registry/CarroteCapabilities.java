package com.industrialcrops.registry;

import com.industrialcrops.replication.UniversalReplicaData;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class CarroteCapabilities {
    private CarroteCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CarroteBlockEntities.CARROTE_STEEL_FORGE.get(),
                (entity, side) -> entity.getAutomationHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CarroteBlockEntities.MATERIAL_HARDENING_DEVICE.get(),
                (entity, side) -> entity.getInventory());
        for (BlockCapability<?, ?> capability : BlockCapability.getAllProxyable()) {
            registerReplicaProxy(event, capability);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerReplicaProxy(RegisterCapabilitiesEvent event, BlockCapability capability) {
        event.registerBlock(capability, (level, pos, shellState, entity, context) -> {
            if (entity == null) {
                return null;
            }
            return UniversalReplicaData.getMirroredState(level, entity)
                    .filter(mirrored -> !mirrored.is(CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get()))
                    .map(mirrored -> UniversalReplicaData.call(level, pos, entity, mirrored,
                            () -> capability.getCapability(level, pos, mirrored, entity, context)))
                    .orElse(null);
        }, CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get());
    }
}
