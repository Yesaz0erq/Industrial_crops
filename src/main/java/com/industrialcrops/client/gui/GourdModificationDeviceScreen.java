package com.industrialcrops.client.gui;

import com.industrialcrops.screen.GourdModificationDeviceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GourdModificationDeviceScreen
        extends AbstractContainerScreen<GourdModificationDeviceMenu> {
    public GourdModificationDeviceScreen(
            GourdModificationDeviceMenu menu,
            Inventory inventory,
            Component title
    ) {
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
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 36, topPos + 34);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 68, topPos + 34);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 123, topPos + 34);
        IndustrialGuiStyle.drawMekanismSmallRight(
                graphics,
                leftPos + 92,
                topPos + 40,
                menu.getScaledProgress(28)
        );
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedComponent(
                graphics, font, title, 8, titleLabelY,
                imageWidth - 16, IndustrialGuiStyle.TEXT, true
        );
        graphics.drawString(
                font,
                playerInventoryTitle,
                inventoryLabelX,
                inventoryLabelY,
                IndustrialGuiStyle.TEXT,
                false
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
