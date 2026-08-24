package com.industrialcrops.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public final class DiamondProcessorSlime extends IndustrialGearSlime {
    private static final DustParticleOptions BLUE_PROCESSOR_PARTICLE =
            new DustParticleOptions(new Vector3f(0.18F, 0.42F, 0.96F), 1.05F);

    public DiamondProcessorSlime(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level, BLUE_PROCESSOR_PARTICLE, "blue_processor_slime");
    }
}
