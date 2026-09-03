package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.screen.BioEnergyMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class BioEnergyScreen extends UpgradeableMachineScreen<BioEnergyMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("bio_energy_machine");
    private static final ResourceLocation SIDE_CONFIG_ICON = IndustrialGuiStyle.containerTexture("side_configuration");
    private static final int CONFIG_PANEL_WIDTH = 60;
    private static final int CONFIG_PANEL_HEIGHT = 81;
    private final Button[] sideButtons = new Button[BioEnergyMenu.RELATIVE_SIDE_COUNT];
    private Button configTab;
    private Button allOnButton;
    private Button allOffButton;
    private boolean configPanelOpen;

    public BioEnergyScreen(BioEnergyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override protected void init() {
        super.init();
        titleLabelX = Math.max(8, (imageWidth - font.width(title)) / 2);
        if (menu.kind() != BioEnergyMachineBlockEntity.Kind.BATTERY) return;
        configTab = addRenderableWidget(Button.builder(Component.empty(), ignored -> {
            configPanelOpen = !configPanelOpen;
            refreshConfigButtons();
        }).bounds(leftPos - 20, topPos + 19, 20, 20).build());
        int panelX = leftPos - 80;
        int panelY = topPos + 19;
        for (int relativeSide = 0; relativeSide < BioEnergyMenu.RELATIVE_SIDE_COUNT; relativeSide++) {
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
        refreshConfigButtons();
    }

    @Override protected void containerTick() {
        super.containerTick();
        refreshConfigButtons();
    }

    @Override protected boolean supportsUpgradeDrawer() {
        return menu.kind() == BioEnergyMachineBlockEntity.Kind.GENERATOR;
    }

    @Override protected void renderBg(GuiGraphics graphics, float tick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawBackground(graphics, BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
        if (menu.kind() != BioEnergyMachineBlockEntity.Kind.BATTERY) {
            IndustrialGuiStyle.drawRs2Slot(graphics, leftPos + 17, topPos + 34);
        }
        IndustrialGuiStyle.drawInsetPanel(graphics, leftPos + 47, topPos + 20, 82, 42);
        if (configPanelOpen) IndustrialGuiStyle.drawCommonPanel(graphics, leftPos - 80, topPos + 19,
                CONFIG_PANEL_WIDTH, CONFIG_PANEL_HEIGHT);
        int firstMeterX = leftPos + imageWidth + 2;
        int secondMeterX = firstMeterX + IndustrialGuiStyle.VERTICAL_METER_WIDTH + 2;
        if (menu.kind() == BioEnergyMachineBlockEntity.Kind.GENERATOR) {
            IndustrialGuiStyle.drawVerticalMeter(graphics, firstMeterX, topPos + 12, 58,
                    menu.energy(), menu.energyCapacity(), IndustrialGuiStyle.ENERGY_RED, false);
            IndustrialGuiStyle.drawVerticalMeter(graphics, secondMeterX, topPos + 12, 58,
                    menu.residue(), menu.residueCapacity(), IndustrialGuiStyle.RESIDUE_BROWN, false);
            IndustrialGuiStyle.drawMekanismSmallRight(graphics, leftPos + 74, topPos + 64, menu.scaledProgress(28));
            drawUpgradeDrawer(graphics, BioEnergyMenu.UPGRADE_X, BioEnergyMenu.UPGRADE_Y);
        } else if (menu.kind() == BioEnergyMachineBlockEntity.Kind.BATTERY) {
            IndustrialGuiStyle.drawVerticalMeter(graphics, firstMeterX, topPos + 12, 58,
                    menu.energy(), menu.energyCapacity(), IndustrialGuiStyle.ENERGY_RED, false);
        } else {
            IndustrialGuiStyle.drawVerticalMeter(graphics, firstMeterX, topPos + 12, 58,
                    menu.burnTime(), menu.burnTimeTotal(), 0xFFFFFFFF, true);
            IndustrialGuiStyle.drawVerticalMeter(graphics, secondMeterX, topPos + 12, 58,
                    menu.residue(), menu.residueCapacity(), IndustrialGuiStyle.RESIDUE_BROWN, false);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, titleLabelY,
                imageWidth - 16, IndustrialGuiStyle.TEXT, true);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
        if (menu.kind() == BioEnergyMachineBlockEntity.Kind.GENERATOR) {
            IndustrialGuiStyle.drawFittedString(graphics, font,
                    Component.translatable("gui.industrialcrops.bio_generator.output", menu.currentYield()).getString(),
                    51, 28, 74, IndustrialGuiStyle.TEXT, false);
            IndustrialGuiStyle.drawFittedString(graphics, font,
                    Component.translatable("gui.industrialcrops.bio_generator.residue",
                            percent(menu.residue(), menu.residueCapacity())).getString(),
                    51, 43, 74, IndustrialGuiStyle.TEXT, false);
        } else if (menu.kind() == BioEnergyMachineBlockEntity.Kind.BATTERY) {
            Component energy = Component.translatable("gui.industrialcrops.energy", menu.energy(), menu.energyCapacity());
            IndustrialGuiStyle.drawFittedString(graphics, font, energy.getString(), 51, 34, 74,
                    IndustrialGuiStyle.TEXT, false);
        } else {
            Component burning = Component.translatable(menu.burnTime() > 0
                    ? "gui.industrialcrops.residue_incinerator.burning"
                    : "gui.industrialcrops.residue_incinerator.waiting");
            IndustrialGuiStyle.drawFittedString(graphics, font, burning.getString(), 51, 28, 74,
                    IndustrialGuiStyle.TEXT, false);
            IndustrialGuiStyle.drawFittedString(graphics, font,
                    Component.translatable("gui.industrialcrops.residue_incinerator.fuel", menu.burnTime()).getString(),
                    51, 43, 74, IndustrialGuiStyle.TEXT, false);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float tick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, tick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        if (configTab != null) {
            graphics.blit(SIDE_CONFIG_ICON, configTab.getX() + 2, configTab.getY() + 2,
                    0, 0, 16, 16, 16, 16);
            if (configPanelOpen) {
                graphics.fill(configTab.getX(), configTab.getY(), configTab.getX() + 20, configTab.getY() + 1, 0xFF62E6A7);
                graphics.fill(configTab.getX(), configTab.getY(), configTab.getX() + 1, configTab.getY() + 20, 0xFF62E6A7);
            }
        }
        drawConfigButtons(graphics);
        if (menu.kind() == BioEnergyMachineBlockEntity.Kind.INCINERATOR
                && mouseX >= leftPos + imageWidth + 2
                && mouseX < leftPos + imageWidth + 2 + IndustrialGuiStyle.VERTICAL_METER_WIDTH
                && mouseY >= topPos + 12 && mouseY < topPos + 70) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable(
                    "gui.industrialcrops.residue_incinerator.fuel", menu.burnTime())), mouseX, mouseY);
        }
        if (menu.kind() != BioEnergyMachineBlockEntity.Kind.INCINERATOR
                && mouseX >= leftPos + imageWidth + 2
                && mouseX < leftPos + imageWidth + 2 + IndustrialGuiStyle.VERTICAL_METER_WIDTH
                && mouseY >= topPos + 12 && mouseY < topPos + 70) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("gui.industrialcrops.energy",
                    menu.energy(), menu.energyCapacity())), mouseX, mouseY);
        }
        int residueBarX = leftPos + imageWidth + 4 + IndustrialGuiStyle.VERTICAL_METER_WIDTH;
        if (menu.kind() != BioEnergyMachineBlockEntity.Kind.BATTERY
                && mouseX >= residueBarX && mouseX < residueBarX + IndustrialGuiStyle.VERTICAL_METER_WIDTH
                && mouseY >= topPos + 12 && mouseY < topPos + 70) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("gui.industrialcrops.bio_generator.residue_amount",
                    menu.residue(), menu.residueCapacity())), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
        renderConfigTooltips(graphics, mouseX, mouseY);
    }

    private void refreshConfigButtons() {
        for (int side = 0; side < sideButtons.length; side++) {
            Button button = sideButtons[side];
            if (button != null) {
                button.visible = configPanelOpen;
            }
        }
        if (allOnButton != null) allOnButton.visible = configPanelOpen;
        if (allOffButton != null) allOffButton.visible = configPanelOpen;
    }

    private void drawConfigButtons(GuiGraphics graphics) {
        if (!configPanelOpen) return;
        for (int side = 0; side < sideButtons.length; side++) {
            Button button = sideButtons[side];
            if (button != null) {
                int color = switch (menu.relativeEnergySideMode(side)) {
                    case OUTPUT -> 0xFF9B2535;
                    case INPUT -> 0xFF285F9B;
                    case NONE -> 0xFF3A3D40;
                };
                drawMekanismButton(graphics, button, color);
            }
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

    private void renderConfigTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (configTab != null && configTab.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.side_config"), mouseX, mouseY);
            return;
        }
        if (!configPanelOpen) return;
        for (int side = 0; side < sideButtons.length; side++) {
            Button button = sideButtons[side];
            if (button != null && button.isMouseOver(mouseX, mouseY)) {
                String key = switch (menu.relativeEnergySideMode(side)) {
                    case OUTPUT -> "gui.industrialcrops.energy_side.output";
                    case INPUT -> "gui.industrialcrops.energy_side.input";
                    case NONE -> "gui.industrialcrops.energy_side.disabled";
                };
                graphics.renderTooltip(font, Component.translatable(key,
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

    private void toggleSide(int relativeSide) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, BioEnergyMenu.BUTTON_SIDE_BASE + relativeSide);
    }

    private void setAllSides(boolean enabled) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, enabled ? BioEnergyMenu.BUTTON_ALL_SIDES_ON : BioEnergyMenu.BUTTON_ALL_SIDES_OFF);
    }

    private static int percent(int value, int capacity) {
        return capacity <= 0 ? 0 : Math.max(0, Math.min(100, value * 100 / capacity));
    }
}
