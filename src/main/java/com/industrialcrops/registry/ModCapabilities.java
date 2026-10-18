package com.industrialcrops.registry;

import com.industrialcrops.IndustrialCrops;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/** Forge-native counterpart of the Neo capability catalog, including side filtering and lifecycle. */
@Mod.EventBusSubscriber(modid = IndustrialCrops.MOD_ID)
public final class ModCapabilities {
    private ModCapabilities() { }
    public static void register(RegisterCapabilitiesEvent event) { }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        add(event, ModBlockEntities.ANIMAL_BREEDER.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.itemCapability());
        add(event, ModBlockEntities.ANIMAL_BREEDER.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.energy());
        add(event, ModBlockEntities.ANIMAL_BREEDER.get(), ForgeCapabilities.FLUID_HANDLER, (machine, side) -> machine.tank());
        add(event, ModBlockEntities.WIRELESS_ENERGY_TRANSMITTER.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.LAUNCH_SITE.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.energy());
        add(event, ModBlockEntities.SATELLITE_CONTROL_DEVICE.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.energy());
        add(event, ModBlockEntities.LAUNCH_SITE.get(), ForgeCapabilities.FLUID_HANDLER, (machine, side) -> machine.tank());
        add(event, ModBlockEntities.SATELLITE_RECEIVER.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.ROOT_ORE_EXTRACTOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.CROP_COMPRESSOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.CROP_ANALYSIS_DEVICE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.MELON_MODIFICATION_DEVICE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.SLIME_INCUBATOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.INCUBATOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.INDUSTRIAL_CROP_STORAGE_ARRAY.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getItemHandler());
        add(event, ModBlockEntities.REINFORCED_INDUSTRIAL_STORAGE_ARRAY.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getPipeItemHandler());
        add(event, ModBlockEntities.REINFORCED_CONTROL_DEVICE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getPipeItemHandler());
        add(event, ModBlockEntities.GOLDEN_LAUNCH_SILO.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getAmmunitionInventory());
        add(event, ModBlockEntities.PROCESSOR_PROGRAMMER.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.AUTOMATIC_PLANTER.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.AUTOMATIC_PLANTER.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.TERRAIN_PROCESSOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.itemCapability());
        add(event, ModBlockEntities.CRAFTING_PROCESSOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.outputCapability());
        add(event, ModBlockEntities.CRYSTAL_LOGISTICS.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.itemCapability());
        add(event, ModBlockEntities.CRYSTAL_LOGISTICS.get(), ForgeCapabilities.FLUID_HANDLER, (machine, side) -> machine.fluidCapability());
        add(event, ModBlockEntities.ENERGY_CABLE.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage());
        add(event, ModBlockEntities.ELECTRIC_FURNACE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.ELECTRIC_FURNACE.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.AP_AGRICULTURE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.automation());
        add(event, ModBlockEntities.AP_AGRICULTURE.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.AP_LIVESTOCK.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.automation());
        add(event, ModBlockEntities.AP_LIVESTOCK.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.ULTRA_HIGH_PRESSURE_INV_FORGE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.EXPANDABLE_STORAGE_DEVICE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getPipeItemHandler());
        add(event, ModBlockEntities.SUPER_COMPRESSOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getAutomationHandler());
        add(event, ModBlockEntities.INTEGRATED_SMELTING_FURNACE.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getAutomationHandler());
        add(event, ModBlockEntities.INTEGRATED_SMELTING_FURNACE.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.SUPER_CROP_CULTIVATOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getAutomationHandler());
        add(event, ModBlockEntities.ULTRA_HIGH_PRESSURE_INV_FORGE.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.MIXER.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.PIPE_SORTER.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInputHandler());
        add(event, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.GOLD_PLASMA_EXTRACTOR.get(), ForgeCapabilities.FLUID_HANDLER, (machine, side) -> machine.getOutputHandler());
        add(event, ModBlockEntities.CRYSTAL_STEEL_WORKBENCH.get(), ForgeCapabilities.FLUID_HANDLER, (machine, side) -> machine.getTank());
        add(event, ModBlockEntities.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), ForgeCapabilities.FLUID_HANDLER, (machine, side) -> machine.getTank());
        add(event, ModBlockEntities.COPPER_FLUID_STORAGE_CABINET.get(), ForgeCapabilities.FLUID_HANDLER, (machine, side) -> machine.getTank());
        add(event, ModBlockEntities.MATTER_DIGITIZER.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.MATTER_DIGITIZER.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.DIGITIZED_ITEM_COPIER.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.DIGITIZED_ITEM_COPIER.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.MATTER_RECONSTRUCTOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.MATTER_RECONSTRUCTOR.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.BIO_ENERGY_GENERATOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.BIO_ENERGY_GENERATOR.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.ENERGY_BATTERY.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.ENERGY_BATTERY.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
        add(event, ModBlockEntities.RESIDUE_INCINERATOR.get(), ForgeCapabilities.ITEM_HANDLER, (machine, side) -> machine.getInventory());
        add(event, ModBlockEntities.RESIDUE_INCINERATOR.get(), ForgeCapabilities.ENERGY, (machine, side) -> machine.getEnergyStorage(side));
    }

    private static <B extends BlockEntity, T> void add(AttachCapabilitiesEvent<BlockEntity> event,
            BlockEntityType<B> type, Capability<T> capability, BiFunction<B, Direction, T> factory) {
        if (event.getObject().getType() != type) return;
        // Providers can attach before the object has entered its Level.
        @SuppressWarnings("unchecked") B attached = (B) event.getObject();
        Provider<B,T> provider = new Provider<>(attached, capability, factory);
        String path = capability == ForgeCapabilities.ITEM_HANDLER ? "items" : capability == ForgeCapabilities.ENERGY ? "energy" : "fluid";
        event.addCapability(new ResourceLocation(IndustrialCrops.MOD_ID, "forge_" + path), provider);
        event.addListener(provider::invalidate);
    }

    private static final class Provider<B extends BlockEntity, T> implements ICapabilityProvider {
        private final B entity;
        private final Capability<T> capability;
        private final BiFunction<B, Direction, T> factory;
        private final Map<Direction, LazyOptional<T>> values = new HashMap<>();
        private Provider(B entity, Capability<T> capability, BiFunction<B, Direction, T> factory) {
            this.entity = entity; this.capability = capability; this.factory = factory;
        }
        @Override
        public <R> LazyOptional<R> getCapability(Capability<R> requested, @Nullable Direction side) {
            if (requested != capability) return LazyOptional.empty();
            T value = factory.apply(entity, side);
            if (value == null) return LazyOptional.empty();
            return values.computeIfAbsent(side, ignored -> LazyOptional.of(() -> value)).cast();
        }
        private void invalidate() { values.values().forEach(LazyOptional::invalidate); values.clear(); }
    }
}
