package com.industrialcrops.client.gui;

import com.industrialcrops.network.payload.ResizeStorageMenuPayload;
import com.industrialcrops.network.payload.StorageSearchPayload;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.industrialcrops.network.ModNetworking;
import org.jetbrains.annotations.Nullable;

/** RS2 crafting-grid layout backed by the terminal's unlimited item-data repository. */
public final class ItemNetworkTerminalScreen extends IndustrialContainerScreen<ItemNetworkTerminalMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("item_network_management_terminal_rs2");
    private static final int BOTTOM_HEIGHT = 156;
    private static final int SEARCH_ICON_X = 79;
    private static final int SEARCH_X = 95;
    private static final int SEARCH_Y = 7;
    private static final int SEARCH_WIDTH = 67;
    private static final int SCROLL_X = 174;
    private static final int SCROLL_Y = 20;
    private static final int SCROLLER_HEIGHT = 15;

    private EditBox searchBox;
    private boolean draggingScrollbar;

    public ItemNetworkTerminalScreen(ItemNetworkTerminalMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 193;
        imageHeight = menu.getImageHeight();
        inventoryLabelY = menu.getPlayerInventoryY() - 13;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        int available = height - 19 - BOTTOM_HEIGHT;
        int desiredRows = Math.max(ItemNetworkTerminalMenu.MIN_ROWS, Math.min(
                ItemNetworkTerminalMenu.MAX_ROWS, available / 18 - 3));
        if (desiredRows != menu.getVisibleRows()) {
            ModNetworking.sendToServer(new ResizeStorageMenuPayload(menu.getBlockPos(), desiredRows, true));
        }
        searchBox = addRenderableWidget(new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y,
                SEARCH_WIDTH, 12, Component.translatable("gui.industrialcrops.storage.search")));
        searchBox.setBordered(false);
        searchBox.setMaxLength(64);
        searchBox.setTextColor(IndustrialGuiStyle.TEXT);
        searchBox.setTextColorUneditable(IndustrialGuiStyle.MUTED_TEXT);
        searchBox.setResponder(value -> ModNetworking.sendToServer(new StorageSearchPayload(value)));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawRs2StretchingBackground(
                graphics, BACKGROUND, leftPos, topPos, menu.getVisibleRows(), BOTTOM_HEIGHT);
        for (int row = 0; row < menu.getVisibleRows(); row++) {
            IndustrialGuiStyle.drawRs2GridRow(graphics, leftPos + 7, topPos + 19 + row * 18);
        }
        IndustrialGuiStyle.drawRs2SearchIcon(graphics, leftPos + SEARCH_ICON_X, topPos + 5);
        drawScrollbar(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawMarqueeTitle(graphics, font, title, 8, 7, 70, leftPos, topPos);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        drawItemCounts(graphics);
        if (menu.selectedVisible() >= 0) {
            int index = menu.selectedVisible();
            int x = leftPos + 8 + (index % 9) * 18;
            int y = topPos + 20 + (index / 9) * 18;
            drawSelection(graphics, x, y);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawItemCounts(GuiGraphics graphics) {
        for (int index = 0; index < menu.getVisibleSlotCount(); index++) {
            int count = menu.count(index);
            if (count <= 1) continue;
            String text = format(count);
            int x = leftPos + 8 + (index % 9) * 18;
            int y = topPos + 20 + (index / 9) * 18;
            drawNetworkCount(graphics, text, x, y);
        }
    }

    private void drawNetworkCount(GuiGraphics graphics, String text, int x, int y) {
        int textWidth = font.width(text);
        float scale = Math.min(1.0F, 15.0F / Math.max(1, textWidth));
        graphics.pose().pushPose();
        graphics.pose().translate(x + 16.0F, y + 16.0F, 300.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, -textWidth, -8, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        if (!insideStorageArea(mouseX, mouseY) && !insideScrollbar(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, verticalAmount);
        }
        if (verticalAmount > 0) setOffset(menu.page() - 1);
        else if (verticalAmount < 0) setOffset(menu.page() + 1);
        else return super.mouseScrolled(mouseX, mouseY, verticalAmount);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && insideScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            setOffsetFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar && button == 0) {
            setOffsetFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = draggingScrollbar;
        draggingScrollbar = false;
        return handled || super.mouseReleased(mouseX, mouseY, button);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int positions = Math.max(1, menu.totalPages());
        boolean enabled = positions > 1;
        int travel = scrollbarHeight() - SCROLLER_HEIGHT;
        int y = topPos + SCROLL_Y + (enabled ? travel * menu.page() / (positions - 1) : 0);
        IndustrialGuiStyle.drawRs2Scrollbar(graphics, leftPos + SCROLL_X, y, draggingScrollbar, enabled);
    }

    private int scrollbarHeight() {
        return menu.getVisibleRows() * 18 - 2;
    }

    private boolean insideScrollbar(double mouseX, double mouseY) {
        return mouseX >= leftPos + SCROLL_X && mouseX < leftPos + SCROLL_X + 12
                && mouseY >= topPos + SCROLL_Y && mouseY < topPos + SCROLL_Y + scrollbarHeight();
    }

    private boolean insideStorageArea(double mouseX, double mouseY) {
        return mouseX >= leftPos + 7 && mouseX < leftPos + 169
                && mouseY >= topPos + 19 && mouseY < topPos + 19 + menu.getVisibleRows() * 18;
    }

    private void setOffsetFromMouse(double mouseY) {
        int positions = Math.max(1, menu.totalPages());
        int travel = scrollbarHeight() - SCROLLER_HEIGHT;
        if (positions <= 1 || travel <= 0) {
            setOffset(0);
            return;
        }
        double fraction = (mouseY - SCROLLER_HEIGHT / 2.0 - (topPos + SCROLL_Y)) / travel;
        setOffset((int) Math.floor(Math.max(0, Math.min(1, fraction)) * (positions - 1)));
    }

    private void setOffset(int offset) {
        if (minecraft == null || minecraft.gameMode == null) return;
        int target = Math.max(0, Math.min(menu.totalPages() - 1, offset));
        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, ItemNetworkTerminalMenu.BUTTON_SET_PAGE_BASE + target);
    }

    private static void drawSelection(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 16, y + 1, 0x80FFFFFF);
        graphics.fill(x, y + 15, x + 16, y + 16, 0x80FFFFFF);
        graphics.fill(x, y, x + 1, y + 16, 0x80FFFFFF);
        graphics.fill(x + 15, y, x + 16, y + 16, 0x80FFFFFF);
    }

    private static String format(int count) {
        return IndustrialContainerScreen.formatCount(count);
    }
}
