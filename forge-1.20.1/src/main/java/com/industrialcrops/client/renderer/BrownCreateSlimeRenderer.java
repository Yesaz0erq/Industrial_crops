package com.industrialcrops.client.renderer;

import com.industrialcrops.client.model.BrownCreateSlimeModel;
import com.industrialcrops.entity.BrownCreateSlime;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public final class BrownCreateSlimeRenderer extends IndustrialSlimeRenderer<BrownCreateSlime> {
    public BrownCreateSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new BrownCreateSlimeModel(), 0.5F, "industrialcrops_copper_gear_slime_shell", true);
    }
}
