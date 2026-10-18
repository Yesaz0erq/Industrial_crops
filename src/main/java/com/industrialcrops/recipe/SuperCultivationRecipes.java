package com.industrialcrops.recipe;

import com.industrialcrops.crop.CropGenetics;
import com.industrialcrops.crop.CropQuality;
import com.industrialcrops.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Super Crop Cultivator recipes: one Super-quality industrial crop plus two or three specific
 * materials grow a new plant variety. Materials may sit in any material slot; slots a recipe does
 * not use must stay empty.
 */
public final class SuperCultivationRecipes {
    /** Cultivation time in ticks: one minute. */
    public static final int DEFAULT_DURATION = 20 * 60;
    public static final int MAX_MATERIALS = 3;

    public record Recipe(String id, Supplier<? extends Item> crop, List<Supplier<? extends Item>> materials,
                         Supplier<? extends Item> result, int duration) {
        /** True when the crop is Super quality and the non-empty material stacks are exactly this recipe's materials. */
        public boolean matches(ItemStack cropStack, List<ItemStack> materialStacks) {
            if (!cropStack.is(crop.get()) || !isSuperQuality(cropStack)) return false;
            List<Item> needed = new ArrayList<>();
            for (Supplier<? extends Item> material : materials) needed.add(material.get());
            for (ItemStack stack : materialStacks) {
                if (stack.isEmpty()) continue;
                if (!needed.remove(stack.getItem())) return false;
            }
            return needed.isEmpty();
        }

        public boolean usesMaterial(ItemStack stack) {
            for (Supplier<? extends Item> material : materials) {
                if (stack.is(material.get())) return true;
            }
            return false;
        }

        public List<ItemStack> materialStacks() {
            List<ItemStack> stacks = new ArrayList<>();
            for (Supplier<? extends Item> material : materials) stacks.add(new ItemStack(material.get()));
            return stacks;
        }

        /** Containers a recipe hands back, e.g. the Glass Bottle of Dragon's Breath. */
        public List<ItemStack> remainders() {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack material : materialStacks()) {
                ItemStack remainder = material.getCraftingRemainingItem();
                if (!remainder.isEmpty()) stacks.add(remainder);
            }
            return stacks;
        }

        public ItemStack resultStack() {
            return new ItemStack(result.get());
        }

        /** The crop as JEI and the guide show it: an item with Super genes. */
        public ItemStack displayCrop() {
            return superQuality(new ItemStack(crop.get()));
        }
    }

    private static final List<Recipe> RECIPES = List.of(
            new Recipe("black_nebula_carrot", ModItems.INDUSTRIAL_CARROT,
                    List.of(ModItems.COMET_FRUIT, ModItems.PRISM_POD), ModItems.BLACK_NEBULA_CARROT, DEFAULT_DURATION),
            new Recipe("purple_dragon_carrot", ModItems.INDUSTRIAL_CARROT,
                    List.of(() -> net.minecraft.world.item.Items.DRAGON_BREATH, ModItems.NEONBULB),
                    ModItems.PURPLE_DRAGON_CARROT, DEFAULT_DURATION),
            new Recipe("omni_carrot", ModItems.INDUSTRIAL_CARROT, List.of(ModItems.OMNI_POTION),
                    ModItems.OMNI_CARROT, DEFAULT_DURATION)
    );

    private SuperCultivationRecipes() {
    }

    public static List<Recipe> all() {
        return RECIPES;
    }

    public static boolean isSuperQuality(ItemStack stack) {
        CropGenetics.Genes genes = CropGenetics.read(stack);
        return genes != null && genes.dominantQuality() == CropQuality.SUPER;
    }

    public static ItemStack superQuality(ItemStack stack) {
        int tier = CropQuality.SUPER.tier();
        CropGenetics.write(stack, new CropGenetics.Genes(tier, tier));
        return stack;
    }

    /** True for any crop item some recipe uses, whatever its quality (the GUI explains the Super requirement). */
    public static boolean isCultivationCrop(ItemStack stack) {
        for (Recipe recipe : RECIPES) {
            if (stack.is(recipe.crop().get())) return true;
        }
        return false;
    }

    public static boolean isMaterial(ItemStack stack) {
        for (Recipe recipe : RECIPES) {
            if (recipe.usesMaterial(stack)) return true;
        }
        return false;
    }

    public static Recipe find(ItemStack crop, List<ItemStack> materials) {
        for (Recipe recipe : RECIPES) {
            if (recipe.matches(crop, materials)) return recipe;
        }
        return null;
    }

    /**
     * Recipe the screen previews: among recipes for this crop, the one whose materials cover the most
     * of what is already loaded (ignoring quality). Falls back to the first recipe for the crop.
     */
    public static Recipe preview(ItemStack crop, List<ItemStack> materials) {
        Recipe best = null;
        int bestScore = -1;
        for (Recipe recipe : RECIPES) {
            if (!crop.is(recipe.crop().get())) continue;
            int score = 0;
            boolean conflict = false;
            for (ItemStack stack : materials) {
                if (stack.isEmpty()) continue;
                if (recipe.usesMaterial(stack)) score++;
                else conflict = true;
            }
            if (conflict) score -= 10;
            if (score > bestScore) {
                best = recipe;
                bestScore = score;
            }
        }
        return best;
    }

    /** All recipes for a crop item. */
    public static List<Recipe> forCropAll(ItemStack crop) {
        List<Recipe> list = new ArrayList<>();
        for (Recipe recipe : RECIPES) {
            if (crop.is(recipe.crop().get())) list.add(recipe);
        }
        return list;
    }

    /** Recipe the crop slot points at, ignoring materials; used for previews and hints. */
    public static Recipe forCrop(ItemStack crop) {
        for (Recipe recipe : RECIPES) {
            if (crop.is(recipe.crop().get())) return recipe;
        }
        return null;
    }
}
