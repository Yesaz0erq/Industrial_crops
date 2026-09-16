package com.industrialcrops.machine;

import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** A bounded, side-effect-free crafting search. Inventory mutations belong to the caller. */
public final class RecursiveCraftingPlanner {
    public record Plan(List<ItemStack> withdrawn, List<ItemStack> outputs, int operations) {}
    private final Level level;
    private final List<ItemStack> original;
    private final Map<Item, List<CraftingRecipe>> recipes = new HashMap<>();
    private List<ItemStack> stock;
    private List<ItemStack> generated = new ArrayList<>();
    private final Set<Item> active = new HashSet<>();
    private int budget = 12000, operations;
    private ItemStack missing = ItemStack.EMPTY;

    public RecursiveCraftingPlanner(Level level, List<ItemStack> stock) {
        this.level = level; this.original = copy(stock); this.stock = copy(stock);
        level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .sorted(Comparator.comparing(h -> h.getId().toString())).forEach(h -> {
                    CraftingRecipe recipe = h;
                    ItemStack result = recipe.getResultItem(level.registryAccess());
                    if (!recipe.isSpecial() && !result.isEmpty() && recipe.canCraftInDimensions(5, 5))
                        recipes.computeIfAbsent(result.getItem(), k -> new ArrayList<>()).add(recipe);
                });
    }
    public ItemStack missing() { return missing; }
    public boolean limited() { return budget <= 0; }
    public Plan plan(ItemStack target, int amount) {
        if (target.isEmpty() || amount < 1 || amount > 64) return null;
        for (int i=0; i<amount; i++) if (takeExact(target, 0).isEmpty()) return null;
        List<ItemStack> withdrawn = new ArrayList<>();
        for (int i=0; i<original.size(); i++) withdrawn.add(original.get(i).copyWithCount(original.get(i).getCount()-stock.get(i).getCount()));
        List<ItemStack> outputs = copy(generated); outputs.removeIf(ItemStack::isEmpty);
        outputs.add(target.copyWithCount(amount));
        return new Plan(withdrawn, outputs, operations);
    }
    private ItemStack takeExact(ItemStack target, int depth) {
        ItemStack found = takeExisting(target); if (!found.isEmpty()) return found;
        if (depth > 24 || --budget <= 0 || !active.add(target.getItem())) return ItemStack.EMPTY;
        try {
            for (CraftingRecipe recipe : recipes.getOrDefault(target.getItem(), List.of())) {
                if (!ItemStack.isSameItemSameTags(recipe.getResultItem(level.registryAccess()), target)) continue;
                List<ItemStack> before=copy(stock), extra=copy(generated); int oldOps=operations;
                if (craft(recipe, depth+1)) {
                    found=takeExisting(target); if (!found.isEmpty()) return found;
                }
                stock=before; generated=extra; operations=oldOps;
                if (budget<=0) break;
            }
            if (missing.isEmpty()) missing=target.copyWithCount(1);
            return ItemStack.EMPTY;
        } finally { active.remove(target.getItem()); }
    }
    private ItemStack takeExisting(ItemStack target) {
        for (List<ItemStack> pool : List.of(generated, stock)) for (ItemStack stack : pool)
            if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack,target)) {
                ItemStack result=stack.copyWithCount(1); stack.shrink(1); return result;
            }
        return ItemStack.EMPTY;
    }
    private boolean craft(CraftingRecipe recipe, int depth) {
        if (--budget<=0) return false;
        int width=recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 5;
        int height=recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : (recipe.getIngredients().size()+4)/5;
        if (width<1 || height<1 || width>5 || height>5) return false;
        List<ItemStack> grid=new ArrayList<>(Collections.nCopies(width*height,ItemStack.EMPTY));
        return fill(recipe, grid, width, height, 0, depth);
    }
    private boolean fill(CraftingRecipe recipe,List<ItemStack> grid,int width,int height,int index,int depth) {
        if (--budget<=0) return false;
        if (index==recipe.getIngredients().size()) {
            net.minecraft.world.inventory.CraftingContainer input = new net.minecraft.world.inventory.TransientCraftingContainer(
                    new net.minecraft.world.inventory.AbstractContainerMenu(null, -1) {
                        @Override public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int slot) { return ItemStack.EMPTY; }
                        @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) { return false; }
                    }, width, height);
            for (int i = 0; i < grid.size(); i++) input.setItem(i, grid.get(i));
            if (!recipe.matches(input,level)) return false;
            ItemStack result=recipe.assemble(input,level.registryAccess());
            if (result.isEmpty()) return false;
            generated.add(result.copy());
            for (ItemStack remainder : recipe.getRemainingItems(input)) if (!remainder.isEmpty()) generated.add(remainder.copy());
            operations++; return true;
        }
        Ingredient ingredient=recipe.getIngredients().get(index);
        if (ingredient.isEmpty()) return fill(recipe,grid,width,height,index+1,depth);
        List<ItemStack> choices=new ArrayList<>();
        for (List<ItemStack> pool : List.of(generated,stock)) for (ItemStack stack : pool)
            if (!stack.isEmpty() && ingredient.test(stack) && choices.stream().noneMatch(c->ItemStack.isSameItemSameTags(c,stack))) choices.add(stack.copyWithCount(1));
        for (ItemStack stack : ingredient.getItems())
            if (choices.stream().noneMatch(c->ItemStack.isSameItemSameTags(c,stack))) choices.add(stack.copyWithCount(1));
        for (ItemStack choice : choices) {
            List<ItemStack> before=copy(stock), extra=copy(generated); int oldOps=operations;
            ItemStack taken=takeExact(choice,depth);
            if (!taken.isEmpty()) {
                grid.set(index,taken);
                if (fill(recipe,grid,width,height,index+1,depth)) return true;
            }
            grid.set(index,ItemStack.EMPTY); stock=before; generated=extra; operations=oldOps;
            if (budget<=0) break;
        }
        return false;
    }
    private static List<ItemStack> copy(List<ItemStack> items) { return new ArrayList<>(items.stream().map(ItemStack::copy).toList()); }
}
