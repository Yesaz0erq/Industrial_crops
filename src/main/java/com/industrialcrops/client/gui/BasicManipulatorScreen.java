package com.industrialcrops.client.gui;

import com.industrialcrops.recipe.ManipulatorIngredient;
import com.industrialcrops.recipe.ManipulatorRecipeDisplay;
import com.industrialcrops.recipe.ManipulatorRecipes;
import com.industrialcrops.screen.BasicManipulatorMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** A recipe browser styled as an oversized vanilla container rather than a separate application UI. */
public final class BasicManipulatorScreen extends IndustrialContainerScreen<BasicManipulatorMenu> {
    private static final ResourceLocation BACKGROUND = IndustrialGuiStyle.containerTexture("basic_manipulation_device");
    private static final int ENTRIES_PER_PAGE = 5;
    private static final int LIST_X = 111;
    private static final int LIST_Y = 31;
    private static final int LIST_WIDTH = 86;
    private static final int LIST_ROW_HEIGHT = 19;
    private static final int PAGE_LEFT_X = 116;
    private static final int PAGE_RIGHT_X = 193;
    private static final int PAGE_Y = 139;
    private static final int PAGE_BUTTON_SIZE = 18;
    private static final int CRAFT_BUTTON_X = 206;
    private static final int CRAFT_BUTTON_Y = 130;
    private static final int CRAFT_BUTTON_WIDTH = 90;
    private static final int CRAFT_BUTTON_HEIGHT = 18;
    private final List<ManipulatorRecipeDisplay> recipes;

    private int selectedRecipe;
    private int page;
    private Button previousPageButton;
    private Button nextPageButton;

    public BasicManipulatorScreen(BasicManipulatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.recipes = menu.recipes();
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
        IndustrialGuiStyle.drawBackground(graphics, BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
        drawDirectoryRows(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        ManipulatorRecipeDisplay recipe = getSelectedRecipe();
        ItemStack output = recipe.output();
        String fittedTitle = IndustrialGuiStyle.fitText(font, title, 286);
        graphics.drawString(font, fittedTitle, (imageWidth - font.width(fittedTitle)) / 2, 7, IndustrialGuiStyle.TEXT, false);
        drawCentered(graphics, Component.translatable("gui.industrialcrops.manipulator.materials"), 251, 27, IndustrialGuiStyle.TEXT);
        drawScaledItem(graphics, output, 54, 62, 2.0F);
        drawProductName(graphics, output);
        drawDirectory(graphics);
        drawIngredients(graphics, recipe);
        drawPageLabel(graphics);
        graphics.drawString(font, playerInventoryTitle, 70, 146, IndustrialGuiStyle.MUTED_TEXT, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderDisplayTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (handleCatalogClick(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawProductName(GuiGraphics graphics, ItemStack output) {
        IndustrialGuiStyle.drawFittedString(graphics, font, output.getHoverName().getString(), 10, 95,
                88, IndustrialGuiStyle.TEXT, false);
    }

    private void drawDirectoryRows(GuiGraphics graphics) {
        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, recipes.size());
        for (int index = start; index < end; index++) {
            int row = index - start;
            int rowY = LIST_Y + row * LIST_ROW_HEIGHT;
            int fill = index == selectedRecipe ? 0xFFCECECE : IndustrialGuiStyle.SLOT;
            IndustrialGuiStyle.drawPanel(graphics, leftPos + LIST_X, topPos + rowY, LIST_WIDTH, 18, fill);
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
            String name = font.plainSubstrByWidth(entry.output().getHoverName().getString(), 60);
            int color = index == selectedRecipe ? IndustrialGuiStyle.TEXT : IndustrialGuiStyle.MUTED_TEXT;
            graphics.drawString(font, name, LIST_X + 22 + Math.max(0, (60 - font.width(name)) / 2), rowY + 5, color, false);
        }
    }

    private void drawIngredients(GuiGraphics graphics, ManipulatorRecipeDisplay recipe) {
        int[][] slots = {{216, 50}, {254, 50}, {216, 88}, {254, 88}};
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
        drawCentered(graphics, Component.literal((page + 1) + "/" + totalPages), (PAGE_LEFT_X + PAGE_RIGHT_X) / 2, PAGE_Y + 3, IndustrialGuiStyle.TEXT);
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
        if (isWithin(relativeX, relativeY, 14, 34, 80, 56)) {
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

        int[][] ingredientSlots = {{216, 50}, {254, 50}, {216, 88}, {254, 88}};
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


