package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.StructureBuilderBlockEntity;
import com.industrialcrops.screen.StructureBuilderMenu;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class StructureBuilderScreen extends AbstractContainerScreen<StructureBuilderMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("industrialcrops", "textures/gui/structure_builder.png");
    private static final int WIDTH = 312, HEIGHT = 236;
    private static final int TEXT = 0xEDF5F6, MUTED = 0xAEC2C6, CYAN = 0x76E2D5, AMBER = 0xFFBF73;
    private static final String[] SIZES = {"16×16", "32×16", "32×32", "48×48"};
    private final boolean creative;
    private final Map<Item, Integer> stock = new HashMap<>();
    private int displayStatus;
    private Button target, tier, start, stop, refresh;

    public StructureBuilderScreen(StructureBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        creative = inventory.player.getAbilities().instabuild;
    }

    private static Component text(String name, Object... args) {
        return Component.translatable("gui.industrialcrops.builder." + name, args);
    }

    private Button button(int x, int y, int w, int h, Component label, int id) {
        Button button = new ConsoleButton(leftPos + x, topPos + y, w, h, label, b -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }, id == 2);
        return addRenderableWidget(button);
    }

    @Override protected void init() {
        super.init();
        target = button(11, 29, 105, 17, text("type.0"), 0);
        tier = button(119, 29, 53, 17, Component.literal(SIZES[0]), 1);
        button(139, 50, 15, 14, Component.literal("<"), 4).setTooltip(Tooltip.create(text("prev")));
        button(157, 50, 15, 14, Component.literal(">"), 5).setTooltip(Tooltip.create(text("next")));
        start = button(184, 196, 50, 15, text("start"), 2);
        stop = button(237, 196, 29, 15, text("stop"), 3);
        refresh = button(269, 196, 33, 15, text("scan"), 6);
        refresh.setTooltip(Tooltip.create(text("refresh")));
        updateControls();
    }

    @Override protected void containerTick() {
        super.containerTick();
        updateControls();
    }

    private void updateControls() {
        target.setMessage(text("type." + menu.type()));
        tier.setMessage(Component.literal(SIZES[menu.tier()]));
        target.active = !menu.running() && menu.anchorMode() == 0;
        tier.active = !menu.running() && menu.anchorMode() != 2;
        start.active = !menu.running() && menu.status() != 2;
        stop.active = menu.running();
        refresh.active = !menu.running();
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        int progress = menu.status() == 2 ? 290 : menu.total() == 0 ? 0 : Math.min(290, menu.progress() * 290 / menu.total());
        if (progress > 0) g.fill(leftPos + 11, topPos + 229, leftPos + 11 + progress, topPos + 231, 0xFF64D6C8);
    }

    private void label(GuiGraphics g, Component label, int x, int y, int maxWidth, int color) {
        String value = label.getString();
        if (font.width(value) > maxWidth) {
            value = font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width("…"))) + "…";
        }
        g.drawString(font, value, x, y, color, false);
    }

    private void right(GuiGraphics g, Component label, int edge, int y, int color) {
        g.drawString(font, label, edge - font.width(label), y, color, false);
    }

    private String mode() {
        if (menu.status() == 10) return "conflict";
        if (menu.status() == 5 && menu.anchorMode() == 0) return "unknown";
        return menu.anchorMode() == 0 ? "manual" : "linked";
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        label(g, title, 31, 11, 151, TEXT);
        boolean linked = menu.anchorMode() > 0;
        int light = linked ? CYAN : AMBER;
        g.fill(193, 13, 197, 17, 0xFF000000 | light);
        label(g, text("mode." + mode()), 201, 11, 100, light);

        label(g, text("storage", menu.page() + 1), 11, 53, 125, TEXT);
        label(g, playerInventoryTitle, 11, 125, 161, MUTED);
        label(g, text("ledger"), 185, 31, 68, TEXT);
        right(g, creative ? text("free") : text("missing"), 300, 31, creative ? CYAN : AMBER);

        stock.clear();
        for (int slot = 0; slot < StructureBuilderBlockEntity.MATERIAL_SLOTS; slot++) {
            ItemStack stack = menu.getSlot(slot).getItem();
            if (!stack.isEmpty()) stock.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        boolean missing = false;
        for (int row = 0; row < StructureBuilderBlockEntity.MATERIAL_ROWS; row++) {
            ItemStack material = menu.material(row);
            if (material.isEmpty()) continue;
            int y = 43 + row * 11;
            int shortage = creative ? 0 : Math.max(0, menu.needed(row) - stock.getOrDefault(material.getItem(), 0));
            missing |= shortage > 0;
            String count = Integer.toString(shortage);
            int countX = 300 - font.width(count);
            g.pose().pushPose();
            g.pose().translate(185, y, 0);
            g.pose().scale(0.625F, 0.625F, 1);
            g.renderItem(material, 0, 0);
            g.pose().popPose();
            label(g, material.getHoverName(), 199, y + 1, countX - 202, MUTED);
            g.drawString(font, count, countX, y + 1, shortage > 0 ? AMBER : CYAN, false);
        }

        label(g, text("rate", menu.speed()), 185, 155, 115, TEXT);
        label(g, text("demolition", menu.demolitionCount()), 185, 185, 117, menu.demolitionCount() > 0 ? AMBER : MUTED);
        displayStatus = menu.status();
        if (displayStatus == StructureBuilderBlockEntity.IDLE || displayStatus == StructureBuilderBlockEntity.MISSING) {
            displayStatus = missing ? StructureBuilderBlockEntity.MISSING : StructureBuilderBlockEntity.IDLE;
        }
        int statusColor = displayStatus >= 3 && displayStatus != 7 ? AMBER : CYAN;
        g.fill(11, 219, 14, 224, 0xFF000000 | statusColor);
        label(g, text("brief." + displayStatus), 18, 217, 179, statusColor);
        String steps = menu.total() > 0 ? menu.progress() + " / " + menu.total() : menu.status() == 2 ? "100%" : "—";
        right(g, Component.literal(steps), 300, 217, TEXT);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        int x = mouseX - leftPos, y = mouseY - topPos;
        Component hint = null;
        if (x >= 182 && x < 304 && y >= 42 && y < 152) {
            int row = (y - 42) / 11;
            ItemStack material = menu.material(row);
            if (!material.isEmpty()) {
                hint = Component.empty().append(material.getHoverName()).append("\n")
                    .append(text("quantity", stock.getOrDefault(material.getItem(), 0), menu.needed(row)));
            }
        } else if (x >= 9 && x < 304 && y >= 214 && y < 232) {
            hint = text("status." + displayStatus);
        } else if ((x >= 187 && x < 304 && y >= 6 && y < 24) || (x >= 11 && x < 172 && y >= 29 && y < 46)) {
            hint = text("help");
        } else if (x >= 182 && x < 304 && y >= 153 && y < 165) {
            hint = text("speed_help");
        } else if (x >= 182 && x < 304 && y >= 184 && y < 195) {
            hint = text("demolition_hint");
        }
        if (hint != null) g.renderTooltip(font, font.split(hint, Math.min(202, width - 24)), mouseX, mouseY);
    }

    private static final class ConsoleButton extends Button {
        private final boolean primary;

        ConsoleButton(int x, int y, int w, int h, Component label, OnPress press, boolean primary) {
            super(x, y, w, h, label, press, DEFAULT_NARRATION);
            this.primary = primary;
        }

        @Override protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY(), w = getWidth(), h = getHeight();
            boolean focus = active && isHoveredOrFocused();
            int rim = focus ? 0xFFA1F9ED : primary && active ? 0xFF61B9AD : 0xFF58686D;
            int fill = !active ? 0xFF293034 : primary ? 0xFF23514E : focus ? 0xFF3F5458 : 0xFF354247;
            g.fill(x, y, x + w, y + h, 0xFF101819);
            g.fill(x + 1, y, x + w - 1, y + h - 1, rim);
            g.fill(x + 1, y + 1, x + w - 1, y + h - 2, fill);
            renderScrollingString(g, Minecraft.getInstance().font, 2, active ? TEXT : 0xA2B0B2);
        }
    }
}
