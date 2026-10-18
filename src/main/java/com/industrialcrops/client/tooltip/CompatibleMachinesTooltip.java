package com.industrialcrops.client.tooltip;

import java.util.List;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/** Tooltip payload: the devices that use a component. */
public record CompatibleMachinesTooltip(List<ItemStack> machines) implements TooltipComponent {
}
