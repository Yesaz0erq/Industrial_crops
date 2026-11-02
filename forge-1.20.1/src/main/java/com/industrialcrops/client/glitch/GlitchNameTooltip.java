package com.industrialcrops.client.glitch;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

/** Tooltip payload replacing the name line of Carrote items. */
public record GlitchNameTooltip(Component name, int seed) implements TooltipComponent {
}
