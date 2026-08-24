package com.industrialcrops.item;

import com.industrialcrops.block.FertileFarmlandBlock;
import com.industrialcrops.block.IndustrialCropBlock;
import com.industrialcrops.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;

public final class FertilizerBagItem extends Item {
    public enum Mode {
        FAST_GROWTH,
        FERTILE_SOIL
    }

    private final Mode mode;

    public FertilizerBagItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        boolean changed = false;
        if (!level.isClientSide()) {
            if (mode == Mode.FAST_GROWTH && level instanceof ServerLevel server
                    && level.getBlockState(pos).getBlock() instanceof IndustrialCropBlock crop) {
                BlockState state = level.getBlockState(pos);
                if (!crop.isMaxAge(state) || crop instanceof com.industrialcrops.block.IndustrialGourdCropBlock) {
                    crop.finishGrowth(server, pos, state);
                    changed = true;
                }
            } else if (mode == Mode.FERTILE_SOIL
                    && level.getBlockState(pos).getBlock() instanceof FarmBlock
                    && !FertileFarmlandBlock.isFertile(level.getBlockState(pos))) {
                BlockState fertile = ModBlocks.FERTILE_FARMLAND.get().defaultBlockState();
                fertile = fertile.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE);
                level.setBlock(pos, fertile, 3);
                changed = true;
            }
            if (changed) {
                if (player != null) {
                    BagItemHelper.consumeAndReturnEmptyBag(player, context.getItemInHand());
                }
                level.playSound(null, pos, SoundEvents.BONE_MEAL_USE,
                        SoundSource.BLOCKS, 0.8F, mode == Mode.FAST_GROWTH ? 1.1F : 0.8F);
            }
        }
        return changed || level.isClientSide()
                ? InteractionResult.sidedSuccess(level.isClientSide())
                : InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(mode == Mode.FAST_GROWTH
                ? "tooltip.industrialcrops.fertilizer_fast_growth"
                : "tooltip.industrialcrops.fertilizer_fertile_soil"));
    }
}
