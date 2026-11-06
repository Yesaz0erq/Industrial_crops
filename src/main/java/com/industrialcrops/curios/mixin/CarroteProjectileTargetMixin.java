package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCombat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Projectile.class)
public abstract class CarroteProjectileTargetMixin {
    @Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
    private void carrote$hitProtectedEntities(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Entity owner = ((Projectile) (Object) this).getOwner();
        if (!cir.getReturnValue() && owner != null && CarroteCombat.breaches(owner, target)
                && target.isAlive() && !target.isSpectator() && !target.isPassengerOfSameVehicle(owner)) {
            cir.setReturnValue(true);
        }
    }
}
