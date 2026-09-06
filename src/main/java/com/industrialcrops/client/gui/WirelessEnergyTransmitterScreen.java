package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.WirelessEnergyTransmitterBlockEntity;
import com.industrialcrops.screen.WirelessEnergyTransmitterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class WirelessEnergyTransmitterScreen extends IndustrialContainerScreen<WirelessEnergyTransmitterMenu> {
    private Button local;
    private Button surrounding;
    public WirelessEnergyTransmitterScreen(WirelessEnergyTransmitterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 248; imageHeight = 176;
    }
    @Override protected void init() {
        super.init();
        local = addRenderableWidget(Button.builder(Component.translatable("gui.industrialcrops.wireless.local"), b -> choose(0))
                .bounds(leftPos + 12, topPos + 119, 108, 20).build());
        surrounding = addRenderableWidget(Button.builder(Component.translatable("gui.industrialcrops.wireless.surrounding"), b -> choose(1))
                .bounds(leftPos + 128, topPos + 119, 108, 20).build());
    }
    private void choose(int radius) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, radius);
    }
    @Override protected void containerTick() {
        super.containerTick();
        local.active = menu.radius() != 0;
        surrounding.active = menu.radius() != 1;
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawContainer(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.fill(leftPos + 12, topPos + 45, leftPos + 236, topPos + 55, 0xff192034);
        int width = (int)(224L * menu.energy() / WirelessEnergyTransmitterBlockEntity.ENERGY_CAPACITY);
        graphics.fill(leftPos + 12, topPos + 45, leftPos + 12 + width, topPos + 55, 0xff9974da);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawFittedString(graphics, font, title.getString(), 12, 8, 224, IndustrialGuiStyle.TEXT, true);
        line(graphics, Component.translatable("gui.industrialcrops.energy", menu.energy(), WirelessEnergyTransmitterBlockEntity.ENERGY_CAPACITY), 30);
        line(graphics, Component.translatable("gui.industrialcrops.wireless.transfer", menu.sent()), 64);
        line(graphics, Component.translatable("gui.industrialcrops.wireless.targets", menu.targets()), 79);
        line(graphics, Component.translatable(menu.active() ? "gui.industrialcrops.wireless.working" : "gui.industrialcrops.wireless.idle"), 94);
        line(graphics, Component.translatable("gui.industrialcrops.wireless.loaded_only"), 148);
        line(graphics, Component.translatable("gui.industrialcrops.wireless.input"), 160);
    }
    private void line(GuiGraphics graphics, Component text, int y) {
        IndustrialGuiStyle.drawFittedString(graphics, font, text.getString(), 12, y, 224, IndustrialGuiStyle.MUTED_TEXT, false);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
