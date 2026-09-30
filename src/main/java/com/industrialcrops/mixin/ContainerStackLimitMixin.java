package com.industrialcrops.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public interface ContainerStackLimitMixin {
    @Inject(method = "getMaxStackSize()I", at = @At("RETURN"), cancellable = true, require = 1)
    private void industrialcrops$raiseContainerStackLimit(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.max(cir.getReturnValueI(), 999));
    }
}
