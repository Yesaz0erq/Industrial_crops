package com.industrialcrops.item;

import com.industrialcrops.block.entity.IncubatorBlockEntity;
import com.industrialcrops.entity.BrownCreateSlime;
import com.industrialcrops.entity.DiamondProcessorSlime;
import com.industrialcrops.entity.GoldenRedstoneLampSlime;
import com.industrialcrops.entity.GrayGearSlime;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
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
            @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
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
        com.industrialcrops.util.ItemStackNbt.update(stack, tag -> {
            tag.putInt(SLIME_TYPE_TAG, type);
            tag.putInt(SLIME_SIZE_TAG, Math.max(1, size));
        });
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
        return IncubatorBlockEntity.SLIME_VANILLA;
    }
}
