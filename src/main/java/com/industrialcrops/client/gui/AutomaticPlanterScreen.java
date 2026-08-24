package com.industrialcrops.client.gui;

import com.industrialcrops.screen.AutomaticPlanterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AutomaticPlanterScreen extends IndustrialContainerScreen<AutomaticPlanterMenu> {
    public AutomaticPlanterScreen(AutomaticPlanterMenu menu, Inventory inventory, Component title) {
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
        for (int slot = 0; slot < 27; slot++) {
            IndustrialGuiStyle.drawSlot(graphics, leftPos + 7 + (slot % 9) * 18,
                    topPos + 17 + (slot / 9) * 18);
        }
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedComponent(graphics, font, title, 8, titleLabelY,
                imageWidth - 16, IndustrialGuiStyle.TEXT, true);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
