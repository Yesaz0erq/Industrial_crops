package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.screen.IncubatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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
        IndustrialGuiStyle.drawBackground(graphics, BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
        IndustrialGuiStyle.drawMekanismBar(graphics, leftPos + 121, topPos + 57, menu.getScaledProgress(25));
        drawUpgradeDrawer(graphics, IncubatorMenu.UPGRADE_X, IncubatorMenu.UPGRADE_Y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        Component status = menu.hasSlime()
                ? Component.translatable(
                        "gui.industrialcrops.slime_converter.contains",
                        IncubatorBlockEntity.getSlimeName(menu.getSlimeType()),
                        menu.getSlimeSize()
                )
                : Component.translatable("gui.industrialcrops.slime_converter.empty");
        IndustrialGuiStyle.drawFittedString(graphics, font, status.getString(), 8, 58, 92,
                IndustrialGuiStyle.TEXT, false);
        IndustrialGuiStyle.drawFittedString(graphics, font,
                Component.translatable("gui.industrialcrops.slime_converter.ore").getString(),
                68, 23, 92, IndustrialGuiStyle.TEXT, true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
