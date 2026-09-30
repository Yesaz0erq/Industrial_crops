package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
        "twilightforest.entity.boss.Lich",
        "com.bobmowzie.mowziesmobs.server.entity.wroughtnaut.EntityWroughtnaut"
}, remap = false, priority = 1500)
public abstract class CarroteBossDamageMixin {
    @Inject(method = {"hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            "m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z"},
            at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void carrote$breachBoss(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity victim = (LivingEntity) (Object) this;
        if (CarroteCombat.handles(victim, source)) cir.setReturnValue(CarroteCombat.hurt(victim, source, amount));
    }
}
