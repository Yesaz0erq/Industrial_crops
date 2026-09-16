package com.industrialcrops.curios;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class CarroteAdvancedEffects {
    public static final String ABSORPTION_READY = "carrote_curios:absorption_ready";

    public static int armor(LivingEntity entity, int armor) {
        long total = armor + (CarroteCuriosEffects.has(entity, CarroteCuriosItems.HARD) ? 4L : 0L);
        if (CarroteCuriosEffects.has(entity, CarroteCuriosItems.UNBREAKABLE)) total *= 2L;
        return (int)Math.min(Integer.MAX_VALUE, total);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()
                || !CarroteCuriosEffects.has(player, CarroteCuriosItems.RESISTANCE)) return;
        long now = player.level().getGameTime();
        if (player.getPersistentData().getLong(ABSORPTION_READY) > now) return;
        player.getPersistentData().putLong(ABSORPTION_READY, now + 600);
        var current = player.getEffect(MobEffects.ABSORPTION);
        // Absorption V grants 20 health points. Never overwrite a stronger potion.
        if (current == null || current.getAmplifier() <= 4) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 4, true, false, true));
            player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), 20F));
        }
    }

    /** Called only after death cancellation/totem checks, before equipment drops are generated. */
    public static void stealEquipment(LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer killer) || killer == victim
                || !CarroteCuriosEffects.has(killer, CarroteCuriosItems.THIEF)) return;
        long now = victim.level().getGameTime();
        if (victim.getPersistentData().contains("carrote_curios:stolen_at")
                && victim.getPersistentData().getLong("carrote_curios:stolen_at") == now) return;
        victim.getPersistentData().putLong("carrote_curios:stolen_at", now);
        var slots = new java.util.ArrayList<EquipmentSlot>();
        for (var slot : EquipmentSlot.values()) if (!victim.getItemBySlot(slot).isEmpty()) slots.add(slot);
        if (slots.isEmpty()) return;
        var slot = slots.get(killer.getRandom().nextInt(slots.size()));
        var equipped = victim.getItemBySlot(slot);
        var stolen = equipped.split(1);
        victim.setItemSlot(slot, equipped);
        if (!killer.getInventory().add(stolen)) killer.drop(stolen, false);
    }
}
