package com.industrialcrops.client.gui;

import com.industrialcrops.crop.CropGenetics;
import com.industrialcrops.crop.CropQuality;
import com.industrialcrops.screen.CropAnalysisDeviceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class CropAnalysisDeviceScreen extends AbstractContainerScreen<CropAnalysisDeviceMenu> {
    public CropAnalysisDeviceScreen(CropAnalysisDeviceMenu menu, Inventory inventory, Component title) {
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
        IndustrialGuiStyle.drawSlot(graphics, leftPos + 24, topPos + 34);
        IndustrialGuiStyle.drawInsetPanel(graphics, leftPos + 49, topPos + 18, 119, 48);
        IndustrialGuiStyle.drawPlayerInventory(graphics, leftPos, topPos, 8, 84, 142);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedComponent(graphics, font, title, 8, titleLabelY,
                imageWidth - 16, IndustrialGuiStyle.TEXT, true);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                IndustrialGuiStyle.TEXT, false);

        ItemStack stack = menu.getAnalyzedStack();
        if (stack.isEmpty()) {
            IndustrialGuiStyle.drawFittedComponent(graphics, font,
                    Component.translatable("gui.industrialcrops.crop_analysis_device.empty"),
                    55, 37, 107, IndustrialGuiStyle.MUTED_TEXT, false);
            return;
        }

        CropGenetics.Genes genes = CropGenetics.read(stack);
        String cropName = IndustrialGuiStyle.fitText(font, stack.getHoverName(), 107);
        graphics.drawString(font, cropName, 55, 23, IndustrialGuiStyle.TEXT, false);
        if (genes == null) {
            IndustrialGuiStyle.drawFittedComponent(graphics, font,
                    Component.translatable("gui.industrialcrops.crop_analysis_device.no_genes"),
                    55, 42, 107, IndustrialGuiStyle.WARNING, false);
            return;
        }

        IndustrialGuiStyle.drawFittedComponent(graphics, font,
                Component.translatable("gui.industrialcrops.crop_analysis_device.dominant",
                        qualityName(genes.dominantQuality())),
                55, 38, 107, IndustrialGuiStyle.TEXT, false);
        IndustrialGuiStyle.drawFittedComponent(graphics, font,
                Component.translatable("gui.industrialcrops.crop_analysis_device.recessive",
                        qualityName(genes.recessiveQuality())),
                55, 52, 107, IndustrialGuiStyle.TEXT, false);
    }

    private static MutableComponent qualityName(CropQuality quality) {
        int color = quality == CropQuality.SUPER
                ? Mth.hsvToRgb((System.currentTimeMillis() % 4000L) / 4000.0F, 0.78F, 1.0F)
                : quality.color();
        return Component.translatable(quality.translationKey())
                .withStyle(style -> style.withColor(color));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
