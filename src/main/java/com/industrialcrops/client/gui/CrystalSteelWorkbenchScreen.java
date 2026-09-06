package com.industrialcrops.client.gui;

import com.industrialcrops.screen.CrystalSteelWorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import java.util.List;

public final class CrystalSteelWorkbenchScreen extends IndustrialContainerScreen<CrystalSteelWorkbenchMenu> {
    public CrystalSteelWorkbenchScreen(CrystalSteelWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 214; imageHeight = 234; inventoryLabelY = 138;
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        IndustrialGuiStyle.drawContainer(graphics, x, y, imageWidth, imageHeight);
        IndustrialGuiStyle.drawWorkPanel(graphics, x + 8, y + 24, 98, 98);
        for (int row = 0; row < 5; row++) for (int col = 0; col < 5; col++)
            IndustrialGuiStyle.drawSlot(graphics, x + 13 + col * 18, y + 29 + row * 18);
        IndustrialGuiStyle.drawWorkPanel(graphics, x + 132, y + 60, 28, 28);
        IndustrialGuiStyle.drawSlot(graphics, x + 137, y + 65);
        IndustrialGuiStyle.drawWorkPanel(graphics, x + 174, y + 24, 30, 98);
        int tint = menu.fluid().isEmpty() ? 0xFF576875 : IClientFluidTypeExtensions.of(menu.fluid().getFluid()).getTintColor(menu.fluid());
        IndustrialGuiStyle.drawVerticalMeter(graphics, x + 180, y + 30, 86, menu.fluid().getAmount(), menu.capacity(), tint, false);
        IndustrialGuiStyle.drawPlayerInventory(graphics, x, y, 26, 150, 208);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        IndustrialGuiStyle.drawFittedString(graphics, font, Component.translatable("gui.industrialcrops.fluid_amount",
            menu.fluid().getAmount(), menu.capacity()).getString(), 110, 125, 96, IndustrialGuiStyle.TEXT, false);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (mouseX >= leftPos + 174 && mouseX < leftPos + 204 && mouseY >= topPos + 24 && mouseY < topPos + 122) {
            graphics.renderComponentTooltip(font, List.of(menu.fluid().isEmpty()
                ? Component.translatable("gui.industrialcrops.empty_tank") : menu.fluid().getDisplayName(),
                Component.translatable("gui.industrialcrops.fluid_amount", menu.fluid().getAmount(), menu.capacity())), mouseX, mouseY);
        }
    }
}
