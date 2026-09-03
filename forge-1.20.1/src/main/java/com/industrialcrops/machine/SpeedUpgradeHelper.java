package com.industrialcrops.machine;

import com.industrialcrops.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/** Resolves the strongest installed speed component. Only one tier is effective at a time. */
public final class SpeedUpgradeHelper {
    private SpeedUpgradeHelper() {
    }

    public static boolean isSpeedUpgrade(ItemStack stack) {
        return stack.is(ModItems.SPEED_COMPONENT_1.get())
                || stack.is(ModItems.SPEED_COMPONENT_2.get())
                || stack.is(ModItems.SPEED_COMPONENT_3.get())
                || stack.is(ModItems.SPEED_COMPONENT_4.get());
    }

    public static int tier(ItemStackHandler inventory, int firstSlot, int slotCount) {
        int tier = 0;
        for (int slot = firstSlot; slot < firstSlot + slotCount && slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(ModItems.SPEED_COMPONENT_4.get())) return 4;
            if (stack.is(ModItems.SPEED_COMPONENT_3.get())) tier = Math.max(tier, 3);
            else if (stack.is(ModItems.SPEED_COMPONENT_2.get())) tier = Math.max(tier, 2);
            else if (stack.is(ModItems.SPEED_COMPONENT_1.get())) tier = Math.max(tier, 1);
        }
        return tier;
    }

    public static int progressStep(ItemStackHandler inventory, int firstSlot, int slotCount, int maximum) {
        return switch (tier(inventory, firstSlot, slotCount)) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 8;
            case 4 -> Math.max(1, maximum);
            default -> 1;
        };
    }
}
