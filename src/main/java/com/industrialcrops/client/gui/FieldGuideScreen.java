package com.industrialcrops.client.gui;
import com.industrialcrops.recipe.CrystalWorkbenchRecipes;
import com.industrialcrops.recipe.GourdModificationRecipeDisplay;
import com.industrialcrops.recipe.GourdModificationRecipes;
import com.industrialcrops.recipe.ManipulatorIngredient;
import com.industrialcrops.recipe.ManipulatorRecipeDisplay;
import com.industrialcrops.recipe.ManipulatorRecipes;
import com.industrialcrops.recipe.MixerRecipeDisplay;
import com.industrialcrops.recipe.MixerRecipes;
import com.industrialcrops.recipe.ProcessorProgrammingRecipes;
import com.industrialcrops.recipe.RootOreExtractorRecipeDisplay;
import com.industrialcrops.recipe.RootOreExtractorRecipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
public final class FieldGuideScreen extends Screen {
   private static final int WIDTH = 340;
   private static final int HEIGHT = 230;
   private static final int TAB_WIDTH = 24;
   private static final int TAB_STEP = 19;
   private static final int RIGHT_WIDTH = 68;
   private static final int LINE_HEIGHT = 9;
   private static final int SCREEN_EDGE = -14012623;
   private static final int SCREEN_FILL = -11840681;
   private static final int SCREEN_SHADE = -12498612;
   private static final int SCREEN_TEXT = -1643282;
   private static final int TAB_FILL = -6380636;
   private static final int TAB_HOVER = -5327949;
   private static final int COLUMN_FILL = -4933448;
   private static int rememberedChapter;
   private static int rememberedPage;
   private int left;
   private int top;
   private int chapter;
   private int page;
   private int contentX;
   private int contentY;
   private int contentW;
   private int contentH;
   private int screenX;
   private int screenY;
   private int screenW;
   private int screenH;
   private int textX;
   private int textY;
   private int textW;
   private int textH;
   private int columnX;
   private int columnY;
   private int columnW;
   private int columnH;
   private int navY;
   private List<FormattedCharSequence> lines = List.of();
   private double scroll;
   private int maxScroll;
   private boolean draggingScrollbar;
   private FieldGuideScreen.RecipeView recipe;
   private boolean recipeRequested;
   private List<ItemStack> related = List.of();
   private ItemStack hoveredStack = ItemStack.EMPTY;
   private List<Component> hoveredText = List.of();
   public FieldGuideScreen() {
      super(Component.translatable("item.industrialcrops.field_guide"));
      this.chapter = Mth.clamp(rememberedChapter, 0, FieldGuideContent.CHAPTERS.size() - 1);
      this.page = Mth.clamp(rememberedPage, 0, this.currentChapter().pages().size() - 1);
   }
   protected void init() {
      this.left = (this.width - 340) / 2;
      this.top = (this.height - 230) / 2;
      this.contentX = this.left + 34;
      this.contentY = this.top + 24;
      this.contentW = 300;
      this.contentH = 178;
      this.screenX = this.contentX + 5;
      this.screenY = this.contentY + 21;
      this.screenW = this.contentW - 10 - 68 - 4;
      this.screenH = this.contentH - 21 - 5;
      this.textX = this.screenX + 7;
      this.textY = this.screenY + 6;
      this.textW = this.screenW - 7 - 12;
      this.textH = this.screenH - 12;
      this.columnX = this.screenX + this.screenW + 4;
      this.columnY = this.screenY;
      this.columnW = 68;
      this.columnH = this.screenH;
      this.navY = this.top + 230 - 25;
      this.loadPage();
   }
   public boolean isPauseScreen() {
      return false;
   }
   public void removed() {
      rememberedChapter = this.chapter;
      rememberedPage = this.page;
      super.removed();
   }
   private FieldGuideContent.Chapter currentChapter() {
      return FieldGuideContent.CHAPTERS.get(this.chapter);
   }
   private FieldGuideContent.Page currentPage() {
      return this.currentChapter().pages().get(this.page);
   }
   private void loadPage() {
      FieldGuideContent.Page current = this.currentPage();
      List<FormattedCharSequence> wrapped = new ArrayList<>();
      String body = Component.translatable(current.bodyKey()).getString();
      for (String paragraph : body.split("\n", -1)) {
         if (paragraph.isEmpty()) {
            wrapped.add(FormattedCharSequence.EMPTY);
         } else {
            wrapped.addAll(this.font.split(Component.literal(paragraph), this.textW));
         }
      }
      this.lines = wrapped;
      this.maxScroll = Math.max(0, this.lines.size() * 9 - this.textH);
      this.scroll = 0.0;
      this.recipeRequested = current.recipe() != null;
      ItemStack target = this.recipeRequested ? stackOf(current.recipe()) : ItemStack.EMPTY;
      this.recipe = target.isEmpty() ? null : FieldGuideScreen.RecipeView.find(target.getItem());
      if (this.recipeRequested && this.recipe == null && !target.isEmpty()) {
         this.recipe = FieldGuideScreen.RecipeView.worldOnly(target);
      }
      List<ItemStack> stacks = new ArrayList<>();
      for (String id : current.items()) {
         ItemStack stack = stackOf(id);
         if (!stack.isEmpty() && stacks.size() < (this.recipeRequested ? 6 : 9)) {
            stacks.add(stack);
         }
      }
      this.related = stacks;
   }
   private static ItemStack stackOf(String id) {
      ResourceLocation location = ResourceLocation.tryParse(id);
      return location == null ? ItemStack.EMPTY : BuiltInRegistries.ITEM.getOptional(location).<ItemStack>map(ItemStack::new).orElse(ItemStack.EMPTY);
   }
   private void goTo(int newChapter, int newPage) {
      int chapters = FieldGuideContent.CHAPTERS.size();
      newChapter = Mth.clamp(newChapter, 0, chapters - 1);
      newPage = Mth.clamp(newPage, 0, FieldGuideContent.CHAPTERS.get(newChapter).pages().size() - 1);
      if (newChapter != this.chapter || newPage != this.page) {
         this.chapter = newChapter;
         this.page = newPage;
         this.loadPage();
         this.playPageSound();
      }
   }
   private boolean hasPrevious() {
      return this.chapter > 0 || this.page > 0;
   }
   private boolean hasNext() {
      return this.chapter < FieldGuideContent.CHAPTERS.size() - 1 || this.page < this.currentChapter().pages().size() - 1;
   }
   private void previousPage() {
      if (this.page > 0) {
         this.goTo(this.chapter, this.page - 1);
      } else if (this.chapter > 0) {
         this.goTo(this.chapter - 1, FieldGuideContent.CHAPTERS.get(this.chapter - 1).pages().size() - 1);
      }
   }
   private void nextPage() {
      if (this.page < this.currentChapter().pages().size() - 1) {
         this.goTo(this.chapter, this.page + 1);
      } else if (this.chapter < FieldGuideContent.CHAPTERS.size() - 1) {
         this.goTo(this.chapter + 1, 0);
      }
   }
   private void playPageSound() {
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
   }
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.render(graphics, mouseX, mouseY, partialTick);
      this.hoveredStack = ItemStack.EMPTY;
      this.hoveredText = List.of();
      int accent = this.currentChapter().accent();
      IndustrialGuiStyle.drawContainer(graphics, this.left, this.top, 340, 230);
      drawRivet(graphics, this.left + 9, this.top + 9);
      drawRivet(graphics, this.left + 340 - 11, this.top + 9);
      Component heading = Component.translatable("gui.industrialcrops.guide.title")
         .append(" · ")
         .append(Component.translatable(this.currentChapter().titleKey()));
      IndustrialGuiStyle.drawFittedComponent(graphics, this.font, heading, this.left + 16, this.top + 8, 308, -12566464, true);
      this.drawTabs(graphics, mouseX, mouseY);
      this.drawContentFrame(graphics, accent);
      this.drawTextScreen(graphics, accent);
      this.drawRecipeColumn(graphics, mouseX, mouseY);
      this.drawNavigation(graphics, mouseX, mouseY, accent);
      if (!this.hoveredStack.isEmpty()) {
         graphics.renderTooltip(this.font, this.hoveredStack, mouseX, mouseY);
      } else if (!this.hoveredText.isEmpty()) {
         graphics.renderComponentTooltip(this.font, this.hoveredText, mouseX, mouseY);
      }
   }
   private static void drawRivet(GuiGraphics graphics, int x, int y) {
      graphics.fill(x, y, x + 2, y + 2, -12236985);
      graphics.fill(x, y, x + 1, y + 1, -790037);
   }
   private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
      int x = this.left + 6;
      for (int i = 0; i < FieldGuideContent.CHAPTERS.size(); i++) {
         FieldGuideContent.Chapter entry = FieldGuideContent.CHAPTERS.get(i);
         int y = this.top + 24 + i * 19;
         boolean selected = i == this.chapter;
         boolean hovered = inside((double)mouseX, (double)mouseY, x, y, 24, 18);
         int width = selected ? 27 : 24;
         int fill = selected ? -3750202 : (hovered ? -5327949 : -6380636);
         IndustrialGuiStyle.drawPanel(graphics, x, y, width, 18, fill);
         int stripe = selected ? entry.accent() : darken(entry.accent(), 0.55F);
         graphics.fill(x + 1, y + 1, x + 3, y + 19 - 2, stripe);
         if (selected) {
            for (int row = 0; row < 3; row++) {
               graphics.fill(x + width - 1, y + 6 + row, x + width + 2 - row, y + 7 + row, -5871035);
               graphics.fill(x + width - 1, y + 11 - row, x + width + 2 - row, y + 12 - row, -5871035);
            }
         }
         ItemStack icon = stackOf(entry.icon());
         if (!icon.isEmpty()) {
            graphics.renderItem(icon, x + 5, y + 1);
         }
         if (hovered) {
            this.hoveredText = List.of(
               Component.translatable("gui.industrialcrops.guide.chapter", new Object[]{i + 1}).withStyle(ChatFormatting.GRAY),
               Component.translatable(entry.titleKey())
            );
         }
      }
   }
   private void drawContentFrame(GuiGraphics graphics, int accent) {
      IndustrialGuiStyle.drawWorkPanel(graphics, this.contentX, this.contentY, this.contentW, this.contentH);
      graphics.fill(this.contentX + 6, this.contentY + 6, this.contentX + 9, this.contentY + 16, accent);
      Component counter = Component.translatable("gui.industrialcrops.guide.page", new Object[]{this.page + 1, this.currentChapter().pages().size()});
      int counterWidth = this.font.width(counter);
      graphics.drawString(this.font, counter, this.contentX + this.contentW - 7 - counterWidth, this.contentY + 7, -10461088, false);
      Component title = Component.translatable(this.currentPage().titleKey()).withStyle(ChatFormatting.BOLD);
      IndustrialGuiStyle.drawFittedComponent(
         graphics, this.font, title, this.contentX + 13, this.contentY + 7, this.contentW - 13 - counterWidth - 14, -12566464, false
      );
      graphics.fill(this.contentX + 6, this.contentY + 18, this.contentX + this.contentW - 6, this.contentY + 19, accent);
   }
   private void drawTextScreen(GuiGraphics graphics, int accent) {
      int x = this.screenX;
      int y = this.screenY;
      int w = this.screenW;
      int h = this.screenH;
      graphics.fill(x, y, x + w, y + h, -14012623);
      graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, -11840681);
      graphics.fill(x + 1, y + 1, x + w - 1, y + 2, -12498612);
      graphics.fill(x + 1, y + 1, x + 2, y + h - 1, -12498612);
      drawBrackets(graphics, x + 3, y + 3, w - 6, h - 6, -5871035);
      graphics.enableScissor(this.textX - 1, y + 4, this.textX + this.textW + 2, y + h - 4);
      int offset = (int)Math.round(this.scroll);
      for (int i = 0; i < this.lines.size(); i++) {
         int lineY = this.textY + i * 9 - offset;
         if (lineY >= y - 9 && lineY <= y + h) {
            graphics.drawString(this.font, this.lines.get(i), this.textX, lineY, -1643282, false);
         }
      }
      graphics.disableScissor();
      if (this.maxScroll > 0) {
         int trackX = x + w - 7;
         int trackTop = y + 6;
         int trackHeight = h - 12;
         graphics.fill(trackX, trackTop, trackX + 3, trackTop + trackHeight, -14012623);
         int thumbHeight = Math.max(12, trackHeight * this.textH / (this.textH + this.maxScroll));
         int thumbY = trackTop + (int)((double)(trackHeight - thumbHeight) * (this.scroll / (double)this.maxScroll));
         graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbHeight, accent);
         if (this.scroll < (double)this.maxScroll - 0.5) {
            int cx = x + w / 2;
            for (int row = 0; row < 3; row++) {
               graphics.fill(cx - 3 + row, y + h - 7 + row, cx + 3 - row, y + h - 6 + row, accent);
            }
         }
      }
   }
   private static void drawBrackets(GuiGraphics graphics, int x, int y, int w, int h, int color) {
      int arm = 6;
      graphics.fill(x, y, x + arm, y + 1, color);
      graphics.fill(x, y, x + 1, y + arm, color);
      graphics.fill(x + w - arm, y, x + w, y + 1, color);
      graphics.fill(x + w - 1, y, x + w, y + arm, color);
      graphics.fill(x, y + h - 1, x + arm, y + h, color);
      graphics.fill(x, y + h - arm, x + 1, y + h, color);
      graphics.fill(x + w - arm, y + h - 1, x + w, y + h, color);
      graphics.fill(x + w - 1, y + h - arm, x + w, y + h, color);
   }
   private void drawRecipeColumn(GuiGraphics graphics, int mouseX, int mouseY) {
      IndustrialGuiStyle.drawPanel(graphics, this.columnX, this.columnY, this.columnW, this.columnH, -4933448);
      int gridX = this.columnX + (this.columnW - 54) / 2;
      if (!this.recipeRequested) {
         this.drawColumnLabel(graphics, Component.translatable("gui.industrialcrops.guide.related"), this.columnW - 8);
         this.drawItemGrid(graphics, this.related, gridX, this.columnY + 14, mouseX, mouseY);
         int rows = (this.related.size() + 2) / 3;
         int ventTop = this.columnY + 14 + Math.max(1, rows) * 18 + 8;
         drawVent(graphics, this.columnX + 9, ventTop, this.columnW - 18, this.columnY + this.columnH - 8 - ventTop);
      } else {
         this.drawColumnLabel(graphics, Component.translatable("gui.industrialcrops.guide.recipe"), this.columnW - 22);
         int gridY = this.columnY + 13;
         if (this.recipe != null && !this.recipe.station().isEmpty()) {
            int iconX = this.columnX + this.columnW - 15;
            int iconY = this.columnY + 2;
            graphics.pose().pushPose();
            graphics.pose().translate((float)iconX, (float)iconY, 0.0F);
            graphics.pose().scale(0.75F, 0.75F, 1.0F);
            graphics.renderItem(this.recipe.station(), 0, 0);
            graphics.pose().popPose();
            if (inside((double)mouseX, (double)mouseY, iconX, iconY, 12, 12)) {
               List<Component> tip = new ArrayList<>();
               tip.add(Component.translatable("gui.industrialcrops.guide.made_in", new Object[]{this.recipe.station().getHoverName()}));
               if (this.recipe.fluid() != null) {
                  tip.add(this.recipe.fluid().copy().withStyle(ChatFormatting.AQUA));
               }
               this.hoveredText = tip;
            }
         }
         for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
               IndustrialGuiStyle.drawSlot(graphics, gridX + column * 18, gridY + row * 18);
            }
         }
         if (this.recipe != null && !this.recipe.entries().isEmpty()) {
            int offsetX = this.recipe.grid() ? (3 - this.recipe.width()) / 2 : 0;
            int offsetY = this.recipe.grid() ? (3 - this.recipe.height()) / 2 : 0;
            int columns = this.recipe.grid() ? this.recipe.width() : 3;
            for (int i = 0; i < this.recipe.entries().size() && i < 9; i++) {
               FieldGuideScreen.Entry entry = this.recipe.entries().get(i);
               ItemStack shown = entry.current();
               if (!shown.isEmpty()) {
                  int slotX = gridX + (i % columns + offsetX) * 18 + 1;
                  int slotY = gridY + (i / columns + offsetY) * 18 + 1;
                  this.drawStack(graphics, shown, slotX, slotY, entry.count(), mouseX, mouseY);
               }
            }
         } else if (this.recipe != null) {
            List<FormattedCharSequence> note = this.font.split(Component.translatable("gui.industrialcrops.guide.no_recipe"), 50);
            for (int ix = 0; ix < note.size() && ix < 4; ix++) {
               int noteWidth = this.font.width(note.get(ix));
               graphics.drawString(this.font, note.get(ix), gridX + (54 - noteWidth) / 2, gridY + 10 + ix * 10, -10461088, false);
            }
         }
         int centerX = this.columnX + this.columnW / 2;
         int arrowY = gridY + 55;
         graphics.fill(centerX - 1, arrowY, centerX + 1, arrowY + 4, -10461088);
         for (int row = 0; row < 4; row++) {
            graphics.fill(centerX - 4 + row, arrowY + 4 + row, centerX + 4 - row, arrowY + 5 + row, -10461088);
         }
         int wellX = this.columnX + (this.columnW - 18) / 2;
         int wellY = gridY + 66;
         IndustrialGuiStyle.drawMachineWell(graphics, wellX, wellY);
         if (this.recipe != null) {
            this.drawStack(graphics, this.recipe.result(), wellX + 1, wellY + 1, this.recipe.result().getCount(), mouseX, mouseY);
         }
         int relatedY = wellY + 24;
         if (!this.related.isEmpty()) {
            IndustrialGuiStyle.drawFittedComponent(
               graphics, this.font, Component.translatable("gui.industrialcrops.guide.related"), this.columnX + 4, relatedY, this.columnW - 8, -10461088, false
            );
            this.drawItemGrid(graphics, this.related, gridX, relatedY + 9, mouseX, mouseY);
         }
      }
   }
   private void drawColumnLabel(GuiGraphics graphics, Component label, int maxWidth) {
      IndustrialGuiStyle.drawFittedComponent(graphics, this.font, label, this.columnX + 4, this.columnY + 4, maxWidth, -10461088, false);
   }
   private void drawItemGrid(GuiGraphics graphics, List<ItemStack> stacks, int x, int y, int mouseX, int mouseY) {
      for (int i = 0; i < stacks.size(); i++) {
         int slotX = x + i % 3 * 18;
         int slotY = y + i / 3 * 18;
         IndustrialGuiStyle.drawSlot(graphics, slotX, slotY);
         this.drawStack(graphics, stacks.get(i), slotX + 1, slotY + 1, 1, mouseX, mouseY);
      }
   }
   private void drawStack(GuiGraphics graphics, ItemStack stack, int x, int y, int count, int mouseX, int mouseY) {
      if (!stack.isEmpty()) {
         graphics.renderItem(stack, x, y);
         graphics.renderItemDecorations(this.font, stack, x, y, count > 1 ? Integer.toString(count) : null);
         if (inside((double)mouseX, (double)mouseY, x - 1, y - 1, 18, 18)) {
            this.hoveredStack = stack;
            graphics.fill(x, y, x + 16, y + 16, 1627389951);
         }
      }
   }
   private static void drawVent(GuiGraphics graphics, int x, int y, int w, int h) {
      if (h >= 14) {
         IndustrialGuiStyle.drawPanel(graphics, x, y, w, h, -5722706);
         for (int lineY = y + 4; lineY + 2 <= y + h - 3; lineY += 3) {
            graphics.fill(x + 4, lineY, x + w - 4, lineY + 1, -9472392);
            graphics.fill(x + 4, lineY + 1, x + w - 4, lineY + 2, -3288880);
         }
         graphics.fill(x + w / 2 - 8, y + h + 3, x + w / 2 + 8, y + h + 4, -5871035);
      }
   }
   private void drawNavigation(GuiGraphics graphics, int mouseX, int mouseY, int accent) {
      int buttonWidth = 22;
      int buttonHeight = 18;
      int prevX = this.contentX;
      int nextX = this.contentX + this.contentW - buttonWidth;
      drawArrowButton(graphics, prevX, this.navY, buttonWidth, buttonHeight, true, this.hasPrevious(), mouseX, mouseY);
      drawArrowButton(graphics, nextX, this.navY, buttonWidth, buttonHeight, false, this.hasNext(), mouseX, mouseY);
      if (this.hasPrevious() && inside((double)mouseX, (double)mouseY, prevX, this.navY, buttonWidth, buttonHeight)) {
         this.hoveredText = List.of(Component.translatable("gui.industrialcrops.guide.prev"));
      }
      if (this.hasNext() && inside((double)mouseX, (double)mouseY, nextX, this.navY, buttonWidth, buttonHeight)) {
         this.hoveredText = List.of(Component.translatable("gui.industrialcrops.guide.next"));
      }
      int pages = this.currentChapter().pages().size();
      int dashWidth = 8;
      int gap = 4;
      int total = pages * (dashWidth + gap) - gap;
      int startX = this.contentX + (this.contentW - total) / 2;
      int dashY = this.navY + 8;
      for (int i = 0; i < pages; i++) {
         int dashX = startX + i * (dashWidth + gap);
         int color = i == this.page ? accent : (i < this.page ? darken(accent, 0.6F) : -7631989);
         int thickness = i == this.page ? 3 : 2;
         graphics.fill(dashX, dashY, dashX + dashWidth, dashY + thickness, color);
         if (inside((double)mouseX, (double)mouseY, dashX - 1, dashY - 4, dashWidth + 2, 10)) {
            this.hoveredText = List.of(Component.translatable(this.currentChapter().pages().get(i).titleKey()));
         }
      }
   }
   private static void drawArrowButton(GuiGraphics graphics, int x, int y, int w, int h, boolean left, boolean enabled, int mouseX, int mouseY) {
      boolean hovered = enabled && inside((double)mouseX, (double)mouseY, x, y, w, h);
      IndustrialGuiStyle.drawSmallButton(graphics, x, y, w, h, hovered);
      int color = !enabled ? -9539986 : (hovered ? -5871035 : -790037);
      int centerX = x + w / 2;
      int centerY = y + h / 2;
      for (int i = 0; i < 4; i++) {
         int column = left ? centerX - 2 + i : centerX + 1 - i;
         graphics.fill(column, centerY - i - 1, column + 1, centerY + i + 1, color);
      }
   }
   private static int darken(int color, float factor) {
      int r = (int)((float)(color >> 16 & 0xFF) * factor);
      int g = (int)((float)(color >> 8 & 0xFF) * factor);
      int b = (int)((float)(color & 0xFF) * factor);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }
   private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
      return mouseX >= (double)x && mouseX < (double)(x + w) && mouseY >= (double)y && mouseY < (double)(y + h);
   }
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         for (int i = 0; i < FieldGuideContent.CHAPTERS.size(); i++) {
            if (inside(mouseX, mouseY, this.left + 6, this.top + 24 + i * 19, 24, 18)) {
               this.goTo(i, 0);
               return true;
            }
         }
         if (this.hasPrevious() && inside(mouseX, mouseY, this.contentX, this.navY, 22, 18)) {
            this.previousPage();
            return true;
         }
         if (this.hasNext() && inside(mouseX, mouseY, this.contentX + this.contentW - 22, this.navY, 22, 18)) {
            this.nextPage();
            return true;
         }
         int pages = this.currentChapter().pages().size();
         int total = pages * 12 - 4;
         int startX = this.contentX + (this.contentW - total) / 2;
         for (int ix = 0; ix < pages; ix++) {
            if (inside(mouseX, mouseY, startX + ix * 12 - 1, this.navY + 4, 10, 10)) {
               this.goTo(this.chapter, ix);
               return true;
            }
         }
         if (this.maxScroll > 0 && inside(mouseX, mouseY, this.screenX + this.screenW - 9, this.screenY + 6, 7, this.screenH - 12)) {
            this.draggingScrollbar = true;
            this.scrollToMouse(mouseY);
            return true;
         }
      }
      return super.mouseClicked(mouseX, mouseY, button);
   }
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.draggingScrollbar) {
         this.scrollToMouse(mouseY);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.draggingScrollbar = false;
      return super.mouseReleased(mouseX, mouseY, button);
   }
   private void scrollToMouse(double mouseY) {
      int trackTop = this.screenY + 6;
      int trackHeight = this.screenH - 12;
      double fraction = (mouseY - (double)trackTop) / (double)Math.max(1, trackHeight);
      this.scroll = Mth.clamp(fraction * (double)this.maxScroll, 0.0, (double)this.maxScroll);
   }
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
      if (this.maxScroll > 0 && inside(mouseX, mouseY, this.screenX, this.screenY, this.screenW, this.screenH)) {
         this.scroll = Mth.clamp(this.scroll - scrollY * 9.0 * 2.0, 0.0, (double)this.maxScroll);
         return true;
      } else if (inside(mouseX, mouseY, this.left + 6, this.top + 24, 24, FieldGuideContent.CHAPTERS.size() * 19)) {
         this.goTo(this.chapter - (int)Math.signum(scrollY), 0);
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, scrollY);
      }
   }
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      switch (keyCode) {
         case 262:
         case 267:
            this.nextPage();
            return true;
         case 263:
         case 266:
            this.previousPage();
            return true;
         case 264:
            this.scroll = Mth.clamp(this.scroll + 9.0, 0.0, (double)this.maxScroll);
            return true;
         case 265:
            this.scroll = Mth.clamp(this.scroll - 9.0, 0.0, (double)this.maxScroll);
            return true;
         case 268:
            this.goTo(0, 0);
            return true;
         default:
            if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
               this.onClose();
               return true;
            } else {
               return super.keyPressed(keyCode, scanCode, modifiers);
            }
      }
   }
   private static record Entry(List<ItemStack> options, int count) {
      ItemStack current() {
         return this.options.isEmpty() ? ItemStack.EMPTY : this.options.get((int)(Util.getMillis() / 1000L % (long)this.options.size()));
      }
   }
   private static record RecipeView(
      boolean grid, int width, int height, List<FieldGuideScreen.Entry> entries, ItemStack result, ItemStack station, Component fluid
   ) {
      static FieldGuideScreen.RecipeView worldOnly(ItemStack result) {
         return new FieldGuideScreen.RecipeView(false, 3, 3, List.of(), result.copyWithCount(1), ItemStack.EMPTY, null);
      }
      static FieldGuideScreen.RecipeView find(Item item) {
         FieldGuideScreen.RecipeView crafting = fromCrafting(item);
         if (crafting != null) {
            return crafting;
         } else {
            ManipulatorRecipeDisplay iron = ManipulatorRecipes.ironDeviceCasing();
            if (iron.output().is(item)) {
               return fromManipulator(iron, station("basic_manipulation_device"));
            } else {
               List<ManipulatorRecipeDisplay> basic = ManipulatorRecipes.forAdvanced(false);
               for (ManipulatorRecipeDisplay display : ManipulatorRecipes.all()) {
                  if (display.output().is(item)) {
                     return fromManipulator(display, station(basic.contains(display) ? "basic_manipulation_device" : "advanced_manipulation_device"));
                  }
               }
               for (ManipulatorRecipeDisplay displayx : ProcessorProgrammingRecipes.all()) {
                  if (displayx.output().is(item)) {
                     return fromManipulator(displayx, station("component_programming_device"));
                  }
               }
               for (MixerRecipeDisplay displayxx : MixerRecipes.all()) {
                  if (displayxx.output().is(item)) {
                     List<FieldGuideScreen.Entry> entries = new ArrayList<>();
                     displayxx.inputs().forEach(stack -> entries.add(single(stack)));
                     entries.add(single(displayxx.bag()));
                     return new FieldGuideScreen.RecipeView(false, 3, 3, entries, displayxx.output().copy(), station("mixer"), null);
                  }
               }
               for (RootOreExtractorRecipeDisplay displayxxx : RootOreExtractorRecipes.all()) {
                  if (displayxxx.output().is(item)) {
                     List<FieldGuideScreen.Entry> entries = new ArrayList<>();
                     entries.add(single(displayxxx.input()));
                     entries.add(single(displayxxx.catalyst()));
                     if (!displayxxx.bag().isEmpty()) {
                        entries.add(single(displayxxx.bag()));
                     }
                     return new FieldGuideScreen.RecipeView(false, 3, 3, entries, displayxxx.output().copy(), station("basic_crop_conversion_device"), null);
                  }
               }
               for (CrystalWorkbenchRecipes.Recipe workbench : CrystalWorkbenchRecipes.all()) {
                  if (workbench.output().is(item)) {
                     Map<Item, Integer> totals = new LinkedHashMap<>();
                     for (ItemStack input : workbench.inputs()) {
                        if (!input.isEmpty()) {
                           totals.merge(input.getItem(), input.getCount(), Integer::sum);
                        }
                     }
                     List<FieldGuideScreen.Entry> entries = new ArrayList<>();
                     totals.forEach((inputx, count) -> entries.add(new FieldGuideScreen.Entry(List.of(new ItemStack(inputx)), count)));
                     Component fluid = workbench.fluid().isEmpty()
                        ? null
                        : Component.translatable(
                           "gui.industrialcrops.guide.fluid", new Object[]{workbench.fluid().getAmount(), workbench.fluid().getDisplayName()}
                        );
                     return new FieldGuideScreen.RecipeView(false, 3, 3, entries, workbench.output().copy(), station("crystal_steel_workbench"), fluid);
                  }
               }
               for (GourdModificationRecipeDisplay displayxxxx : GourdModificationRecipes.all()) {
                  if (displayxxxx.output().is(item)) {
                     return new FieldGuideScreen.RecipeView(
                        false,
                        3,
                        3,
                        List.of(single(displayxxxx.melon()), single(displayxxxx.pumpkin())),
                        displayxxxx.output().copy(),
                        station("gourd_modification_device"),
                        null
                     );
                  }
               }
               return null;
            }
         }
      }
      private static FieldGuideScreen.RecipeView fromCrafting(Item item) {
         Level level = Minecraft.getInstance().level;
         if (level == null) {
            return null;
         } else {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            CraftingRecipe best = null;
            for (CraftingRecipe holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
               ItemStack result = ((CraftingRecipe)holder).getResultItem(level.registryAccess());
               if (!result.isEmpty() && result.is(item) && !((CraftingRecipe)holder).getIngredients().isEmpty()) {
                  Recipe var8 = holder;
                  if (var8 instanceof ShapedRecipe) {
                     ShapedRecipe shaped = (ShapedRecipe)var8;
                     if (shaped.getWidth() > 3 || shaped.getHeight() > 3) {
                        continue;
                     }
                  }
                  if (((CraftingRecipe)holder).getIngredients().size() <= 9 && (best == null || holder.getId().equals(itemId))) {
                     best = holder;
                  }
               }
            }
            if (best == null) {
               return null;
            } else {
               CraftingRecipe value = (CraftingRecipe)best;
               List<FieldGuideScreen.Entry> entries = new ArrayList<>();
               for (Ingredient ingredient : value.getIngredients()) {
                  entries.add(new FieldGuideScreen.Entry(Arrays.asList(ingredient.getItems()), 1));
               }
               ItemStack result = value.getResultItem(level.registryAccess()).copy();
               return value instanceof ShapedRecipe shaped
                  ? new FieldGuideScreen.RecipeView(true, shaped.getWidth(), shaped.getHeight(), entries, result, new ItemStack(Items.CRAFTING_TABLE), null)
                  : new FieldGuideScreen.RecipeView(false, 3, 3, entries, result, new ItemStack(Items.CRAFTING_TABLE), null);
            }
         }
      }
      private static FieldGuideScreen.RecipeView fromManipulator(ManipulatorRecipeDisplay display, ItemStack station) {
         List<FieldGuideScreen.Entry> entries = new ArrayList<>();
         for (ManipulatorIngredient ingredient : display.ingredients()) {
            List<ItemStack> options = ingredient.acceptedStacks().isEmpty() ? List.of(ingredient.displayStack().copyWithCount(1)) : ingredient.acceptedStacks();
            entries.add(new FieldGuideScreen.Entry(options, ingredient.count()));
         }
         return new FieldGuideScreen.RecipeView(false, 3, 3, entries, display.output().copy(), station, null);
      }
      private static FieldGuideScreen.Entry single(ItemStack stack) {
         return new FieldGuideScreen.Entry(List.of(stack.copyWithCount(1)), stack.getCount());
      }
      private static ItemStack station(String path) {
         return FieldGuideScreen.stackOf("industrialcrops:" + path);
      }
   }
}
