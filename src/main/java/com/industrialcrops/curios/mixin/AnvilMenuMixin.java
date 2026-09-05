package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
    protected AnvilMenuMixin(MenuType<?> type, int id, Inventory inventory, ContainerLevelAccess access) {
        super(type, id, inventory, access);
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void carroteCurios$arcane(CallbackInfo ci) {
        var output = resultSlots.getItem(0);
        if (!output.isEmpty() && CarroteCuriosEffects.has(player, CarroteCuriosItems.ARCANE)
                && !EnchantmentHelper.getEnchantmentsForCrafting(inputSlots.getItem(1)).isEmpty()) {
            // Each preview is rebuilt from the inputs; rename/repair alone never awards levels.
            CarroteCuriosEffects.boostEnchantments(output, CarroteCuriosEffects.count(player, CarroteCuriosItems.ARCANE));
            broadcastChanges();
        }
    }
}
