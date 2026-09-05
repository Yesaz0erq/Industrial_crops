package com.industrialcrops.curios;

import com.industrialcrops.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
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

    public static List<ItemStack> stacks(LivingEntity entity, boolean carroteSlotOnly) {
        List<ItemStack> result = new ArrayList<>();
        CuriosApi.getCuriosInventory(entity).ifPresent(inventory -> inventory.getCurios().forEach((id, handler) -> {
            if (carroteSlotOnly && !id.equals("carrote")) return;
            var slots = handler.getStacks();
            for (int i = 0; i < slots.getSlots(); i++) result.add(slots.getStackInSlot(i));
        }));
        return result;
    }
}
