package com.industrialcrops.mixin.client;

import com.industrialcrops.client.creative.CreativeSectionClient;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Set;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabSectionsMixin {
    @Shadow
    private Collection<ItemStack> displayItems;

    @Shadow
    private Set<ItemStack> displayItemsSearchTab;

    @Inject(method = "buildContents", at = @At("TAIL"))
    private void industrialcrops$buildSectionedContents(
            CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo callbackInfo) {
        CreativeSectionClient.BuiltContent content =
                CreativeSectionClient.build((CreativeModeTab) (Object) this);
        if (content != null) {
            this.displayItems = content.displayItems();
            this.displayItemsSearchTab = content.searchItems();
        }
    }
}
