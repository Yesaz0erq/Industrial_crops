package com.industrialcrops.client.gui;

import com.industrialcrops.screen.ReinforcedControlDeviceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Six-row storage screen rendered with the shared vanilla container primitives. */
public final class ReinforcedControlDeviceScreen extends IndustrialContainerScreen<ReinforcedControlDeviceMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("reinforced_control_device");
    private static final int PAGE_LEFT_X = 126;
    private static final int PAGE_RIGHT_X = 162;
    private static final int PAGE_BUTTON_Y = 5;
    private static final int PAGE_BUTTON_SIZE = 12;
    private static final int PAGE_TEXT_Y = 8;

    private Button previousPageButton;
    private Button nextPageButton;

    public ReinforcedControlDeviceScreen(ReinforcedControlDeviceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 8;
        previousPageButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(true))
                .bounds(leftPos + PAGE_LEFT_X, topPos + PAGE_BUTTON_Y, PAGE_BUTTON_SIZE, PAGE_BUTTON_SIZE)
                .build());
        nextPageButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(false))
                .bounds(leftPos + PAGE_RIGHT_X, topPos + PAGE_BUTTON_Y, PAGE_BUTTON_SIZE, PAGE_BUTTON_SIZE)
                .build());
        updatePageButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updatePageButtons();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawBackground(graphics, BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    protected void renderSlotContents(GuiGraphics graphics, ItemStack stack, Slot slot, @Nullable String countString) {
        // Storage counts are synchronized separately because vanilla slot packets cap the visible stack count.
        super.renderSlotContents(graphics, stack, slot, slot.index < 54 ? "" : countString);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String displayTitle = font.plainSubstrByWidth(title.getString(), 112);
        graphics.drawString(font, displayTitle, 8, titleLabelY, IndustrialGuiStyle.TEXT, false);
        String pageText = (menu.getPage() + 1) + "/" + menu.getTotalPages();
        graphics.drawString(font, pageText, 149 - font.width(pageText) / 2, PAGE_TEXT_Y, IndustrialGuiStyle.TEXT, false);
        if (!menu.isConnected()) {
            Component disconnected = Component.translatable("gui.industrialcrops.storage.disconnected");
            graphics.drawString(font, disconnected, 8, inventoryLabelY, 0xFFAA0000, false);
        } else {
            graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        drawStorageCounts(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawStorageCounts(GuiGraphics graphics) {
        for (int slot = 0; slot < 54; slot++) {
            int count = menu.getStoredCount(slot);
            if (count <= 0) continue;
            String text = IndustrialContainerScreen.formatCount(count);
            int x = leftPos + 8 + slot % 9 * 18;
            int y = topPos + 18 + slot / 9 * 18;
            int textWidth = font.width(text);
            float scale = Math.min(1.0F, 15.0F / Math.max(1, textWidth));
            graphics.pose().pushPose();
            graphics.pose().translate(x + 16.0F, y + 16.0F, 300.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, text, -textWidth, -8, 0xFFFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    private void changePage(boolean previous) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    previous
                            ? ReinforcedControlDeviceMenu.BUTTON_PREVIOUS_PAGE
                            : ReinforcedControlDeviceMenu.BUTTON_NEXT_PAGE
            );
        }
    }

    private void updatePageButtons() {
        if (previousPageButton == null || nextPageButton == null) {
            return;
        }
        previousPageButton.active = menu.getTotalPages() > 1 && menu.getPage() > 0;
        nextPageButton.active = menu.getTotalPages() > 1 && menu.getPage() < menu.getTotalPages() - 1;
    }
}
