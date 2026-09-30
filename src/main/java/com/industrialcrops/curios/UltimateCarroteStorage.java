package com.industrialcrops.curios;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** Complete item state is stored independently of the visible menu page. */
public final class UltimateCarroteStorage {
    private static final String CONTENTS = "CarroteBagContents";
    private UltimateCarroteStorage() {}

    public static boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && CarroteCuriosItems.isAccessory(stack)
                && !stack.is(CarroteCuriosItems.ULTIMATE.get());
    }

    public static List<ItemStack> contents(ItemStack bag, RegistryAccess registries) {
        if (!bag.is(CarroteCuriosItems.ULTIMATE.get())) return List.of();
        var data = bag.getOrCreateTag();
        var result = new ArrayList<ItemStack>();
        var list = data.getList(CONTENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) result.add(ItemStack.of(list.getCompound(i)));
        return result;
    }

    public static void save(ItemStack bag, List<ItemStack> contents, RegistryAccess registries) {
        var list = new ListTag();
        int last = contents.size() - 1;
        while (last >= 0 && contents.get(last).isEmpty()) last--;
        for (int i = 0; i <= last; i++) list.add(contents.get(i).isEmpty() ? new CompoundTag() : contents.get(i).save(new CompoundTag()));
        CompoundTag tag = bag.getOrCreateTag();
            if (list.isEmpty()) tag.remove(CONTENTS);
            else tag.put(CONTENTS, list);

    }

    public static void replace(ItemStack bag, int index, ItemStack stack, RegistryAccess registries) {
        var items = new ArrayList<>(contents(bag, registries));
        if (index < 0 || index >= items.size()) return;
        items.set(index, stack.copy());
        save(bag, items, registries);
    }

    public static int rows(List<ItemStack> contents) {
        long count = contents.stream().filter(s -> !s.isEmpty()).count();
        return Math.max(2, (int) (count / 9) + 2);
    }
}
