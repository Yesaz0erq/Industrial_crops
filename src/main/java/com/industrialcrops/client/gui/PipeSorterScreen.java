package com.industrialcrops.client.gui;

import com.industrialcrops.screen.PipeSorterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PipeSorterScreen extends IndustrialContainerScreen<PipeSorterMenu> {
    private Button whitelistButton;
    private Button blacklistButton;

    public PipeSorterScreen(PipeSorterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 170;
        inventoryLabelY = 76;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        whitelistButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.industrialcrops.pipe_sorter.whitelist"),
                ignored -> setMode(PipeSorterMenu.BUTTON_WHITELIST))
                .bounds(leftPos + 24, topPos + 57, 62, 18).build());
        blacklistButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.industrialcrops.pipe_sorter.blacklist"),
                ignored -> setMode(PipeSorterMenu.BUTTON_BLACKLIST))
                .bounds(leftPos + 90, topPos + 57, 62, 18).build());
        refreshButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshButtons();
    }

    private void refreshButtons() {
        if (whitelistButton == null) return;
        whitelistButton.active = menu.isBlacklist();
        blacklistButton.active = !menu.isBlacklist();
    }

    private void setMode(int mode) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, mode);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawCommonPanel(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 9; column++) {
                IndustrialGuiStyle.drawSlot(graphics, leftPos + 7 + column * 18, topPos + 17 + row * 18);
            }
        }
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 88, 146);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, IndustrialGuiStyle.TEXT, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
