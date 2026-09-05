package com.industrialcrops.client.gui;

import com.industrialcrops.screen.ProcessorProgrammerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class ProcessorProgrammerScreen extends UpgradeableMachineScreen<ProcessorProgrammerMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("processor_programming_device");

    public ProcessorProgrammerScreen(ProcessorProgrammerMenu menu, Inventory inventory, Component title) {
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
        IndustrialGuiStyle.drawBackground(graphics, BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
        for (int slot = 0; slot < 5; slot++) {
            IndustrialGuiStyle.drawSlot(graphics, leftPos + 16 + slot * 18, topPos + 34);
        }
        IndustrialGuiStyle.drawMekanismSmallRight(graphics, leftPos + 111, topPos + 40, menu.getScaledProgress(28));
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 142, topPos + 34);
        drawUpgradeDrawer(graphics, ProcessorProgrammerMenu.UPGRADE_X, ProcessorProgrammerMenu.UPGRADE_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
