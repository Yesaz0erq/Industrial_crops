package com.industrialcrops.crop;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Set;

public final class CropGenetics {
    private static final String DOMINANT_TAG = "IndustrialCropDominantQuality";
    private static final String RECESSIVE_TAG = "IndustrialCropRecessiveQuality";
    private static final String CREATIVE_TEMPLATE_TAG = "IndustrialCropCreativeTemplate";
    private static final int MAX_INITIAL_DOMINANT = CropQuality.EXCELLENT.tier();
    private static final int MAX_INITIAL_RECESSIVE = CropQuality.FINE.tier();
    private static final Set<String> GENETIC_ITEM_IDS = Set.of(
            "industrial_carrot",
            "bagged_industrial_carrot",
            "industrial_potato",
            "bagged_industrial_potato",
            "industrial_wheat",
            "bagged_industrial_wheat_seeds",
            "bagged_industrial_melon_seeds",
            "bagged_industrial_pumpkin_seeds",
            "industrial_melon",
            "industrial_pumpkin"
    );

    private CropGenetics() {
    }

    public static Genes createInitial(RandomSource random) {
        // Creative extraction and machine conversion use exactly three equally
        // likely visible qualities: normal, good and excellent.
        int dominant = random.nextInt(MAX_INITIAL_DOMINANT + 1);
        int minimumRecessive = dominant + 1;
        int maximumRecessive = Math.min(MAX_INITIAL_RECESSIVE, dominant + 2);
        int recessive = minimumRecessive
                + random.nextInt(maximumRecessive - minimumRecessive + 1);
        return new Genes(dominant, recessive);
    }

    public static boolean isGeneticCrop(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "industrialcrops".equals(id.getNamespace()) && GENETIC_ITEM_IDS.contains(id.getPath());
    }

    public static ItemStack createInitialStack(Item item) {
        ItemStack stack = new ItemStack(item);
        write(stack, createInitial(RandomSource.create()));
        return stack;
    }

    public static ItemStack initializeInitial(ItemStack stack, RandomSource random) {
        if (!hasGenes(stack)) {
            write(stack, createInitial(random));
        }
        return stack;
    }

    public static ItemStack createCreativeTemplate(ItemStack stack, RandomSource random) {
        write(stack, new Genes(CropQuality.NORMAL.tier(), CropQuality.NORMAL.tier()));
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putBoolean(CREATIVE_TEMPLATE_TAG, true));
        return stack;
    }

    public static void initializeInventoryStack(ItemStack stack, RandomSource random) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        boolean creativeTemplate = data != null
                && data.copyTag().getBoolean(CREATIVE_TEMPLATE_TAG);
        if (!hasGenes(stack) || creativeTemplate) {
            write(stack, createInitial(random));
        }
        if (creativeTemplate) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack,
                    tag -> tag.remove(CREATIVE_TEMPLATE_TAG));
        }
    }

    public static boolean hasGenes(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return false;
        }
        var tag = data.copyTag();
        return tag.contains(DOMINANT_TAG) && tag.contains(RECESSIVE_TAG);
    }

    public static Genes read(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        var tag = data.copyTag();
        if (!tag.contains(DOMINANT_TAG) || !tag.contains(RECESSIVE_TAG)) {
            return null;
        }
        return normalize(tag.getInt(DOMINANT_TAG), tag.getInt(RECESSIVE_TAG));
    }

    public static void write(ItemStack stack, Genes genes) {
        Genes normalized = normalize(genes.dominantTier(), genes.recessiveTier());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(DOMINANT_TAG, normalized.dominantTier());
            tag.putInt(RECESSIVE_TAG, normalized.recessiveTier());
        });
    }

    public static Genes cross(Genes first, Genes second, RandomSource random) {
        boolean firstRecessive = random.nextBoolean();
        boolean secondRecessive = random.nextBoolean();
        int firstAllele = firstRecessive ? first.recessiveTier() : first.dominantTier();
        int secondAllele = secondRecessive ? second.recessiveTier() : second.dominantTier();
        int qualityFloor = Math.max(first.dominantTier(), second.dominantTier());

        if (firstRecessive && secondRecessive) {
            int guaranteedPromotion = Math.min(CropQuality.SUPER.tier(), qualityFloor + 1);
            int promotedDominant = Math.max(
                    guaranteedPromotion, Math.max(firstAllele, secondAllele));
            int promotedRecessive = promotedDominant;
            int room = CropQuality.SUPER.tier() - promotedDominant;
            if (room > 0) {
                promotedRecessive += 1 + random.nextInt(Math.min(2, room));
            }
            return normalize(promotedDominant, promotedRecessive);
        }

        int inheritedDominant = Math.max(qualityFloor, Math.min(firstAllele, secondAllele));
        int inheritedRecessive = Math.max(inheritedDominant, Math.max(firstAllele, secondAllele));
        return normalize(inheritedDominant, inheritedRecessive);
    }

    public static Genes normalize(int dominant, int recessive) {
        int normalizedDominant = Mth.clamp(dominant, 0, CropQuality.SUPER.tier());
        int normalizedRecessive = Mth.clamp(recessive, normalizedDominant,
                Math.min(CropQuality.SUPER.tier(), normalizedDominant + 2));
        return new Genes(normalizedDominant, normalizedRecessive);
    }

    public record Genes(int dominantTier, int recessiveTier) {
        public CropQuality dominantQuality() {
            return CropQuality.byTier(dominantTier);
        }

        public CropQuality recessiveQuality() {
            return CropQuality.byTier(recessiveTier);
        }
    }
}
