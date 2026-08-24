package com.industrialcrops.client.gui;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.resources.model.ModelBakery;

/**
 * Shared rendering primitives for Industrial Crops container screens.
 *
 * <p>The values intentionally mirror Minecraft's classic container palette:
 * {@code #C6C6C6} panels, {@code #8B8B8B} slot wells, white highlights and
 * {@code #373737} shadows. Keeping the primitives here prevents individual
 * machines from drifting into unrelated UI styles.</p>
 */
final class IndustrialGuiStyle {
    private static final ResourceLocation COMMON_BACKGROUND = new ResourceLocation(
            IndustrialCrops.MOD_ID, "textures/gui/common.png");
    private static final ResourceLocation WIDGETS = containerTexture("widgets");
    private static final ResourceLocation RS2_GRID_ROW = containerTexture("rs2_grid_row");
    private static final ResourceLocation RS2_SLOT = containerTexture("rs2_slot");
    private static final ResourceLocation RS2_SEARCH = containerTexture("rs2_search");
    private static final ResourceLocation RS2_SCROLLBAR = containerTexture("rs2_scrollbar");
    private static final ResourceLocation RS2_SCROLLBAR_CLICKED = containerTexture("rs2_scrollbar_clicked");
    private static final ResourceLocation RS2_SCROLLBAR_DISABLED = containerTexture("rs2_scrollbar_disabled");
    private static final ResourceLocation MEKANISM_SMALL_RIGHT = containerTexture("mekanism_small_right");
    private static final ResourceLocation MEKANISM_BAR = containerTexture("mekanism_bar");
    private static final ResourceLocation METER_TICKS = containerTexture("meter_ticks");
    private static final int WIDGET_TEXTURE_WIDTH = 128;
    private static final int WIDGET_TEXTURE_HEIGHT = 64;
    private static final int SLOT_SIZE = 18;
    private static final int ARROW_SOURCE_WIDTH = 28;
    private static final int ARROW_HEIGHT = 17;

    static final int PANEL = 0xFFC6C6C6;
    static final int SLOT = 0xFF8B8B8B;
    static final int SHADOW = 0xFF373737;
    static final int DARK_SHADOW = 0xFF555555;
    static final int HIGHLIGHT = 0xFFFFFFFF;
    static final int TEXT = 0xFF404040;
    static final int MUTED_TEXT = 0xFF606060;
    static final int ACTIVE = 0xFFB85A30;
    static final int INACTIVE = 0xFF555555;
    static final int WARNING = 0xFFAA0000;
    static final int ENERGY_RED = 0xFFC63D36;
    static final int RESIDUE_BROWN = 0xFF4A2C1B;

    private IndustrialGuiStyle() {
    }

    static ResourceLocation containerTexture(String name) {
        return new ResourceLocation(
                IndustrialCrops.MOD_ID,
                "textures/gui/container/" + name + ".png"
        );
    }

    static void drawBackground(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height
    ) {
        graphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }

    /** Draws an RS2 stretching container exactly as its row-based screen implementation does. */
    static void drawRs2StretchingBackground(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int rows,
            int bottomHeight
    ) {
        final int width = 193;
        final int topHeight = 19;
        final int rowHeight = 18;
        graphics.blit(texture, x, y, 0, 0, width, topHeight, 256, 256);
        for (int row = 0; row < rows; row++) {
            int sourceY = row == 0 ? 19 : row == rows - 1 ? 55 : 37;
            graphics.blit(texture, x, y + topHeight + row * rowHeight,
                    0, sourceY, width, rowHeight, 256, 256);
        }
        graphics.blit(texture, x, y + topHeight + rows * rowHeight,
                0, 73, width, bottomHeight, 256, 256);
    }

    static void drawRs2GridRow(GuiGraphics graphics, int x, int y) {
        graphics.blit(RS2_GRID_ROW, x, y, 0, 0, 162, 18, 162, 18);
    }

    static void drawRs2Slot(GuiGraphics graphics, int x, int y) {
        graphics.blit(RS2_SLOT, x, y, 0, 0, 18, 18, 18, 18);
    }

    static void drawRs2SearchIcon(GuiGraphics graphics, int x, int y) {
        graphics.blit(RS2_SEARCH, x, y, 0, 0, 12, 12, 12, 12);
    }

    static void drawRs2Scrollbar(GuiGraphics graphics, int x, int y, boolean clicked, boolean enabled) {
        ResourceLocation texture = !enabled
                ? RS2_SCROLLBAR_DISABLED
                : clicked ? RS2_SCROLLBAR_CLICKED : RS2_SCROLLBAR;
        graphics.blit(texture, x, y, 0, 0, 12, 15, 12, 15);
    }

    static void drawMarqueeTitle(
            GuiGraphics graphics,
            Font font,
            Component title,
            int x,
            int y,
            int maxWidth,
            int screenX,
            int screenY
    ) {
        int textWidth = font.width(title);
        if (textWidth <= maxWidth) {
            graphics.drawString(font, title, x, y, TEXT, false);
            return;
        }
        int overflow = textWidth - maxWidth;
        long travelMillis = Math.max(1, overflow) * 70L;
        long cycle = 2_000L + travelMillis;
        long time = System.currentTimeMillis() % cycle;
        int offset;
        if (time < 1_000L) offset = 0;
        else if (time < 1_000L + travelMillis) {
            offset = (int) ((time - 1_000L) * overflow / travelMillis);
        } else offset = overflow;
        graphics.enableScissor(screenX + x, screenY + y - 1, screenX + x + maxWidth, screenY + y + 10);
        graphics.drawString(font, title, x - offset, y, TEXT, false);
        graphics.disableScissor();
    }

    static String fitText(Font font, Component text, int maxWidth) {
        String value = text.getString();
        if (font.width(value) <= maxWidth) {
            return value;
        }
        String ellipsis = "...";
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width(ellipsis))) + ellipsis;
    }

    static void drawFittedString(GuiGraphics graphics, Font font, String text, int x, int y,
                                 int maxWidth, int color, boolean centered) {
        int width = Math.max(1, font.width(text));
        float scale = Math.min(1.0F, maxWidth / (float) width);
        graphics.pose().pushPose();
        float drawX = centered ? x + maxWidth / 2.0F - width * scale / 2.0F : x;
        graphics.pose().translate(drawX, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    static void drawFittedComponent(GuiGraphics graphics, Font font, Component text, int x, int y,
                                    int maxWidth, int color, boolean centered) {
        int width = Math.max(1, font.width(text));
        float scale = Math.min(1.0F, maxWidth / (float) width);
        graphics.pose().pushPose();
        float drawX = centered ? x + maxWidth / 2.0F - width * scale / 2.0F : x;
        graphics.pose().translate(drawX, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    static void drawContainer(GuiGraphics graphics, int x, int y, int width, int height) {
        drawCommonPanel(graphics, x, y, width, height);
    }

    /** Scales the shared neutral panel texture to the requested GUI or drawer bounds. */
    static void drawCommonPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.blit(COMMON_BACKGROUND, x, y, width, height,
                0.0F, 0.0F, 256, 256, 256, 256);
    }

    static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int fill) {
        graphics.fill(x, y, x + width, y + height, fill);
        graphics.fill(x, y, x + width, y + 1, HIGHLIGHT);
        graphics.fill(x, y, x + 1, y + height, HIGHLIGHT);
        graphics.fill(x, y + height - 1, x + width, y + height, DARK_SHADOW);
        graphics.fill(x + width - 1, y, x + width, y + height, DARK_SHADOW);
    }

    static void drawInsetPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, SLOT);
        graphics.fill(x, y, x + width - 1, y + 1, SHADOW);
        graphics.fill(x, y, x + 1, y + height - 1, SHADOW);
        graphics.fill(x + 1, y + height - 1, x + width, y + height, HIGHLIGHT);
        graphics.fill(x + width - 1, y + 1, x + width, y + height, HIGHLIGHT);
    }

    static final int VERTICAL_METER_WIDTH = 18;

    /** Wide vertical meter using the complete 16-pixel ruler supplied by the UI reference. */
    static void drawVerticalMeter(GuiGraphics graphics, int x, int y, int height,
                                  int value, int capacity, int color, boolean lavaTexture) {
        int width = VERTICAL_METER_WIDTH;
        graphics.fill(x, y, x + width, y + height, 0xFF777777);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF1C1E20);
        graphics.fill(x, y, x + width - 1, y + 1, 0xFF3A3D40);
        graphics.fill(x, y, x + 1, y + height - 1, 0xFF3A3D40);
        graphics.fill(x + width - 1, y + 1, x + width, y + height, 0xFFB0B0B0);
        graphics.fill(x + 1, y + height - 1, x + width, y + height, 0xFFB0B0B0);
        int innerHeight = height - 4;
        int filled = capacity <= 0 ? 0 : (int) ((long) innerHeight * Math.max(0, value) / capacity);
        filled = Math.max(0, Math.min(innerHeight, filled));
        if (filled > 0) {
            int fillTop = y + 2 + innerHeight - filled;
            if (lavaTexture) {
                int fillLeft = x + 1;
                int fillRight = x + width - 1;
                graphics.enableScissor(fillLeft, fillTop, fillRight, y + height - 2);
                for (int tileY = y + height - 18; tileY >= fillTop - 16; tileY -= 16) {
                    graphics.blit(fillLeft, tileY, 0, 16, 16, ModelBakery.LAVA_FLOW.sprite());
                }
                graphics.disableScissor();
            } else {
                graphics.fill(x + 1, fillTop, x + width - 1, y + height - 2, color);
                if (filled > 2) graphics.fill(x + 1, fillTop, x + 2, y + height - 2, lighten(color));
            }
        }
        graphics.blit(METER_TICKS, x + 1, y, 0, 0, 16, height, 16, 58);
    }

    private static int lighten(int color) {
        int r = Math.min(255, ((color >> 16) & 255) + 28);
        int g = Math.min(255, ((color >> 8) & 255) + 28);
        int b = Math.min(255, (color & 255) + 28);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.blit(WIDGETS, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, WIDGET_TEXTURE_WIDTH, WIDGET_TEXTURE_HEIGHT);
    }

    static void drawDisabledSlot(GuiGraphics graphics, int x, int y) {
        graphics.blit(WIDGETS, x, y, 18, 0, SLOT_SIZE, SLOT_SIZE, WIDGET_TEXTURE_WIDTH, WIDGET_TEXTURE_HEIGHT);
    }

    static void drawLock(GuiGraphics graphics, int x, int y) {
        graphics.blit(WIDGETS, x, y, 96, 0, 10, 14, WIDGET_TEXTURE_WIDTH, WIDGET_TEXTURE_HEIGHT);
    }

    static void drawPlayerInventory(GuiGraphics graphics, int leftPos, int topPos, int x, int inventoryY, int hotbarY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, leftPos + x + column * 18 - 1, topPos + inventoryY + row * 18 - 1);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, leftPos + x + column * 18 - 1, topPos + hotbarY - 1);
        }
    }

    static void drawProgressArrow(GuiGraphics graphics, int x, int y, int progress, int width) {
        int scaled = width <= 0 ? 0 : Math.max(0, Math.min(28, progress * 28 / width));
        drawMekanismSmallRight(graphics, x, y, scaled);
    }

    static void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int progress) {
        int scaled = width <= 0 ? 0 : Math.max(0, Math.min(25, progress * 25 / width));
        drawMekanismBar(graphics, x, y, scaled);
    }

    /** Mekanism progress sprite: first row is the background, second row is the filled state. */
    static void drawMekanismSmallRight(GuiGraphics graphics, int x, int y, int filled) {
        graphics.blit(MEKANISM_SMALL_RIGHT, x, y, 0, 0, 28, 8, 28, 24);
        int clipped = Math.max(0, Math.min(28, filled));
        if (clipped > 0) graphics.blit(MEKANISM_SMALL_RIGHT, x, y, clipped, 8, 0, 8, clipped, 8, 28, 24);
    }

    /** Mekanism bar sprite without the third yellow-black warning frame. */
    static void drawMekanismBar(GuiGraphics graphics, int x, int y, int filled) {
        graphics.blit(MEKANISM_BAR, x, y, 0, 0, 25, 9, 25, 27);
        int clipped = Math.max(0, Math.min(25, filled));
        if (clipped > 0) graphics.blit(MEKANISM_BAR, x, y, clipped, 9, 0, 9, clipped, 9, 25, 27);
    }

    static void drawSmallButton(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered) {
        int fill = hovered ? 0xFF9B9B9B : SLOT;
        graphics.fill(x, y, x + width, y + height, fill);
        graphics.fill(x, y, x + width, y + 1, HIGHLIGHT);
        graphics.fill(x, y, x + 1, y + height, HIGHLIGHT);
        graphics.fill(x, y + height - 1, x + width, y + height, SHADOW);
        graphics.fill(x + width - 1, y, x + width, y + height, SHADOW);
    }
}
