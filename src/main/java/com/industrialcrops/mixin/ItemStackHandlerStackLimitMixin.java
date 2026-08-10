package com.industrialcrops.mixin;

import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStackHandler.class, remap = false)
public abstract class ItemStackHandlerStackLimitMixin {
    @Inject(
            method = "getSlotLimit",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void industrialcrops$raiseItemHandlerStackLimit(int slot, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(999);
    }
}
