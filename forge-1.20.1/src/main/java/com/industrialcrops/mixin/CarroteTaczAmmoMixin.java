package com.industrialcrops.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.tacz.guns.item.ModernKineticGunScriptAPI", remap = false)
public abstract class CarroteTaczAmmoMixin {
    @Shadow public abstract LivingEntity getShooter();

    // These points follow TACZ's own empty-magazine and empty-chamber checks.
    // Reloading and chambering still move existing bullets instead of creating more.
    @Inject(method = "reduceAmmoOnce()Z", at = {
            @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/gun/AbstractGunItem;reduceCurrentAmmoCount(Lnet/minecraft/world/item/ItemStack;)V"),
            @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/gun/AbstractGunItem;setBulletInBarrel(Lnet/minecraft/world/item/ItemStack;Z)V"),
            @At(value = "INVOKE", target = "Lcom/tacz/guns/item/ModernKineticGunScriptAPI;consumeAmmoFromPlayer(I)I")
    }, cancellable = true, require = 1)
    private void industrialcrops$preserveAmmo(CallbackInfoReturnable<Boolean> cir) {
        if (CarroteCuriosEffects.has(getShooter(), CarroteCuriosItems.AMMO)) cir.setReturnValue(true);
    }
}
