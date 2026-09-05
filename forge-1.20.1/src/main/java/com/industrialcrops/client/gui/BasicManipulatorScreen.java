package com.industrialcrops.client.gui;

import com.industrialcrops.recipe.ManipulatorIngredient;
import com.industrialcrops.recipe.ManipulatorRecipeDisplay;
import com.industrialcrops.recipe.ManipulatorRecipes;
import com.industrialcrops.screen.BasicManipulatorMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Shared recipe browser with tier-specific metal panels and matching display hitboxes. */
public final class BasicManipulatorScreen extends IndustrialContainerScreen<BasicManipulatorMenu> {
    private final ResourceLocation background;
    private static final int[][] INGREDIENT_SLOTS = {{218, 48}, {262, 48}, {218, 88}, {262, 88}};
    private static final int ENTRIES_PER_PAGE = 5;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 36;
    private static final int LIST_WIDTH = 108;
    private static final int LIST_ROW_HEIGHT = 18;
    private static final int PAGE_LEFT_X = 20;
    private static final int PAGE_RIGHT_X = 104;
    private static final int PAGE_Y = 135;
    private static final int PAGE_BUTTON_SIZE = 18;
    private static final int CRAFT_BUTTON_X = 214;
    private static final int CRAFT_BUTTON_Y = 128;
    private static final int CRAFT_BUTTON_WIDTH = 80;
    private static final int CRAFT_BUTTON_HEIGHT = 18;
    private final List<ManipulatorRecipeDisplay> recipes;

    private int selectedRecipe;
    private int page;
    private Button previousPageButton;
    private Button nextPageButton;

    public BasicManipulatorScreen(BasicManipulatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.recipes = menu.recipes();
        this.background = IndustrialGuiStyle.containerTexture(menu.isAdvanced()
                ? "advanced_manipulation_device" : "basic_manipulation_device");
        imageWidth = 302;
        imageHeight = 240;
        inventoryLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 0;
        previousPageButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(
                        leftPos + PAGE_LEFT_X - PAGE_BUTTON_SIZE / 2,
                        topPos + PAGE_Y - PAGE_BUTTON_SIZE / 2,
                        PAGE_BUTTON_SIZE,
                        PAGE_BUTTON_SIZE
                )
                .build());
        nextPageButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(
                        leftPos + PAGE_RIGHT_X - PAGE_BUTTON_SIZE / 2,
                        topPos + PAGE_Y - PAGE_BUTTON_SIZE / 2,
                        PAGE_BUTTON_SIZE,
                        PAGE_BUTTON_SIZE
                )
                .build());
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.industrialcrops.manipulator.craft"),
                        button -> craftSelectedRecipe()
                )
                .bounds(
                        leftPos + CRAFT_BUTTON_X,
                        topPos + CRAFT_BUTTON_Y,
                        CRAFT_BUTTON_WIDTH,
                        CRAFT_BUTTON_HEIGHT
                )
                .build());
        updatePageButtons();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawBackground(graphics, background, leftPos, topPos, imageWidth, imageHeight);
        drawDirectoryRows(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        ManipulatorRecipeDisplay recipe = getSelectedRecipe();
        ItemStack output = recipe.output();
        String fittedTitle = IndustrialGuiStyle.fitText(font, title, 260);
        graphics.drawString(font, fittedTitle, (imageWidth - font.width(fittedTitle)) / 2, 7, IndustrialGuiStyle.TEXT, false);
        drawCentered(graphics, Component.translatable("gui.industrialcrops.manipulator.materials"), 254, 25, IndustrialGuiStyle.TEXT);
        drawCentered(graphics, Component.translatable("gui.industrialcrops.manipulator.products"), 62, 25, IndustrialGuiStyle.TEXT);
        drawCentered(graphics, Component.translatable("gui.industrialcrops.manipulator.product_name"), 164, 25, IndustrialGuiStyle.TEXT);
        drawScaledItem(graphics, output, 164, 67, 2.5F);
        drawProductName(graphics, output);
        drawDirectory(graphics);
        drawIngredients(graphics, recipe);
        drawPageLabel(graphics);
        graphics.drawString(font, playerInventoryTitle, 70, 146, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderDisplayTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && handleCatalogClick(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawProductName(GuiGraphics graphics, ItemStack output) {
        List<FormattedCharSequence> lines = font.split(output.getHoverName(), 76);
        for (int line = 0; line < Math.min(3, lines.size()); line++) {
            FormattedCharSequence text = lines.get(line);
            graphics.drawString(font, text, 164 - font.width(text) / 2, 99 + line * 10,
                    IndustrialGuiStyle.TEXT, false);
        }
    }

    private void drawDirectoryRows(GuiGraphics graphics) {
        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, recipes.size());
        for (int index = start; index < end; index++) {
            int row = index - start;
            int rowY = LIST_Y + row * LIST_ROW_HEIGHT;
            int fill = index == selectedRecipe ? (menu.isAdvanced() ? 0xFFE1D5AD : 0xFFD8BDA7) : 0xFFAAAAAA;
            IndustrialGuiStyle.drawPanel(graphics, leftPos + LIST_X, topPos + rowY, LIST_WIDTH, 18, fill);
            if (index == selectedRecipe) graphics.fill(leftPos + LIST_X + 1, topPos + rowY + 1,
                    leftPos + LIST_X + 3, topPos + rowY + 17, menu.isAdvanced() ? 0xFFAE842F : 0xFF99512F);
        }
    }

    private void drawDirectory(GuiGraphics graphics) {
        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, recipes.size());
        for (int index = start; index < end; index++) {
            int row = index - start;
            int rowY = LIST_Y + row * LIST_ROW_HEIGHT;
            ManipulatorRecipeDisplay entry = recipes.get(index);
            graphics.renderItem(entry.output(), LIST_X + 4, rowY + 1);
            String name = font.plainSubstrByWidth(entry.output().getHoverName().getString(), LIST_WIDTH - 27);
            int color = index == selectedRecipe ? IndustrialGuiStyle.TEXT : IndustrialGuiStyle.MUTED_TEXT;
            graphics.drawString(font, name, LIST_X + 23, rowY + 5, color, false);
        }
    }

    private void drawIngredients(GuiGraphics graphics, ManipulatorRecipeDisplay recipe) {
        int[][] slots = INGREDIENT_SLOTS;
        for (int i = 0; i < recipe.ingredients().size() && i < slots.length; i++) {
            ManipulatorIngredient ingredient = recipe.ingredients().get(i);
            ItemStack stack = ingredient.displayStack();
            graphics.renderItem(stack, slots[i][0] + 1, slots[i][1] + 1);
            String count = "x" + stack.getCount();
            graphics.drawString(font, count, slots[i][0] + (18 - font.width(count)) / 2, slots[i][1] + 20, IndustrialGuiStyle.TEXT, false);
        }
    }

    private void drawPageLabel(GuiGraphics graphics) {
        int totalPages = Math.max(1, (int) Math.ceil((double) recipes.size() / ENTRIES_PER_PAGE));
        drawCentered(graphics, Component.literal((page + 1) + "/" + totalPages), (PAGE_LEFT_X + PAGE_RIGHT_X) / 2, PAGE_Y - 4, IndustrialGuiStyle.TEXT);
    }

    private ManipulatorRecipeDisplay getSelectedRecipe() {
        selectedRecipe = Math.max(0, Math.min(selectedRecipe, recipes.size() - 1));
        return recipes.isEmpty() ? ManipulatorRecipes.ironDeviceCasing() : recipes.get(selectedRecipe);
    }

    private void changePage(int direction) {
        int totalPages = Math.max(1, (int) Math.ceil((double) recipes.size() / ENTRIES_PER_PAGE));
        int newPage = Math.max(0, Math.min(totalPages - 1, page + direction));
        if (newPage != page) {
            page = newPage;
            selectedRecipe = Math.min(page * ENTRIES_PER_PAGE, recipes.size() - 1);
        }
        updatePageButtons();
    }

    private boolean handleCatalogClick(double mouseX, double mouseY) {
        int relativeX = (int) mouseX - leftPos;
        int relativeY = (int) mouseY - topPos;
        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, recipes.size());
        for (int index = start; index < end; index++) {
            int row = index - start;
            if (isWithin(relativeX, relativeY, LIST_X, LIST_Y + row * LIST_ROW_HEIGHT, LIST_WIDTH, LIST_ROW_HEIGHT)) {
                selectedRecipe = index;
                return true;
            }
        }
        return false;
    }

    private void craftSelectedRecipe() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, selectedRecipe);
        }
    }

    private void renderDisplayTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int relativeX = mouseX - leftPos;
        int relativeY = mouseY - topPos;
        ManipulatorRecipeDisplay recipe = getSelectedRecipe();
        if (isWithin(relativeX, relativeY, 124, 41, 80, 50)) {
            graphics.renderTooltip(font, recipe.output(), mouseX, mouseY);
            return;
        }

        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, recipes.size());
        for (int index = start; index < end; index++) {
            int row = index - start;
            if (isWithin(relativeX, relativeY, LIST_X, LIST_Y + row * LIST_ROW_HEIGHT, LIST_WIDTH, 18)) {
                graphics.renderTooltip(font, recipes.get(index).output(), mouseX, mouseY);
                return;
            }
        }

        int[][] ingredientSlots = INGREDIENT_SLOTS;
        for (int index = 0; index < recipe.ingredients().size() && index < ingredientSlots.length; index++) {
            int[] slot = ingredientSlots[index];
            if (isWithin(relativeX, relativeY, slot[0], slot[1], 18, 18)) {
                graphics.renderTooltip(font, recipe.ingredients().get(index).displayStack(), mouseX, mouseY);
                return;
            }
        }
    }

    private void updatePageButtons() {
        if (previousPageButton == null || nextPageButton == null) {
            return;
        }
        int totalPages = Math.max(1, (int) Math.ceil((double) recipes.size() / ENTRIES_PER_PAGE));
        previousPageButton.active = page > 0;
        nextPageButton.active = page < totalPages - 1;
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y, int color) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    private void drawScaledItem(GuiGraphics graphics, ItemStack stack, int centerX, int centerY, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.renderItem(stack, -8, -8);
        graphics.pose().popPose();
    }

    private static boolean isWithin(int mouseX, int mouseY, int areaX, int areaY, int width, int height) {
        return mouseX >= areaX && mouseX < areaX + width && mouseY >= areaY && mouseY < areaY + height;
    }
}

