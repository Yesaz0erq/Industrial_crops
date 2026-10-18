package com.industrialcrops.client.gui;

import com.industrialcrops.block.entity.SuperCropCultivatorBlockEntity;
import com.industrialcrops.recipe.SuperCultivationRecipes;
import com.industrialcrops.screen.SuperCropCultivatorMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Super Crop Cultivator screen. A crystal-steel chassis holds a glass nebula chamber: a vanilla-style
 * farmland bed grows the plant while material beams feed a glowing seed orb, stars twinkle, an orbit
 * ring turns and a DNA helix fills as the minute passes. Everything is drawn procedurally per frame.
 */
public final class SuperCropCultivatorScreen extends AbstractContainerScreen<SuperCropCultivatorMenu> {
    private static final int W = 176, H = 224;
    private static final int CH_X = 44, CH_Y = 22, CH_W = 84, CH_H = 104;
    private static final int HX_X = 131, HX_Y = 22, HX_W = 9, HX_H = 104;
    private static final int SOIL_H = 14;

    private static final int COLOR_STEEL_DARK = 0xFF292D39;
    private static final int COLOR_STEEL = 0xFF68768F;
    private static final int COLOR_STEEL_LIGHT = 0xFFB6D2DF;
    private static final int COLOR_CYAN = 0xFF68DCFF;
    private static final int COLOR_PURPLE = 0xFFAE70FF;

    private static final int STAR_COUNT = 26;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private final int[] starColor = new int[STAR_COUNT];

    private int lastHarvests = -1;
    private long harvestFlashStart = -10_000L;
    private float shownProgress;

    public SuperCropCultivatorScreen(SuperCropCultivatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = W;
        imageHeight = H;
        inventoryLabelY = SuperCropCultivatorMenu.INVENTORY_Y - 11;
        long seed = 0x5DEECE66DL;
        int[] palette = {0xFFFFFFFF, 0xFF9DEFFF, 0xFFD899FF, 0xFFFFE9A8};
        for (int i = 0; i < STAR_COUNT; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            starX[i] = ((seed >>> 33) & 0xFFFF) / 65535.0F;
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            starY[i] = ((seed >>> 33) & 0xFFFF) / 65535.0F;
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            starPhase[i] = ((seed >>> 33) & 0xFFFF) / 65535.0F * Mth.TWO_PI;
            starColor[i] = palette[i % palette.length];
        }
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = Math.max(8, (imageWidth - font.width(title)) / 2);
    }

    private boolean growing() {
        return menu.getStatus() == SuperCropCultivatorBlockEntity.STATUS_GROWING;
    }

    private static float time() {
        return (Util.getMillis() % 3_600_000L) / 1000.0F;
    }

    // ------------------------------------------------------------------ background

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        float t = time();
        boolean active = growing();
        float target = menu.getProgressFraction();
        // Ease the displayed progress towards the server value so the plant grows smoothly.
        shownProgress = target < shownProgress - 0.2F ? target : Mth.lerp(0.25F, shownProgress, target);

        int harvests = menu.getHarvests();
        if (lastHarvests >= 0 && harvests != lastHarvests) harvestFlashStart = Util.getMillis();
        lastHarvests = harvests;

        IndustrialGuiStyle.drawContainer(g, x, y, W, H);
        drawLeftColumn(g, x, y, t, active);
        drawChamber(g, x + CH_X, y + CH_Y, t, active);
        drawHelix(g, x + HX_X, y + HX_Y, t, active);
        drawOutput(g, x, y, t, active);
        IndustrialGuiStyle.drawPlayerInventory(g, x, y, 8, SuperCropCultivatorMenu.INVENTORY_Y, SuperCropCultivatorMenu.HOTBAR_Y);
        drawGhostItems(g, x, y);
    }

    private void drawLeftColumn(GuiGraphics g, int x, int y, float t, boolean active) {
        IndustrialGuiStyle.drawMachineWell(g, x + SuperCropCultivatorMenu.CROP_X - 1, y + SuperCropCultivatorMenu.CROP_Y - 1);
        for (int my : SuperCropCultivatorMenu.MATERIAL_Y) {
            IndustrialGuiStyle.drawMachineWell(g, x + SuperCropCultivatorMenu.MATERIAL_X - 1, y + my - 1);
        }

        // Feed tubes into the chamber; light pulses travel along them while growing.
        drawTube(g, x + 34, y + SuperCropCultivatorMenu.CROP_Y + 6, CH_X - 34, t, active, 0xFF8BE26A, 0.0F);
        for (int i = 0; i < SuperCropCultivatorMenu.MATERIAL_Y.length; i++) {
            ItemStack material = menu.getSlot(SuperCropCultivatorBlockEntity.MATERIAL_A_SLOT + i).getItem();
            drawTube(g, x + 34, y + SuperCropCultivatorMenu.MATERIAL_Y[i] + 6, CH_X - 34, t, active && !material.isEmpty(),
                    materialColor(material, t), 0.25F * (i + 1));
        }

        // Quality chip on the crop well.
        ItemStack crop = menu.getSlot(SuperCropCultivatorBlockEntity.CROP_SLOT).getItem();
        if (!crop.isEmpty()) {
            int cx = x + SuperCropCultivatorMenu.CROP_X + 13, cy = y + SuperCropCultivatorMenu.CROP_Y - 5;
            if (SuperCultivationRecipes.isSuperQuality(crop)) {
                int c = Mth.hsvToRgb((t * 0.5F) % 1.0F, 0.7F, 1.0F) | 0xFF000000;
                g.fill(cx, cy, cx + 7, cy + 7, 0xFF1A1030);
                drawStar(g, cx + 3, cy + 3, c);
            } else {
                g.fill(cx, cy, cx + 7, cy + 7, 0xFF3A1010);
                g.fill(cx + 3, cy + 1, cx + 4, cy + 4, 0xFFFF5555);
                g.fill(cx + 3, cy + 5, cx + 4, cy + 6, 0xFFFF5555);
            }
        }
    }

    private void drawOutput(GuiGraphics g, int x, int y, float t, boolean active) {
        int ox = x + SuperCropCultivatorMenu.OUTPUT_X - 1, oy = y + SuperCropCultivatorMenu.OUTPUT_Y - 1;
        // Halo behind the output well after a harvest.
        float flash = flashAmount();
        if (flash > 0.0F) {
            int a = (int) (flash * 140);
            for (int r = 16; r >= 10; r -= 2) {
                g.fill(ox + 9 - r, oy + 9 - r, ox + 9 + r, oy + 9 + r, (a / 3) << 24 | 0xE7C6FF);
            }
        }
        IndustrialGuiStyle.drawMachineWell(g, ox, oy);
        drawTube(g, x + HX_X + HX_W, y + SuperCropCultivatorMenu.OUTPUT_Y + 6, SuperCropCultivatorMenu.OUTPUT_X - 4 - HX_X - HX_W,
                t, active, COLOR_PURPLE, 0.5F);
        drawTube(g, x + CH_X + CH_W, y + SuperCropCultivatorMenu.OUTPUT_Y + 6, HX_X - CH_X - CH_W, t, active, COLOR_PURPLE, 0.1F);

        // Byproduct well (returned bottles) with its own small feed line.
        int bx = x + SuperCropCultivatorMenu.BYPRODUCT_X - 1, by = y + SuperCropCultivatorMenu.BYPRODUCT_Y - 1;
        IndustrialGuiStyle.drawMachineWell(g, bx, by);
        SuperCultivationRecipes.Recipe recipe = previewRecipe();
        boolean returns = recipe != null && !recipe.remainders().isEmpty();
        drawTube(g, x + HX_X + HX_W, y + SuperCropCultivatorMenu.BYPRODUCT_Y + 6, SuperCropCultivatorMenu.BYPRODUCT_X - 4 - HX_X - HX_W,
                t, active && returns, 0xFFD8E8F0, 0.7F);
    }

    /** Recipe the screen is showing, following the loaded crop and materials; cycles through options when unclear. */
    private SuperCultivationRecipes.Recipe previewRecipe() {
        ItemStack crop = menu.getSlot(SuperCropCultivatorBlockEntity.CROP_SLOT).getItem();
        java.util.List<ItemStack> materials = java.util.List.of(
                menu.getSlot(SuperCropCultivatorBlockEntity.MATERIAL_A_SLOT).getItem(),
                menu.getSlot(SuperCropCultivatorBlockEntity.MATERIAL_B_SLOT).getItem(),
                menu.getSlot(SuperCropCultivatorBlockEntity.MATERIAL_C_SLOT).getItem());
        boolean anyMaterial = materials.stream().anyMatch(stack -> !stack.isEmpty());
        java.util.List<SuperCultivationRecipes.Recipe> options = crop.isEmpty()
                ? SuperCultivationRecipes.all() : SuperCultivationRecipes.forCropAll(crop);
        if (options.isEmpty()) return null;
        if (!anyMaterial) return options.get((int) (Util.getMillis() / 2500L % options.size()));
        SuperCultivationRecipes.Recipe preview = crop.isEmpty() ? null : SuperCultivationRecipes.preview(crop, materials);
        if (preview != null) return preview;
        for (SuperCultivationRecipes.Recipe option : options) {
            for (ItemStack stack : materials) {
                if (!stack.isEmpty() && option.usesMaterial(stack)) return option;
            }
        }
        return options.get(0);
    }

    /** Colour the plant ripens into: violet for Black Nebula, magenta for Purple Dragon. */
    private int ripeColor(int fallback) {
        SuperCultivationRecipes.Recipe recipe = previewRecipe();
        if (recipe != null && recipe.id().equals("purple_dragon_carrot")) return 0xFFD04DFF;
        if (recipe != null && recipe.id().equals("omni_carrot")) return Mth.hsvToRgb((time() * 0.25F) % 1.0F, 0.6F, 1.0F) | 0xFF000000;
        return fallback;
    }

    // ------------------------------------------------------------------ chamber

    private void drawChamber(GuiGraphics g, int cx, int cy, float t, boolean active) {
        // Crystal-steel frame.
        g.fill(cx - 3, cy - 3, cx + CH_W + 3, cy + CH_H + 3, COLOR_STEEL_DARK);
        g.fill(cx - 2, cy - 2, cx + CH_W + 2, cy + CH_H + 2, COLOR_STEEL);
        g.fill(cx - 2, cy - 2, cx + CH_W + 2, cy - 1, COLOR_STEEL_LIGHT);
        g.fill(cx - 2, cy - 2, cx - 1, cy + CH_H + 2, COLOR_STEEL_LIGHT);
        g.fill(cx - 1, cy - 1, cx + CH_W + 1, cy + CH_H + 1, 0xFF0B1222);
        for (int[] corner : new int[][]{{-3, -3}, {CH_W + 1, -3}, {-3, CH_H + 1}, {CH_W + 1, CH_H + 1}}) {
            g.fill(cx + corner[0], cy + corner[1], cx + corner[0] + 2, cy + corner[1] + 2, 0xFF5F8FCA);
        }

        g.enableScissor(cx, cy, cx + CH_W, cy + CH_H);
        // Deep-space gradient, shifted towards violet as the plant matures.
        int top = lerpColor(0xFF070B1C, 0xFF0E0624, shownProgress);
        int bottom = lerpColor(0xFF13203A, 0xFF2A1048, shownProgress);
        g.fillGradient(cx, cy, cx + CH_W, cy + CH_H, top, bottom);
        drawNebulaClouds(g, cx, cy, t, active);
        drawStars(g, cx, cy, t, active);

        int soilTop = cy + CH_H - SOIL_H;
        int plantX = cx + CH_W / 2;
        float p = displayProgress();
        int stemTop = soilTop - 3 - (int) (48 * p);
        int orbY = Math.min(soilTop - 6, stemTop - 4);

        drawOrbitRing(g, plantX, orbY, t, active, false);
        if (active) drawBeams(g, cx, cy, plantX, orbY, t);
        drawSoil(g, cx, soilTop, t);
        drawPlant(g, plantX, soilTop, stemTop, p, t);
        drawOrb(g, plantX, orbY, p, t, active);
        drawOrbitRing(g, plantX, orbY, t, active, true);
        if (active) drawScanLine(g, cx, cy, t);
        drawHarvestBurst(g, plantX, orbY);
        drawGlass(g, cx, cy, t);
        drawHud(g, cx, cy, t);
        g.disableScissor();
    }

    private float displayProgress() {
        ItemStack crop = menu.getSlot(SuperCropCultivatorBlockEntity.CROP_SLOT).getItem();
        if (crop.isEmpty()) return 0.0F;
        // An idle but loaded chamber shows a resting sprout.
        return growing() ? Math.max(0.06F, shownProgress) : 0.06F;
    }

    private void drawNebulaClouds(GuiGraphics g, int cx, int cy, float t, boolean active) {
        float speed = active ? 3.0F : 1.0F;
        int[][] clouds = {{18, 20, 16, 0x6A3FC9}, {60, 34, 20, 0x2F8FCE}, {35, 52, 14, 0xAE70FF}, {72, 14, 12, 0xD05AC8}};
        for (int i = 0; i < clouds.length; i++) {
            int[] c = clouds[i];
            float drift = (c[0] + t * speed * (i % 2 == 0 ? 1.5F : -1.1F)) % (CH_W + 2 * c[2]);
            if (drift < 0) drift += CH_W + 2 * c[2];
            int px = cx + (int) drift - c[2];
            int py = cy + c[1] + (int) (Mth.sin(t * 0.6F + i) * 2);
            int baseAlpha = (int) ((active ? 34 : 20) * (0.75F + 0.25F * Mth.sin(t + i * 1.7F)));
            for (int r = c[2]; r > 2; r -= 3) {
                int alpha = baseAlpha * (c[2] - r + 3) / c[2] / 2 + 4;
                fillDisk(g, px, py, r, (alpha & 0xFF) << 24 | c[3]);
            }
        }
    }

    private void drawStars(GuiGraphics g, int cx, int cy, float t, boolean active) {
        for (int i = 0; i < STAR_COUNT; i++) {
            float fall = active ? (t * 4.0F * (0.5F + starPhase[i] / Mth.TWO_PI)) : 0.0F;
            int sx = cx + 1 + (int) (starX[i] * (CH_W - 3));
            int sy = cy + 1 + (int) ((starY[i] * (CH_H - SOIL_H - 4) + fall) % (CH_H - SOIL_H - 4));
            float tw = 0.5F + 0.5F * Mth.sin(t * (1.5F + i % 5 * 0.4F) + starPhase[i]);
            int alpha = (int) (60 + 195 * tw);
            int color = (alpha << 24) | (starColor[i] & 0xFFFFFF);
            g.fill(sx, sy, sx + 1, sy + 1, color);
            if (i % 6 == 0 && tw > 0.7F) {
                int glow = ((int) (alpha * 0.45F) << 24) | (starColor[i] & 0xFFFFFF);
                g.fill(sx - 1, sy, sx, sy + 1, glow);
                g.fill(sx + 1, sy, sx + 2, sy + 1, glow);
                g.fill(sx, sy - 1, sx + 1, sy, glow);
                g.fill(sx, sy + 1, sx + 1, sy + 2, glow);
            }
        }
    }

    /** Vanilla-style moist farmland bed on a glowing crystal tray. */
    private void drawSoil(GuiGraphics g, int cx, int soilTop, float t) {
        g.fill(cx, soilTop, cx + CH_W, soilTop + SOIL_H, 0xFF3B2716);
        for (int col = 0; col < CH_W; col++) {
            int hash = (col * 73 + 19) % 7;
            int shade = hash < 2 ? 0xFF2A1B0F : hash < 4 ? 0xFF4A321D : 0xFF3B2716;
            g.fill(cx + col, soilTop + 1 + (col * 5 % 3), cx + col + 1, soilTop + 2 + (col * 5 % 3), shade);
            if (col % 4 == 0) g.fill(cx + col, soilTop + 5, cx + col + 1, soilTop + 9, 0xFF2A1B0F);
            if ((col + 2) % 4 == 0) g.fill(cx + col, soilTop + 6, cx + col + 1, soilTop + 8, 0xFF56391F);
        }
        g.fill(cx, soilTop, cx + CH_W, soilTop + 1, 0xFF5A3D23);
        // Crystal tray rim with a slow light sweep.
        int rimY = soilTop + SOIL_H - 3;
        g.fill(cx, rimY, cx + CH_W, rimY + 3, 0xFF2F3E57);
        g.fill(cx, rimY, cx + CH_W, rimY + 1, 0xFF6F8CA4);
        int sweep = (int) ((t * 30.0F) % (CH_W + 20)) - 10;
        for (int i = -6; i <= 6; i++) {
            int px = cx + sweep + i;
            if (px < cx || px >= cx + CH_W) continue;
            int a = 200 - Math.abs(i) * 30;
            g.fill(px, rimY + 1, px + 1, rimY + 2, (a << 24) | (COLOR_CYAN & 0xFFFFFF));
        }
    }

    /** Pixel plant: a stem with paired leaves that sway, turning from leaf green to nebula black-violet. */
    private void drawPlant(GuiGraphics g, int px, int soilTop, int stemTop, float p, float t) {
        if (menu.getSlot(SuperCropCultivatorBlockEntity.CROP_SLOT).getItem().isEmpty()) return;
        int ripe = ripeColor(0xFFAE70FF);
        int stemColor = lerpColor(0xFF3E9A2E, lerpColor(0xFF1A0A2E, ripe, 0.35F), p);
        int leafColor = lerpColor(0xFF5BC236, lerpColor(0xFF1A0A2E, ripe, 0.6F), p);
        int leafEdge = lerpColor(0xFF8BE26A, ripe, p);
        int height = soilTop - stemTop;
        for (int i = 0; i < height; i++) {
            int yy = soilTop - 1 - i;
            int sway = (int) (Mth.sin(t * 1.8F + i * 0.25F) * (i / 14.0F));
            g.fill(px + sway, yy, px + sway + 2, yy + 1, stemColor);
            if (i > 0 && i % 6 == 3) {
                int len = Math.min(7, 2 + (height - i) / 4);
                int dir = (i / 6) % 2 == 0 ? 1 : -1;
                for (int k = 1; k <= len; k++) {
                    int lx = dir > 0 ? px + sway + 1 + k : px + sway - k;
                    int ly = yy - k / 2 + (int) (Mth.sin(t * 2.4F + i) * 0.8F);
                    g.fill(lx, ly, lx + 1, ly + 2, k == len ? leafEdge : leafColor);
                }
            }
        }
        // Root glow in the soil.
        int a = (int) (90 + 60 * Mth.sin(t * 3.0F));
        g.fill(px - 2, soilTop, px + 4, soilTop + 2, (a << 24) | (lerpColor(0xFF8BE26A, COLOR_PURPLE, p) & 0xFFFFFF));
    }

    /** Seed orb at the top of the stem; the variety being grown condenses inside it. */
    private void drawOrb(GuiGraphics g, int px, int orbY, float p, float t, boolean active) {
        ItemStack crop = menu.getSlot(SuperCropCultivatorBlockEntity.CROP_SLOT).getItem();
        if (crop.isEmpty()) return;
        float pulse = 0.5F + 0.5F * Mth.sin(t * (active ? 5.0F : 1.5F));
        int radius = 2 + (int) (p * 9);
        int core = lerpColor(0xFFC8FF9E, 0xFF1A0A2E, p);
        int halo = lerpColor(0xFF8BE26A, ripeColor(0xFFAE70FF), p) & 0xFFFFFF;
        for (int r = radius + 6; r > radius; r--) {
            int alpha = (int) ((radius + 6 - r) * (14 + 10 * pulse));
            fillDisk(g, px + 1, orbY, r, Math.min(255, alpha) << 24 | halo);
        }
        fillDisk(g, px + 1, orbY, radius, core);
        if (radius > 3) {
            // Inner swirl of the black nebula.
            for (int i = 0; i < 6; i++) {
                float ang = t * 2.2F + i * Mth.TWO_PI / 6;
                int sx = px + 1 + (int) (Mth.cos(ang) * (radius - 2) * 0.7F);
                int sy = orbY + (int) (Mth.sin(ang) * (radius - 2) * 0.7F);
                g.fill(sx, sy, sx + 1, sy + 1, i % 2 == 0 ? 0xFFD899FF : 0xFF68DCFF);
            }
            g.fill(px - radius / 2, orbY - radius / 2, px - radius / 2 + 2, orbY - radius / 2 + 1, 0xAAFFFFFF);
        }

        SuperCultivationRecipes.Recipe recipe = previewRecipe();
        if (recipe != null && p > 0.45F) {
            float s = Mth.clamp((p - 0.45F) / 0.55F, 0.0F, 1.0F);
            float scale = 0.35F + 0.65F * s;
            float bob = Mth.sin(t * 2.0F) * 1.5F;
            g.pose().pushPose();
            g.pose().translate(px + 1, orbY + bob, 40.0F);
            g.pose().scale(scale, scale, 1.0F);
            g.renderItem(recipe.resultStack(), -8, -8);
            g.pose().popPose();
        }
    }

    private void drawOrbitRing(GuiGraphics g, int px, int orbY, float t, boolean active, boolean front) {
        if (menu.getSlot(SuperCropCultivatorBlockEntity.CROP_SLOT).getItem().isEmpty()) return;
        float spin = t * (active ? 1.6F : 0.4F);
        int rx = 22 + (int) (displayProgress() * 8), ry = 6;
        for (int i = 0; i < 28; i++) {
            float ang = spin + i * Mth.TWO_PI / 28;
            float depth = Mth.sin(ang);
            if (front != depth > 0) continue;
            int x = px + 1 + (int) (Mth.cos(ang) * rx);
            int y = orbY + (int) (depth * ry);
            int alpha = front ? 220 : 90;
            int color = i % 7 == 0 ? COLOR_CYAN : COLOR_PURPLE;
            g.fill(x, y, x + 1, y + 1, (alpha << 24) | (color & 0xFFFFFF));
            if (i % 7 == 0 && front) g.fill(x - 1, y, x + 2, y + 1, (alpha / 2 << 24) | (color & 0xFFFFFF));
        }
    }

    /** Dotted energy beams from the three inlets to the orb, with travelling sparks. */
    private void drawBeams(GuiGraphics g, int cx, int cy, int px, int orbY, float t) {
        int inlets = 1 + SuperCropCultivatorMenu.MATERIAL_Y.length;
        int[] inletY = new int[inlets];
        int[] colors = new int[inlets];
        boolean[] fed = new boolean[inlets];
        inletY[0] = topPos + SuperCropCultivatorMenu.CROP_Y + 7;
        colors[0] = 0xFF8BE26A;
        fed[0] = true;
        for (int i = 0; i < SuperCropCultivatorMenu.MATERIAL_Y.length; i++) {
            ItemStack material = menu.getSlot(SuperCropCultivatorBlockEntity.MATERIAL_A_SLOT + i).getItem();
            inletY[i + 1] = topPos + SuperCropCultivatorMenu.MATERIAL_Y[i] + 7;
            colors[i + 1] = materialColor(material, t);
            fed[i + 1] = !material.isEmpty();
        }
        for (int b = 0; b < inlets; b++) {
            if (!fed[b]) continue;
            int sx = cx, sy = inletY[b];
            int steps = 26;
            for (int i = 0; i <= steps; i++) {
                float f = i / (float) steps;
                // Quadratic curve bulging upward.
                float mx = Mth.lerp(0.5F, sx, px), my = Math.min(sy, orbY) - 10;
                float x = (1 - f) * (1 - f) * sx + 2 * (1 - f) * f * mx + f * f * (px + 1);
                float y = (1 - f) * (1 - f) * sy + 2 * (1 - f) * f * my + f * f * orbY;
                float spark = ((f - t * 0.9F - b * 0.25F) % 1.0F + 1.0F) % 1.0F;
                int alpha = spark < 0.12F ? 255 : (i % 2 == 0 ? 70 : 0);
                if (alpha == 0) continue;
                g.fill((int) x, (int) y, (int) x + 1, (int) y + 1, (alpha << 24) | (colors[b] & 0xFFFFFF));
            }
        }
    }

    private void drawScanLine(GuiGraphics g, int cx, int cy, float t) {
        int span = CH_H - SOIL_H;
        int sy = cy + (int) ((t * 22.0F) % span);
        g.fill(cx, sy, cx + CH_W, sy + 1, 0x5568DCFF);
        g.fill(cx, sy - 1, cx + CH_W, sy, 0x2268DCFF);
        g.fill(cx, sy + 1, cx + CH_W, sy + 2, 0x2268DCFF);
    }

    private float flashAmount() {
        long elapsed = Util.getMillis() - harvestFlashStart;
        return elapsed < 0 || elapsed > 900 ? 0.0F : 1.0F - elapsed / 900.0F;
    }

    private void drawHarvestBurst(GuiGraphics g, int px, int orbY) {
        float f = flashAmount();
        if (f <= 0.0F) return;
        float age = 1.0F - f;
        g.fill(leftPos + CH_X, topPos + CH_Y, leftPos + CH_X + CH_W, topPos + CH_Y + CH_H, ((int) (f * f * 160) << 24) | 0xF4E6FF);
        int ring = (int) (age * 46);
        for (int i = 0; i < 40; i++) {
            float ang = i * Mth.TWO_PI / 40;
            int x = px + (int) (Mth.cos(ang) * ring);
            int y = orbY + (int) (Mth.sin(ang) * ring * 0.8F);
            g.fill(x, y, x + 2, y + 2, ((int) (f * 255) << 24) | (i % 2 == 0 ? 0xD899FF : 0x9DEFFF));
        }
        for (int i = 0; i < 14; i++) {
            float ang = i * 2.399F;
            float dist = age * (18 + i * 2);
            int x = px + (int) (Mth.cos(ang) * dist);
            int y = orbY + (int) (Mth.sin(ang) * dist) + (int) (age * age * 20);
            drawStar(g, x, y, ((int) (f * 255) << 24) | 0xFFF2A8);
        }
    }

    private void drawGlass(GuiGraphics g, int cx, int cy, float t) {
        g.fill(cx + 3, cy + 2, cx + 5, cy + CH_H - SOIL_H - 4, 0x18FFFFFF);
        g.fill(cx + 7, cy + 2, cx + 8, cy + CH_H - SOIL_H - 10, 0x12FFFFFF);
        g.fill(cx + 2, cy + 1, cx + CH_W - 2, cy + 2, 0x22FFFFFF);
        // A soft glint that slides across the glass every few seconds.
        float phase = (t % 6.0F) / 6.0F;
        int gx = cx - 20 + (int) (phase * (CH_W + 40));
        for (int i = 0; i < 6; i++) {
            g.fill(gx + i, cy + 1, gx + i + 1, cy + CH_H - SOIL_H, (0x10 - i * 2) << 24 | 0xFFFFFF);
        }
    }

    private void drawHud(GuiGraphics g, int cx, int cy, float t) {
        int status = menu.getStatus();
        Component label = statusText(status);
        int color = switch (status) {
            case SuperCropCultivatorBlockEntity.STATUS_GROWING -> 0xFF9DEFFF;
            case SuperCropCultivatorBlockEntity.STATUS_NOT_SUPER, SuperCropCultivatorBlockEntity.STATUS_OUTPUT_FULL -> 0xFFFF7A7A;
            case SuperCropCultivatorBlockEntity.STATUS_MISSING_MATERIALS -> 0xFFFFD27A;
            default -> 0xFFB6D2DF;
        };
        if (status != SuperCropCultivatorBlockEntity.STATUS_GROWING && (int) (t * 2) % 2 == 0 && status != 0) {
            color = (color & 0x00FFFFFF) | 0xB0000000;
        }
        g.fill(cx, cy, cx + CH_W, cy + 11, 0x88050814);
        IndustrialGuiStyle.drawFittedComponent(g, font, label, cx + 3, cy + 2, CH_W - 30, color, false);
        if (status == SuperCropCultivatorBlockEntity.STATUS_GROWING) {
            int secs = menu.getSecondsLeft();
            String time = String.format("%d:%02d", secs / 60, secs % 60);
            g.drawString(font, time, cx + CH_W - 3 - font.width(time), cy + 2, 0xFFFFFFFF, true);
        }
    }

    // ------------------------------------------------------------------ helix gauge

    private void drawHelix(GuiGraphics g, int hx, int hy, float t, boolean active) {
        g.fill(hx - 1, hy - 3, hx + HX_W + 1, hy + HX_H + 3, COLOR_STEEL_DARK);
        g.fill(hx, hy - 2, hx + HX_W, hy + HX_H + 2, COLOR_STEEL);
        g.fill(hx + 1, hy, hx + HX_W - 1, hy + HX_H, 0xFF0B1222);
        float p = growing() ? shownProgress : 0.0F;
        int filledRows = (int) (p * HX_H);
        float phase = t * (active ? 4.0F : 0.6F);
        int mid = hx + HX_W / 2;
        for (int row = 0; row < HX_H; row++) {
            int yy = hy + HX_H - 1 - row;
            boolean filled = row < filledRows;
            float a = row * 0.32F + phase;
            int x1 = mid + Math.round(Mth.sin(a) * 2.6F);
            int x2 = mid + Math.round(Mth.sin(a + Mth.PI) * 2.6F);
            boolean frontA = Mth.cos(a) > 0;
            int colA = filled ? COLOR_CYAN : 0xFF2F3E57;
            int colB = filled ? COLOR_PURPLE : 0xFF2A2A48;
            if (row % 5 == 0) {
                int rung = filled ? 0x99FFFFFF : 0x40FFFFFF;
                g.fill(Math.min(x1, x2), yy, Math.max(x1, x2) + 1, yy + 1, rung);
            }
            if (frontA) {
                g.fill(x2, yy, x2 + 1, yy + 1, colB);
                g.fill(x1, yy, x1 + 1, yy + 1, colA);
            } else {
                g.fill(x1, yy, x1 + 1, yy + 1, colA);
                g.fill(x2, yy, x2 + 1, yy + 1, colB);
            }
        }
        if (filledRows > 0 && filledRows < HX_H) {
            int yy = hy + HX_H - filledRows;
            g.fill(hx + 1, yy, hx + HX_W - 1, yy + 1, 0xCCFFFFFF);
        }
    }

    // ------------------------------------------------------------------ small pieces

    private void drawTube(GuiGraphics g, int x, int y, int length, float t, boolean active, int color, float offset) {
        if (length <= 0) return;
        g.fill(x, y, x + length, y + 5, COLOR_STEEL_DARK);
        g.fill(x, y + 1, x + length, y + 4, 0xFF3D4F69);
        g.fill(x, y + 2, x + length, y + 3, 0xFF11192C);
        g.fill(x, y + 1, x + length, y + 2, 0xFF6F8CA4);
        if (active) {
            float pos = ((t * 1.2F + offset) % 1.0F) * (length + 4) - 2;
            for (int i = 0; i < 3; i++) {
                int px = x + (int) pos - i;
                if (px < x || px >= x + length) continue;
                g.fill(px, y + 2, px + 1, y + 3, ((255 - i * 80) << 24) | (color & 0xFFFFFF));
            }
        }
    }

    private void drawGhostItems(GuiGraphics g, int x, int y) {
        ItemStack crop = menu.getSlot(SuperCropCultivatorBlockEntity.CROP_SLOT).getItem();
        SuperCultivationRecipes.Recipe recipe = previewRecipe();
        if (recipe == null) return;
        if (crop.isEmpty()) {
            ghost(g, recipe.displayCrop(), x + SuperCropCultivatorMenu.CROP_X, y + SuperCropCultivatorMenu.CROP_Y);
        }
        // Show the materials still missing as faded items in the empty material slots.
        java.util.List<ItemStack> missing = new ArrayList<>(recipe.materialStacks());
        for (int slot = SuperCropCultivatorBlockEntity.MATERIAL_A_SLOT; slot <= SuperCropCultivatorBlockEntity.MATERIAL_C_SLOT; slot++) {
            ItemStack in = menu.getSlot(slot).getItem();
            missing.removeIf(stack -> !in.isEmpty() && stack.is(in.getItem()));
        }
        for (int i = 0; i < SuperCropCultivatorMenu.MATERIAL_Y.length && !missing.isEmpty(); i++) {
            if (menu.getSlot(SuperCropCultivatorBlockEntity.MATERIAL_A_SLOT + i).getItem().isEmpty()) {
                ghost(g, missing.remove(0), x + SuperCropCultivatorMenu.MATERIAL_X, y + SuperCropCultivatorMenu.MATERIAL_Y[i]);
            }
        }
        // Hint that containers come back here.
        if (!recipe.remainders().isEmpty() && menu.getSlot(SuperCropCultivatorBlockEntity.BYPRODUCT_SLOT).getItem().isEmpty()) {
            ghost(g, recipe.remainders().get(0), x + SuperCropCultivatorMenu.BYPRODUCT_X, y + SuperCropCultivatorMenu.BYPRODUCT_Y);
        }
    }

    private static void ghost(GuiGraphics g, ItemStack stack, int x, int y) {
        g.renderFakeItem(stack, x, y);
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 250.0F);
        g.fill(x, y, x + 16, y + 16, 0xA88B8B8B);
        g.pose().popPose();
    }

    private static void drawStar(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y - 2, x + 1, y + 3, color);
        g.fill(x - 2, y, x + 3, y + 1, color);
    }

    private static void fillDisk(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt(Math.max(0, r * r - dy * dy));
            g.fill(cx - half, cy + dy, cx + half, cy + dy + 1, color);
        }
    }

    private static int lerpColor(int a, int b, float f) {
        f = Mth.clamp(f, 0.0F, 1.0F);
        int aa = a >>> 24, ar = a >> 16 & 0xFF, ag = a >> 8 & 0xFF, ab = a & 0xFF;
        int ba = b >>> 24, br = b >> 16 & 0xFF, bg = b >> 8 & 0xFF, bb = b & 0xFF;
        return (int) (aa + (ba - aa) * f) << 24 | (int) (ar + (br - ar) * f) << 16 | (int) (ag + (bg - ag) * f) << 8 | (int) (ab + (bb - ab) * f);
    }

    private static int materialColor(ItemStack stack, float t) {
        net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return switch (id.getPath()) {
            case "comet_essence" -> 0xFFB36BFF;
            case "prism_pod" -> Mth.hsvToRgb((t * 0.4F) % 1.0F, 0.55F, 1.0F) | 0xFF000000;
            case "dragon_breath" -> 0xFFE070FF;
            case "neonbulb" -> 0xFF7DFF6A;
            case "omni_potion" -> Mth.hsvToRgb((t * 0.6F) % 1.0F, 0.5F, 1.0F) | 0xFF000000;
            default -> COLOR_CYAN;
        };
    }

    private Component statusText(int status) {
        String key = switch (status) {
            case SuperCropCultivatorBlockEntity.STATUS_NOT_SUPER -> "not_super";
            case SuperCropCultivatorBlockEntity.STATUS_MISSING_MATERIALS -> "missing";
            case SuperCropCultivatorBlockEntity.STATUS_OUTPUT_FULL -> "output_full";
            case SuperCropCultivatorBlockEntity.STATUS_GROWING -> "growing";
            default -> "idle";
        };
        return Component.translatable("gui.industrialcrops.super_crop_cultivator.status." + key);
    }

    // ------------------------------------------------------------------ foreground

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (inside(mouseX, mouseY, leftPos + CH_X, topPos + CH_Y, CH_W, CH_H)) {
            graphics.renderComponentTooltip(font, chamberTooltip(), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, leftPos + HX_X, topPos + HX_Y, HX_W, HX_H)) {
            int pct = growing() ? Math.round(menu.getProgressFraction() * 100) : 0;
            graphics.renderTooltip(font, Component.translatable("gui.industrialcrops.super_crop_cultivator.progress", pct), mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    private List<Component> chamberTooltip() {
        List<Component> lines = new ArrayList<>();
        lines.add(statusText(menu.getStatus()).copy().withStyle(ChatFormatting.AQUA));
        ItemStack crop = menu.getSlot(SuperCropCultivatorBlockEntity.CROP_SLOT).getItem();
        SuperCultivationRecipes.Recipe recipe = crop.isEmpty() ? null : previewRecipe();
        if (recipe != null) {
            lines.add(Component.translatable("gui.industrialcrops.super_crop_cultivator.result", recipe.resultStack().getHoverName())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            MutableComponent joined = Component.empty();
            List<ItemStack> materials = recipe.materialStacks();
            for (int i = 0; i < materials.size(); i++) {
                if (i > 0) joined.append(" + ");
                joined.append(materials.get(i).getHoverName());
            }
            MutableComponent needs = Component.translatable("gui.industrialcrops.super_crop_cultivator.needs", joined);
            lines.add(needs.withStyle(ChatFormatting.GRAY));
            if (!recipe.remainders().isEmpty()) {
                lines.add(Component.translatable("gui.industrialcrops.super_crop_cultivator.returns", recipe.remainders().get(0).getHoverName())
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            lines.add(Component.translatable("gui.industrialcrops.super_crop_cultivator.hint").withStyle(ChatFormatting.GRAY));
        }
        if (growing()) {
            int secs = menu.getSecondsLeft();
            lines.add(Component.translatable("gui.industrialcrops.super_crop_cultivator.time", String.format("%d:%02d", secs / 60, secs % 60))
                    .withStyle(ChatFormatting.WHITE));
        }
        return lines;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
