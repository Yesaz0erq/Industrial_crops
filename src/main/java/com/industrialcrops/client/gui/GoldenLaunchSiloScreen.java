package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.GoldenLaunchSiloBlockEntity;
import com.industrialcrops.network.payload.GoldenSiloCommandPayload;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.GoldenLaunchSiloMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import com.industrialcrops.network.ModNetworking;

/** Coordinate entry and four-slot upgrade screen for the Golden Launch Silo. */
public final class GoldenLaunchSiloScreen extends IndustrialContainerScreen<GoldenLaunchSiloMenu> {

    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private Button modeButton;
    private Button ignoreYButton;
    private Button upgradeTab;
    private boolean relativeMode;
    private boolean ignoreYMode;
    private boolean fieldsInitializedFromMenu;
    private boolean upgradePanelOpen;

    public GoldenLaunchSiloScreen(GoldenLaunchSiloMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 202;
        inventoryLabelY = 110;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;

        xField = addRenderableWidget(coordinateField(55, 22, menu.getTargetX()));
        yField = addRenderableWidget(coordinateField(55, 42, menu.getTargetY()));
        zField = addRenderableWidget(coordinateField(55, 62, menu.getTargetZ()));
        relativeMode = menu.isRelativeMode();
        ignoreYMode = menu.isIgnoreYMode();
        yField.setEditable(!ignoreYMode);
        fieldsInitializedFromMenu = false;

        modeButton = addRenderableWidget(Button.builder(modeButtonText(), button -> toggleCoordinateMode())
                .bounds(leftPos + 112, topPos + 20, 54, 18)
                .build());
        ignoreYButton = addRenderableWidget(Button.builder(ignoreYButtonText(), button -> toggleIgnoreYMode())
                .bounds(leftPos + 112, topPos + 83, 54, 18)
                .build());
        ignoreYButton.setTooltip(Tooltip.create(Component.translatable("gui.industrialcrops.golden_silo.mode.ignore_y.tooltip")));
        addRenderableWidget(Button.builder(Component.translatable("gui.industrialcrops.golden_silo.set_target"), button -> sendCommand(false))
                .bounds(leftPos + 112, topPos + 41, 54, 18)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.industrialcrops.golden_silo.launch"), button -> sendCommand(true))
                .bounds(leftPos + 112, topPos + 62, 54, 20)
                .build());
        upgradeTab = addRenderableWidget(Button.builder(Component.empty(), button -> {
                    upgradePanelOpen = !upgradePanelOpen;
                    menu.setUpgradeSlotsVisible(upgradePanelOpen);
                }).bounds(leftPos - 20, topPos + 20, 20, 20)
                .build());
        menu.setUpgradeSlotsVisible(false);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!fieldsInitializedFromMenu && menu.hasTarget()) {
            xField.setValue(Integer.toString(menu.getTargetX()));
            yField.setValue(Integer.toString(menu.getTargetY()));
            zField.setValue(Integer.toString(menu.getTargetZ()));
            relativeMode = menu.isRelativeMode();
            ignoreYMode = menu.isIgnoreYMode();
            yField.setEditable(!ignoreYMode);
            modeButton.setMessage(modeButtonText());
            ignoreYButton.setMessage(ignoreYButtonText());
            fieldsInitializedFromMenu = true;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawContainer(graphics, leftPos, topPos, imageWidth, imageHeight);
        IndustrialGuiStyle.drawWorkPanel(graphics, leftPos + 40, topPos + 21, 66, 58);
        IndustrialGuiStyle.drawWorkPanel(graphics, leftPos + 109, topPos + 20, 59, 81);
        IndustrialGuiStyle.drawMachineWell(graphics, leftPos + 17, topPos + 35);
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8,
                GoldenLaunchSiloMenu.PLAYER_INVENTORY_Y, GoldenLaunchSiloMenu.PLAYER_HOTBAR_Y);
        if (upgradePanelOpen) {
            IndustrialGuiStyle.drawCommonPanel(graphics, leftPos - 80, topPos + 20, 60, 52);
            for (int index = 0; index < GoldenLaunchSiloBlockEntity.UPGRADE_SLOT_COUNT; index++) {
                IndustrialGuiStyle.drawSlot(graphics,
                        leftPos + GoldenLaunchSiloMenu.UPGRADE_X - 1 + index % 2 * GoldenLaunchSiloMenu.UPGRADE_SPACING,
                        topPos + GoldenLaunchSiloMenu.UPGRADE_Y - 1 + index / 2 * GoldenLaunchSiloMenu.UPGRADE_SPACING);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, 6, 160,
                IndustrialGuiStyle.TEXT, true);
        graphics.drawString(font, Component.literal("X"), 45, 26, IndustrialGuiStyle.TEXT, false);
        graphics.drawString(font, Component.literal("Y"), 45, 46, IndustrialGuiStyle.TEXT, false);
        graphics.drawString(font, Component.literal("Z"), 45, 66, IndustrialGuiStyle.TEXT, false);
        graphics.drawString(
                font,
                Component.literal("x" + (menu.getPowerUpgradeCount() == 0 ? 1 : menu.getPowerUpgradeCount() * 4)),
                78,
                89,
                IndustrialGuiStyle.TEXT,
                false
        );
        Component status = statusComponent();
        IndustrialGuiStyle.drawFittedString(graphics, font, status.getString(), 8, 101, 160,
                menu.getStatus() == GoldenLaunchSiloBlockEntity.STATUS_READY
                        ? IndustrialGuiStyle.TEXT : IndustrialGuiStyle.WARNING, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (upgradeTab != null) {
            graphics.renderItem(new ItemStack(ModItems.SPEED_COMPONENT_1.get()), upgradeTab.getX() + 2, upgradeTab.getY() + 2);
            if (upgradePanelOpen) {
                graphics.fill(upgradeTab.getX(), upgradeTab.getY(), upgradeTab.getX() + 20, upgradeTab.getY() + 1,
                        IndustrialGuiStyle.ACTIVE);
                graphics.fill(upgradeTab.getX(), upgradeTab.getY(), upgradeTab.getX() + 1, upgradeTab.getY() + 20,
                        IndustrialGuiStyle.ACTIVE);
            }
            if (upgradeTab.isMouseOver(mouseX, mouseY)) {
                graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.upgrade_slots"), mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void removed() {
        menu.setUpgradeSlotsVisible(false);
        super.removed();
    }

    private EditBox coordinateField(int x, int y, int value) {
        EditBox field = new EditBox(font, leftPos + x, topPos + y, 50, 15, Component.empty());
        field.setValue(Integer.toString(value));
        field.setMaxLength(11);
        return field;
    }

    private void toggleCoordinateMode() {
        Integer currentX = parseCoordinate(xField.getValue());
        Integer currentY = parseCoordinate(yField.getValue());
        Integer currentZ = parseCoordinate(zField.getValue());
        boolean newRelativeMode = !relativeMode;
        boolean convertedExistingTarget = false;
        if (currentX != null && currentY != null && currentZ != null && menu.hasTarget()) {
            long convertedX = newRelativeMode ? currentX - (long) menu.getSiloPos().getX() : currentX + (long) menu.getSiloPos().getX();
            long convertedY = newRelativeMode ? currentY - (long) menu.getSiloPos().getY() : currentY + (long) menu.getSiloPos().getY();
            long convertedZ = newRelativeMode ? currentZ - (long) menu.getSiloPos().getZ() : currentZ + (long) menu.getSiloPos().getZ();
            if (convertedX >= Integer.MIN_VALUE && convertedX <= Integer.MAX_VALUE
                    && convertedY >= Integer.MIN_VALUE && convertedY <= Integer.MAX_VALUE
                    && convertedZ >= Integer.MIN_VALUE && convertedZ <= Integer.MAX_VALUE) {
                xField.setValue(Long.toString(convertedX));
                yField.setValue(Long.toString(convertedY));
                zField.setValue(Long.toString(convertedZ));
                convertedExistingTarget = true;
            }
        }
        relativeMode = newRelativeMode;
        fieldsInitializedFromMenu = true;
        modeButton.setMessage(modeButtonText());
        if (convertedExistingTarget) {
            sendCommand(false);
        }
    }

    private Component modeButtonText() {
        return Component.translatable(relativeMode
                ? "gui.industrialcrops.golden_silo.mode.relative"
                : "gui.industrialcrops.golden_silo.mode.absolute");
    }

    private void toggleIgnoreYMode() {
        ignoreYMode = !ignoreYMode;
        yField.setEditable(!ignoreYMode);
        ignoreYButton.setMessage(ignoreYButtonText());
        fieldsInitializedFromMenu = true;
        if (menu.hasTarget()) {
            sendCommand(false);
        }
    }

    private Component ignoreYButtonText() {
        return Component.translatable(ignoreYMode
                ? "gui.industrialcrops.golden_silo.mode.ignore_y"
                : "gui.industrialcrops.golden_silo.mode.precise_y");
    }

    private void sendCommand(boolean launch) {
        if (launch) {
            ModNetworking.sendToServer(new GoldenSiloCommandPayload(
                    menu.getSiloPos(),
                    0,
                    0,
                    0,
                    relativeMode,
                    ignoreYMode,
                    true
            ));
            return;
        }

        Integer targetX = parseCoordinate(xField.getValue());
        Integer targetY = parseCoordinate(yField.getValue());
        Integer targetZ = parseCoordinate(zField.getValue());
        if (targetX == null || targetY == null || targetZ == null) {
            return;
        }
        fieldsInitializedFromMenu = true;
        ModNetworking.sendToServer(new GoldenSiloCommandPayload(
                menu.getSiloPos(),
                targetX,
                targetY,
                targetZ,
                relativeMode,
                ignoreYMode,
                false
        ));
    }

    private Component statusComponent() {
        return switch (menu.getStatus()) {
            case GoldenLaunchSiloBlockEntity.STATUS_TARGET_UNLOADED -> Component.translatable("gui.industrialcrops.golden_silo.status.target_unloaded");
            case GoldenLaunchSiloBlockEntity.STATUS_ROCKET_REQUIRED -> Component.translatable(
                    "gui.industrialcrops.golden_silo.status.rocket_required",
                    menu.getRequiredPotatoes()
            );
            case GoldenLaunchSiloBlockEntity.STATUS_COOLDOWN -> Component.translatable("gui.industrialcrops.golden_silo.status.cooldown");
            case GoldenLaunchSiloBlockEntity.STATUS_READY -> Component.translatable("gui.industrialcrops.golden_silo.status.ready");
            default -> Component.translatable("gui.industrialcrops.golden_silo.status.target_required");
        };
    }

    private static Integer parseCoordinate(String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
