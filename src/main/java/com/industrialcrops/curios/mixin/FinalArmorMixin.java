package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteAdvancedEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 900)
public abstract class FinalArmorMixin {
    @Inject(method = "getArmorValue", at = @At("RETURN"), cancellable = true)
    private void carrote$armor(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(CarroteAdvancedEffects.armor((LivingEntity)(Object)this, cir.getReturnValue()));
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void carrote$steal(ServerLevel level, DamageSource source, CallbackInfo ci) {
        CarroteAdvancedEffects.stealEquipment((LivingEntity)(Object)this, source);
    }
}
