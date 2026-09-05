package com.industrialcrops.client.gui;

import com.industrialcrops.screen.CropCompressorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class CropCompressorScreen extends UpgradeableMachineScreen<CropCompressorMenu> {

    public CropCompressorScreen(CropCompressorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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
        IndustrialGuiStyle.drawWorkPanel(graphics, leftPos + 30, topPos + 25, 112, 39);
        IndustrialGuiStyle.drawMachineWell(graphics, leftPos + 55, topPos + 34);
        IndustrialGuiStyle.drawMachineWell(graphics, leftPos + 115, topPos + 34);
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);
        IndustrialGuiStyle.drawMekanismSmallRight(graphics, leftPos + 80, topPos + 40, menu.getScaledProgress(28));
        drawUpgradeDrawer(graphics, CropCompressorMenu.UPGRADE_X, CropCompressorMenu.UPGRADE_Y);
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
