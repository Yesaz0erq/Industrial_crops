package com.industrialcrops.registry;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public final class ModCauldronInteractions {
    public static final Map<Item, CauldronInteraction> PLASMA_JUICE = CauldronInteraction.newInteractionMap();

    private static boolean initialized;

    private ModCauldronInteractions() {
    }

    public static void bootstrap() {
        if (initialized) {
            return;
        }
        initialized = true;

        CauldronInteraction fillPlasmaJuice = (state, level, pos, player, hand, stack) ->
                CauldronInteraction.emptyBucket(level, pos, player, hand, stack,
                        ModBlocks.PLASMA_JUICE_CAULDRON.get().defaultBlockState(),
                        SoundEvents.BUCKET_EMPTY);

        CauldronInteraction.EMPTY.put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);
        CauldronInteraction.WATER.put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);
        CauldronInteraction.LAVA.put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);
        CauldronInteraction.POWDER_SNOW.put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);
        PLASMA_JUICE.put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);

        PLASMA_JUICE.put(Items.BUCKET,
                (state, level, pos, player, hand, stack) -> CauldronInteraction.fillBucket(
                        state, level, pos, player, hand, stack,
                        new ItemStack(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get()),
                        ignored -> true, SoundEvents.BUCKET_FILL));
        PLASMA_JUICE.put(Items.WATER_BUCKET, CauldronInteraction.FILL_WATER);
        PLASMA_JUICE.put(Items.LAVA_BUCKET, CauldronInteraction.FILL_LAVA);
        PLASMA_JUICE.put(Items.POWDER_SNOW_BUCKET, CauldronInteraction.FILL_POWDER_SNOW);
    }
}
