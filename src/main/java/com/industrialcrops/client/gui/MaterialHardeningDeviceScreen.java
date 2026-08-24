package com.industrialcrops.client.gui;

import com.industrialcrops.screen.MaterialHardeningDeviceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MaterialHardeningDeviceScreen extends IndustrialContainerScreen<MaterialHardeningDeviceMenu> {
    public MaterialHardeningDeviceScreen(MaterialHardeningDeviceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawContainer(graphics, leftPos, topPos, imageWidth, imageHeight);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 55, topPos + 34);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 115, topPos + 34);
        IndustrialGuiStyle.drawProgressArrow(graphics, leftPos + 82, topPos + 40,
                menu.getScaledProgress(28), 28);
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, 6,
                imageWidth - 16, IndustrialGuiStyle.TEXT, true);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
