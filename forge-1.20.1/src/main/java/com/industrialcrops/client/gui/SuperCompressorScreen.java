package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.SuperCompressorBlockEntity;
import com.industrialcrops.screen.SuperCompressorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class SuperCompressorScreen extends UpgradeableMachineScreen<SuperCompressorMenu> {
    private static final int BAR_X = 50, BAR_Y = 28, BAR_W = 76, BAR_H = 6;

    public SuperCompressorScreen(SuperCompressorMenu menu, Inventory inventory, Component title) {
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
        IndustrialGuiStyle.drawWorkPanel(graphics, leftPos + 18, topPos + 22, 140, 44);
        IndustrialGuiStyle.drawMachineWell(graphics, leftPos + SuperCompressorMenu.INPUT_X - 1, topPos + SuperCompressorMenu.INPUT_Y - 1);
        IndustrialGuiStyle.drawMachineWell(graphics, leftPos + SuperCompressorMenu.OUTPUT_X - 1, topPos + SuperCompressorMenu.OUTPUT_Y - 1);
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);

        // Next-batch gauge: buffered potatoes out of 10,000.
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        graphics.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xFF1A1A1A);
        graphics.fill(x, y, x + BAR_W, y + BAR_H, 0xFF3B3B3B);
        int filled = menu.getScaledBuffer(BAR_W);
        if (filled > 0) {
            graphics.fill(x, y, x + filled, y + BAR_H, 0xFFD9B44A);
            graphics.fill(x, y, x + filled, y + 1, 0xFFF3DC8A);
        }

        IndustrialGuiStyle.drawMekanismSmallRight(graphics, leftPos + 74, topPos + 40, menu.getScaledProgress(28));
        drawUpgradeDrawer(graphics, SuperCompressorMenu.UPGRADE_X, SuperCompressorMenu.UPGRADE_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int shown = Math.min(menu.getStoredPotatoes(), SuperCompressorBlockEntity.REQUIRED_POTATOES);
        String text = String.format("%,d / %,d", shown, SuperCompressorBlockEntity.REQUIRED_POTATOES);
        graphics.drawString(font, text, 88 - font.width(text) / 2, 52, 0xFF404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        int x = leftPos + BAR_X, y = topPos + BAR_Y;
        if (mouseX >= x - 1 && mouseX <= x + BAR_W && mouseY >= y - 1 && mouseY <= y + BAR_H) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.super_compressor.buffer",
                    String.format("%,d", menu.getStoredPotatoes()),
                    String.format("%,d", SuperCompressorBlockEntity.BUFFER_CAPACITY)), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
