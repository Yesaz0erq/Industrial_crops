package com.industrialcrops.client.gui;

import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.BasicControlDeviceMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class BasicControlDeviceScreen extends IndustrialContainerScreen<BasicControlDeviceMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("basic_control_device");
    private final List<Button> actionButtons = new ArrayList<>();

    public BasicControlDeviceScreen(BasicControlDeviceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = Math.max(8, (imageWidth - font.width(title)) / 2);
        actionButtons.clear();
        addButton(86, 18, 32, 20, "gui.industrialcrops.storage.store", BasicControlDeviceMenu.STORE_ALL_CARROTS);
        addButton(86, 44, 32, 20, "gui.industrialcrops.storage.store", BasicControlDeviceMenu.STORE_ALL_POTATOES);
        addButton(122, 18, 20, 20, "gui.industrialcrops.storage.one", BasicControlDeviceMenu.TAKE_ONE_CARROT);
        addButton(147, 18, 21, 20, "gui.industrialcrops.storage.stack", BasicControlDeviceMenu.TAKE_STACK_CARROTS);
        addButton(122, 44, 20, 20, "gui.industrialcrops.storage.one", BasicControlDeviceMenu.TAKE_ONE_POTATO);
        addButton(147, 44, 21, 20, "gui.industrialcrops.storage.stack", BasicControlDeviceMenu.TAKE_STACK_POTATOES);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (Button button : actionButtons) {
            button.active = menu.isConnected();
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawBackground(graphics, BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        drawEntry(graphics, new ItemStack(ModItems.INDUSTRIAL_CARROT.get()), Component.translatable("gui.industrialcrops.storage.industrial_carrot"), menu.getIndustrialCarrotCount(), 18);
        drawEntry(graphics, new ItemStack(ModItems.INDUSTRIAL_POTATO.get()), Component.translatable("gui.industrialcrops.storage.industrial_potato"), menu.getIndustrialPotatoCount(), 44);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawEntry(GuiGraphics graphics, ItemStack stack, Component label, int count, int y) {
        graphics.renderItem(stack, 10, y);
        IndustrialGuiStyle.drawFittedString(graphics, font, label.getString(), 29, y + 1,
                54, IndustrialGuiStyle.TEXT, false);
        Component detail = menu.isConnected()
                ? Component.translatable("gui.industrialcrops.storage.count", count)
                : Component.translatable("gui.industrialcrops.storage.disconnected");
        IndustrialGuiStyle.drawFittedString(graphics, font, detail.getString(), 29, y + 11, 54,
                menu.isConnected() ? IndustrialGuiStyle.MUTED_TEXT : IndustrialGuiStyle.WARNING, false);
    }

    private void addButton(int x, int y, int width, int height, String key, int id) {
        Button control = addRenderableWidget(Button.builder(Component.translatable(key), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(leftPos + x, topPos + y, width, height).build());
        actionButtons.add(control);
    }
}
