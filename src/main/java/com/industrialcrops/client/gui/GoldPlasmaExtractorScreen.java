package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import com.industrialcrops.screen.GoldPlasmaExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GoldPlasmaExtractorScreen extends IndustrialContainerScreen<GoldPlasmaExtractorMenu> {
    public GoldPlasmaExtractorScreen(GoldPlasmaExtractorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawContainer(graphics, leftPos, topPos, imageWidth, imageHeight);
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 52, topPos + 34);
        IndustrialGuiStyle.drawProgressArrow(graphics, leftPos + 79, topPos + 39, menu.progress(), menu.maxProgress());
        IndustrialGuiStyle.drawVerticalMeter(graphics, leftPos + 112, topPos + 22, 52, menu.energy(),
                GoldPlasmaExtractorBlockEntity.ENERGY_CAPACITY, IndustrialGuiStyle.ENERGY_RED, false);
        IndustrialGuiStyle.drawVerticalMeter(graphics, leftPos + 136, topPos + 22, 52, menu.fluidAmount(),
                menu.fluidCapacity(), 0xFF4FBEFF, false);
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, 6, 160, IndustrialGuiStyle.TEXT, true);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + 112 && mouseX < leftPos + 130 && mouseY >= topPos + 22 && mouseY < topPos + 74) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.energy", menu.energy(),
                    GoldPlasmaExtractorBlockEntity.ENERGY_CAPACITY), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
