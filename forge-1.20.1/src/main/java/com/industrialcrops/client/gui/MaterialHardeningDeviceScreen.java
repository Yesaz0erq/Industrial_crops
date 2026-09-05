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
        IndustrialGuiStyle.drawParadoxContainer(graphics, leftPos, topPos, imageWidth, imageHeight);
        IndustrialGuiStyle.drawParadoxSlot(graphics, leftPos + 55, topPos + 34);
        IndustrialGuiStyle.drawParadoxSlot(graphics, leftPos + 115, topPos + 34);
        graphics.setColor(0.85F, 0.27F, 0.37F, 1.0F);
        IndustrialGuiStyle.drawProgressArrow(graphics, leftPos + 82, topPos + 40,
                menu.getScaledProgress(28), 28);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                IndustrialGuiStyle.drawParadoxSlot(graphics, leftPos + 7 + column * 18, topPos + 83 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            IndustrialGuiStyle.drawParadoxSlot(graphics, leftPos + 7 + column * 18, topPos + 141);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, 6,
                imageWidth - 16, 0xFFF4DDE2, true);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY,
                0xFFC4A5B2, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
