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
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.village.WandererTradesEvent;

/** Adds the fictional crop discovery loop without replacing vanilla data files. */
@EventBusSubscriber(modid = IndustrialCrops.MOD_ID)
public final class IndustrialCropLootEvents {
    private static final ResourceLocation VANILLA_CHEST_PREFIX =
            new ResourceLocation("minecraft", "chests");

    private IndustrialCropLootEvents() {
    }

    @SubscribeEvent
    public static void addSeedsToVanillaChests(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (!id.getNamespace().equals(VANILLA_CHEST_PREFIX.getNamespace())
                || !id.getPath().startsWith(VANILLA_CHEST_PREFIX.getPath() + "/")) {
            return;
        }

        // Each crop has its own roll; several different crops can appear together.
        addCropPool(event, "prism_pod_seeds", ModItems.PRISM_POD_SEEDS.get(), 0.25F);
        addCropPool(event, "embercoil_seeds", ModItems.EMBERCOIL_SEEDS.get(), 0.25F);
        addCropPool(event, "starbloom_seeds", ModItems.STARBLOOM_SEEDS.get(), 0.25F);
        addCropPool(event, "neonbulb_seeds", ModItems.NEONBULB_SEEDS.get(), 0.25F);
        addCropPool(event, "fluxstalk_seeds", ModItems.FLUXSTALK_SEEDS.get(), 0.25F);
        addCropPool(event, "plasma_fruit", ModItems.PLASMA_BERRY.get(), 0.10F);
    }

    private static void addCropPool(LootTableLoadEvent event, String name,
            net.minecraft.world.item.Item item, float chance) {
        event.getTable().addPool(LootPool.lootPool()
                .name("industrialcrops_crop_discovery_" + name)
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(seedEntry(item))
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
