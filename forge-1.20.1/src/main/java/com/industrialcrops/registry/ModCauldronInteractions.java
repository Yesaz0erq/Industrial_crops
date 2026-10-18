package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public final class ModCauldronInteractions {
    public static final java.util.Map<net.minecraft.world.item.Item, CauldronInteraction> PLASMA_JUICE =
            CauldronInteraction.newInteractionMap();
    public static final java.util.Map<net.minecraft.world.item.Item, CauldronInteraction> OMNI_POTION =
            CauldronInteraction.newInteractionMap();

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

        // Omni Bucket: pours a full cauldron of Omni Potion and is kept (it never runs dry).
        CauldronInteraction pourOmni = (state, level, pos, player, hand, stack) -> {
            BlockState full = ModBlocks.OMNI_POTION_CAULDRON.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3);
            if (state.equals(full)) return InteractionResult.PASS;
            if (!level.isClientSide) {
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                level.setBlockAndUpdate(pos, full);
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.7F, 1.3F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        };
        CauldronInteraction.EMPTY.put(ModItems.OMNI_BUCKET.get(), pourOmni);
        CauldronInteraction.WATER.put(ModItems.OMNI_BUCKET.get(), pourOmni);
        CauldronInteraction.LAVA.put(ModItems.OMNI_BUCKET.get(), pourOmni);
        CauldronInteraction.POWDER_SNOW.put(ModItems.OMNI_BUCKET.get(), pourOmni);
        PLASMA_JUICE.put(ModItems.OMNI_BUCKET.get(), pourOmni);
        OMNI_POTION.put(ModItems.OMNI_BUCKET.get(), pourOmni);

        // Glass Bottle draws an Omni Potion; an Omni Potion pours back in.
        OMNI_POTION.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(ModItems.OMNI_POTION.get())));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                net.minecraft.world.level.block.LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        OMNI_POTION.put(ModItems.OMNI_POTION.get(), (state, level, pos, player, hand, stack) -> {
            int fill = state.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL);
            if (fill >= 3) return InteractionResult.PASS;
            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                level.setBlockAndUpdate(pos, state.setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, fill + 1));
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        CauldronInteraction.EMPTY.put(ModItems.OMNI_POTION.get(), (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                level.setBlockAndUpdate(pos, ModBlocks.OMNI_POTION_CAULDRON.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
    }
}
