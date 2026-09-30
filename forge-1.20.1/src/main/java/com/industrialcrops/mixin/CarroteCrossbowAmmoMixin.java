package com.industrialcrops.mixin;

import com.industrialcrops.curios.CarroteAmmo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrossbowItem.class)
public abstract class CarroteCrossbowAmmoMixin {
    private static final String INDUSTRIALCROPS_INFINITE_AMMO = "industrialcrops:infinite_ammo";

    @Redirect(method = "loadProjectile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack industrialcrops$preserveAmmo(ItemStack source, int count, LivingEntity shooter,
            ItemStack crossbow, ItemStack ammo, boolean extraProjectile, boolean creative) {
        if (!CarroteAmmo.preserves(shooter, crossbow, ammo)) return source.split(count);
        ItemStack projectile = source.copyWithCount(count);
        projectile.getOrCreateTag().putBoolean(INDUSTRIALCROPS_INFINITE_AMMO, true);
        return projectile;
    }

    @Redirect(method = "shootProjectile", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean industrialcrops$preventPickup(Level level, Entity projectile, Level ignored,
            LivingEntity shooter, InteractionHand hand, ItemStack crossbow, ItemStack ammo, float pitch,
            boolean creative, float velocity, float inaccuracy, float angle) {
        if (projectile instanceof AbstractArrow arrow && ammo.hasTag()
                && ammo.getTag().getBoolean(INDUSTRIALCROPS_INFINITE_AMMO)) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        return level.addFreshEntity(projectile);
    }
}
