package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies accessory modifiers after armor and magic mitigation, before absorption. */
@Mixin(LivingEntity.class)
public abstract class FinalDamageMixin {
    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"), cancellable = true)
    private void carroteCurios$finalDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            cir.setReturnValue(CarroteCuriosEffects.finalDamage(attacker, cir.getReturnValue()));
        }
    }
}
