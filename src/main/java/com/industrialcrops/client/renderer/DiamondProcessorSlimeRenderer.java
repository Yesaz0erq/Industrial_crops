package com.industrialcrops.client.renderer;

import com.industrialcrops.client.model.DiamondProcessorSlimeModel;
import com.industrialcrops.entity.DiamondProcessorSlime;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;

public final class DiamondProcessorSlimeRenderer extends IndustrialSlimeRenderer<DiamondProcessorSlime> {
    public DiamondProcessorSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new DiamondProcessorSlimeModel(), 0.5F, "industrialcrops_blue_processor_slime_shell", true);
    }

    @Override
    protected @Nullable RenderType getRenderTypeOverrideForExtraBone(GeoBone bone, ResourceLocation texture) {
        if ("processor".equals(bone.getName())) {
            return RenderType.entityTranslucentEmissive(texture);
        }
        return null;
    }
}
