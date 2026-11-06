package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCombat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public abstract class CarroteArrowDamageMixin {
    @WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean carrote$arrowPenetrates(Entity target, DamageSource source, float amount,
            Operation<Boolean> original) {
        return target instanceof LivingEntity victim && CarroteCombat.handles(victim, source)
                ? CarroteCombat.hurt(victim, source, amount) : original.call(target, source, amount);
    }
}
