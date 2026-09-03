package com.industrialcrops.registry;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

/** Forge 1.20.1 capability registration compatibility hook. */
public final class CarroteCapabilities {
    private CarroteCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        // Capability providers are implemented by the migrated block entities.
    }
}
