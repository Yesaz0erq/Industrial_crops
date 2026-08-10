package com.industrialcrops.block.entity;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.entity.GoldenRocketEntity;
import com.industrialcrops.entity.TargetMarkerEntity;
import com.industrialcrops.launcher.RedstoneTriggeredLauncher;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.screen.GoldenLaunchSiloMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/** Stores explosive potatoes, four upgrade slots and the persistent coordinate target. */
public final class GoldenLaunchSiloBlockEntity extends BlockEntity implements MenuProvider, RedstoneTriggeredLauncher {
    public static final int LAUNCH_COOLDOWN = 100;
    public static final int ROCKET_SLOT = 0;
    public static final int UPGRADE_SLOT_START = 1;
    public static final int UPGRADE_SLOT_COUNT = 4;
    public static final int MAX_POWER_UPGRADES = 4;

    public static final int STATUS_TARGET_REQUIRED = 0;
    public static final int STATUS_ROCKET_REQUIRED = 1;
    public static final int STATUS_COOLDOWN = 2;
    public static final int STATUS_READY = 3;
    public static final int STATUS_TARGET_UNLOADED = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(1 + UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == ROCKET_SLOT) {
                return isRocket(stack);
            }
            if (slot < UPGRADE_SLOT_START || slot >= UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT) {
                return false;
            }
            if (stack.is(ModItems.RAPID_FIRE_COMPONENT.get())) {
                return getUpgradeCount(ModItems.RAPID_FIRE_COMPONENT.get(), slot) == 0
                        || getStackInSlot(slot).is(ModItems.RAPID_FIRE_COMPONENT.get());
            }
            if (stack.is(ModItems.POWER_COMPONENT.get())) {
                return getUpgradeCount(ModItems.POWER_COMPONENT.get(), slot) < MAX_POWER_UPGRADES
                        || getStackInSlot(slot).is(ModItems.POWER_COMPONENT.get());
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };
    private final IItemHandler ammunitionInventory = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(ROCKET_SLOT);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return inventory.insertItem(ROCKET_SLOT, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.extractItem(ROCKET_SLOT, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(ROCKET_SLOT);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return inventory.isItemValid(ROCKET_SLOT, stack);
        }
    };

    private BlockPos target = BlockPos.ZERO;
    private int coordinateX;
    private int coordinateY;
    private int coordinateZ;
    private boolean relativeMode;
    private boolean ignoreYMode;
    private boolean targetConfigured;
    private int cooldown;
    private boolean redstonePowered;
    private int markerCheckTicks;
    private @Nullable UUID markerUuid;

    public GoldenLaunchSiloBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOLDEN_LAUNCH_SILO.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GoldenLaunchSiloBlockEntity silo) {
        if (level.isClientSide()) {
            return;
        }
        silo.checkRedstoneTrigger(level, pos);
        if (silo.cooldown > 0) {
            silo.cooldown--;
            silo.setChangedAndSync();
        }
        if (++silo.markerCheckTicks >= 40) {
            silo.markerCheckTicks = 0;
            silo.ensureTargetMarker();
        }
    }

    public boolean configureTarget(int inputX, int inputY, int inputZ, boolean useRelativeMode, boolean useIgnoreYMode) {
        if (level == null) {
            return false;
        }

        long resolvedX = useRelativeMode ? worldPosition.getX() + (long) inputX : inputX;
        long resolvedZ = useRelativeMode ? worldPosition.getZ() + (long) inputZ : inputZ;
        if (Math.abs(resolvedX) > 29_999_984L
                || Math.abs(resolvedZ) > 29_999_984L) {
            return false;
        }
        if (useIgnoreYMode && !level.hasChunk((int) resolvedX >> 4, (int) resolvedZ >> 4)) {
            return false;
        }
        long resolvedY = useIgnoreYMode
                ? Math.max(level.getMinBuildHeight(), level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (int) resolvedX,
                        (int) resolvedZ
                ) - 1L)
                : useRelativeMode ? worldPosition.getY() + (long) inputY : inputY;
        if (resolvedY < level.getMinBuildHeight() || resolvedY >= level.getMaxBuildHeight()) {
            return false;
        }

        removeTargetMarker();
        coordinateX = inputX;
        coordinateY = inputY;
        coordinateZ = inputZ;
        relativeMode = useRelativeMode;
        ignoreYMode = useIgnoreYMode;
        target = new BlockPos((int) resolvedX, (int) resolvedY, (int) resolvedZ);
        targetConfigured = true;
        setChangedAndSync();
        ensureTargetMarker();
        return true;
    }

    public boolean launch() {
        if (!(level instanceof ServerLevel serverLevel) || getLaunchStatus() != STATUS_READY) {
            return false;
        }

        int powerMultiplier = getPowerMultiplier();
        GoldenRocketEntity rocket = new GoldenRocketEntity(
                serverLevel,
                worldPosition,
                target,
                powerMultiplier,
                ignoreYMode
        );
        rocket.setPos(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.2D, worldPosition.getZ() + 0.5D);
        serverLevel.addFreshEntity(rocket);
        inventory.extractItem(ROCKET_SLOT, getRequiredPotatoes(), false);
        cooldown = hasRapidFireUpgrade() ? 0 : LAUNCH_COOLDOWN;
        serverLevel.playSound(null, worldPosition, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 1.4F, 0.7F);
        setChangedAndSync();
        return true;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public IItemHandler getAmmunitionInventory() {
        return ammunitionInventory;
    }

    public int getSlots() {
        return inventory.getSlots();
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public BlockPos getTarget() {
        return target;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public int getCoordinateY() {
        return coordinateY;
    }

    public int getCoordinateZ() {
        return coordinateZ;
    }

    public boolean isRelativeMode() {
        return relativeMode;
    }

    public boolean isIgnoreYMode() {
        return ignoreYMode;
    }

    public boolean hasTarget() {
        return targetConfigured;
    }

    public int getCooldown() {
        return cooldown;
    }

    @Override
    public boolean wasRedstonePowered() {
        return redstonePowered;
    }

    @Override
    public void setRedstonePowered(boolean powered) {
        if (redstonePowered != powered) {
            redstonePowered = powered;
            setChanged();
        }
    }

    public boolean hasRapidFireUpgrade() {
        return getUpgradeCount(ModItems.RAPID_FIRE_COMPONENT.get(), -1) > 0;
    }

    public int getPowerUpgradeCount() {
        return Math.min(MAX_POWER_UPGRADES, getUpgradeCount(ModItems.POWER_COMPONENT.get(), -1));
    }

    public int getRequiredPotatoes() {
        int powerUpgrades = getPowerUpgradeCount();
        return powerUpgrades == 0 ? 1 : powerUpgrades * 4;
    }

    public int getPowerMultiplier() {
        return getRequiredPotatoes();
    }

    public int getLaunchStatus() {
        if (!targetConfigured) {
            return STATUS_TARGET_REQUIRED;
        }
        if (!isTrajectoryLoaded()) {
            return STATUS_TARGET_UNLOADED;
        }
        ItemStack ammunition = inventory.getStackInSlot(ROCKET_SLOT);
        if (!isRocket(ammunition) || ammunition.getCount() < getRequiredPotatoes()) {
            return STATUS_ROCKET_REQUIRED;
        }
        if (!hasRapidFireUpgrade() && cooldown > 0) {
            return STATUS_COOLDOWN;
        }
        return STATUS_READY;
    }

    /** Every horizontal chunk crossed by the launch-to-target line must already be loaded. */
    public boolean isTrajectoryLoaded() {
        if (level == null || !targetConfigured) {
            return false;
        }

        int chunkX = worldPosition.getX() >> 4;
        int chunkZ = worldPosition.getZ() >> 4;
        int targetChunkX = target.getX() >> 4;
        int targetChunkZ = target.getZ() >> 4;
        int deltaX = Math.abs(targetChunkX - chunkX);
        int deltaZ = Math.abs(targetChunkZ - chunkZ);
        int stepX = chunkX < targetChunkX ? 1 : -1;
        int stepZ = chunkZ < targetChunkZ ? 1 : -1;
        int error = deltaX - deltaZ;

        while (true) {
            if (!level.hasChunk(chunkX, chunkZ)) {
                return false;
            }
            if (chunkX == targetChunkX && chunkZ == targetChunkZ) {
                return true;
            }
            int doubledError = error * 2;
            if (doubledError > -deltaZ) {
                error -= deltaZ;
                chunkX += stepX;
            }
            if (doubledError < deltaX) {
                error += deltaX;
                chunkZ += stepZ;
            }
        }
    }

    public static boolean isRocket(ItemStack stack) {
        return stack.is(ModItems.EXPLOSIVE_POTATO.get());
    }

    public static boolean isUpgrade(ItemStack stack) {
        return stack.is(ModItems.RAPID_FIRE_COMPONENT.get()) || stack.is(ModItems.POWER_COMPONENT.get());
    }

    public void removeTargetMarker() {
        if (level instanceof ServerLevel serverLevel && markerUuid != null
                && serverLevel.getEntity(markerUuid) instanceof TargetMarkerEntity marker) {
            marker.discard();
        }
        markerUuid = null;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("TargetX", target.getX());
        tag.putInt("TargetY", target.getY());
        tag.putInt("TargetZ", target.getZ());
        tag.putInt("CoordinateX", coordinateX);
        tag.putInt("CoordinateY", coordinateY);
        tag.putInt("CoordinateZ", coordinateZ);
        tag.putBoolean("RelativeMode", relativeMode);
        tag.putBoolean("IgnoreYMode", ignoreYMode);
        tag.putBoolean("TargetConfigured", targetConfigured);
        tag.putInt("Cooldown", cooldown);
        tag.putBoolean("RedstonePowered", redstonePowered);
        if (markerUuid != null) {
            tag.putUUID("TargetMarker", markerUuid);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
            normalizeUpgradeSlotCount();
        }
        target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        if (tag.contains("CoordinateX")) {
            coordinateX = tag.getInt("CoordinateX");
            coordinateY = tag.getInt("CoordinateY");
            coordinateZ = tag.getInt("CoordinateZ");
            relativeMode = tag.getBoolean("RelativeMode");
        } else {
            coordinateX = target.getX();
            coordinateY = target.getY();
            coordinateZ = target.getZ();
            relativeMode = false;
        }
        ignoreYMode = tag.getBoolean("IgnoreYMode");
        targetConfigured = tag.getBoolean("TargetConfigured");
        cooldown = tag.getInt("Cooldown");
        redstonePowered = tag.getBoolean("RedstonePowered");
        markerUuid = tag.hasUUID("TargetMarker") ? tag.getUUID("TargetMarker") : null;
    }

    /** Compacts inventories saved by builds that exposed five upgrade slots. */
    private void normalizeUpgradeSlotCount() {
        int expectedSlots = 1 + UPGRADE_SLOT_COUNT;
        if (inventory.getSlots() == expectedSlots) {
            return;
        }

        ItemStack rocket = inventory.getSlots() > ROCKET_SLOT
                ? inventory.getStackInSlot(ROCKET_SLOT).copy()
                : ItemStack.EMPTY;
        List<ItemStack> upgrades = new ArrayList<>();
        for (int slot = UPGRADE_SLOT_START; slot < inventory.getSlots(); slot++) {
            ItemStack remaining = inventory.getStackInSlot(slot).copy();
            if (remaining.isEmpty()) {
                continue;
            }
            for (ItemStack existing : upgrades) {
                if (!remaining.isEmpty() && ItemStack.isSameItemSameComponents(existing, remaining)) {
                    int moved = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(moved);
                    remaining.shrink(moved);
                }
            }
            if (!remaining.isEmpty()) {
                upgrades.add(remaining);
            }
        }

        inventory.setSize(expectedSlots);
        inventory.setStackInSlot(ROCKET_SLOT, rocket);
        int targetSlot = UPGRADE_SLOT_START;
        for (ItemStack upgrade : upgrades) {
            if (targetSlot >= expectedSlots) {
                IndustrialCrops.LOGGER.warn(
                        "Could not compact every legacy launcher upgrade at {}: {}",
                        worldPosition,
                        upgrade
                );
                break;
            }
            inventory.setStackInSlot(targetSlot++, upgrade);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.explosive_potato_launcher");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GoldenLaunchSiloMenu(containerId, playerInventory, this, worldPosition);
    }

    private int getUpgradeCount(Item item, int ignoredSlot) {
        int count = 0;
        for (int slot = UPGRADE_SLOT_START; slot < UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT; slot++) {
            if (slot != ignoredSlot && inventory.getStackInSlot(slot).is(item)) {
                count += inventory.getStackInSlot(slot).getCount();
            }
        }
        return count;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    private void ensureTargetMarker() {
        if (!(level instanceof ServerLevel serverLevel) || !targetConfigured || !isTrajectoryLoaded()) {
            return;
        }
        if (markerUuid != null && serverLevel.getEntity(markerUuid) instanceof TargetMarkerEntity marker) {
            if (marker.getSiloPos().equals(worldPosition) && marker.blockPosition().equals(target)) {
                return;
            }
            marker.discard();
        }
        for (TargetMarkerEntity marker : serverLevel.getEntitiesOfClass(
                TargetMarkerEntity.class,
                new AABB(target).inflate(0.25D),
                candidate -> candidate.getSiloPos().equals(worldPosition)
        )) {
            markerUuid = marker.getUUID();
            setChanged();
            return;
        }

        TargetMarkerEntity marker = new TargetMarkerEntity(serverLevel, worldPosition, target);
        if (serverLevel.addFreshEntity(marker)) {
            markerUuid = marker.getUUID();
            setChanged();
        }
    }
}
