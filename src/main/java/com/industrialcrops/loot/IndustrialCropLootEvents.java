package com.industrialcrops.loot;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

/** Adds the fictional crop discovery loop without replacing vanilla data files. */
@EventBusSubscriber(modid = IndustrialCrops.MOD_ID)
public final class IndustrialCropLootEvents {
    private static final ResourceLocation VANILLA_CHEST_PREFIX =
            ResourceLocation.fromNamespaceAndPath("minecraft", "chests");

    private IndustrialCropLootEvents() {
    }

    @SubscribeEvent
    public static void addSeedsToVanillaChests(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (!id.getNamespace().equals(VANILLA_CHEST_PREFIX.getNamespace())
                || !id.getPath().startsWith(VANILLA_CHEST_PREFIX.getPath() + "/")) {
            return;
        }

        event.getTable().addPool(LootPool.lootPool()
                .name("industrialcrops_seed_discovery")
                .when(LootItemRandomChanceCondition.randomChance(0.25F))
                .add(seedEntry(ModItems.PRISM_POD_SEEDS.get()))
                .add(seedEntry(ModItems.EMBERCOIL_SEEDS.get()))
                .add(seedEntry(ModItems.STARBLOOM_SEEDS.get()))
                .build());
    }

    private static LootPoolEntryContainer.Builder<?> seedEntry(net.minecraft.world.item.Item item) {
        return LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)));
    }

    @SubscribeEvent
    public static void addWanderingTraderSeeds(WandererTradesEvent event) {
        event.getRareTrades().add(new BasicItemListing(
                1, new ItemStack(ModItems.PRISM_POD_SEEDS.get()), 8, 2));
        event.getRareTrades().add(new BasicItemListing(
                8, new ItemStack(ModItems.EMBERCOIL_SEEDS.get()), 5, 5));
        event.getRareTrades().add(new BasicItemListing(
                24, new ItemStack(ModItems.STARBLOOM_SEEDS.get()), 2, 10));
    }
}
