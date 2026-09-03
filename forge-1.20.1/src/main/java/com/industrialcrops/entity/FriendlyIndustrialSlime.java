package com.industrialcrops.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public abstract class FriendlyIndustrialSlime extends Slime {
    private static final int SPLIT_COOLDOWN_TICKS = 20 * 60;
    private static final double FOLLOW_RANGE_SQUARED = 16.0D * 16.0D;
    private static final double STOP_RANGE_SQUARED = 4.0D * 4.0D;
    private static final String SPLIT_COOLDOWN_TAG = "IndustrialSplitCooldown";
    private static final String STAYING_TAG = "IndustrialStaying";
    private static final int COMMAND_HOP_TICKS = 12;

    private int splitCooldown;
    private boolean staying;
    private int commandHopTicks;
    private boolean commandJumpPending;

    protected FriendlyIndustrialSlime(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FriendlySlimeMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FriendlySlimeFloatGoal(this));
        this.goalSelector.addGoal(1, new FollowPlayerGoal(this));
    }

    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (splitCooldown > 0) {
                splitCooldown--;
            }
            if (staying) {
                setNoAi(true);
                setJumping(false);
                getNavigation().stop();
                setTarget(null);
                ((FriendlySlimeMoveControl) getMoveControl()).stopMovement();
                var movement = getDeltaMovement();
                if (commandHopTicks > 0) {
                    commandHopTicks--;
                }
                double verticalMovement = commandHopTicks == 0 && onGround() ? 0.0D : movement.y;
                setDeltaMovement(0.0D, verticalMovement, 0.0D);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(Items.SLIME_BALL)) {
            if (splitCooldown > 0) {
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
            if (level() instanceof ServerLevel serverLevel) {
                Entity created = getType().create(serverLevel);
                if (created instanceof FriendlyIndustrialSlime splitSlime) {
                    splitSlime.setSize(1, true);
                    splitSlime.splitCooldown = SPLIT_COOLDOWN_TICKS;
                    splitSlime.moveTo(
                            getX() + (random.nextBoolean() ? 0.65D : -0.65D),
                            getY(),
                            getZ() + (random.nextBoolean() ? 0.65D : -0.65D),
                            getYRot(),
                            0.0F
                    );
                    if (serverLevel.addFreshEntity(splitSlime)) {
                        splitCooldown = SPLIT_COOLDOWN_TICKS;
                        if (!player.getAbilities().instabuild) {
                            heldItem.shrink(1);
                        }
                    }
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        if (hand == InteractionHand.MAIN_HAND && heldItem.isEmpty()) {
            if (!level().isClientSide()) {
                staying = !staying;
                commandHopTicks = staying ? COMMAND_HOP_TICKS : 0;
                commandJumpPending = false;
                if (staying) {
                    getNavigation().stop();
                    setTarget(null);
                    ((FriendlySlimeMoveControl) getMoveControl()).stopMovement();
                    if (onGround()) {
                        commandJumpPending = true;
                        jumpFromGround();
                    }
                    setNoAi(true);
                } else {
                    setNoAi(false);
                    setJumping(false);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(SPLIT_COOLDOWN_TAG, splitCooldown);
        tag.putBoolean(STAYING_TAG, staying);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        splitCooldown = Math.max(0, tag.getInt(SPLIT_COOLDOWN_TAG));
        staying = tag.getBoolean(STAYING_TAG);
        commandHopTicks = 0;
        commandJumpPending = false;
        if (staying) {
            setNoAi(true);
            setJumping(false);
        }
    }

    public boolean isStaying() {
        return staying;
    }

    protected boolean consumeJumpPermission() {
        if (!staying) {
            return true;
        }
        if (commandJumpPending) {
            commandJumpPending = false;
            return true;
        }
        return false;
    }

    @Override
    protected boolean doPlayJumpSound() {
        return !staying && super.doPlayJumpSound();
    }

    private static final class FollowPlayerGoal extends Goal {
        private final FriendlyIndustrialSlime slime;
        private Player player;

        private FollowPlayerGoal(FriendlyIndustrialSlime slime) {
            this.slime = slime;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            player = slime.level().getNearestPlayer(slime, 16.0D);
            return !slime.isStaying() && player != null && !player.isSpectator()
                    && slime.distanceToSqr(player) <= FOLLOW_RANGE_SQUARED;
        }

        @Override
        public boolean canContinueToUse() {
            return !slime.isStaying() && player != null && player.isAlive() && !player.isSpectator()
                    && slime.distanceToSqr(player) <= FOLLOW_RANGE_SQUARED;
        }

        @Override
        public void tick() {
            slime.getLookControl().setLookAt(player, 10.0F, 10.0F);
            FriendlySlimeMoveControl moveControl = (FriendlySlimeMoveControl) slime.getMoveControl();
            if (slime.distanceToSqr(player) >= STOP_RANGE_SQUARED) {
                double deltaX = player.getX() - slime.getX();
                double deltaZ = player.getZ() - slime.getZ();
                float targetYaw = (float) (Mth.atan2(deltaZ, deltaX) * Mth.RAD_TO_DEG) - 90.0F;
                moveControl.setDirection(targetYaw);
                moveControl.setWantedMovement(1.0D);
            } else {
                moveControl.stopMovement();
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            player = null;
            ((FriendlySlimeMoveControl) slime.getMoveControl()).stopMovement();
        }
    }

    private static final class FriendlySlimeFloatGoal extends Goal {
        private final FriendlyIndustrialSlime slime;

        private FriendlySlimeFloatGoal(FriendlyIndustrialSlime slime) {
            this.slime = slime;
            setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
            slime.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return slime.isInWater() || slime.isInLava();
        }

        @Override
        public void tick() {
            if (slime.getRandom().nextFloat() < 0.8F) {
                slime.getJumpControl().jump();
            }
            ((FriendlySlimeMoveControl) slime.getMoveControl()).setWantedMovement(1.2D);
        }
    }

    private static final class FriendlySlimeMoveControl extends MoveControl {
        private final FriendlyIndustrialSlime slime;
        private float targetYaw;
        private int jumpDelay;

        private FriendlySlimeMoveControl(FriendlyIndustrialSlime slime) {
            super(slime);
            this.slime = slime;
            this.targetYaw = slime.getYRot();
        }

        private void setDirection(float targetYaw) {
            this.targetYaw = targetYaw;
        }

        private void setWantedMovement(double speed) {
            this.speedModifier = speed;
            this.operation = Operation.MOVE_TO;
        }

        private void stopMovement() {
            this.operation = Operation.WAIT;
            this.speedModifier = 0.0D;
            slime.setSpeed(0.0F);
            slime.setXxa(0.0F);
            slime.setZza(0.0F);
            var movement = slime.getDeltaMovement();
            slime.setDeltaMovement(0.0D, movement.y, 0.0D);
        }

        @Override
        public void tick() {
            if (slime.isStaying()) {
                stopMovement();
                return;
            }
            slime.setYRot(rotlerp(slime.getYRot(), targetYaw, 90.0F));
            slime.setYHeadRot(slime.getYRot());
            slime.yBodyRot = slime.getYRot();
            if (operation != Operation.MOVE_TO) {
                slime.setZza(0.0F);
                return;
            }

            operation = Operation.WAIT;
            float speed = (float) (speedModifier * slime.getAttributeValue(Attributes.MOVEMENT_SPEED));
            if (slime.onGround()) {
                slime.setSpeed(speed);
                if (jumpDelay-- <= 0) {
                    jumpDelay = slime.getJumpDelay();
                    slime.getJumpControl().jump();
                } else {
                    slime.setXxa(0.0F);
                    slime.setZza(0.0F);
                    slime.setSpeed(0.0F);
                }
            } else {
                slime.setSpeed(speed);
            }
        }
    }
}
