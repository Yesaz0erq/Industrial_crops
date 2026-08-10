package com.industrialcrops.block.entity;

import com.industrialcrops.item.IncubatorBlockItem;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.MachineInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
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

public final class SlimeIncubatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int MAX_PROGRESS = 160;
    public static final int UPGRADE_SLOT_START = 2, UPGRADE_SLOT_COUNT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == INPUT_SLOT ? 1 : super.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && isAcceptedSlimeInput(stack)
                    || slot >= UPGRADE_SLOT_START && SpeedUpgradeHelper.isSpeedUpgrade(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int activeSlimeType = IncubatorBlockEntity.SLIME_NONE;
    private int progress;

    public SlimeIncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLIME_INCUBATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SlimeIncubatorBlockEntity slime_converter) {
        if (level.isClientSide()) {
            return;
        }

        ItemStack input = slime_converter.inventory.getStackInSlot(INPUT_SLOT);
        int slimeType = getInputSlimeType(input);
        if (slimeType == IncubatorBlockEntity.SLIME_NONE) {
            slime_converter.resetProgress();
            return;
        }

        if (slime_converter.activeSlimeType != slimeType) {
            slime_converter.activeSlimeType = slimeType;
            slime_converter.progress = 0;
            slime_converter.setChanged();
        }

        ItemStack result = getSlimeDrop(slimeType);
        if (result.isEmpty() || !canOutputAccept(slime_converter.inventory.getStackInSlot(OUTPUT_SLOT), result)) {
            return;
        }

        slime_converter.progress += SpeedUpgradeHelper.progressStep(slime_converter.inventory, UPGRADE_SLOT_START, UPGRADE_SLOT_COUNT, MAX_PROGRESS);
        if (slime_converter.progress >= MAX_PROGRESS) {
            slime_converter.insertOutput(result);
            slime_converter.progress = 0;
        }
        slime_converter.setChanged();
    }

    public static boolean isAcceptedSlimeInput(ItemStack stack) {
        return getInputSlimeType(stack) != IncubatorBlockEntity.SLIME_NONE;
    }

    public static int getInputSlimeType(ItemStack stack) {
        if (stack.isEmpty()) {
            return IncubatorBlockEntity.SLIME_NONE;
        }
        if (stack.is(ModItems.INCUBATOR.get())) {
            return IncubatorBlockItem.getStoredType(stack);
        }
        if (stack.is(Items.SLIME_SPAWN_EGG)) {
            return IncubatorBlockEntity.SLIME_VANILLA;
        }
        if (stack.is(ModItems.BROWN_CREATE_SLIME_SPAWN_EGG.get())) {
            return IncubatorBlockEntity.SLIME_COPPER;
        }
        if (stack.is(ModItems.GRAY_GEAR_SLIME_SPAWN_EGG.get())) {
            return IncubatorBlockEntity.SLIME_IRON;
        }
        if (stack.is(ModItems.GOLDEN_REDSTONE_LAMP_SLIME_SPAWN_EGG.get())) {
            return IncubatorBlockEntity.SLIME_GOLD;
        }
        if (stack.is(ModItems.BLUE_COMPONENT_SUBSTRATE_SLIME_SPAWN_EGG.get())) {
            return IncubatorBlockEntity.SLIME_DIAMOND;
        }
        return IncubatorBlockEntity.SLIME_NONE;
    }

    public static ItemStack getSlimeDrop(int slimeType) {
        return switch (slimeType) {
            case IncubatorBlockEntity.SLIME_VANILLA -> new ItemStack(Items.SLIME_BALL);
            case IncubatorBlockEntity.SLIME_COPPER -> new ItemStack(Items.COPPER_INGOT);
            case IncubatorBlockEntity.SLIME_IRON -> new ItemStack(Items.IRON_INGOT);
            case IncubatorBlockEntity.SLIME_GOLD -> new ItemStack(Items.GOLD_INGOT);
            case IncubatorBlockEntity.SLIME_DIAMOND -> new ItemStack(ModItems.COMPONENT_SUBSTRATE.get());
            default -> ItemStack.EMPTY;
        };
    }

    public int getProgress() {
        return progress;
    }

    public int getActiveSlimeType() {
        return activeSlimeType;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getSlots() {
        return inventory.getSlots();
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("ActiveSlimeType", activeSlimeType);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
            MachineInventoryHelper.ensureSize(inventory, UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT);
        }
        activeSlimeType = tag.getInt("ActiveSlimeType");
        progress = tag.getInt("Progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.slime_incubator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.industrialcrops.screen.SlimeIncubatorMenu(containerId, inventory, this, worldPosition);
    }

    private void insertOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
            inventory.setStackInSlot(OUTPUT_SLOT, output);
        }
    }

    private void resetProgress() {
        if (progress != 0 || activeSlimeType != IncubatorBlockEntity.SLIME_NONE) {
            progress = 0;
            activeSlimeType = IncubatorBlockEntity.SLIME_NONE;
            setChanged();
        }
    }

    private static boolean canOutputAccept(ItemStack outputStack, ItemStack resultStack) {
        if (outputStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(outputStack, resultStack)) {
            return false;
        }
        return outputStack.getCount() + resultStack.getCount() <= outputStack.getMaxStackSize();
    }
}
