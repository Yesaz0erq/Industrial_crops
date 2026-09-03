package com.industrialcrops.client.renderer;

import com.industrialcrops.client.model.GrayGearSlimeModel;
import com.industrialcrops.entity.GrayGearSlime;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class GrayGearSlimeRenderer extends IndustrialSlimeRenderer<GrayGearSlime> {
    public GrayGearSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new GrayGearSlimeModel(), 0.5F, "industrialcrops_gray_gear_slime_shell", true);
    }
}
