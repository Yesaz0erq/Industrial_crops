package com.industrialcrops.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Client-only implementation, reached only when an item tooltip is actually requested. */
public final class CarroteTooltip {
    private CarroteTooltip() {}

    public static boolean isShiftDown() { return Screen.hasShiftDown(); }

    public static void addCopyStatus(ItemStack stack, List<Component> lines) {
        var player = Minecraft.getInstance().player;
        ItemStack copied = player != null && player.getItemBySlot(EquipmentSlot.HEAD) == stack
                ? CarroteCuriosEffects.copiedCarrote(player) : ItemStack.EMPTY;
        lines.add(copied.isEmpty()
                ? Component.translatable("tooltip.carrote_curios.not_copied").withStyle(ChatFormatting.DARK_GRAY)
                : Component.translatable("tooltip.carrote_curios.copied", copied.getHoverName()).withStyle(ChatFormatting.GREEN));
    }
}
