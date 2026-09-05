package com.industrialcrops.curios;

import com.industrialcrops.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Loaded only when Curios is present. No optional API types escape this boundary. */
public final class CuriosIntegration {
    private CuriosIntegration() {}

    public static void registerItems() {
        ICurioItem accessory = new ICurioItem() {};
        CarroteCuriosItems.ITEMS.getEntries().forEach(item -> CuriosApi.registerCurio(item.get(), accessory));
        CuriosApi.registerCurio(ModItems.REMOTE_ACCESS_DEVICE.get(), accessory);
    }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity)
                .map(inventory -> inventory.findFirstCurio(stack -> stack.is(item)).isPresent()).orElse(false);
    }
}
