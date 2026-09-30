package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "committee.nova.mods.avaritia.init.handler.InfinityHandler", remap = false)
public abstract class AvaritiaInfinityImmunityMixin {
    @Inject(method = "onAttackedInfinite(Lnet/neoforged/neoforge/event/entity/player/AttackEntityEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$allowAttack(AttackEntityEvent event, CallbackInfo ci) {
        if (event.getTarget() instanceof LivingEntity victim && victim != event.getEntity()
                && CarroteCuriosEffects.has(event.getEntity(), CarroteCuriosItems.ARMOR_PIERCING)) ci.cancel();
    }

    @Inject(method = "onInfiniteHurt(Lnet/neoforged/neoforge/event/entity/living/LivingIncomingDamageEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$allowIncoming(LivingIncomingDamageEvent event, CallbackInfo ci) {
        if (carrote$penetrates(event.getSource(), event.getEntity())) ci.cancel();
    }

    @Inject(method = "onLivingDamage(Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$allowDamage(LivingDamageEvent.Pre event, CallbackInfo ci) {
        if (carrote$penetrates(event.getSource(), event.getEntity())) ci.cancel();
    }

    @Inject(method = "onLivingHurt(Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Post;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$keepDamageState(LivingDamageEvent.Post event, CallbackInfo ci) {
        if (carrote$penetrates(event.getSource(), event.getEntity())) ci.cancel();
    }

    @Inject(method = "onDeath(Lnet/neoforged/neoforge/event/entity/living/LivingDeathEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$allowDeath(LivingDeathEvent event, CallbackInfo ci) {
        if (carrote$penetrates(event.getSource(), event.getEntity())) ci.cancel();
    }

    @Unique
    private static boolean carrote$penetrates(DamageSource source, LivingEntity victim) {
        return source.getEntity() instanceof LivingEntity attacker && attacker != victim
                && CarroteCuriosEffects.has(attacker, CarroteCuriosItems.ARMOR_PIERCING);
    }
}
