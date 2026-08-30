package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
            NeoForgeRegistries.Keys.FLUID_TYPES, IndustrialCrops.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(
            Registries.FLUID, IndustrialCrops.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> CONCENTRATED_PLASMA_JUICE_TYPE = FLUID_TYPES.register(
            "concentrated_plasma_juice", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.industrialcrops.concentrated_plasma_juice")
                    .density(1100).viscosity(6000).lightLevel(2).canDrown(false)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> CONCENTRATED_PLASMA_JUICE = FLUIDS.register(
            "concentrated_plasma_juice", () -> new BaseFlowingFluid.Source(plasmaProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> CONCENTRATED_PLASMA_JUICE_FLOWING = FLUIDS.register(
            "flowing_concentrated_plasma_juice", () -> new BaseFlowingFluid.Flowing(plasmaProperties()));

    private ModFluids() {
    }

    private static BaseFlowingFluid.Properties plasmaProperties() {
        return new BaseFlowingFluid.Properties(CONCENTRATED_PLASMA_JUICE_TYPE,
                CONCENTRATED_PLASMA_JUICE, CONCENTRATED_PLASMA_JUICE_FLOWING)
                .bucket(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET)
                .block(ModBlocks.CONCENTRATED_PLASMA_JUICE)
                .tickRate(30)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
    }
}
