package com.industrialcrops.client.gui;

import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.UpgradeableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

abstract class UpgradeableMachineScreen<M extends AbstractContainerMenu & UpgradeableMenu>
        extends IndustrialContainerScreen<M> {
    private Button upgradeTab;
    private boolean upgradesOpen;

    protected UpgradeableMachineScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        if (!supportsUpgradeDrawer()) return;
        upgradeTab = addRenderableWidget(Button.builder(Component.empty(), ignored -> {
            upgradesOpen = !upgradesOpen;
            menu.setUpgradeSlotsVisible(upgradesOpen);
        }).bounds(leftPos - 20, topPos + 20, 20, 20).build());
        menu.setUpgradeSlotsVisible(false);
    }

    protected boolean supportsUpgradeDrawer() {
        return true;
    }

    protected final void drawUpgradeDrawer(GuiGraphics graphics, int slotX, int slotY) {
        if (!upgradesOpen) return;
        IndustrialGuiStyle.drawCommonPanel(graphics, leftPos - 80, topPos + 20, 60, 52);
        for (int index = 0; index < 4; index++) {
            IndustrialGuiStyle.drawSlot(graphics, leftPos + slotX - 1 + index % 2 * 22,
                    topPos + slotY - 1 + index / 2 * 22);
        }
    }

    protected final void renderUpgradeTab(GuiGraphics graphics, int mouseX, int mouseY) {
        if (upgradeTab == null) return;
        graphics.renderItem(new ItemStack(ModItems.SPEED_COMPONENT_1.get()), upgradeTab.getX() + 2, upgradeTab.getY() + 2);
        if (upgradesOpen) {
            graphics.fill(upgradeTab.getX(), upgradeTab.getY(), upgradeTab.getX() + 20, upgradeTab.getY() + 1, IndustrialGuiStyle.ACTIVE);
            graphics.fill(upgradeTab.getX(), upgradeTab.getY(), upgradeTab.getX() + 1, upgradeTab.getY() + 20, IndustrialGuiStyle.ACTIVE);
        }
        if (upgradeTab.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.upgrade_slots"), mouseX, mouseY);
        }
    }

    @Override
    public void removed() {
        menu.setUpgradeSlotsVisible(false);
        super.removed();
    }
}
