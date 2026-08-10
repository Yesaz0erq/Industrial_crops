package com.industrialcrops.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Shared slot rendering that keeps counts above items but below hover tooltips. */
abstract class IndustrialContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    protected IndustrialContainerScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, titleLabelY,
                imageWidth - 16, IndustrialGuiStyle.TEXT, true);
        IndustrialGuiStyle.drawFittedString(graphics, font, playerInventoryTitle.getString(), 8, inventoryLabelY,
                imageWidth - 16, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, @Nullable String countString) {
        int seed = slot.x + slot.y * imageWidth;
        if (slot.isFake()) graphics.renderFakeItem(stack, slot.x, slot.y, seed);
        else graphics.renderItem(stack, slot.x, slot.y, seed);

        String displayedCount = countString;
        if (displayedCount == null && !stack.isEmpty() && stack.getCount() != 1) {
            displayedCount = formatCount(stack.getCount());
        }
        // Vanilla owns the decoration depth, keeping counts above every item and below every tooltip.
        graphics.renderItemDecorations(font, stack, slot.x, slot.y, displayedCount);
    }

    static String formatCount(int count) {
        return count > 9_999 ? "9999+" : Integer.toString(count);
    }
}
