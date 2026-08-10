package com.industrialcrops.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public final class GoldenRedstoneLampSlime extends IndustrialGearSlime {
    private static final DustParticleOptions GOLDEN_PARTICLE =
            new DustParticleOptions(new Vector3f(1.0F, 0.58F, 0.04F), 1.0F);

    public GoldenRedstoneLampSlime(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level, GOLDEN_PARTICLE, "golden_redstone_lamp_slime");
    }
}
