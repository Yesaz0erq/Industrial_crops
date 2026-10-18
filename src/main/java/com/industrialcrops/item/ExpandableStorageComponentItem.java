package com.industrialcrops.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Storage component for the Expandable Storage Device (inspired by Beyond Dimensions' unified storage):
 * every installed component unlocks a number of item-type slots, and sets how many items of a single
 * type the device can hold.
 */
public final class ExpandableStorageComponentItem extends Item {
    private final int itemTypes;
    private final int perTypeLimit;

    public ExpandableStorageComponentItem(Properties properties, int itemTypes, int perTypeLimit) {
        super(properties);
        this.itemTypes = itemTypes;
        this.perTypeLimit = perTypeLimit;
    }

    /** Number of distinct item types this component adds to the device. */
    public int itemTypes() {
        return itemTypes;
    }

    /** Maximum amount of one item type the device may store while this component is installed. */
    public int perTypeLimit() {
        return perTypeLimit;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.industrialcrops.expandable_component.per_type",
                String.format("%,d", perTypeLimit)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.industrialcrops.expandable_component.types", itemTypes)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.industrialcrops.expandable_component.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
