package com.industrialcrops.item;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fusion tool that can harvest natural unbreakable blocks while deliberately
 * refusing command-only/admin blocks listed in the protected tag.
 */
public final class FusionIngotPickaxeItem extends PickaxeItem {
    public static final TagKey<Block> PROTECTED_BLOCKS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "fusion_pickaxe_unbreakable")
    );

    private static final Tier FUSION_TIER = new Tier() {
        @Override
        public int getUses() {
            return 8192;
        }

        @Override
        public float getSpeed() {
            return 64.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 8.0F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 30;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.FUSION_INGOT.get());
        }
    };

    public FusionIngotPickaxeItem(Properties properties) {
        super(FUSION_TIER, properties.attributes(
                PickaxeItem.createAttributes(FUSION_TIER, 1.0F, -2.8F)));
    }

    public static boolean canBreak(BlockState state) {
        return !(state.getBlock() instanceof GameMasterBlock) && !state.is(PROTECTED_BLOCKS);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return canBreak(state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return canBreak(state) ? FUSION_TIER.getSpeed() : 0.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return canBreak(state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (level instanceof ServerLevel serverLevel && canBreak(state)) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            ItemStack harvestingTool = stack.copy();
            ensureEnchantments(harvestingTool, serverLevel);
            if (Block.getDrops(state, serverLevel, pos, blockEntity, miner, harvestingTool).isEmpty()) {
                var blockItem = state.getBlock().asItem();
                if (blockItem != Items.AIR) {
                    Block.popResource(serverLevel, pos, new ItemStack(blockItem));
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, miner);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        super.inventoryTick(stack, level, entity, slotId, selected);
        ensureEnchantments(stack, level);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        ensureEnchantments(stack, level);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public static void ensureEnchantments(ItemStack stack, Level level) {
        if (level.isClientSide) {
            return;
        }

        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> silkTouch = enchantments.getOrThrow(Enchantments.SILK_TOUCH);
        Holder<Enchantment> fortune = enchantments.getOrThrow(Enchantments.FORTUNE);
        if (stack.getEnchantments().getLevel(silkTouch) < 1) {
            stack.enchant(silkTouch, 1);
        }
        if (stack.getEnchantments().getLevel(fortune) < 3) {
            stack.enchant(fortune, 3);
        }
    }
}
