package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.entity.BrownCreateSlime;
import com.industrialcrops.entity.DiamondProcessorSlime;
import com.industrialcrops.entity.GrayGearSlime;
import com.industrialcrops.entity.GoldenRedstoneLampSlime;
import com.industrialcrops.entity.GoldenRocketEntity;
import com.industrialcrops.entity.TargetMarkerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, IndustrialCrops.MOD_ID);

    public static final RegistryObject<EntityType<BrownCreateSlime>> BROWN_CREATE_SLIME =
            ENTITY_TYPES.register("copper_gear_slime", () -> EntityType.Builder
                    .of(BrownCreateSlime::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(8)
                    .build("copper_gear_slime"));

    public static final RegistryObject<EntityType<GrayGearSlime>> GRAY_GEAR_SLIME =
            ENTITY_TYPES.register("gray_gear_slime", () -> EntityType.Builder
                    .of(GrayGearSlime::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(8)
                    .build("gray_gear_slime"));

    public static final RegistryObject<EntityType<GoldenRedstoneLampSlime>> GOLDEN_REDSTONE_LAMP_SLIME =
            ENTITY_TYPES.register("golden_redstone_lamp_slime", () -> EntityType.Builder
                    .of(GoldenRedstoneLampSlime::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(8)
                    .build("golden_redstone_lamp_slime"));

    public static final RegistryObject<EntityType<DiamondProcessorSlime>> DIAMOND_PROCESSOR_SLIME =
            ENTITY_TYPES.register("blue_processor_slime", () -> EntityType.Builder
                    .of(DiamondProcessorSlime::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(8)
                    .build("blue_processor_slime"));

    public static final RegistryObject<EntityType<GoldenRocketEntity>> GOLDEN_ROCKET =
            ENTITY_TYPES.register("explosive_potato", () -> EntityType.Builder.<GoldenRocketEntity>of(
                            GoldenRocketEntity::new,
                            MobCategory.MISC
                    )
                    .sized(0.35F, 1.1F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("explosive_potato"));

    public static final RegistryObject<EntityType<TargetMarkerEntity>> TARGET_MARKER =
            ENTITY_TYPES.register("impact_target", () -> EntityType.Builder.<TargetMarkerEntity>of(
                            TargetMarkerEntity::new,
                            MobCategory.MISC
                    )
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(128)
                    .updateInterval(20)
                    .build("impact_target"));

    private ModEntities() {
    }
}
