package com.industrialcrops.client.gui;

import com.industrialcrops.screen.RootOreExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RootOreExtractorScreen extends UpgradeableMachineScreen<RootOreExtractorMenu> {

    public RootOreExtractorScreen(RootOreExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = Math.max(8, (imageWidth - font.width(title)) / 2);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawContainer(graphics, leftPos, topPos, imageWidth, imageHeight);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 38, topPos + 24);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 62, topPos + 24);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 50, topPos + 52);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 115, topPos + 34);
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);
        IndustrialGuiStyle.drawMekanismSmallRight(graphics, leftPos + 79, topPos + 40, menu.getScaledProgress(28));
        drawUpgradeDrawer(graphics, RootOreExtractorMenu.UPGRADE_X, RootOreExtractorMenu.UPGRADE_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
