package com.industrialcrops.item;

import com.industrialcrops.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BagItemHelper {
    private BagItemHelper() {
    }

    public static void consumeAndReturnEmptyBag(Player player, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return;
        }
        stack.shrink(1);
        giveEmptyBag(player);
    }

    public static void giveEmptyBag(Player player) {
        giveEmptyBags(player, 1);
    }

    public static void giveEmptyBags(Player player, int count) {
        if (count <= 0) {
            return;
        }
        ItemStack emptyBag = new ItemStack(ModItems.EMPTY_BAG.get(), count);
        if (!player.getInventory().add(emptyBag)) {
            player.drop(emptyBag, false);
        }
    }
}
