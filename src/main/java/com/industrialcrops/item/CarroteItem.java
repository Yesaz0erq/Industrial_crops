package com.industrialcrops.item;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import com.industrialcrops.block.CarroteSteelForgeBlock;

/** A deliberately unstable item whose displayed name glitches without changing its registry identity. */
public final class CarroteItem extends Item {
    private static final String[] CORRUPTED_NAMES = {
            "胡罗贝", "胡□贝", "CΔRR0TE", "胡罗#@", "carr0?e", "█罗贝"
    };

    public CarroteItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        int phase = (int) ((Util.getMillis() / 450L) % CORRUPTED_NAMES.length);
        if (phase == 0) return Component.translatable(getDescriptionId(stack));
        ChatFormatting color = phase % 2 == 0 ? ChatFormatting.AQUA : ChatFormatting.LIGHT_PURPLE;
        return Component.literal(CORRUPTED_NAMES[phase]).withStyle(color, ChatFormatting.OBFUSCATED);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.LAVA_CAULDRON)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            var player = context.getPlayer();
            var facing = player == null
                    ? net.minecraft.core.Direction.NORTH
                    : player.getDirection().getOpposite();
            var forgeState = com.industrialcrops.registry.CarroteBlocks.CARROTE_STEEL_FORGE.get().defaultBlockState()
                    .setValue(CarroteSteelForgeBlock.FACING, facing);
            level.setBlock(pos, forgeState, Block.UPDATE_ALL);
            if (player == null || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.72F);
            level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.8F, 1.15F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                        18, 0.35D, 0.15D, 0.35D, 0.04D);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                        12, 0.3D, 0.2D, 0.3D, 0.03D);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
