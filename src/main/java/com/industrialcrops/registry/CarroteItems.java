package com.industrialcrops.registry;

import com.industrialcrops.Carrote;
import com.industrialcrops.item.CarroteItem;
import com.industrialcrops.item.UniversalReplicationDeviceItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CarroteItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Carrote.MOD_ID);

    public static final DeferredItem<CarroteItem> CARROTE = ITEMS.register("carrote",
            () -> new CarroteItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> CARROTE_STEEL_INGOT = ITEMS.register("carrote_steel_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> STABLE_MATTER_INGOT = ITEMS.register("stable_matter_ingot",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<BlockItem> CARROTE_STEEL_DEVICE_CASING =
            registerBlockItem("carrote_steel_device_casing", CarroteBlocks.CARROTE_STEEL_DEVICE_CASING);
    public static final DeferredItem<BlockItem> CARROTE_STEEL_BLOCK =
            registerBlockItem("carrote_steel_block", CarroteBlocks.CARROTE_STEEL_BLOCK);
    public static final DeferredItem<BlockItem> STABLE_MATTER_BLOCK =
            registerBlockItem("stable_matter_block", CarroteBlocks.STABLE_MATTER_BLOCK);
    public static final DeferredItem<BlockItem> MIMIC_BLOCK =
            registerBlockItem("mimic_block", CarroteBlocks.MIMIC_BLOCK);
    public static final DeferredItem<BlockItem> CARROTE_STEEL_FORGE =
            registerBlockItem("carrote_steel_forge", CarroteBlocks.CARROTE_STEEL_FORGE);
    public static final DeferredItem<BlockItem> MATERIAL_HARDENING_DEVICE =
            registerBlockItem("material_hardening_device", CarroteBlocks.MATERIAL_HARDENING_DEVICE);
    public static final DeferredItem<UniversalReplicationDeviceItem> UNIVERSAL_REPLICATION_DEVICE =
            ITEMS.register("universal_replication_device",
                    () -> new UniversalReplicationDeviceItem(
                            CarroteBlocks.UNIVERSAL_REPLICATION_DEVICE.get(),
                            new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    private CarroteItems() {
    }

    private static DeferredItem<BlockItem> registerBlockItem(
            String id, net.neoforged.neoforge.registries.DeferredBlock<? extends net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
