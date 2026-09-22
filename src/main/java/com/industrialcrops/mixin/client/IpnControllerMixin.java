package com.industrialcrops.mixin.client;

import com.industrialcrops.client.gui.ControllerSortingCompat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.anti_ad.mc.ipnext.inventory.GeneralInventoryActions", remap = false)
public abstract class IpnControllerMixin {
    @Inject(method = {
            "doSort(Lnet/minecraft/world/inventory/AbstractContainerMenu;ZZ)V",
            "doSortInRows(Lnet/minecraft/world/inventory/AbstractContainerMenu;ZZ)V",
            "doSortInColumns(Lnet/minecraft/world/inventory/AbstractContainerMenu;ZZ)V"
    }, at = @At("HEAD"), cancellable = true, require = 0)
    private void industrialcrops$sortStorage(AbstractContainerMenu menu, boolean gui, boolean forcePlayer, CallbackInfo ci) {
        if (ControllerSortingCompat.request(forcePlayer, gui)) ci.cancel();
    }
}
