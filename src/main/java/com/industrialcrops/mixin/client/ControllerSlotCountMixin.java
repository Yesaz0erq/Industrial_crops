package com.industrialcrops.mixin.client;

import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.ReinforcedControlDeviceMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
public abstract class ControllerSlotCountMixin {
    @Shadow @Final protected AbstractContainerMenu menu;

    @Redirect(method = "renderSlot", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    private void industrialcrops$drawCountOnce(GuiGraphics graphics, Font font, ItemStack stack,
                                              int x, int y, String count, GuiGraphics context, Slot slot) {
        boolean storage = menu instanceof ReinforcedControlDeviceMenu && slot.index < 54
                || menu instanceof AdvancedIndustrialStorageMenu advanced && slot.index < advanced.getVisibleSlotCount();
        graphics.renderItemDecorations(font, stack, x, y, storage ? "" : count);
    }
}
