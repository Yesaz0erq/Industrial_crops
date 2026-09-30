package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCombat;
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
