package com.industrialcrops.item;

import net.minecraft.world.item.Item;

/** Capacity-bearing storage cell used by the reinforced storage controller. */
public final class IndustrialStorageComponentItem extends Item {
    private final int capacityMultiplier;

    public IndustrialStorageComponentItem(Properties properties, int capacityMultiplier) {
        super(properties);
        this.capacityMultiplier = capacityMultiplier;
    }

    public int capacityMultiplier() {
        return capacityMultiplier;
    }
}
