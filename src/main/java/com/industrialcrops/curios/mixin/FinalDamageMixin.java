package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Runs after ALL LivingDamageEvent.Pre listeners, including LOWEST priority listeners. */
@Mixin(value = CommonHooks.class, remap = false)
public abstract class FinalDamageMixin {
    @Inject(method = "onLivingDamagePre", at = @At("RETURN"), cancellable = true)
    private static void carroteCurios$finalDamage(LivingEntity victim, DamageContainer container, CallbackInfoReturnable<Float> cir) {
        if (container.getSource().getEntity() instanceof LivingEntity attacker) {
            float damage = CarroteCuriosEffects.finalDamage(attacker, cir.getReturnValue());
            if (damage != cir.getReturnValue()) {

                container.setNewDamage(damage);
                cir.setReturnValue(damage);
            }
        }
    }
}
