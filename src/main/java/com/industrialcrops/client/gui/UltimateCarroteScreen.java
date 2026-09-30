package com.industrialcrops.client.gui;

import com.industrialcrops.screen.UltimateCarroteMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class UltimateCarroteScreen extends IndustrialContainerScreen<UltimateCarroteMenu> {
    public UltimateCarroteScreen(UltimateCarroteMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        resizePanel();
    }

    @Override protected void init() {
        int limit = Math.max(2, Math.min(6, (height - 124) / 18));
        menu.visibleRowLimit(limit);
        resizePanel();
        super.init();
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, -limit);
    }

    private void resizePanel() {
        menu.layout();
        imageHeight = 114 + menu.visibleRows() * 18;
        inventoryLabelY = menu.inventoryY() - 12;
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawParadoxContainer(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (var slot : menu.slots) {
            if (slot.isActive()) IndustrialGuiStyle.drawParadoxSlot(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
        }
        if (menu.rows() > menu.visibleRows()) {
            int y = topPos + 18;
            int available = menu.visibleRows() * 18;
            graphics.fill(leftPos + 170, y, leftPos + 173, y + available, 0xFF403743);
            int thumb = Math.max(6, available * menu.visibleRows() / menu.rows());
            int offset = (available - thumb) * menu.scrollRow() / (menu.rows() - menu.visibleRows());
            graphics.fill(leftPos + 170, y + offset, leftPos + 173, y + offset + thumb, 0xFFD0AE69);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, 6,
                imageWidth - 16, 0xFFF4DDE2, true);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xFFC4A5B2, false);
    }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= leftPos && x < leftPos + imageWidth && y >= topPos + 16 && y < topPos + 18 + menu.visibleRows() * 18) {
            int row = Math.max(0, Math.min(menu.scrollRow() + (vertical < 0 ? 1 : -1), menu.rows() - menu.visibleRows()));
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        resizePanel();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
