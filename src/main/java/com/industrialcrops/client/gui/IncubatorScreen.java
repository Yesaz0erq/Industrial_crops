package com.industrialcrops.client.gui;

import com.industrialcrops.screen.IncubatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class IncubatorScreen extends UpgradeableMachineScreen<IncubatorMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("slime_converter");
    private Button releaseButton;

    public IncubatorScreen(IncubatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        releaseButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.industrialcrops.slime_converter.release"),
                        button -> {
                            if (minecraft != null && minecraft.gameMode != null) {
                                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                            }
                        })
                .bounds(leftPos + 105, topPos + 31, 58, 20)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        releaseButton.active = menu.hasSlime();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawContainer(graphics, leftPos, topPos, imageWidth, imageHeight);
        IndustrialGuiStyle.drawWorkPanel(graphics, leftPos + 68, topPos + 24, 98, 45);
        IndustrialGuiStyle.drawMachineWell(graphics, leftPos + 79, topPos + 34);
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);
        IndustrialGuiStyle.drawMekanismBar(graphics, leftPos + 121, topPos + 57, menu.getScaledProgress(25));
        drawUpgradeDrawer(graphics, IncubatorMenu.UPGRADE_X, IncubatorMenu.UPGRADE_Y);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
