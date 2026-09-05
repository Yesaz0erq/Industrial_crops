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

    private CarroteCuriosEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void incoming(LivingAttackEvent event) {
        if (event.getAmount() > 0 && event.getEntity() instanceof Player player && blockAttack(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (has(player, CarroteCuriosItems.LUCK)) {
            var effect = player.getEffect(MobEffects.LUCK);
            if (effect == null || effect.getAmplifier() < 2
                    || (effect.getAmplifier() == 2 && !effect.isInfiniteDuration() && effect.getDuration() < 10)) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 20, 2, true, false, true));
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
        event.getEntity().getPersistentData().putLong(STEEL_READY,
                event.getOriginal().getPersistentData().getLong(STEEL_READY));
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
