package com.industrialcrops.block;

import com.industrialcrops.registry.ModCauldronInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Cauldron filled from the Omni Bucket; Glass Bottles draw Omni Potions from it (3 per fill). */
public final class OmniPotionCauldronBlock extends LayeredCauldronBlock {


    public OmniPotionCauldronBlock(Properties properties) {
        super(properties, precipitation -> false, ModCauldronInteractions.OMNI_POTION);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        double y = pos.getY() + getContentHeight(state) + 0.05D;
        level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.25D + random.nextDouble() * 0.5D, y,
                pos.getZ() + 0.25D + random.nextDouble() * 0.5D, 0.0D, 0.02D, 0.0D);
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5D, y, pos.getZ() + 0.5D,
                    (random.nextDouble() - 0.5D) * 0.02D, 0.04D, (random.nextDouble() - 0.5D) * 0.02D);
        }
    }
}
