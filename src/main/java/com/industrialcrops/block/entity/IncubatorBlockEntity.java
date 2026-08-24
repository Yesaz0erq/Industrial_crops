package com.industrialcrops.block.entity;

import com.industrialcrops.block.IncubatorBlock;
import com.industrialcrops.item.IncubatorBlockItem;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModEntities;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.MachineInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class IncubatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLIME_NONE = 0;
    public static final int SLIME_VANILLA = 1;
    public static final int SLIME_COPPER = 2;
    public static final int SLIME_IRON = 3;
    public static final int SLIME_GOLD = 4;
    public static final int SLIME_DIAMOND = 5;
    public static final int MAX_PROGRESS = 160;
    public static final int UPGRADE_SLOT_START = 1, UPGRADE_SLOT_COUNT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 ? isRawOre(stack) : SpeedUpgradeHelper.isSpeedUpgrade(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private int slimeType;
    private int slimeSize = 1;
    private int progress;

    public IncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INCUBATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, IncubatorBlockEntity slime_converter) {
        if (level.isClientSide()) {
            return;
        }
        int targetType = getOreSlimeType(slime_converter.inventory.getStackInSlot(0));
        if (!slime_converter.hasSlime() || targetType == SLIME_NONE || targetType == slime_converter.slimeType) {
            slime_converter.resetProgress();
            return;
        }

        slime_converter.progress += SpeedUpgradeHelper.progressStep(slime_converter.inventory, UPGRADE_SLOT_START, UPGRADE_SLOT_COUNT, MAX_PROGRESS);
        if (slime_converter.progress >= MAX_PROGRESS) {
            slime_converter.inventory.extractItem(0, 1, false);
            slime_converter.slimeType = targetType;
            slime_converter.slimeSize = 1;
            slime_converter.progress = 0;
            slime_converter.sync();
        } else {
            slime_converter.setChanged();
        }
    }

    public boolean releaseSlime(Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !hasSlime()) {
            return false;
        }

        EntityType<? extends Slime> entityType = switch (slimeType) {
            case SLIME_COPPER -> ModEntities.BROWN_CREATE_SLIME.get();
            case SLIME_IRON -> ModEntities.GRAY_GEAR_SLIME.get();
            case SLIME_GOLD -> ModEntities.GOLDEN_REDSTONE_LAMP_SLIME.get();
            case SLIME_DIAMOND -> ModEntities.DIAMOND_PROCESSOR_SLIME.get();
            default -> EntityType.SLIME;
        };
        Slime slime = entityType.create(serverLevel);
        if (slime == null) {
            return false;
        }

        slime.setSize(Math.max(1, slimeSize), true);
        Direction facing = getBlockState().getValue(IncubatorBlock.FACING);
        double x = worldPosition.getX() + 0.5D + facing.getStepX() * 1.15D;
        double y = worldPosition.getY() + 0.2D;
        double z = worldPosition.getZ() + 0.5D + facing.getStepZ() * 1.15D;
        slime.moveTo(x, y, z, facing.toYRot(), 0.0F);
        slime.setDeltaMovement(facing.getStepX() * 0.45D, 0.22D, facing.getStepZ() * 0.45D);
        if (!serverLevel.addFreshEntity(slime)) {
            return false;
        }

        slimeType = SLIME_NONE;
        slimeSize = 1;
        progress = 0;
        sync();
        return true;
    }

    public void setStoredSlime(int type, int size) {
        slimeType = Math.max(SLIME_NONE, Math.min(SLIME_DIAMOND, type));
        slimeSize = Math.max(1, size);
        progress = 0;
        sync();
    }

    public void writeStoredSlime(ItemStack stack) {
        IncubatorBlockItem.setStoredSlime(stack, slimeType, slimeSize);
    }

    public boolean hasSlime() {
        return slimeType != SLIME_NONE;
    }

    public int getSlimeType() {
        return slimeType;
    }

    public int getSlimeSize() {
        return slimeSize;
    }

    public int getProgress() {
        return progress;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public static boolean isRawOre(ItemStack stack) {
        return stack.is(Items.COPPER_ORE)
                || stack.is(Items.DEEPSLATE_COPPER_ORE)
                || stack.is(Items.IRON_ORE)
                || stack.is(Items.DEEPSLATE_IRON_ORE)
                || stack.is(Items.GOLD_ORE)
                || stack.is(Items.DEEPSLATE_GOLD_ORE)
                || stack.is(Items.DIAMOND_ORE)
                || stack.is(Items.DEEPSLATE_DIAMOND_ORE);
    }

    public static Component getSlimeName(int type) {
        return switch (type) {
            case SLIME_VANILLA -> Component.translatable("entity.minecraft.slime");
            case SLIME_COPPER -> Component.translatable("entity.industrialcrops.copper_gear_slime");
            case SLIME_IRON -> Component.translatable("entity.industrialcrops.gray_gear_slime");
            case SLIME_GOLD -> Component.translatable("entity.industrialcrops.golden_redstone_lamp_slime");
            case SLIME_DIAMOND -> Component.translatable("entity.industrialcrops.blue_processor_slime");
            default -> Component.translatable("gui.industrialcrops.slime_converter.empty");
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("SlimeType", slimeType);
        tag.putInt("SlimeSize", slimeSize);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
            MachineInventoryHelper.ensureSize(inventory, UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT);
        }
        slimeType = tag.getInt("SlimeType");
        slimeSize = Math.max(1, tag.getInt("SlimeSize"));
        progress = tag.getInt("Progress");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.slime_converter");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.industrialcrops.screen.IncubatorMenu(containerId, inventory, this, worldPosition);
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private static int getOreSlimeType(ItemStack stack) {
        if (stack.is(Items.COPPER_ORE) || stack.is(Items.DEEPSLATE_COPPER_ORE)) {
            return SLIME_COPPER;
        }
        if (stack.is(Items.IRON_ORE) || stack.is(Items.DEEPSLATE_IRON_ORE)) {
            return SLIME_IRON;
        }
        if (stack.is(Items.GOLD_ORE) || stack.is(Items.DEEPSLATE_GOLD_ORE)) {
            return SLIME_GOLD;
        }
        if (stack.is(Items.DIAMOND_ORE) || stack.is(Items.DEEPSLATE_DIAMOND_ORE)) {
            return SLIME_DIAMOND;
        }
        return SLIME_NONE;
    }
}
