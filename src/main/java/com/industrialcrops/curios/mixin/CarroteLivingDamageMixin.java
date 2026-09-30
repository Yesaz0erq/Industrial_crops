package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 1500)
public abstract class CarroteLivingDamageMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void carrote$penetrate(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity victim = (LivingEntity) (Object) this;
        if (CarroteCombat.handles(victim, source)) cir.setReturnValue(CarroteCombat.hurt(victim, source, amount));
    }
}
