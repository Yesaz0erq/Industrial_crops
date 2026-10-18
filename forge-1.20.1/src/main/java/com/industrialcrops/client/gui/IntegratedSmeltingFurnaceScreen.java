package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.IntegratedSmeltingFurnaceBlockEntity;
import com.industrialcrops.screen.IntegratedSmeltingFurnaceMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Chest-style furnace screen in the Incinerator's style: the window only shows the rows the storage upgrade
 * unlocks, meters and the storage upgrade sit in a side panel on the right, and a smelt-mode panel opens on the left.
 */
public final class IntegratedSmeltingFurnaceScreen extends UpgradeableMachineScreen<IntegratedSmeltingFurnaceMenu> {
    private static final String KEY = "gui.industrialcrops.integrated_smelting_furnace.";
    private static final String[] MODE_KEYS = {"mode.once", "mode.twice", "mode.full"};

    // Right side panel (relative to leftPos/topPos).
    private static final int SIDE_X = 176, SIDE_Y = 10, SIDE_W = 42, SIDE_H = 96;
    private static final int METER_Y = 12, METER_H = 58;
    private static final int ENERGY_X = SIDE_X + 2, HEAT_X = SIDE_X + 22;

    // Smelt-mode tab and panel on the left.
    private static final int MODE_TAB_Y = 42;
    private static final int MODE_PANEL_X = -86, MODE_PANEL_Y = 66, MODE_PANEL_W = 66, MODE_PANEL_H = 76;
    private static final int MODE_BUTTON_W = 58, MODE_BUTTON_H = 16;

    /** Remembered across the reopen that follows a storage-upgrade change. */
    private static boolean modePanelOpen;

    private final Button[] modeButtons = new Button[3];
    private Button modeTab;

    public IntegratedSmeltingFurnaceScreen(IntegratedSmeltingFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = IntegratedSmeltingFurnaceMenu.imageHeight(menu.rows());
        inventoryLabelY = IntegratedSmeltingFurnaceMenu.inventoryY(menu.rows()) - 11;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        modeTab = addRenderableWidget(Button.builder(Component.empty(), ignored -> {
            modePanelOpen = !modePanelOpen;
            refreshModeButtons();
        }).bounds(leftPos - 20, topPos + MODE_TAB_Y, 20, 20).build());
        for (int mode = 0; mode < modeButtons.length; mode++) {
            final int id = mode;
            modeButtons[mode] = addRenderableWidget(Button.builder(Component.empty(), ignored -> selectMode(id))
                    .bounds(leftPos + MODE_PANEL_X + 4, topPos + MODE_PANEL_Y + 18 + mode * 18, MODE_BUTTON_W, MODE_BUTTON_H)
                    .build());
            modeButtons[mode].setAlpha(0.0F);
        }
        refreshModeButtons();
    }

    private void refreshModeButtons() {
        for (Button button : modeButtons) {
            if (button != null) button.visible = modePanelOpen;
        }
    }

    private void selectMode(int mode) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, mode);
        }
    }

    // ------------------------------------------------------------------ background

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos, rows = menu.rows();
        IndustrialGuiStyle.drawContainer(g, x, y, imageWidth, imageHeight);

        // Storage grid: only unlocked rows exist in this window.
        int gridX = x + IntegratedSmeltingFurnaceMenu.GRID_X, gridY = y + IntegratedSmeltingFurnaceMenu.GRID_Y;
        IndustrialGuiStyle.drawWorkPanel(g, gridX - 3, gridY - 3, 166, rows * 18 + 5);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < IntegratedSmeltingFurnaceBlockEntity.COLUMNS; column++) {
                int sx = gridX - 1 + column * 18, sy = gridY - 1 + row * 18;
                IndustrialGuiStyle.drawSlot(g, sx, sy);
                int index = row * IntegratedSmeltingFurnaceBlockEntity.COLUMNS + column;
                if (menu.furnace().isFinished(index) && menu.getSlot(index).hasItem()) {
                    g.fill(sx + 2, sy + 16, sx + 16, sy + 17, 0xFFFFB347);
                }
            }
        }
        IndustrialGuiStyle.drawPlayerInventory(g, x, y, 8,
                IntegratedSmeltingFurnaceMenu.inventoryY(rows), IntegratedSmeltingFurnaceMenu.hotbarY(rows));

        // Right side panel: energy meter, heat (progress) meter, storage upgrade well.
        IndustrialGuiStyle.drawCommonPanel(g, x + SIDE_X, y + SIDE_Y, SIDE_W, SIDE_H);
        IndustrialGuiStyle.drawVerticalMeter(g, x + ENERGY_X, y + METER_Y, METER_H,
                menu.energy(), IntegratedSmeltingFurnaceBlockEntity.ENERGY_CAPACITY, IndustrialGuiStyle.ENERGY_RED, false);
        boolean working = menu.lanes() > 0 && menu.progress() > 0;
        IndustrialGuiStyle.drawVerticalMeter(g, x + HEAT_X, y + METER_Y, METER_H,
                working ? menu.progress() : 0, IntegratedSmeltingFurnaceBlockEntity.PROCESS_TICKS, 0xFFFFFFFF, true);
        IndustrialGuiStyle.drawMachineWell(g, x + IntegratedSmeltingFurnaceMenu.STORAGE_UPGRADE_X - 1,
                y + IntegratedSmeltingFurnaceMenu.STORAGE_UPGRADE_Y - 1);

        drawUpgradeDrawer(g, IntegratedSmeltingFurnaceMenu.UPGRADE_X, IntegratedSmeltingFurnaceMenu.UPGRADE_Y);
        if (modePanelOpen) drawModePanel(g, mouseX, mouseY);
    }

    private void drawModePanel(GuiGraphics g, int mouseX, int mouseY) {
        int px = leftPos + MODE_PANEL_X, py = topPos + MODE_PANEL_Y;
        IndustrialGuiStyle.drawCommonPanel(g, px, py, MODE_PANEL_W, MODE_PANEL_H);
        IndustrialGuiStyle.drawFittedString(g, font, Component.translatable(KEY + "mode").getString(),
                px + 5, py + 6, MODE_PANEL_W - 10, IndustrialGuiStyle.TEXT, true);
        g.fill(px + 8, py + 15, px + MODE_PANEL_W - 8, py + 16, IndustrialGuiStyle.ACTIVE);
        for (int mode = 0; mode < modeButtons.length; mode++) {
            Button button = modeButtons[mode];
            int bx = button.getX(), by = button.getY();
            boolean selected = menu.mode() == mode;
            boolean hovered = button.isMouseOver(mouseX, mouseY);
            IndustrialGuiStyle.drawSmallButton(g, bx, by, MODE_BUTTON_W, MODE_BUTTON_H, hovered);
            if (selected) {
                g.fill(bx + 1, by + 1, bx + MODE_BUTTON_W - 1, by + MODE_BUTTON_H - 1, 0xFF4E7A52);
                g.fill(bx + 1, by + MODE_BUTTON_H - 2, bx + MODE_BUTTON_W - 1, by + MODE_BUTTON_H - 1, 0xFF62E6A7);
            }
            drawModeIcon(g, bx + 3, by + 4, mode, selected);
            IndustrialGuiStyle.drawFittedString(g, font, Component.translatable(KEY + MODE_KEYS[mode]).getString(),
                    bx + 15, by + 4, MODE_BUTTON_W - 18, selected ? 0xFFFFFFFF : IndustrialGuiStyle.TEXT, false);
        }
    }

    /** Pips for once / twice and an arrow loop for "to the end". */
    private static void drawModeIcon(GuiGraphics g, int x, int y, int mode, boolean selected) {
        int color = selected ? 0xFFFFD36A : 0xFFB0461F;
        if (mode == IntegratedSmeltingFurnaceBlockEntity.MODE_FULL) {
            g.fill(x, y + 1, x + 9, y + 2, color);
            g.fill(x, y + 6, x + 9, y + 7, color);
            g.fill(x, y + 1, x + 1, y + 7, color);
            g.fill(x + 8, y + 1, x + 9, y + 7, color);
            g.fill(x + 6, y, x + 7, y + 3, color);
            return;
        }
        for (int pip = 0; pip <= mode; pip++) {
            int px = x + pip * 5;
            g.fill(px, y + 1, px + 3, y + 7, color);
            g.fill(px + 1, y, px + 2, y + 1, color);
        }
    }

    // ------------------------------------------------------------------ labels & tooltips

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, titleLabelY,
                imageWidth - 16, IndustrialGuiStyle.TEXT, true);
        IndustrialGuiStyle.drawFittedString(graphics, font, playerInventoryTitle.getString(), 8, inventoryLabelY,
                imageWidth - 16, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        renderModeTab(graphics);

        if (inside(mouseX, mouseY, leftPos + ENERGY_X, topPos + METER_Y, IndustrialGuiStyle.VERTICAL_METER_WIDTH, METER_H)) {
            graphics.renderTooltip(font, Component.translatable(KEY + "energy",
                    String.format("%,d", menu.energy()), String.format("%,d", IntegratedSmeltingFurnaceBlockEntity.ENERGY_CAPACITY)),
                    mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + HEAT_X, topPos + METER_Y, IndustrialGuiStyle.VERTICAL_METER_WIDTH, METER_H)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable(KEY + "lanes", menu.lanes(), IntegratedSmeltingFurnaceBlockEntity.LANES),
                    Component.translatable(KEY + "progress",
                            menu.progress() * 100 / IntegratedSmeltingFurnaceBlockEntity.PROCESS_TICKS).withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + IntegratedSmeltingFurnaceMenu.STORAGE_UPGRADE_X - 1,
                topPos + IntegratedSmeltingFurnaceMenu.STORAGE_UPGRADE_Y - 1, 18, 18)
                && !menu.getSlot(IntegratedSmeltingFurnaceBlockEntity.MAX_SLOTS + IntegratedSmeltingFurnaceBlockEntity.SPEED_SLOTS).hasItem()) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable(KEY + "storage_slot").withStyle(ChatFormatting.GOLD),
                    Component.translatable(KEY + "storage_now", menu.activeRows(), menu.slotLimit()).withStyle(ChatFormatting.GRAY),
                    Component.translatable(KEY + "storage_tiers").withStyle(ChatFormatting.DARK_GRAY)
            ), mouseX, mouseY);
        } else if (modeTab != null && modeTab.isMouseOver(mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable(KEY + "mode"),
                    Component.translatable(KEY + MODE_KEYS[Math.max(0, Math.min(2, menu.mode()))]).withStyle(ChatFormatting.GOLD)
            ), mouseX, mouseY);
        } else if (modePanelOpen && hoveredMode(mouseX, mouseY) >= 0) {
            int mode = hoveredMode(mouseX, mouseY);
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable(KEY + MODE_KEYS[mode]).withStyle(ChatFormatting.GOLD),
                    Component.translatable(KEY + MODE_KEYS[mode] + ".desc").withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    private void renderModeTab(GuiGraphics graphics) {
        if (modeTab == null) return;
        graphics.renderItem(new ItemStack(Items.FURNACE), modeTab.getX() + 2, modeTab.getY() + 2);
        if (modePanelOpen) {
            graphics.fill(modeTab.getX(), modeTab.getY(), modeTab.getX() + 20, modeTab.getY() + 1, IndustrialGuiStyle.ACTIVE);
            graphics.fill(modeTab.getX(), modeTab.getY(), modeTab.getX() + 1, modeTab.getY() + 20, IndustrialGuiStyle.ACTIVE);
        }
    }

    private int hoveredMode(int mouseX, int mouseY) {
        for (int mode = 0; mode < modeButtons.length; mode++) {
            if (modeButtons[mode] != null && modeButtons[mode].isMouseOver(mouseX, mouseY)) return mode;
        }
        return -1;
    }

    // ------------------------------------------------------------------ side panels count as part of the window

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        for (Rect2i area : extraAreas()) {
            if (mouseX >= area.getX() && mouseX < area.getX() + area.getWidth()
                    && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight()) return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    /** Screen areas outside the main window (side panel, tabs, open drawers) — also reported to JEI. */
    public List<Rect2i> extraAreas() {
        List<Rect2i> areas = new ArrayList<>();
        areas.add(new Rect2i(leftPos + SIDE_X, topPos + SIDE_Y, SIDE_W, SIDE_H));
        areas.add(new Rect2i(leftPos - 62, topPos + 20, 62, 44));
        if (modePanelOpen) areas.add(new Rect2i(leftPos + MODE_PANEL_X, topPos + MODE_PANEL_Y, MODE_PANEL_W, MODE_PANEL_H));
        return areas;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
