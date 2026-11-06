package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCombat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, priority = 1500)
public abstract class CarrotePlayerDamageMixin {
    @WrapOperation(method = "attack", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/neoforged/neoforge/common/CommonHooks;onPlayerAttackTarget(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean carrote$breachAttackGate(Player attacker, Entity target, Operation<Boolean> original) {
        return CarroteCombat.breaches(attacker, target) || original.call(attacker, target);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;isAttackable()Z"))
    private boolean carrote$breachUntargetable(Entity target, Operation<Boolean> original) {
        return CarroteCombat.breaches((Player) (Object) this, target) || original.call(target);
    }

    @WrapOperation(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;skipAttackInteraction(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean carrote$breachInteraction(Entity target, Entity attacker, Operation<Boolean> original) {
        return !CarroteCombat.breaches(attacker, target) && original.call(target, attacker);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean carrote$attack(Entity victim, DamageSource source, float amount) {
        return CarroteCombat.attack(victim, source, amount);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void carrote$penetrate(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player victim = (Player) (Object) this;
        if (CarroteCombat.handles(victim, source)) cir.setReturnValue(CarroteCombat.hurt(victim, source, amount));
    }
}
