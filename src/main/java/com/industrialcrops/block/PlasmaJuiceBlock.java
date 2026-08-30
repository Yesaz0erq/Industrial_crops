package com.industrialcrops.block;

import com.industrialcrops.mechanic.PlasmaTransmutation;
import com.industrialcrops.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public final class PlasmaJuiceBlock extends LiquidBlock {
    private static final int ELECTROCUTION_DURATION_TICKS = 10 * 20;
    private static final int REFRESH_THRESHOLD_TICKS = 8 * 20;

    public PlasmaJuiceBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide) {
            return;
        }

        double fluidSurface = pos.getY() + state.getFluidState().getHeight(level, pos);
        if (entity.getBoundingBox().minY >= fluidSurface) {
            return;
        }

        if (entity instanceof ItemEntity itemEntity) {
            PlasmaTransmutation.tryConvertDiamond(level, itemEntity);
            return;
        }

        if (!(entity instanceof ServerPlayer player)
                || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
            return;
        }

        MobEffectInstance current = player.getEffect(ModEffects.ELECTROCUTION);
        if (current == null || current.getDuration() <= REFRESH_THRESHOLD_TICKS) {
            player.addEffect(new MobEffectInstance(
                    ModEffects.ELECTROCUTION,
                    ELECTROCUTION_DURATION_TICKS,
                    0,
                    false,
                    true,
                    true
            ));
        }
    }
}
