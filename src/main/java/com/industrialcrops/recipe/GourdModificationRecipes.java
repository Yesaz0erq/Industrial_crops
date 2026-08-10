package com.industrialcrops.recipe;

import com.industrialcrops.crop.CropGenetics;
import com.industrialcrops.crop.CropQuality;
import com.industrialcrops.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class GourdModificationRecipes {
    private GourdModificationRecipes() {
    }

    public static List<GourdModificationRecipeDisplay> all() {
        ItemStack melon = superQuality(ModItems.INDUSTRIAL_MELON.get().getDefaultInstance());
        ItemStack pumpkin = superQuality(ModItems.INDUSTRIAL_PUMPKIN.get().getDefaultInstance());
        return List.of(new GourdModificationRecipeDisplay(
                melon,
                pumpkin,
                new ItemStack(ModItems.FUSION_MELON.get())
        ));
    }

    private static ItemStack superQuality(ItemStack stack) {
        int tier = CropQuality.SUPER.tier();
        CropGenetics.write(stack, new CropGenetics.Genes(tier, tier));
        return stack;
    }
}
