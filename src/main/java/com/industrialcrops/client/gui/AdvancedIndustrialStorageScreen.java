package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.network.payload.ResizeStorageMenuPayload;
import com.industrialcrops.network.payload.StorageSearchPayload;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/** RS2-style stretching storage/crafting screen used by the reinforced controller. */
public final class AdvancedIndustrialStorageScreen extends IndustrialContainerScreen<AdvancedIndustrialStorageMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("advanced_industrial_storage_rs2");
    private static final int BOTTOM_HEIGHT = 174;
    private static final int SEARCH_ICON_X = 79;
    private static final int SEARCH_X = 95;
    private static final int SEARCH_Y = 7;
    private static final int SEARCH_WIDTH = 67;
    private static final int SCROLL_X = 174;
    private static final int SCROLL_Y = 20;
    private static final int SCROLLER_HEIGHT = 15;

    private EditBox searchBox;
    private boolean draggingScrollbar;
    private Button cellSlotsTab;
    private boolean cellSlotsOpen;

    public AdvancedIndustrialStorageScreen(AdvancedIndustrialStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 193;
        imageHeight = menu.getImageHeight();
        inventoryLabelY = menu.getPlayerInventoryY() - 11;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        int available = height - 19 - BOTTOM_HEIGHT;
        int desiredRows = Math.max(AdvancedIndustrialStorageMenu.MIN_ROWS, Math.min(
                AdvancedIndustrialStorageMenu.MAX_ROWS, available / 18 - 3));
        if (desiredRows != menu.getVisibleRows()) {
            PacketDistributor.sendToServer(new ResizeStorageMenuPayload(menu.getBlockPos(), desiredRows, false));
        }
        searchBox = addRenderableWidget(new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y,
                SEARCH_WIDTH, 12, Component.translatable("gui.industrialcrops.storage.search")));
        searchBox.setBordered(false);
        searchBox.setMaxLength(64);
        searchBox.setTextColor(IndustrialGuiStyle.TEXT);
        searchBox.setTextColorUneditable(IndustrialGuiStyle.MUTED_TEXT);
        searchBox.setResponder(value -> PacketDistributor.sendToServer(new StorageSearchPayload(value)));
        cellSlotsTab = addRenderableWidget(Button.builder(Component.empty(), ignored -> {
                    cellSlotsOpen = !cellSlotsOpen;
                    menu.setCellSlotsVisible(cellSlotsOpen);
                }).bounds(leftPos - 20, topPos + 19, 20, 20)
                .build());
        menu.setCellSlotsVisible(false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawRs2StretchingBackground(
                graphics, BACKGROUND, leftPos, topPos, menu.getVisibleRows(), BOTTOM_HEIGHT);
        for (int row = 0; row < menu.getVisibleRows(); row++) {
            IndustrialGuiStyle.drawRs2GridRow(graphics, leftPos + 7, topPos + 19 + row * 18);
        }
        if (cellSlotsOpen) {
            IndustrialGuiStyle.drawCommonPanel(graphics, leftPos - 80, topPos + 19, 60, 43);
            for (int slot = 0; slot < AdvancedIndustrialStorageBlockEntity.CELL_SLOT_COUNT; slot++) {
                IndustrialGuiStyle.drawRs2Slot(graphics,
                        leftPos + AdvancedIndustrialStorageMenu.CELL_SLOTS_X - 1 + slot % 3 * 18,
                        topPos + AdvancedIndustrialStorageMenu.CELL_SLOTS_Y - 1 + slot / 3 * 18);
            }
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
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, @Nullable String countString) {
        // The virtual storage can exceed the vanilla 64-item stack limit. The
        // real count is drawn from the synchronized menu data below.
        super.renderSlotContents(graphics, stack, slot,
                slot.index < menu.getVisibleSlotCount() ? "" : countString);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        drawStorageCounts(graphics);
        if (cellSlotsTab != null) {
            graphics.renderItem(new ItemStack(ModItems.INDUSTRIAL_STORAGE_COMPONENT_1.get()),
                    cellSlotsTab.getX() + 2, cellSlotsTab.getY() + 2);
            if (cellSlotsOpen) {
                graphics.fill(cellSlotsTab.getX(), cellSlotsTab.getY(), cellSlotsTab.getX() + 20,
                        cellSlotsTab.getY() + 1, IndustrialGuiStyle.ACTIVE);
                graphics.fill(cellSlotsTab.getX(), cellSlotsTab.getY(), cellSlotsTab.getX() + 1,
                        cellSlotsTab.getY() + 20, IndustrialGuiStyle.ACTIVE);
            }
            if (cellSlotsTab.isMouseOver(mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.storage_cells"), mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawStorageCounts(GuiGraphics graphics) {
        for (int slot = 0; slot < menu.getVisibleSlotCount(); slot++) {
            int count = menu.getStoredCount(slot);
            if (count <= 1) continue;
            String text = IndustrialContainerScreen.formatCount(count);
            int x = leftPos + AdvancedIndustrialStorageMenu.STORAGE_SLOTS_X + slot % 9 * 18;
            int y = topPos + AdvancedIndustrialStorageMenu.STORAGE_SLOTS_Y + slot / 9 * 18;
            int textWidth = font.width(text);
            float scale = Math.min(1.0F, 15.0F / Math.max(1, textWidth));
            graphics.pose().pushPose();
            graphics.pose().translate(x + 16.0F, y + 16.0F, 300.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, text, -textWidth, -8, 0xFFFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    @Override
    public void removed() {
        menu.setCellSlotsVisible(false);
        super.removed();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!insideStorageArea(mouseX, mouseY) && !insideScrollbar(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        int rows = hasControlDown() ? menu.getVisibleRows() : 1;
        if (verticalAmount > 0) setOffset(menu.getScrollRow() - rows);
        else if (verticalAmount < 0) setOffset(menu.getScrollRow() + rows);
        else return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
        int positions = Math.max(1, menu.getScrollPositions());
        boolean enabled = positions > 1;
        int travel = scrollbarHeight() - SCROLLER_HEIGHT;
        int y = topPos + SCROLL_Y + (enabled ? travel * menu.getScrollRow() / (positions - 1) : 0);
        IndustrialGuiStyle.drawRs2Scrollbar(graphics, leftPos + SCROLL_X, y, draggingScrollbar, enabled);
    }

    private int scrollbarHeight() {
        return menu.getVisibleRows() * 18 - 2;
    }

    private boolean insideScrollbar(double x, double y) {
        return x >= leftPos + SCROLL_X && x < leftPos + SCROLL_X + 12
                && y >= topPos + SCROLL_Y && y < topPos + SCROLL_Y + scrollbarHeight();
    }

    private boolean insideStorageArea(double x, double y) {
        return x >= leftPos + 7 && x < leftPos + 169
                && y >= topPos + 19 && y < topPos + 19 + menu.getVisibleRows() * 18;
    }

    private void setOffsetFromMouse(double mouseY) {
        int positions = Math.max(1, menu.getScrollPositions());
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
        int target = Math.max(0, Math.min(menu.getScrollPositions() - 1, offset));
        minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, AdvancedIndustrialStorageMenu.BUTTON_SET_SCROLL_ROW_BASE + target);
    }
}
