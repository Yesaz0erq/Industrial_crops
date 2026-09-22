package com.industrialcrops.client.gui;

import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.ControllerStorageSorting;
import com.industrialcrops.screen.ReinforcedControlDeviceMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public final class ControllerSortingCompat {
    private ControllerSortingCompat() {}

    public static boolean request(boolean playerSide, boolean fromButton) {
        return request(playerSide, fromButton, java.util.Set.of());
    }

    public static boolean requestFromSimpleSorter() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.player.containerMenu instanceof ReinforcedControlDeviceMenu
                || minecraft.player.containerMenu instanceof AdvancedIndustrialStorageMenu)) return false;
        try {
            Class<?> manager = Class.forName("simplesorter.mc.LockManager");
            Object locks = manager.getMethod("getContainerLockedSlots", int.class)
                    .invoke(manager.getField("INSTANCE").get(null), minecraft.player.containerMenu.containerId);
            if (!(locks instanceof java.util.Set<?> locked)) throw new ReflectiveOperationException("Unknown slot lock format");
            return request(false, true, locked);
        } catch (ReflectiveOperationException ex) {
            com.mojang.logging.LogUtils.getLogger().warn("Cannot read SimpleSorterR slot locks; controller sort cancelled", ex);
            return true;
        }
    }

    private static boolean request(boolean playerSide, boolean fromButton, java.util.Set<?> locked) {
        Minecraft minecraft = Minecraft.getInstance();
        if (playerSide || minecraft.player == null || minecraft.gameMode == null
                || !(minecraft.screen instanceof AbstractContainerScreen<?> screen)) return false;
        AbstractContainerMenu menu = screen.getMenu();
        if (!(menu instanceof ReinforcedControlDeviceMenu || menu instanceof AdvancedIndustrialStorageMenu)) return false;
        Slot hovered = screen.getSlotUnderMouse();
        if (!fromButton && hovered != null && hovered.container == minecraft.player.getInventory()) return false;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ControllerStorageSorting.BUTTON_RESET_LOCKS);
        for (Object value : locked) {
            if (value instanceof Integer index && index >= 0 && index < menu.slots.size()
                    && menu.slots.get(index) instanceof com.industrialcrops.screen.ControllerStorageSlot) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ControllerStorageSorting.BUTTON_LOCK_BASE + index);
            }
        }
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ControllerStorageSorting.BUTTON_SORT);
        return true;
    }
}
