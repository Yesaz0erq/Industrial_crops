package com.industrialcrops.client.gui;

import com.industrialcrops.screen.UltimateCarroteMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class UltimateCarroteScreen extends IndustrialContainerScreen<UltimateCarroteMenu> {
    private static final ResourceLocation GENERIC_CONTAINER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

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
        int storageHeight = menu.visibleRows() * 18 + 17;
        graphics.blit(GENERIC_CONTAINER, leftPos, topPos, 0, 0,
                imageWidth, storageHeight, 256, 256);
        graphics.blit(GENERIC_CONTAINER, leftPos, topPos + storageHeight, 0, 126,
                imageWidth, 96, 256, 256);

        if (menu.rows() > menu.visibleRows()) {
            int y = topPos + 18;
            int available = menu.visibleRows() * 18;
            graphics.fill(leftPos + 171, y, leftPos + 174, y + available, 0xFF8B8B8B);
            int thumb = Math.max(6, available * menu.visibleRows() / menu.rows());
            int offset = (available - thumb) * menu.scrollRow() / (menu.rows() - menu.visibleRows());
            graphics.fill(leftPos + 171, y + offset, leftPos + 174, y + offset + thumb, 0xFF555555);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, 6,
                imageWidth - 16, 0xFF404040, true);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xFF404040, false);
    }

    @Override public boolean mouseScrolled(double x, double y, double vertical) {
        if (x >= leftPos && x < leftPos + imageWidth && y >= topPos + 16 && y < topPos + 18 + menu.visibleRows() * 18) {
            int row = Math.max(0, Math.min(menu.scrollRow() + (vertical < 0 ? 1 : -1), menu.rows() - menu.visibleRows()));
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
            return true;
        }
        return super.mouseScrolled(x, y, vertical);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        resizePanel();
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
