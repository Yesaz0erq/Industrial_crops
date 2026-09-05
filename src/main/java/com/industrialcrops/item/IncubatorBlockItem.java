package com.industrialcrops.item;

import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.entity.BrownCreateSlime;
import com.industrialcrops.entity.DiamondProcessorSlime;
import com.industrialcrops.entity.GoldenRedstoneLampSlime;
import com.industrialcrops.entity.GrayGearSlime;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
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
        if (!(target instanceof Slime slime)) {
            return InteractionResult.PASS;
        }
        if (getStoredType(stack) != IncubatorBlockEntity.SLIME_NONE) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.industrialcrops.slime_converter.occupied"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!player.level().isClientSide()) {
            int type = getSlimeType(slime);
            setStoredSlime(stack, type, slime.getSize());
            player.level().playSound(null, slime.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 0.8F);
            slime.discard();
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltip, flag);
        int type = getStoredType(stack);
        if (type == IncubatorBlockEntity.SLIME_NONE) {
            tooltip.add(Component.translatable("tooltip.industrialcrops.slime_converter.empty"));
        } else {
            tooltip.add(Component.translatable(
                    "tooltip.industrialcrops.slime_converter.contains",
                    IncubatorBlockEntity.getSlimeName(type),
                    getStoredSize(stack)
            ));
        }
    }

    public static void setStoredSlime(ItemStack stack, int type, int size) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(SLIME_TYPE_TAG, type);
            tag.putInt(SLIME_SIZE_TAG, Math.max(1, size));
        });
    }

    public static int getStoredType(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getInt(SLIME_TYPE_TAG);
    }

    public static int getStoredSize(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return Math.max(1, data.copyTag().getInt(SLIME_SIZE_TAG));
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
        return IncubatorBlockEntity.SLIME_VANILLA;
    }
}
