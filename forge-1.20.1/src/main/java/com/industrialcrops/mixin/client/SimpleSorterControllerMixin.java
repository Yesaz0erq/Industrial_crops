package com.industrialcrops.mixin.client;

import com.industrialcrops.client.gui.ControllerSortingCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "simplesorter.mc.InventoryScanner", remap = false)
public abstract class SimpleSorterControllerMixin {
    @Inject(method = "requestSort()V", at = @At("HEAD"), cancellable = true, require = 1)
    private void industrialcrops$sortStorage(CallbackInfo ci) {
        if (ControllerSortingCompat.requestFromSimpleSorter()) ci.cancel();
    }
}
