package com.industrialcrops.effect;

import com.industrialcrops.registry.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class CropCombatEvents {
    public static int level(LivingEntity entity, Holder<MobEffect> effect) {
        var active = entity.getEffect(effect);
        return active == null ? 0 : Math.min(5, active.getAmplifier() + 1);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void impact(ProjectileImpactEvent event) {
        if (event.getProjectile().level().isClientSide || !(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof Player player)) return;
        int level = level(player, ModEffects.REFRACTION);
        if (level > 0 && player.getRandom().nextFloat() < level * .2F) {
            event.setCanceled(true);
            event.getProjectile().discard();
        }
    }
    @SubscribeEvent
    public static void flame(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide || event.getNewDamage() <= 0
                || !(event.getSource().getEntity() instanceof Player attacker)
                || event.getSource().getDirectEntity() != attacker) return;
        int level = level(attacker, ModEffects.FLAME);
        if (level > 0) event.getEntity().igniteForSeconds(4 * level);
    }
}
