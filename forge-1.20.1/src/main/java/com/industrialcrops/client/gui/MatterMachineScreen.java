package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.ItemNetworkTerminalBlockEntity;
import com.industrialcrops.block.entity.MatterMachineBlockEntity;
import com.industrialcrops.network.payload.MatterMachineSelectionPayload;
import com.industrialcrops.network.payload.StorageSearchPayload;
import com.industrialcrops.screen.MatterMachineMenu;
import com.industrialcrops.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import com.industrialcrops.network.ModNetworking;

public final class MatterMachineScreen extends IndustrialContainerScreen<MatterMachineMenu> {
    private static final ResourceLocation SIDE_CONFIG_ICON = IndustrialGuiStyle.containerTexture("side_configuration");
    private static final ResourceLocation MACHINE_BACKGROUND = IndustrialGuiStyle.containerTexture("matter_terminal_machine");
    private static final int CONFIG_PANEL_WIDTH = 60;
    private static final int CONFIG_PANEL_HEIGHT = 81;
    private static final int SEARCH_X = 95;
    private static final int SEARCH_Y = 7;
    private static final int SEARCH_WIDTH = 67;
    private static final int SCROLL_X = 174;
    private static final int SCROLL_Y = 20;
    private static final int SCROLLER_HEIGHT = 15;
    private final Button[] sideButtons = new Button[MatterMachineMenu.RELATIVE_SIDE_COUNT];
    private Button configTab;
    private Button upgradeTab;
    private Button allOnButton;
    private Button allOffButton;
    private Button operateButton;
    private EditBox searchBox;
    private boolean configPanelOpen;
    private boolean upgradePanelOpen;
    private boolean draggingScrollbar;
    private int clientSelected = -1;

    public MatterMachineScreen(MatterMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        boolean digitizer = menu.kind() == MatterMachineBlockEntity.Kind.DIGITIZER;
        imageWidth = digitizer ? 176 : 193;
        imageHeight = 190;
        inventoryLabelY = 99;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        MatterMachineBlockEntity.Kind kind = menu.kind();
        boolean digitizer = kind == MatterMachineBlockEntity.Kind.DIGITIZER;
        Component action = Component.translatable(digitizer
                ? "gui.industrialcrops.matter_machine.upload"
                : kind == MatterMachineBlockEntity.Kind.COPIER
                    ? "gui.industrialcrops.network.copy"
                    : "gui.industrialcrops.network.reconstruct");
        int buttonX = kind == MatterMachineBlockEntity.Kind.RECONSTRUCTOR ? 8 : 111;
        operateButton = addRenderableWidget(Button.builder(action, button -> operate())
                .bounds(leftPos + buttonX, topPos + 79, 56, 18).build());

        if (!digitizer) {
            searchBox = addRenderableWidget(new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y,
                    SEARCH_WIDTH, 12, Component.translatable("gui.industrialcrops.storage.search")));
            searchBox.setBordered(false);
            searchBox.setMaxLength(64);
            searchBox.setTextColor(IndustrialGuiStyle.TEXT);
            searchBox.setTextColorUneditable(IndustrialGuiStyle.MUTED_TEXT);
            searchBox.setResponder(value -> ModNetworking.sendToServer(new StorageSearchPayload(value)));
        }

        configTab = addRenderableWidget(Button.builder(Component.empty(), ignored -> {
                    configPanelOpen = !configPanelOpen;
                    upgradePanelOpen = false;
                    refreshButtons();
                }).bounds(leftPos - 20, topPos + 19, 20, 20).build());
        upgradeTab = addRenderableWidget(Button.builder(Component.empty(), ignored -> {
                    upgradePanelOpen = !upgradePanelOpen;
                    configPanelOpen = false;
                    refreshButtons();
                }).bounds(leftPos - 20, topPos + 41, 20, 20).build());
        int panelX = leftPos - 80;
        int panelY = topPos + 19;
        for (int relativeSide = 0; relativeSide < MatterMachineMenu.RELATIVE_SIDE_COUNT; relativeSide++) {
            int position = switch (relativeSide) {
                case 0 -> 1;
                case 1 -> 7;
                case 2 -> 4;
                case 3 -> 6;
                case 4 -> 3;
                case 5 -> 5;
                default -> 0;
            };
            final int side = relativeSide;
            sideButtons[side] = addRenderableWidget(Button.builder(Component.empty(), ignored -> toggleSide(side))
                    .bounds(panelX + 3 + position % 3 * 18, panelY + 3 + position / 3 * 18, 18, 18).build());
            sideButtons[side].setAlpha(0.0F);
        }
        allOnButton = addRenderableWidget(Button.builder(Component.empty(),
                ignored -> setAllSides(true)).bounds(panelX + 3, panelY + 58, 27, 18).build());
        allOffButton = addRenderableWidget(Button.builder(Component.empty(),
                ignored -> setAllSides(false)).bounds(panelX + 30, panelY + 58, 27, 18).build());
        allOnButton.setAlpha(0.0F);
        allOffButton.setAlpha(0.0F);
        refreshButtons();
    }

    @Override protected void containerTick() { super.containerTick(); refreshButtons(); }

    private void refreshButtons() {
        if (operateButton != null) {
            if (menu.kind() == MatterMachineBlockEntity.Kind.DIGITIZER) {
                operateButton.active = menu.canStartOperation() && !menu.isOperating();
            } else {
                int selected = menu.selectedVisible() >= 0 ? menu.selectedVisible() : clientSelected;
                boolean validBatch = menu.kind() != MatterMachineBlockEntity.Kind.RECONSTRUCTOR
                        || selected >= 0 && menu.count(selected) > 0;
                // Selection and operation are validated atomically by the server. Keeping the button
                // active here prevents one-tick menu-data latency from swallowing the user's click.
                operateButton.active = (menu.kind() == MatterMachineBlockEntity.Kind.COPIER
                        && menu.isAutomaticCopying())
                        || (selected >= 0 && menu.count(selected) > 0
                        && validBatch && menu.hasConnectedTerminal() && !menu.isOperating());
            }
        }
        for (int side = 0; side < sideButtons.length; side++) {
            Button button = sideButtons[side];
            if (button != null) button.visible = configPanelOpen;
        }
        if (allOnButton != null) allOnButton.visible = configPanelOpen;
        if (allOffButton != null) allOffButton.visible = configPanelOpen;
        menu.setUpgradeSlotsVisible(upgradePanelOpen);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float tick, int mouseX, int mouseY) {
        boolean digitizer = menu.kind() == MatterMachineBlockEntity.Kind.DIGITIZER;
        if (digitizer) {
            IndustrialGuiStyle.drawBackground(graphics, IndustrialGuiStyle.containerTexture("matter_digitization_device"),
                    leftPos, topPos, imageWidth, imageHeight);
        } else {
            drawTerminalMachineBackground(graphics);
            for (int row = 0; row < MatterMachineMenu.TERMINAL_ROWS; row++) {
                IndustrialGuiStyle.drawRs2GridRow(graphics, leftPos + 7, topPos + 19 + row * 18);
            }
            IndustrialGuiStyle.drawRs2SearchIcon(graphics, leftPos + 79, topPos + 5);
            drawScrollbar(graphics);
            if (menu.kind() == MatterMachineBlockEntity.Kind.RECONSTRUCTOR) {
                IndustrialGuiStyle.drawRs2Slot(graphics, leftPos + 115, topPos + 81);
            }
        }
        drawVerticalEnergyBar(graphics);
        int width = menu.maxProgress() <= 0 ? 0 : Math.min(28, 28 * menu.progress() / menu.maxProgress());
        IndustrialGuiStyle.drawMekanismSmallRight(graphics, leftPos + 76, topPos + 84, width);
        if (configPanelOpen) IndustrialGuiStyle.drawCommonPanel(graphics, leftPos - 80, topPos + 19,
                CONFIG_PANEL_WIDTH, CONFIG_PANEL_HEIGHT);
        if (upgradePanelOpen) {
            IndustrialGuiStyle.drawCommonPanel(graphics, leftPos - 80, topPos + 41, 60, 49);
            for (int index = 0; index < MatterMachineBlockEntity.UPGRADE_SLOT_COUNT; index++) {
                IndustrialGuiStyle.drawSlot(graphics,
                        leftPos + MatterMachineMenu.UPGRADE_SLOT_X - 1 + index % 2 * 22,
                        topPos + MatterMachineMenu.UPGRADE_SLOT_Y - 1 + index / 2 * 22);
            }
        }
    }

    private void drawTerminalMachineBackground(GuiGraphics graphics) {
        IndustrialGuiStyle.drawBackground(graphics, MACHINE_BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
    }

    private void drawVerticalEnergyBar(GuiGraphics graphics) {
        int x = leftPos + imageWidth + 2;
        int y = topPos + 18;
        IndustrialGuiStyle.drawVerticalMeter(graphics, x, y, 58, menu.energy(),
                MatterMachineBlockEntity.ENERGY_CAPACITY, IndustrialGuiStyle.ENERGY_RED, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.kind() == MatterMachineBlockEntity.Kind.DIGITIZER) {
            IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, titleLabelY,
                    imageWidth - 16, IndustrialGuiStyle.TEXT, true);
        } else {
            IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, 7,
                    70, IndustrialGuiStyle.TEXT, false);
        }
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float tick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, tick);
        if (menu.kind() != MatterMachineBlockEntity.Kind.DIGITIZER) {
            drawItemCounts(graphics);
            int selected = menu.selectedVisible() >= 0 ? menu.selectedVisible() : clientSelected;
            if (selected >= 0) {
                int index = selected;
                drawSelection(graphics, leftPos + 8 + index % 9 * 18, topPos + 20 + index / 9 * 18);
            }
        }
        if (configTab != null) {
            graphics.blit(SIDE_CONFIG_ICON, configTab.getX() + 2, configTab.getY() + 2,
                    0, 0, 16, 16, 16, 16);
            if (configPanelOpen) {
                graphics.fill(configTab.getX(), configTab.getY(), configTab.getX() + 20, configTab.getY() + 1, 0xFF62E6A7);
                graphics.fill(configTab.getX(), configTab.getY(), configTab.getX() + 1, configTab.getY() + 20, 0xFF62E6A7);
            }
        }
        drawConfigButtons(graphics);
        if (upgradeTab != null) {
            graphics.renderItem(new ItemStack(ModItems.SPEED_COMPONENT_1.get()), upgradeTab.getX() + 2, upgradeTab.getY() + 2);
            if (upgradePanelOpen) {
                graphics.fill(upgradeTab.getX(), upgradeTab.getY(), upgradeTab.getX() + 20, upgradeTab.getY() + 1, 0xFFB85A30);
                graphics.fill(upgradeTab.getX(), upgradeTab.getY(), upgradeTab.getX() + 1, upgradeTab.getY() + 20, 0xFFB85A30);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
        renderCustomTooltips(graphics, mouseX, mouseY);
    }

    private void drawItemCounts(GuiGraphics graphics) {
        for (int index = 0; index < MatterMachineMenu.TERMINAL_SLOTS; index++) {
            int count = menu.count(index);
            if (count <= 0) continue;
            String text = IndustrialContainerScreen.formatCount(count);
            int x = leftPos + 8 + index % 9 * 18;
            int y = topPos + 20 + index / 9 * 18;
            int textWidth = font.width(text);
            float scale = Math.min(1.0F, 15.0F / Math.max(1, textWidth));
            graphics.pose().pushPose();
            graphics.pose().translate(x + 16.0F, y + 16.0F, 300.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, text, -textWidth, -8, 0xFFFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    private void drawConfigButtons(GuiGraphics graphics) {
        if (!configPanelOpen) return;
        for (int side = 0; side < sideButtons.length; side++) {
            Button button = sideButtons[side];
            if (button != null) drawMekanismButton(graphics, button,
                    menu.isRelativeEnergySideEnabled(side) ? 0xFF287F76 : 0xFF363A3E);
        }
        if (allOnButton != null) drawMekanismButton(graphics, allOnButton, 0xFF238E4C);
        if (allOffButton != null) drawMekanismButton(graphics, allOffButton, 0xFFA52330);
    }

    private static void drawMekanismButton(GuiGraphics graphics, Button button, int color) {
        int x = button.getX();
        int y = button.getY();
        int right = x + button.getWidth();
        int bottom = y + button.getHeight();
        graphics.fill(x, y, right, bottom, 0xFF101214);
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFF25292D);
        graphics.fill(x + 2, y + 2, right - 2, bottom - 2, color);
        graphics.fill(x + 2, y + 2, right - 2, y + 3, adjustColor(color, 28));
        graphics.fill(x + 2, y + 3, x + 3, bottom - 2, adjustColor(color, 18));
        graphics.fill(x + 2, bottom - 3, right - 2, bottom - 2, adjustColor(color, -32));
        graphics.fill(right - 3, y + 3, right - 2, bottom - 2, adjustColor(color, -32));
        if (button.isHovered()) {
            graphics.fill(x, y, right, y + 1, 0xFFA8DED9);
            graphics.fill(x, y, x + 1, bottom, 0xFFA8DED9);
        }
    }

    private static int adjustColor(int color, int amount) {
        int r = Math.max(0, Math.min(255, (color >> 16 & 255) + amount));
        int g = Math.max(0, Math.min(255, (color >> 8 & 255) + amount));
        int b = Math.max(0, Math.min(255, (color & 255) + amount));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private String worldSideKey(int relativeSide) {
        return "gui.industrialcrops.world_side." + menu.worldDirectionForRelative(relativeSide).getName();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        if (menu.kind() == MatterMachineBlockEntity.Kind.DIGITIZER
                || (!insideStorageArea(mouseX, mouseY) && !insideScrollbar(mouseX, mouseY))) {
            return super.mouseScrolled(mouseX, mouseY, verticalAmount);
        }
        if (verticalAmount > 0) setOffset(menu.page() - 1);
        else if (verticalAmount < 0) setOffset(menu.page() + 1);
        else return super.mouseScrolled(mouseX, mouseY, verticalAmount);
        return true;
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.kind() != MatterMachineBlockEntity.Kind.DIGITIZER && button == 0 && insideScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true; setOffsetFromMouse(mouseY); return true;
        }
        if (menu.kind() != MatterMachineBlockEntity.Kind.DIGITIZER && button == 0 && insideStorageArea(mouseX, mouseY)) {
            int column = (int) (mouseX - (leftPos + 8)) / 18;
            int row = (int) (mouseY - (topPos + 20)) / 18;
            if (column >= 0 && column < 9 && row >= 0 && row < MatterMachineMenu.TERMINAL_ROWS) {
                selectVisibleSlot(row * 9 + column);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar && button == 0) { setOffsetFromMouse(mouseY); return true; }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = draggingScrollbar; draggingScrollbar = false;
        return handled || super.mouseReleased(mouseX, mouseY, button);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int positions = Math.max(1, menu.totalPages());
        boolean enabled = positions > 1;
        int travel = 52 - SCROLLER_HEIGHT;
        int y = topPos + SCROLL_Y + (enabled ? travel * menu.page() / (positions - 1) : 0);
        IndustrialGuiStyle.drawRs2Scrollbar(graphics, leftPos + SCROLL_X, y, draggingScrollbar, enabled);
    }
    private boolean insideScrollbar(double mouseX, double mouseY) {
        return mouseX >= leftPos + SCROLL_X && mouseX < leftPos + SCROLL_X + 12
                && mouseY >= topPos + SCROLL_Y && mouseY < topPos + SCROLL_Y + 52;
    }
    private boolean insideStorageArea(double mouseX, double mouseY) {
        return mouseX >= leftPos + 7 && mouseX < leftPos + 169
                && mouseY >= topPos + 19 && mouseY < topPos + 73;
    }
    private void setOffsetFromMouse(double mouseY) {
        int positions = Math.max(1, menu.totalPages());
        int travel = 52 - SCROLLER_HEIGHT;
        if (positions <= 1) { setOffset(0); return; }
        double fraction = (mouseY - SCROLLER_HEIGHT / 2.0 - (topPos + SCROLL_Y)) / travel;
        setOffset((int) Math.floor(Math.max(0, Math.min(1, fraction)) * (positions - 1)));
    }
    private void setOffset(int offset) {
        if (minecraft == null || minecraft.gameMode == null) return;
        int target = Math.max(0, Math.min(menu.totalPages() - 1, offset));
        clientSelected = -1;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MatterMachineMenu.BUTTON_SET_PAGE_BASE + target);
    }

    private void selectVisibleSlot(int index) {
        if (menu.count(index) <= 0) return;
        clientSelected = index;
        ModNetworking.sendToServer(new MatterMachineSelectionPayload(menu.containerId, index, false));
        refreshButtons();
    }

    private static void drawSelection(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 16, y + 1, 0x80FFFFFF);
        graphics.fill(x, y + 15, x + 16, y + 16, 0x80FFFFFF);
        graphics.fill(x, y, x + 1, y + 16, 0x80FFFFFF);
        graphics.fill(x + 15, y, x + 16, y + 16, 0x80FFFFFF);
    }

    private void renderCustomTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int energyX = leftPos + imageWidth + 2;
        if (mouseX >= energyX && mouseX < energyX + IndustrialGuiStyle.VERTICAL_METER_WIDTH
                && mouseY >= topPos + 18 && mouseY < topPos + 76) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.energy", menu.energy(),
                    MatterMachineBlockEntity.ENERGY_CAPACITY), mouseX, mouseY);
            return;
        }
        if (configTab != null && configTab.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.side_config"), mouseX, mouseY);
            return;
        }
        if (upgradeTab != null && upgradeTab.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.upgrade_slots"), mouseX, mouseY);
            return;
        }
        if (!configPanelOpen) return;
        for (int side = 0; side < sideButtons.length; side++) {
            Button button = sideButtons[side];
            if (button != null && button.isMouseOver(mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.translatable(menu.isRelativeEnergySideEnabled(side)
                                ? "gui.industrialcrops.energy_side.input" : "gui.industrialcrops.energy_side.disabled",
                        Component.translatable(worldSideKey(side))), mouseX, mouseY);
                return;
            }
        }
        if (allOnButton != null && allOnButton.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.side_config.all_on"), mouseX, mouseY);
        } else if (allOffButton != null && allOffButton.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.side_config.all_off"), mouseX, mouseY);
        }
    }

    private void operate() {
        if (menu.kind() != MatterMachineBlockEntity.Kind.DIGITIZER) {
            int selected = menu.selectedVisible() >= 0 ? menu.selectedVisible() : clientSelected;
            if (selected >= 0) {
                ModNetworking.sendToServer(new MatterMachineSelectionPayload(menu.containerId, selected, true));
            }
        } else if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MatterMachineMenu.BUTTON_OPERATE);
        }
    }
    private void toggleSide(int relativeSide) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, MatterMachineMenu.BUTTON_SIDE_BASE + relativeSide);
    }
    private void setAllSides(boolean enabled) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, enabled ? MatterMachineMenu.BUTTON_ALL_SIDES_ON : MatterMachineMenu.BUTTON_ALL_SIDES_OFF);
    }
}
