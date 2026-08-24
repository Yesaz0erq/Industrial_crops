package com.industrialcrops.registry;

import com.industrialcrops.Carrote;
import com.industrialcrops.block.CarroteSteelForgeBlock;
import com.industrialcrops.block.MaterialHardeningDeviceBlock;
import com.industrialcrops.block.MimicBlock;
import com.industrialcrops.block.UniversalReplicationDeviceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CarroteBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Carrote.MOD_ID);

    public static final RegistryObject<Block> CARROTE_STEEL_DEVICE_CASING =
            registerMachineBlock("carrote_steel_device_casing");
    public static final RegistryObject<Block> CARROTE_STEEL_BLOCK = BLOCKS.register("carrote_steel_block",
            () -> new Block(machineProperties().mapColor(MapColor.COLOR_RED).strength(5.0F, 6.0F)));
    public static final RegistryObject<Block> STABLE_MATTER_BLOCK = BLOCKS.register("stable_matter_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(4.0F, 8.0F)
                    .sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> MIMIC_BLOCK = BLOCKS.register("mimic_block",
            () -> new MimicBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> CARROTE_STEEL_FORGE = BLOCKS.register("carrote_steel_forge",
            () -> new CarroteSteelForgeBlock(machineProperties()
                    .mapColor(MapColor.COLOR_RED)
                    .noOcclusion()
                    .sound(SoundType.METAL)));
    public static final RegistryObject<Block> MATERIAL_HARDENING_DEVICE = BLOCKS.register("material_hardening_device",
            () -> new MaterialHardeningDeviceBlock(machineProperties()
                    .mapColor(MapColor.COLOR_RED)
                    .sound(SoundType.METAL)));
    public static final RegistryObject<Block> UNIVERSAL_REPLICATION_DEVICE = BLOCKS.register("universal_replication_device",
            () -> new UniversalReplicationDeviceBlock(
                    machineProperties().mapColor(MapColor.COLOR_RED).sound(SoundType.METAL)));

    private CarroteBlocks() {
    }

    private static RegistryObject<Block> registerMachineBlock(String id) {
        return BLOCKS.register(id, () -> new Block(machineProperties()));
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5F)
                .sound(SoundType.METAL);
    }
}
