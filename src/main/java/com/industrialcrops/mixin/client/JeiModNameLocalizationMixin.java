package com.industrialcrops.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Localizes JEI's source-mod labels without changing mod identities or other languages. */
@Pseudo
@Mixin(targets = "mezz.jei.library.helpers.ModIdHelper", remap = false)
public abstract class JeiModNameLocalizationMixin {
    @Inject(method = "getModNameForModId(Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"),
            cancellable = true, remap = false, require = 0)
    private void industrialcrops$localizeCarroteName(String modId, CallbackInfoReturnable<String> callback) {
        if (!"zh_cn".equals(Minecraft.getInstance().getLanguageManager().getSelected())) {
            return;
        }
        if ("carrote".equals(modId)) {
            callback.setReturnValue(I18n.get("itemGroup.carrote.carrote"));
        } else if ("carrote_curios".equals(modId)) {
            callback.setReturnValue(I18n.get("itemGroup.carrote_curios.section"));
        }
    }
}
