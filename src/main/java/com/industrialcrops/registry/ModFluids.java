package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
            ForgeRegistries.Keys.FLUID_TYPES, IndustrialCrops.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(
            ForgeRegistries.FLUIDS, IndustrialCrops.MOD_ID);

    public static final RegistryObject<FluidType> CONCENTRATED_PLASMA_JUICE_TYPE = FLUID_TYPES.register(
            "concentrated_plasma_juice", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.industrialcrops.concentrated_plasma_juice")
                    .density(1100).viscosity(6000).lightLevel(2).canDrown(false)) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation STILL = new ResourceLocation(
                                IndustrialCrops.MOD_ID, "block/concentrated_plasma_juice_still");
                        private static final ResourceLocation FLOWING = new ResourceLocation(
                                IndustrialCrops.MOD_ID, "block/concentrated_plasma_juice_flowing");

                        @Override public ResourceLocation getStillTexture() { return STILL; }
                        @Override public ResourceLocation getFlowingTexture() { return FLOWING; }
                    });
                }
            });

    public static final RegistryObject<ForgeFlowingFluid.Source> CONCENTRATED_PLASMA_JUICE = FLUIDS.register(
            "concentrated_plasma_juice", () -> new ForgeFlowingFluid.Source(plasmaProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> CONCENTRATED_PLASMA_JUICE_FLOWING = FLUIDS.register(
            "flowing_concentrated_plasma_juice", () -> new ForgeFlowingFluid.Flowing(plasmaProperties()));

    private ModFluids() { }

    private static ForgeFlowingFluid.Properties plasmaProperties() {
        return new ForgeFlowingFluid.Properties(CONCENTRATED_PLASMA_JUICE_TYPE,
                CONCENTRATED_PLASMA_JUICE, CONCENTRATED_PLASMA_JUICE_FLOWING)
                .bucket(ModItems.CONCENTRATED_PLASMA_JUICE_BUCKET)
                .block(ModBlocks.CONCENTRATED_PLASMA_JUICE)
                .tickRate(30)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2);
    }
}
