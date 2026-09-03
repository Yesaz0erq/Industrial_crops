package com.industrialcrops.client.gui;

import com.industrialcrops.screen.SlimeIncubatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class SlimeIncubatorScreen extends UpgradeableMachineScreen<SlimeIncubatorMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("slime_incubator");

    public SlimeIncubatorScreen(SlimeIncubatorMenu menu, Inventory inventory, Component title) {
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
        IndustrialGuiStyle.drawMekanismBar(graphics, leftPos + 75, topPos + 40, menu.getScaledProgress(25));
        drawUpgradeDrawer(graphics, SlimeIncubatorMenu.UPGRADE_X, SlimeIncubatorMenu.UPGRADE_Y);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
