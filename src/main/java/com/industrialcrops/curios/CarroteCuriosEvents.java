package com.industrialcrops.curios;

import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import java.util.ArrayList;

import static com.industrialcrops.curios.CarroteCuriosEffects.*;

public final class CarroteCuriosEvents {
    private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));
    private static final ResourceLocation FLIGHT = ResourceLocation.fromNamespaceAndPath("carrote_curios", "flight");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void incoming(LivingIncomingDamageEvent event) {
        if (event.getAmount() > 0 && event.getEntity() instanceof Player player && blockAttack(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (has(player, CarroteCuriosItems.LUCK)) {
            var effect = player.getEffect(MobEffects.LUCK);
            if (effect == null || effect.getAmplifier() < 2
                    || (effect.getAmplifier() == 2 && !effect.isInfiniteDuration() && effect.getDuration() < 10)) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20, 2, true, false, true));
            }
        }
        boolean equipped = has(player, CarroteCuriosItems.FLIGHT);
        var flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (equipped && !flight.hasModifier(FLIGHT)) {
            flight.addTransientModifier(new AttributeModifier(FLIGHT, 1D, AttributeModifier.Operation.ADD_VALUE));
        } else if (!equipped && flight.hasModifier(FLIGHT)) {
            flight.removeModifier(FLIGHT);
            if (!player.mayFly()) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        // Shared cooldown survives respawn. Flight uses a transient, independently removable attribute.
        var previous = event.getOriginal().getPersistentData();
        var next = event.getEntity().getPersistentData();
        next.putLong(STEEL_READY, previous.getLong(STEEL_READY));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void smelt(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player) || !has(player, CarroteCuriosItems.SMELTING)
                || !event.getState().is(ORES)) return;
        var extra = new ArrayList<ItemEntity>();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack input = drop.getItem();
            var recipeInput = new SingleRecipeInput(input);
            var recipe = event.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, recipeInput, event.getLevel());
            if (recipe.isEmpty()) continue;
            ItemStack output = recipe.get().value().assemble(recipeInput, event.getLevel().registryAccess());
            if (output.isEmpty()) continue;
            int remaining = input.getCount() * output.getCount();
            int first = Math.min(remaining, output.getMaxStackSize());
            drop.setItem(output.copyWithCount(first));
            remaining -= first;
            while (remaining > 0) {
                int count = Math.min(remaining, output.getMaxStackSize());
                extra.add(new ItemEntity(event.getLevel(), drop.getX(), drop.getY(), drop.getZ(), output.copyWithCount(count)));
                remaining -= count;
            }
        }
        event.getDrops().addAll(extra);
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        syncCooldowns(event.getEntity());
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        syncCooldowns(event.getEntity());
    }
}
