package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Unique private Player carroteCurios$player;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    private void carroteCurios$owner(int id, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        carroteCurios$player = inventory.player;
    }

    @ModifyArg(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"), index = 2)
    private int carroteCurios$bookshelves(int actual) {
        return CarroteCuriosEffects.has(carroteCurios$player, CarroteCuriosItems.ENCHANTING) ? Math.max(15, actual) : actual;
    }

    @Inject(method = "getEnchantmentList", at = @At("RETURN"), cancellable = true)
    private void carroteCurios$arcane(ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (CarroteCuriosEffects.has(carroteCurios$player, CarroteCuriosItems.ARCANE)) {
            cir.setReturnValue(cir.getReturnValue().stream()
                    .map(entry -> new EnchantmentInstance(entry.enchantment, Math.min(255, entry.level + 1))).toList());
        }
    }
}
