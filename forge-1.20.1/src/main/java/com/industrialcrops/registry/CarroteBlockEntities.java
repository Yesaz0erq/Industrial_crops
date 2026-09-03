package com.industrialcrops.registry;

import com.industrialcrops.Carrote;
import com.industrialcrops.block.entity.CarroteSteelForgeBlockEntity;
import com.industrialcrops.block.entity.MaterialHardeningDeviceBlockEntity;
import com.industrialcrops.block.entity.MimicBlockEntity;
import com.industrialcrops.block.entity.UniversalReplicationDeviceBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class CarroteBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Carrote.MOD_ID);

    public static final RegistryObject<BlockEntityType<CarroteSteelForgeBlockEntity>> CARROTE_STEEL_FORGE =
            BLOCK_ENTITIES.register("carrote_steel_forge", () -> BlockEntityType.Builder
                    .of(CarroteSteelForgeBlockEntity::new, CarroteBlocks.CARROTE_STEEL_FORGE.get()).build(null));
    public static final RegistryObject<BlockEntityType<MaterialHardeningDeviceBlockEntity>> MATERIAL_HARDENING_DEVICE =
            BLOCK_ENTITIES.register("material_hardening_device", () -> BlockEntityType.Builder
                    .of(MaterialHardeningDeviceBlockEntity::new, CarroteBlocks.MATERIAL_HARDENING_DEVICE.get()).build(null));
    public static final RegistryObject<BlockEntityType<UniversalReplicationDeviceBlockEntity>> UNIVERSAL_REPLICATION_DEVICE =
            BLOCK_ENTITIES.register("universal_replication_device", () -> BlockEntityType.Builder
                    .of(UniversalReplicationDeviceBlockEntity::new, CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get()).build(null));
    public static final RegistryObject<BlockEntityType<MimicBlockEntity>> MIMIC_BLOCK =
            BLOCK_ENTITIES.register("mimic_block", () -> BlockEntityType.Builder
                    .of(MimicBlockEntity::new, CarroteBlocks.MIMIC_BLOCK.get()).build(null));

    private CarroteBlockEntities() {
    }
}
