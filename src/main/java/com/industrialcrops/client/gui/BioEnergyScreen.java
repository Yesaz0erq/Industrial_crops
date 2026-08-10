package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.screen.BioEnergyMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class BioEnergyScreen extends UpgradeableMachineScreen<BioEnergyMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("bio_energy_machine");

    public BioEnergyScreen(BioEnergyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override protected void init() {
        super.init();
        titleLabelX = Math.max(8, (imageWidth - font.width(title)) / 2);
    }

    @Override protected boolean supportsUpgradeDrawer() {
        return menu.kind() == BioEnergyMachineBlockEntity.Kind.GENERATOR;
    }

    @Override protected void renderBg(GuiGraphics graphics, float tick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawBackground(graphics, BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
        if (menu.kind() != BioEnergyMachineBlockEntity.Kind.BATTERY) {
            IndustrialGuiStyle.drawRs2Slot(graphics, leftPos + 17, topPos + 34);
        }
        IndustrialGuiStyle.drawInsetPanel(graphics, leftPos + 47, topPos + 20, 82, 42);
        int firstMeterX = leftPos + imageWidth + 2;
        int secondMeterX = firstMeterX + IndustrialGuiStyle.VERTICAL_METER_WIDTH + 2;
        if (menu.kind() == BioEnergyMachineBlockEntity.Kind.GENERATOR) {
            IndustrialGuiStyle.drawVerticalMeter(graphics, firstMeterX, topPos + 12, 58,
                    menu.energy(), menu.energyCapacity(), IndustrialGuiStyle.ENERGY_RED, false);
            IndustrialGuiStyle.drawVerticalMeter(graphics, secondMeterX, topPos + 12, 58,
                    menu.residue(), menu.residueCapacity(), IndustrialGuiStyle.RESIDUE_BROWN, false);
            IndustrialGuiStyle.drawMekanismSmallRight(graphics, leftPos + 74, topPos + 64, menu.scaledProgress(28));
            drawUpgradeDrawer(graphics, BioEnergyMenu.UPGRADE_X, BioEnergyMenu.UPGRADE_Y);
        } else if (menu.kind() == BioEnergyMachineBlockEntity.Kind.BATTERY) {
            IndustrialGuiStyle.drawVerticalMeter(graphics, firstMeterX, topPos + 12, 58,
                    menu.energy(), menu.energyCapacity(), IndustrialGuiStyle.ENERGY_RED, false);
        } else {
            IndustrialGuiStyle.drawVerticalMeter(graphics, firstMeterX, topPos + 12, 58,
                    menu.burnTime(), menu.burnTimeTotal(), 0xFFFFFFFF, true);
            IndustrialGuiStyle.drawVerticalMeter(graphics, secondMeterX, topPos + 12, 58,
                    menu.residue(), menu.residueCapacity(), IndustrialGuiStyle.RESIDUE_BROWN, false);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 8, titleLabelY,
                imageWidth - 16, IndustrialGuiStyle.TEXT, true);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
        if (menu.kind() == BioEnergyMachineBlockEntity.Kind.GENERATOR) {
            IndustrialGuiStyle.drawFittedString(graphics, font,
                    Component.translatable("gui.industrialcrops.bio_generator.output", menu.currentYield()).getString(),
                    51, 28, 74, IndustrialGuiStyle.TEXT, false);
            IndustrialGuiStyle.drawFittedString(graphics, font,
                    Component.translatable("gui.industrialcrops.bio_generator.residue",
                            percent(menu.residue(), menu.residueCapacity())).getString(),
                    51, 43, 74, IndustrialGuiStyle.TEXT, false);
        } else if (menu.kind() == BioEnergyMachineBlockEntity.Kind.BATTERY) {
            Component energy = Component.translatable("gui.industrialcrops.energy", menu.energy(), menu.energyCapacity());
            IndustrialGuiStyle.drawFittedString(graphics, font, energy.getString(), 51, 34, 74,
                    IndustrialGuiStyle.TEXT, false);
        } else {
            Component burning = Component.translatable(menu.burnTime() > 0
                    ? "gui.industrialcrops.residue_incinerator.burning"
                    : "gui.industrialcrops.residue_incinerator.waiting");
            IndustrialGuiStyle.drawFittedString(graphics, font, burning.getString(), 51, 28, 74,
                    IndustrialGuiStyle.TEXT, false);
            IndustrialGuiStyle.drawFittedString(graphics, font,
                    Component.translatable("gui.industrialcrops.residue_incinerator.fuel", menu.burnTime()).getString(),
                    51, 43, 74, IndustrialGuiStyle.TEXT, false);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float tick) {
        renderBackground(graphics, mouseX, mouseY, tick);
        super.render(graphics, mouseX, mouseY, tick);
        renderUpgradeTab(graphics, mouseX, mouseY);
        if (menu.kind() == BioEnergyMachineBlockEntity.Kind.INCINERATOR
                && mouseX >= leftPos + imageWidth + 2
                && mouseX < leftPos + imageWidth + 2 + IndustrialGuiStyle.VERTICAL_METER_WIDTH
                && mouseY >= topPos + 12 && mouseY < topPos + 70) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable(
                    "gui.industrialcrops.residue_incinerator.fuel", menu.burnTime())), mouseX, mouseY);
        }
        if (menu.kind() != BioEnergyMachineBlockEntity.Kind.INCINERATOR
                && mouseX >= leftPos + imageWidth + 2
                && mouseX < leftPos + imageWidth + 2 + IndustrialGuiStyle.VERTICAL_METER_WIDTH
                && mouseY >= topPos + 12 && mouseY < topPos + 70) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("gui.industrialcrops.energy",
                    menu.energy(), menu.energyCapacity())), mouseX, mouseY);
        }
        int residueBarX = leftPos + imageWidth + 4 + IndustrialGuiStyle.VERTICAL_METER_WIDTH;
        if (menu.kind() != BioEnergyMachineBlockEntity.Kind.BATTERY
                && mouseX >= residueBarX && mouseX < residueBarX + IndustrialGuiStyle.VERTICAL_METER_WIDTH
                && mouseY >= topPos + 12 && mouseY < topPos + 70) {
            graphics.renderComponentTooltip(font, List.of(Component.translatable("gui.industrialcrops.bio_generator.residue_amount",
                    menu.residue(), menu.residueCapacity())), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static int percent(int value, int capacity) {
        return capacity <= 0 ? 0 : Math.max(0, Math.min(100, value * 100 / capacity));
    }
}
