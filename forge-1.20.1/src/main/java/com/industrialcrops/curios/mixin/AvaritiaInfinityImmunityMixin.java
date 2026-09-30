package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "committee.nova.mods.avaritia.init.handler.InfinityHandler", remap = false)
public abstract class AvaritiaInfinityImmunityMixin {
    @Inject(method = "onAttacked(Lnet/minecraftforge/event/entity/living/LivingAttackEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$allowIncoming(LivingAttackEvent event, CallbackInfo ci) {
        if (carrote$penetrates(event.getSource(), event.getEntity())) ci.cancel();
    }

    @Inject(method = "onGetHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void carrote$allowDamage(LivingHurtEvent event, CallbackInfo ci) {
        if (carrote$penetrates(event.getSource(), event.getEntity())) ci.cancel();
    }

    @Redirect(method = "onGetHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setCanceled(Z)V", ordinal = 0),
            require = 1, remap = false)
    private static void carrote$breachSwordBlock(LivingHurtEvent event, boolean canceled) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != event.getEntity()
                && CarroteCuriosEffects.has(attacker, CarroteCuriosItems.BREACH)) return;
        event.setCanceled(canceled);
    }

    @Inject(method = "onDeath(Lnet/minecraftforge/event/entity/living/LivingDeathEvent;)V",
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
