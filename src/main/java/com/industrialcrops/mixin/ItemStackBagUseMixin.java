package com.industrialcrops.mixin;

import com.industrialcrops.item.BaggedCropItem;
import com.industrialcrops.item.FertilizerBagItem;
import com.industrialcrops.item.BagItemHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackBagUseMixin {
    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", remap = false,
            target = "Lnet/neoforged/neoforge/common/CommonHooks;onPlaceItemIntoWorld(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult industrialcrops$returnConsumedBags(UseOnContext context,
            Operation<InteractionResult> original) {
        ItemStack stack = context.getItemInHand();
        boolean bag = stack.getItem() instanceof BaggedCropItem
                || stack.getItem() instanceof FertilizerBagItem;
        int before = stack.getCount();
        InteractionResult result = original.call(context);
        var player = context.getPlayer();
        if (bag && result.consumesAction() && !context.getLevel().isClientSide()
                && player != null && !player.getAbilities().instabuild) {
            BagItemHelper.giveEmptyBags(player, Math.max(0, before - stack.getCount()));
        }
        return result;
    }
}
