package com.industrialcrops.block.entity;

import com.industrialcrops.recipe.ManipulatorIngredient;
import com.industrialcrops.recipe.ManipulatorRecipeDisplay;
import com.industrialcrops.recipe.ProcessorProgrammingRecipes;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.ProcessorProgrammerMenu;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.MachineInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class ProcessorProgrammerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT_COUNT = 5;
    public static final int OUTPUT_SLOT = INPUT_SLOT_COUNT;
    public static final int SLOT_COUNT = INPUT_SLOT_COUNT + 1;
    public static final int UPGRADE_SLOT_START = SLOT_COUNT, UPGRADE_SLOT_COUNT = 4;
    public static final int MAX_PROGRESS = 160;

    private boolean crafting;
    private int progress;
    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT + UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot < INPUT_SLOT_COUNT && ProcessorProgrammingRecipes.isIngredient(stack)
                    || slot >= UPGRADE_SLOT_START && SpeedUpgradeHelper.isSpeedUpgrade(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndSync();
        }
    };

    public ProcessorProgrammerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PROCESSOR_PROGRAMMER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ProcessorProgrammerBlockEntity programmer) {
        if (!level.isClientSide()) {
            programmer.tryProgram();
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public int getProgress() {
        return progress;
    }

    private void tryProgram() {
        if (crafting) {
            return;
        }
        for (ManipulatorRecipeDisplay recipe : ProcessorProgrammingRecipes.all()) {
            if (hasIngredients(recipe) && canAcceptOutput(recipe.output())) {
                progress += SpeedUpgradeHelper.progressStep(inventory, UPGRADE_SLOT_START, UPGRADE_SLOT_COUNT, MAX_PROGRESS);
                if (progress < MAX_PROGRESS) {
                    setChangedAndSync();
                    return;
                }
                crafting = true;
                try {
                    consumeIngredients(recipe);
                    ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
                    if (output.isEmpty()) {
                        inventory.setStackInSlot(OUTPUT_SLOT, recipe.output().copy());
                    } else {
                        output.grow(recipe.output().getCount());
                        inventory.setStackInSlot(OUTPUT_SLOT, output);
                    }
                    progress = 0;
                    setChangedAndSync();
                } finally {
                    crafting = false;
                }
                return;
            }
        }
        if (progress != 0) {
            progress = 0;
            setChangedAndSync();
        }
    }

    private boolean hasIngredients(ManipulatorRecipeDisplay recipe) {
        for (ManipulatorIngredient ingredient : recipe.ingredients()) {
            int found = 0;
            for (int slot = 0; slot < INPUT_SLOT_COUNT; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (matches(stack, ingredient)) {
                    found += stack.getCount();
                }
            }
            if (found < ingredient.count()) {
                return false;
            }
        }
        return true;
    }

    private void consumeIngredients(ManipulatorRecipeDisplay recipe) {
        for (ManipulatorIngredient ingredient : recipe.ingredients()) {
            int remaining = ingredient.count();
            for (int slot = 0; slot < INPUT_SLOT_COUNT && remaining > 0; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (matches(stack, ingredient)) {
                    int removed = Math.min(remaining, stack.getCount());
                    inventory.extractItem(slot, removed, false);
                    remaining -= removed;
                }
            }
        }
    }

    private boolean canAcceptOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || ItemStack.isSameItemSameTags(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private static boolean matches(ItemStack stack, ManipulatorIngredient ingredient) {
        return !stack.isEmpty() && ingredient.acceptedStacks().stream()
                .anyMatch(accepted -> ItemStack.isSameItemSameTags(stack, accepted));
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
            MachineInventoryHelper.ensureSize(inventory, SLOT_COUNT + UPGRADE_SLOT_COUNT);
        }
        progress = tag.getInt("Progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.processor_programming_device");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ProcessorProgrammerMenu(containerId, playerInventory, this, worldPosition);
    }
}
