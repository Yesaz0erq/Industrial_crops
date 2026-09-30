package com.industrialcrops.client.gui;

import com.industrialcrops.screen.UltimateCarroteMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class UltimateCarroteScreen extends IndustrialContainerScreen<UltimateCarroteMenu> {
    private static final ResourceLocation GENERIC_CONTAINER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");
    private static final int SCROLL_X = 171;
    private static final int SCROLL_Y = 18;
    private static final int SCROLLER_HEIGHT = 15;
    private boolean draggingScrollbar;

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

        drawScrollbar(graphics);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, 6,
                imageWidth - 16, 0xFF404040, true);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xFF404040, false);
    }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (insideStorageArea(x, y) || insideScrollbar(x, y)) {
            int row = Math.max(0, Math.min(menu.scrollRow() + (vertical < 0 ? 1 : -1), menu.rows() - menu.visibleRows()));
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Override public boolean mouseClicked(double x, double y, int button) {
        if (button == 0 && insideScrollbar(x, y)) {
            draggingScrollbar = true;
            setScrollFromMouse(y);
            return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        if (draggingScrollbar && button == 0) {
            setScrollFromMouse(y);
            return true;
        }
        return super.mouseDragged(x, y, button, dragX, dragY);
    }

    @Override public boolean mouseReleased(double x, double y, int button) {
        boolean handled = draggingScrollbar;
        draggingScrollbar = false;
        return handled || super.mouseReleased(x, y, button);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int positions = Math.max(1, menu.rows() - menu.visibleRows() + 1);
        boolean enabled = positions > 1;
        int travel = scrollbarHeight() - SCROLLER_HEIGHT;
        int y = topPos + SCROLL_Y + (enabled ? travel * menu.scrollRow() / (positions - 1) : 0);
        IndustrialGuiStyle.drawRs2Scrollbar(graphics, leftPos + SCROLL_X, y, draggingScrollbar, enabled);
    }

    private int scrollbarHeight() {
        return menu.visibleRows() * 18;
    }

    private boolean insideScrollbar(double x, double y) {
        return x >= leftPos + SCROLL_X && x < leftPos + SCROLL_X + 12
                && y >= topPos + SCROLL_Y && y < topPos + SCROLL_Y + scrollbarHeight();
    }

    private boolean insideStorageArea(double x, double y) {
        return x >= leftPos + 7 && x < leftPos + 169
                && y >= topPos + SCROLL_Y && y < topPos + SCROLL_Y + menu.visibleRows() * 18;
    }

    private void setScrollFromMouse(double mouseY) {
        int maxScroll = Math.max(0, menu.rows() - menu.visibleRows());
        int travel = scrollbarHeight() - SCROLLER_HEIGHT;
        if (maxScroll <= 0 || travel <= 0) {
            setScroll(0);
            return;
        }
        double fraction = (mouseY - SCROLLER_HEIGHT / 2.0 - (topPos + SCROLL_Y)) / travel;
        setScroll((int) Math.floor(Math.max(0, Math.min(1, fraction)) * maxScroll));
    }

    private void setScroll(int row) {
        if (minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                Math.max(0, Math.min(menu.rows() - menu.visibleRows(), row)));
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        resizePanel();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
