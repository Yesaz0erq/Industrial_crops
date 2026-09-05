package com.industrialcrops.block.entity;

import com.industrialcrops.block.MimicBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public final class MimicBlockEntity extends BlockEntity {
    private static final int TRANSFORM_TICKS = 100;
    private int progress;
    private UUID owner;
    private BlockState lastTarget;

    public MimicBlockEntity(BlockPos pos, BlockState state) {
        super(com.industrialcrops.registry.CarroteBlockEntities.MIMIC_BLOCK.get(), pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MimicBlockEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos targetPos = MimicBlock.targetPosition(pos, state.getValue(MimicBlock.FACING));
        if (!MimicBlock.canMimic(level, targetPos)) {
            entity.resetProgress();
            entity.showStatus(serverLevel,
                    Component.translatable("message.industrialcrops.mimic_block.waiting"));
            return;
        }
        BlockState targetState = level.getBlockState(targetPos);
        if (entity.lastTarget != null && !entity.lastTarget.equals(targetState)) {
            entity.progress = 0;
        }
        entity.lastTarget = targetState;
        entity.progress++;
        entity.setChanged();

        if (entity.progress % 2 == 0 && entity.progress < TRANSFORM_TICKS) {
            int percent = entity.progress * 100 / TRANSFORM_TICKS;
            int filled = Math.min(10, entity.progress * 10 / TRANSFORM_TICKS);
            String bar = "■".repeat(filled) + "□".repeat(10 - filled);
            entity.showStatus(serverLevel,
                    Component.translatable("message.industrialcrops.mimic_block.progress", bar, percent));
        }
        if (entity.progress < TRANSFORM_TICKS) {
            return;
        }

        entity.showStatus(serverLevel,
                Component.translatable("message.industrialcrops.mimic_block.complete"));
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                36, 0.45D, 0.45D, 0.45D, 0.06D);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                18, 0.4D, 0.4D, 0.4D, 0.0D);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 1.0F, 1.2F);
        serverLevel.setBlockAndUpdate(pos, targetState);
    }

    private void resetProgress() {
        if (progress != 0 || lastTarget != null) {
            progress = 0;
            lastTarget = null;
            setChanged();
        }
    }

    private void showStatus(ServerLevel level, Component message) {
        Player player = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
        if (!(player instanceof ServerPlayer serverPlayer)
                || serverPlayer.level() != level
                || serverPlayer.distanceToSqr(worldPosition.getCenter()) > 1024.0D) {
            player = level.getNearestPlayer(
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D,
                    32.0D, false);
        }
        if (player != null) {
            player.displayClientMessage(message, true);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = Math.max(0, Math.min(TRANSFORM_TICKS, tag.getInt("Progress")));
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
