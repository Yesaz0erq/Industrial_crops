package com.industrialcrops.curios;

import com.industrialcrops.crop.CropQuality;
import net.minecraft.world.item.ItemStack;

/** Accessory quality is independent of crop genetics and never rerolls existing stacks. */
public final class CarroteQuality {
    public static final String TAG = "CarroteQuality";

    private CarroteQuality() {}

    public static CropQuality get(ItemStack stack) {
        var data = stack.getTag();
        return CropQuality.byTier(data == null || !data.contains(TAG)
                ? CarroteCuriosItems.defaultQuality(stack.getItem())
                : Math.max(CarroteCuriosItems.defaultQuality(stack.getItem()), data.getInt(TAG)));
    }

    public static void set(ItemStack stack, CropQuality quality) {
        stack.getOrCreateTag().putInt(TAG, quality.tier());
    }
}
