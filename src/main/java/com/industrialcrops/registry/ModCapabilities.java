package com.industrialcrops.registry;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

/** Forge 1.20.1 exposes block-entity capabilities from BlockEntity#getCapability. */
public final class ModCapabilities {
    private ModCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        // Capability providers are implemented by the migrated block entities.
    }
}
