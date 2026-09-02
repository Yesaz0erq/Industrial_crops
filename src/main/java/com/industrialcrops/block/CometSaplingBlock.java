package com.industrialcrops.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class CometSaplingBlock extends SaplingBlock {
    public CometSaplingBlock(TreeGrower treeGrower, BlockBehaviour.Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) != 0) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.58D;
        double y = pos.getY() + 0.18D + random.nextDouble() * 0.76D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.58D;
        double angle = random.nextDouble() * Math.PI * 2.0D;
        level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z,
                Math.cos(angle) * 0.008D, 0.012D + random.nextDouble() * 0.008D,
                Math.sin(angle) * 0.008D);

        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.END_ROD, x, y + 0.05D, z,
                    0.0D, 0.006D, 0.0D);
        }
    }
}
