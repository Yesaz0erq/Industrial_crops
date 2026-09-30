package com.industrialcrops.mixin;

import com.industrialcrops.curios.CarroteAmmo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileWeaponItem.class)
public abstract class CarroteProjectileAmmoMixin {
    @Inject(method = "useAmmo", at = @At("HEAD"), cancellable = true)
    private static void industrialcrops$preserveAmmo(ItemStack weapon, ItemStack ammo, LivingEntity shooter,
                                                    boolean extraProjectile, CallbackInfoReturnable<ItemStack> cir) {
        if (CarroteAmmo.preserves(shooter, weapon, ammo)) {
            ItemStack projectile = ammo.copyWithCount(1);
            projectile.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            cir.setReturnValue(projectile);
        }
    }
}
