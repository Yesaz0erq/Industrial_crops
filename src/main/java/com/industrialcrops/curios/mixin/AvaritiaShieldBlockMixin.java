package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "committee.nova.mods.avaritia.init.handler.InfinityShieldHandler", remap = false)
public abstract class AvaritiaShieldBlockMixin {
    @Inject(method = "onDefiniteDefendingDamage(Lnet/neoforged/neoforge/event/entity/living/LivingIncomingDamageEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$breachDefendingMode(LivingIncomingDamageEvent event, CallbackInfo ci) {
        if (carrote$breaches(event.getSource().getEntity(), event.getEntity())) ci.cancel();
    }

    @Inject(method = "onDefendingShieldBlock(Lnet/neoforged/neoforge/event/entity/living/LivingShieldBlockEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$breachShield(LivingShieldBlockEvent event, CallbackInfo ci) {
        if (carrote$breaches(event.getDamageSource().getEntity(), event.getEntity())) ci.cancel();
    }

    @Inject(method = "onDefendingProjectile(Lnet/neoforged/neoforge/event/entity/ProjectileImpactEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$keepProjectileHit(ProjectileImpactEvent event, CallbackInfo ci) {
        if (event.getRayTraceResult() instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity victim
                && carrote$breaches(event.getProjectile().getOwner(), victim)) ci.cancel();
    }

    @Unique
    private static boolean carrote$breaches(Entity owner, LivingEntity victim) {
        return owner instanceof LivingEntity attacker && attacker != victim
                && (CarroteCuriosEffects.has(attacker, CarroteCuriosItems.BREACH)
                || CarroteCuriosEffects.has(attacker, CarroteCuriosItems.ARMOR_PIERCING));
    }
}
