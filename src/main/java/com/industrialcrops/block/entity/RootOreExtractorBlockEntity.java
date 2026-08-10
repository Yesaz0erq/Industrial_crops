package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModBlocks;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class RootOreExtractorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int CATALYST_SLOT = 1;
    public static final int BAG_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;
    public static final int MAX_PROGRESS = 160;
    public static final int UPGRADE_SLOT_START = 4, UPGRADE_SLOT_COUNT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case INPUT_SLOT -> isAcceptedInput(stack);
                case CATALYST_SLOT -> isCatalyst(stack);
                case BAG_SLOT -> isEmptyBag(stack);
                default -> slot >= UPGRADE_SLOT_START && SpeedUpgradeHelper.isSpeedUpgrade(stack);
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private int progress;

    public RootOreExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROOT_ORE_EXTRACTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RootOreExtractorBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        ItemStack inputStack = blockEntity.inventory.getStackInSlot(INPUT_SLOT);
        if (!blockEntity.hasCatalyst()
                || !isAcceptedInput(inputStack)
                || requiresBag(inputStack) && !blockEntity.hasBag()) {
            blockEntity.resetProgress();
            return;
        }

        ItemStack resultStack = blockEntity.getProcessResult(inputStack);
        if (resultStack.isEmpty()) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.progress += SpeedUpgradeHelper.progressStep(blockEntity.inventory, UPGRADE_SLOT_START, UPGRADE_SLOT_COUNT, MAX_PROGRESS);
        if (blockEntity.progress >= MAX_PROGRESS) {
            blockEntity.finishProcessing(resultStack, requiresBag(inputStack));
        }
        blockEntity.setChanged();
    }

    public static boolean isAcceptedInput(ItemStack stack) {
        return stack.is(Items.CARROT)
                || stack.is(Items.POTATO)
                || stack.is(Items.WHEAT)
                || stack.is(Items.WHEAT_SEEDS)
                || stack.is(Items.MELON_SEEDS)
                || stack.is(Items.PUMPKIN_SEEDS)
                || stack.is(Blocks.HAY_BLOCK.asItem())
                || stack.is(Blocks.MELON.asItem())
                || stack.is(Blocks.PUMPKIN.asItem());
    }

    public static boolean isCatalyst(ItemStack stack) {
        return stack.is(ModItems.REDSTONE_BONEMEAL.get());
    }

    public static boolean isEmptyBag(ItemStack stack) {
        return stack.is(ModItems.EMPTY_BAG.get());
    }

    public static boolean requiresBag(ItemStack inputStack) {
        return inputStack.is(Items.CARROT)
                || inputStack.is(Items.POTATO)
                || inputStack.is(Items.WHEAT_SEEDS)
                || inputStack.is(Items.MELON_SEEDS)
                || inputStack.is(Items.PUMPKIN_SEEDS);
    }

    public int getProgress() {
        return progress;
    }

    public boolean hasCatalyst() {
        return isCatalyst(inventory.getStackInSlot(CATALYST_SLOT));
    }

    public boolean hasBag() {
        return isEmptyBag(inventory.getStackInSlot(BAG_SLOT));
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

    public ItemStack insertPipeStack(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (isAcceptedInput(stack)) {
            return inventory.insertItem(INPUT_SLOT, stack, simulate);
        }
        if (isCatalyst(stack)) {
            return inventory.insertItem(CATALYST_SLOT, stack, simulate);
        }
        if (isEmptyBag(stack)) {
            return inventory.insertItem(BAG_SLOT, stack, simulate);
        }
        return stack;
    }

    public ItemStack extractPipeOutput(int amount, boolean simulate) {
        return inventory.extractItem(OUTPUT_SLOT, amount, simulate);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            CompoundTag inventoryTag = tag.getCompound("Inventory");
            boolean legacyLayout = inventoryTag.getInt("Size") == 7;
            inventory.deserializeNBT(registries, inventoryTag);
            if (legacyLayout) {
                ItemStack legacyOutput = inventory.getStackInSlot(2).copy();
                ItemStack[] legacyUpgrades = new ItemStack[UPGRADE_SLOT_COUNT];
                for (int index = 0; index < UPGRADE_SLOT_COUNT; index++) {
                    legacyUpgrades[index] = inventory.getStackInSlot(3 + index).copy();
                }
                MachineInventoryHelper.ensureSize(inventory, UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT);
                for (int index = 2; index < 7; index++) {
                    inventory.setStackInSlot(index, ItemStack.EMPTY);
                }
                inventory.setStackInSlot(OUTPUT_SLOT, legacyOutput);
                for (int index = 0; index < UPGRADE_SLOT_COUNT; index++) {
                    inventory.setStackInSlot(UPGRADE_SLOT_START + index, legacyUpgrades[index]);
                }
            }
            MachineInventoryHelper.ensureSize(inventory, UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT);
        }
        progress = tag.getInt("Progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.basic_crop_conversion_device");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new com.industrialcrops.screen.RootOreExtractorMenu(containerId, playerInventory, this, worldPosition);
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    private void finishProcessing(ItemStack resultStack, boolean consumeBag) {
        if (level != null && com.industrialcrops.crop.CropGenetics.isGeneticCrop(resultStack)) {
            com.industrialcrops.crop.CropGenetics.initializeInitial(resultStack, level.random);
        }
        ItemStack outputStack = inventory.getStackInSlot(OUTPUT_SLOT);
        if (!canOutputAccept(outputStack, resultStack)) {
            resetProgress();
            return;
        }

        inventory.extractItem(INPUT_SLOT, 1, false);
        inventory.extractItem(CATALYST_SLOT, 1, false);
        if (consumeBag) {
            inventory.extractItem(BAG_SLOT, 1, false);
        }
        if (outputStack.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, resultStack.copy());
        } else {
            outputStack.grow(resultStack.getCount());
            inventory.setStackInSlot(OUTPUT_SLOT, outputStack);
        }
        progress = 0;
    }

    private ItemStack getProcessResult(ItemStack inputStack) {
        ItemStack resultStack = ItemStack.EMPTY;
        if (inputStack.is(Items.CARROT)) {
            resultStack = new ItemStack(ModItems.BAGGED_INDUSTRIAL_CARROT.get());
        } else if (inputStack.is(Items.POTATO)) {
            resultStack = new ItemStack(ModItems.BAGGED_INDUSTRIAL_POTATO.get());
        } else if (inputStack.is(Items.WHEAT)) {
            resultStack = new ItemStack(ModItems.INDUSTRIAL_WHEAT.get());
        } else if (inputStack.is(Items.WHEAT_SEEDS)) {
            resultStack = new ItemStack(ModItems.BAGGED_INDUSTRIAL_WHEAT_SEEDS.get());
        } else if (inputStack.is(Items.MELON_SEEDS)) {
            resultStack = new ItemStack(ModItems.BAGGED_INDUSTRIAL_MELON_SEEDS.get());
        } else if (inputStack.is(Items.PUMPKIN_SEEDS)) {
            resultStack = new ItemStack(ModItems.BAGGED_INDUSTRIAL_PUMPKIN_SEEDS.get());
        } else if (inputStack.is(Blocks.HAY_BLOCK.asItem())) {
            resultStack = new ItemStack(ModBlocks.INDUSTRIAL_WHEAT_BLOCK.asItem());
        } else if (inputStack.is(Blocks.MELON.asItem())) {
            resultStack = new ItemStack(ModItems.INDUSTRIAL_MELON.get());
        } else if (inputStack.is(Blocks.PUMPKIN.asItem())) {
            resultStack = new ItemStack(ModItems.INDUSTRIAL_PUMPKIN.get());
        }

        if (resultStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return canOutputAccept(inventory.getStackInSlot(OUTPUT_SLOT), resultStack)
                ? resultStack
                : ItemStack.EMPTY;
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
