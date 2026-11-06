package com.industrialcrops.item;

import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.entity.BrownCreateSlime;
import com.industrialcrops.entity.DiamondProcessorSlime;
import com.industrialcrops.entity.GoldenRedstoneLampSlime;
import com.industrialcrops.entity.GrayGearSlime;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class IncubatorBlockItem extends BlockItem {
    private static final String SLIME_TYPE_TAG = "IncubatorSlimeType";
    private static final String SLIME_SIZE_TAG = "IncubatorSlimeSize";

    public IncubatorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        if (!(target instanceof Slime slime) || !slime.isAlive()) {
            return InteractionResult.PASS;
        }
        boolean cultivation = getBlock() instanceof com.industrialcrops.block.SlimeIncubatorBlock;
        int type = getSlimeType(slime);
        if (cultivation && (!com.industrialcrops.block.entity.SlimeIncubatorBlockEntity.isCultivatableType(type)
                || type == IncubatorBlockEntity.SLIME_VANILLA
                && slime.getType() != net.minecraft.world.entity.EntityType.SLIME)) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.industrialcrops.slime_cultivation_device.unsupported"), true);
            }
            return InteractionResult.FAIL;
        }
        if (getStoredType(stack) != IncubatorBlockEntity.SLIME_NONE) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable(cultivation ? "message.industrialcrops.slime_cultivation_device.occupied"
                        : "message.industrialcrops.slime_conversion_device.occupied"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide()) {
            // Old saves may still contain stacked machines. Capture into exactly one device.
            ItemStack filled = stack.getCount() == 1 ? stack : stack.copyWithCount(1);
            setStoredSlime(filled, type, slime.getSize(),
                    slime instanceof com.industrialcrops.entity.FluxSlime flux ? flux.knownIngots() : java.util.Set.of());
            if (filled != stack) {
                stack.shrink(1);
                if (!player.getInventory().add(filled)) player.drop(filled, false);
            }
            player.level().playSound(null, slime.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 0.8F);
            slime.discard();
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        int type = getStoredType(stack);
        if (type == IncubatorBlockEntity.SLIME_NONE) {
            tooltip.add(Component.translatable(getBlock() instanceof com.industrialcrops.block.SlimeIncubatorBlock
                    ? "tooltip.industrialcrops.slime_cultivation_device.empty"
                    : "tooltip.industrialcrops.slime_conversion_device.empty"));
        } else {
            tooltip.add(Component.translatable(
                    "tooltip.industrialcrops.slime_conversion_device.contains",
                    IncubatorBlockEntity.getSlimeName(type),
                    getStoredSize(stack)
            ));
            if (type == IncubatorBlockEntity.SLIME_FLUX) {
                tooltip.add(Component.translatable("tooltip.industrialcrops.flux_ore_gel.count", getStoredOres(stack).size())
                        .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    public static void setStoredSlime(ItemStack stack, int type, int size) {
        setStoredSlime(stack, type, size, java.util.Set.of());
    }

    public static void setStoredSlime(ItemStack stack, int type, int size,
            java.util.Collection<net.minecraft.resources.ResourceLocation> ores) {
        com.industrialcrops.util.ItemStackNbt.update(stack, tag -> {
            tag.putInt(SLIME_TYPE_TAG, type);
            tag.putInt(SLIME_SIZE_TAG, Math.max(1, size));
            if (type == IncubatorBlockEntity.SLIME_FLUX) {
                tag.put(com.industrialcrops.machine.FluxOreIndex.TAG_ORES, com.industrialcrops.machine.FluxOreIndex.writeIds(ores));
            } else {
                tag.remove(com.industrialcrops.machine.FluxOreIndex.TAG_ORES);
            }
        });
    }

    /** Ore data of a captured Flux Slime. */
    public static java.util.Set<net.minecraft.resources.ResourceLocation> getStoredOres(ItemStack stack) {
        com.industrialcrops.compat.LegacyItemData.CustomData data = com.industrialcrops.compat.LegacyItemData.getOrEmpty(stack);
        return com.industrialcrops.machine.FluxOreIndex.readIds(data.copyTag());
    }

    public static int getStoredType(ItemStack stack) {
        var data = com.industrialcrops.util.ItemStackNbt.copyTag(stack);
        return data.getInt(SLIME_TYPE_TAG);
    }

    public static int getStoredSize(ItemStack stack) {
        var data = com.industrialcrops.util.ItemStackNbt.copyTag(stack);
        return Math.max(1, data.getInt(SLIME_SIZE_TAG));
    }

    private static int getSlimeType(Slime slime) {
        if (slime instanceof BrownCreateSlime) {
            return IncubatorBlockEntity.SLIME_COPPER;
        }
        if (slime instanceof GrayGearSlime) {
            return IncubatorBlockEntity.SLIME_IRON;
        }
        if (slime instanceof GoldenRedstoneLampSlime) {
            return IncubatorBlockEntity.SLIME_GOLD;
        }
        if (slime instanceof DiamondProcessorSlime) {
            return IncubatorBlockEntity.SLIME_DIAMOND;
        }
        if (slime instanceof com.industrialcrops.entity.FluxSlime) {
            return IncubatorBlockEntity.SLIME_FLUX;
        }
        return IncubatorBlockEntity.SLIME_VANILLA;
    }
}
