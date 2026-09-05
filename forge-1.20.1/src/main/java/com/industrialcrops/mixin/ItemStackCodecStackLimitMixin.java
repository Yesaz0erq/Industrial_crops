package com.industrialcrops.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Preserve counts above the signed-byte range used by 1.20.1 item NBT. */
@Mixin(ItemStack.class)
public abstract class ItemStackCodecStackLimitMixin {
    @Shadow private int count;

    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void industrialcrops$readCount(CompoundTag tag, CallbackInfo ci) {
        count = tag.getInt("Count");
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void industrialcrops$writeCount(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (count > Byte.MAX_VALUE) cir.getReturnValue().putInt("Count", count);
    }
}
