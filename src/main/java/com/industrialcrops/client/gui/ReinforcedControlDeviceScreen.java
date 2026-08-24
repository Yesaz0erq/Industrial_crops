package com.industrialcrops.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.industrialcrops.screen.ReinforcedControlDeviceMenu;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.jetbrains.annotations.Nullable;

/** Six-row storage screen rendered with the shared vanilla container primitives. */
public final class ReinforcedControlDeviceScreen extends IndustrialContainerScreen<ReinforcedControlDeviceMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("reinforced_control_device");
    // Keep the page controls clear of both the title and the page number.
    private static final int PAGE_LEFT_X = 116;
    private static final int PAGE_RIGHT_X = 160;
    private static final int PAGE_BUTTON_Y = 5;
    private static final int PAGE_BUTTON_SIZE = 12;
    private static final int PAGE_TEXT_Y = 8;

    private Button previousPageButton;
    private Button nextPageButton;
    private final Set<Integer> gestureSlots = new HashSet<>();
    private boolean extractingWithGesture;

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
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String displayTitle = font.plainSubstrByWidth(title.getString(), 102);
        graphics.drawString(font, displayTitle, 8, titleLabelY, IndustrialGuiStyle.TEXT, false);
        String pageText = (menu.getPage() + 1) + "/" + menu.getTotalPages();
        graphics.drawString(font, pageText, 144 - font.width(pageText) / 2, PAGE_TEXT_Y, IndustrialGuiStyle.TEXT, false);
        if (!menu.isConnected()) {
            Component disconnected = Component.translatable("gui.industrialcrops.storage.disconnected");
            graphics.drawString(font, disconnected, 8, inventoryLabelY, 0xFFAA0000, false);
        } else {
            graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, IndustrialGuiStyle.MUTED_TEXT, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        drawStorageCounts(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawStorageCounts(GuiGraphics graphics) {
        for (int slot = 0; slot < 54; slot++) {
            int count = menu.getStoredCount(slot);
            // A count of one is rendered by renderSlotContents above; larger
            // values use this scaled renderer so 9999+ stays inside the slot.
            if (count <= 1) continue;
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

    /**
     * The unmodified wheel moves through the attached storage pages.  Holding
     * Shift turns the wheel into the same quick-transfer gesture used by the
     * basic controller: scroll over a storage slot to withdraw it, or over a
     * player-inventory slot to deposit it into the active storage page.
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        if (verticalAmount == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, verticalAmount);
        }

        if (isShiftPhysicallyDown()) {
            int storageSlot = storageSlotAt(mouseX, mouseY);
            if (storageSlot >= 0 && minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
                // A held item is deposited directly into the storage slot.
                // With an empty cursor, Shift+wheel withdraws a stack instead.
                if (menu.getCarried().isEmpty()) {
                    quickMoveSlot(storageSlot);
                } else {
                    minecraft.gameMode.handleInventoryMouseClick(
                            menu.containerId, storageSlot, 0, ClickType.PICKUP, minecraft.player);
                }
                return true;
            }
            int playerSlot = playerInventorySlotAt(mouseX, mouseY);
            if (playerSlot >= 0) {
                quickMoveSlot(playerSlot);
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, verticalAmount);
        }

        if (mouseX >= leftPos && mouseX < leftPos + imageWidth
                && mouseY >= topPos && mouseY < topPos + imageHeight) {
            changePage(verticalAmount > 0.0D);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, verticalAmount);
    }

    /**
     * Hold Shift and drag across the storage grid to move one stack from every
     * visited slot into the player inventory. Shift+right-click uses the same
     * fast extraction path for the currently hovered slot.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int slot = storageSlotAt(mouseX, mouseY);
        if (isShiftPhysicallyDown() && (button == 0 || button == 1) && slot >= 0) {
            extractingWithGesture = true;
            gestureSlots.clear();
            withdrawStack(slot, button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (extractingWithGesture && isShiftPhysicallyDown() && (button == 0 || button == 1)) {
            int slot = storageSlotAt(mouseX, mouseY);
            if (slot >= 0) {
                withdrawStack(slot, button);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = extractingWithGesture;
        extractingWithGesture = false;
        gestureSlots.clear();
        return handled || super.mouseReleased(mouseX, mouseY, button);
    }

    private int storageSlotAt(double mouseX, double mouseY) {
        int x = (int) mouseX - leftPos - 8;
        int y = (int) mouseY - topPos - 18;
        if (x < 0 || y < 0 || x >= 9 * 18 || y >= 6 * 18) {
            return -1;
        }
        if (x % 18 >= 16 || y % 18 >= 16) {
            return -1;
        }
        return x / 18 + y / 18 * 9;
    }

    private int playerInventorySlotAt(double mouseX, double mouseY) {
        for (int index = 54; index < menu.slots.size(); index++) {
            Slot slot = menu.slots.get(index);
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                return index;
            }
        }
        return -1;
    }

    private void quickMoveSlot(int slot) {
        if (minecraft == null || minecraft.gameMode == null || minecraft.player == null) {
            return;
        }
        minecraft.gameMode.handleInventoryMouseClick(
                menu.containerId, slot, 0, ClickType.QUICK_MOVE, minecraft.player);
    }

    /**
     * Screen modifier helpers can be stale while the mouse wheel event is
     * dispatched. Query both physical Shift keys so Shift+wheel never falls
     * through to page scrolling.
     */
    private boolean isShiftPhysicallyDown() {
        if (minecraft == null) {
            return false;
        }
        long window = minecraft.getWindow().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private void withdrawStack(int slot, int button) {
        if (!gestureSlots.add(slot) || minecraft == null || minecraft.gameMode == null || minecraft.player == null) {
            return;
        }
        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, slot, button, ClickType.QUICK_MOVE, minecraft.player);
    }
}
