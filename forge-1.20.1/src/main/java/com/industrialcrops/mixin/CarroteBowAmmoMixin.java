package com.industrialcrops.mixin;

import com.industrialcrops.curios.CarroteAmmo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BowItem.class)
public abstract class CarroteBowAmmoMixin {
    @Redirect(method = "releaseUsing", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ArrowItem;isInfinite(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Z", remap = false))
    private boolean industrialcrops$preserveArrow(ArrowItem arrow, ItemStack ammo, ItemStack bow, Player player) {
        return CarroteAmmo.preserves(player, bow, ammo) || arrow.isInfinite(ammo, bow, player);
    }
}
