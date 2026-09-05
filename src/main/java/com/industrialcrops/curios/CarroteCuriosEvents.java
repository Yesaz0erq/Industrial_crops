package com.industrialcrops.curios;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;

import static com.industrialcrops.curios.CarroteCuriosEffects.*;

public final class CarroteCuriosEvents {
    private static final String FLIGHT_GRANTED = "carrote_curios:flight_granted";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void incoming(LivingAttackEvent event) {
        if (event.getAmount() > 0 && event.getEntity() instanceof Player player
                && ((event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) && has(player, CarroteCuriosItems.BLAST))
                || (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL) && has(player, CarroteCuriosItems.FALL))
                || blockAttack(player))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void death(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (saveFromDeath(player)) event.setCanceled(true);
        else resetCarriedCharges(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void peacefulSpawns(net.minecraftforge.event.entity.living.MobSpawnEvent.PositionCheck event) {
        if (protectedSpawn(event, event.getSpawnType())) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void peacefulFinalize(net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn event) {
        if (protectedSpawn(event, event.getSpawnType())) event.setSpawnCancelled(true);
    }

    private static boolean protectedSpawn(net.minecraftforge.event.entity.living.MobSpawnEvent event,
            net.minecraft.world.entity.MobSpawnType reason) {
        if (reason != net.minecraft.world.entity.MobSpawnType.NATURAL
                || event.getEntity().getType().getCategory() != net.minecraft.world.entity.MobCategory.MONSTER
                || event.getEntity().getType().is(net.minecraftforge.common.Tags.EntityTypes.BOSSES)) return false;
        for (var player : event.getLevel().getLevel().players()) {
            if (player.isAlive() && !player.isSpectator() && player.distanceToSqr(event.getX(), event.getY(), event.getZ()) <= 32 * 32
                    && has(player, CarroteCuriosItems.PEACE)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void villageLoot(net.minecraftforge.event.LootTableLoadEvent event) {
        if (!event.getName().getNamespace().equals("minecraft") || !event.getName().getPath().startsWith("chests/village/")) return;
        var pool = net.minecraft.world.level.storage.loot.LootPool.lootPool().name("carrote_curios:village_carrote")
                .setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1))
                .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.25F));
        CarroteCuriosItems.ITEMS.getEntries().forEach(item -> pool.add(net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem(item.get())));
        event.getTable().addPool(pool.build());
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 == 0) updateSubstituteCooldowns(player);
        if (has(player, CarroteCuriosItems.NIGHT_VISION)) {
            var vision = player.getEffect(MobEffects.NIGHT_VISION);
            if (vision == null || (!vision.isInfiniteDuration() && vision.getDuration() < 220)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false, true));
            }
        }
        if (has(player, CarroteCuriosItems.LUCK)) {
            int amplifier = 3 * count(player, CarroteCuriosItems.LUCK) - 1;
            var effect = player.getEffect(MobEffects.LUCK);
            if (effect == null || effect.getAmplifier() < amplifier
                    || (effect.getAmplifier() == amplifier && !effect.isInfiniteDuration() && effect.getDuration() < 10)) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20, amplifier, true, false, true));
            }
        }
        var abilities = player.getAbilities();
        var data = player.getPersistentData();
        boolean equipped = has(player, CarroteCuriosItems.FLIGHT);
        if (equipped && !abilities.mayfly) {
            data.putBoolean(FLIGHT_GRANTED, true);
            abilities.mayfly = true;
            player.onUpdateAbilities();
        } else if (!equipped && data.getBoolean(FLIGHT_GRANTED)) {
            data.remove(FLIGHT_GRANTED);
            if (!player.isCreative() && !player.isSpectator()) {
                abilities.mayfly = false;
                abilities.flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        // Preserve independent steel charges across respawn.
        var previous = event.getOriginal().getPersistentData();
        var next = event.getEntity().getPersistentData();
        next.putLong(STEEL_READY, previous.getLong(STEEL_READY));
        next.putLong(HELMET_STEEL_READY, previous.getLong(HELMET_STEEL_READY));
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
