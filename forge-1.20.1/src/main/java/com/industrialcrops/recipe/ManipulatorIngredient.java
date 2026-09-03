package com.industrialcrops.recipe;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ManipulatorIngredient(List<ItemStack> acceptedStacks, ItemStack displayStack, int count) {
    public static ManipulatorIngredient ofItem(ItemLike item, int count) {
        ItemStack stack = new ItemStack(item, count);
        return new ManipulatorIngredient(List.of(stack.copyWithCount(1)), stack, count);
    }

    public static ManipulatorIngredient ofOptions(List<ItemStack> acceptedStacks, ItemStack displayStack, int count) {
        return new ManipulatorIngredient(
                acceptedStacks.stream().map(stack -> stack.copyWithCount(1)).toList(),
                displayStack.copyWithCount(count),
                count
        );
    }

    public boolean isExactItem() {
        return acceptedStacks.size() == 1;
    }

    @Override
    public ItemStack displayStack() {
        return displayStack.copy();
    }
}
