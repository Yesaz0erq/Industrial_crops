package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ModCauldronInteractions {
    public static final CauldronInteraction.InteractionMap PLASMA_JUICE =
            CauldronInteraction.newInteractionMap(IndustrialCrops.MOD_ID + ":plasma_juice");

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

        CauldronInteraction.EMPTY.map().put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);
        CauldronInteraction.WATER.map().put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);
        CauldronInteraction.LAVA.map().put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);
        CauldronInteraction.POWDER_SNOW.map().put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);
        PLASMA_JUICE.map().put(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get(), fillPlasmaJuice);

        PLASMA_JUICE.map().put(Items.BUCKET,
                (state, level, pos, player, hand, stack) -> CauldronInteraction.fillBucket(
                        state, level, pos, player, hand, stack,
                        new ItemStack(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET.get()),
                        ignored -> true, SoundEvents.BUCKET_FILL));
        PLASMA_JUICE.map().put(Items.WATER_BUCKET, CauldronInteraction.FILL_WATER);
        PLASMA_JUICE.map().put(Items.LAVA_BUCKET, CauldronInteraction.FILL_LAVA);
        PLASMA_JUICE.map().put(Items.POWDER_SNOW_BUCKET, CauldronInteraction.FILL_POWDER_SNOW);
    }
}
