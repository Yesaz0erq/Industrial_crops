package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class CarroteDeathMixin {
    @Inject(method = "onLivingDeath", at = @At("RETURN"), cancellable = true)
    private static void carrote$finishDamage(LivingEntity victim, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (CarroteCombat.forcedDeath(victim)) cir.setReturnValue(false);
    }
}
