package com.industrialcrops.client.creative;

import com.industrialcrops.registry.CreativeSectionCatalog;
import com.industrialcrops.registry.ModCreativeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CreativeSectionClient {
    public record BuiltContent(Collection<ItemStack> displayItems, Set<ItemStack> searchItems) {
    }

    private record Layout(Map<Integer, CreativeSectionCatalog.Section> banners) {
    }

    private static final Map<CreativeModeTab, Layout> LAYOUTS = new IdentityHashMap<>();
    private static int currentRow;
    private static Layout activeLayout;

    private CreativeSectionClient() {
    }

    public static BuiltContent build(CreativeModeTab tab) {
        List<CreativeSectionCatalog.Section> sections;
        if (tab != ModCreativeTabs.INDUSTRIAL_CROPS.get()) {
            return null;
        }
        sections = CreativeSectionCatalog.combinedSections();

        List<ItemStack> display = new ArrayList<>();
        Set<ItemStack> search = ItemStackLinkedSet.createTypeAndTagSet();
        Map<Integer, CreativeSectionCatalog.Section> banners = new LinkedHashMap<>();
        for (CreativeSectionCatalog.Section section : sections) {
            banners.put(display.size() / 9, section);
            for (int i = 0; i < 9; i++) {
                display.add(ItemStack.EMPTY);
            }
            for (var supplier : section.items()) {
                ItemStack stack = supplier.get();
                if (!stack.isEmpty()) {
                    display.add(stack);
                    search.add(stack);
                }
            }
            while (display.size() % 9 != 0) {
                display.add(ItemStack.EMPTY);
            }
        }
        LAYOUTS.put(tab, new Layout(Map.copyOf(banners)));
        return new BuiltContent(List.copyOf(display), search);
    }

    public static void setCurrentRow(int row) {
        currentRow = Math.max(0, row);
    }

    public static void render(GuiGraphics graphics, CreativeModeTab tab, int left, int top) {
        Layout layout = LAYOUTS.get(tab);
        activeLayout = layout;
        if (layout == null) {
            return;
        }
        var font = Minecraft.getInstance().font;
        for (var entry : layout.banners().entrySet()) {
            int visibleRow = entry.getKey() - currentRow;
            if (visibleRow < 0 || visibleRow >= 5) {
                continue;
            }
            int x = left + 8;
            int y = top + 17 + visibleRow * 18;
            CreativeSectionCatalog.Section section = entry.getValue();
            int frames = section.banner().getNamespace().equals("carrote") ? 8 : 1;
            var player = Minecraft.getInstance().player;
            int frame = player == null ? 0 : (player.tickCount / 3) % frames;
            graphics.blit(section.banner(), x, y, 0, frame * 18, 162, 18, 162, 18 * frames);
            graphics.drawString(font, section.title(), x + 6, y + 5, 0xFFF4F7FA, true);
        }
    }

    /** Prevents the vanilla 16x16 hover tint from covering a visible section banner row. */
    public static boolean isBannerVisibleSlot(int slotIndex) {
        if (activeLayout == null || slotIndex < 0 || slotIndex >= 45) {
            return false;
        }
        int absoluteRow = currentRow + slotIndex / 9;
        return activeLayout.banners().containsKey(absoluteRow);
    }
}
