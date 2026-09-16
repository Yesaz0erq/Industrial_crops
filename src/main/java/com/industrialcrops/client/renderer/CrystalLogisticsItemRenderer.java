package com.industrialcrops.client.renderer;

import com.industrialcrops.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** The block and its item share the vanilla gateway renderer and textures. */
public final class CrystalLogisticsItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final CrystalLogisticsRenderer gateway = new CrystalLogisticsRenderer(null);

    public CrystalLogisticsItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                ModBlocks.CRYSTAL_LOGISTICS.get().defaultBlockState(), poses, buffers, light, overlay);
        gateway.renderGateway(poses, buffers, light, overlay);
    }
}
