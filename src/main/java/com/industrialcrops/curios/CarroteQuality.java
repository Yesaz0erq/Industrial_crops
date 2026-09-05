package com.industrialcrops.curios;

import com.industrialcrops.crop.CropQuality;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Accessory quality is independent of crop genetics and never rerolls existing stacks. */
public final class CarroteQuality {
    public static final String TAG = "CarroteQuality";

    private CarroteQuality() {}

    public static CropQuality get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return CropQuality.byTier(data == null || !data.copyTag().contains(TAG)
                ? CarroteCuriosItems.defaultQuality(stack.getItem())
                : Math.max(CarroteCuriosItems.defaultQuality(stack.getItem()), data.copyTag().getInt(TAG)));
    }

    public static void set(ItemStack stack, CropQuality quality) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(TAG, quality.tier()));
    }
}
