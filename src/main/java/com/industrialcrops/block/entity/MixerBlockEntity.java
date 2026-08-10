package com.industrialcrops.block.entity;

import com.industrialcrops.block.MixerBlock;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.recipe.MixerRecipeDisplay;
import com.industrialcrops.recipe.MixerRecipes;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MixerBlockEntity extends BlockEntity {
    public static final int INGREDIENT_SLOT_START = 0;
    public static final int INGREDIENT_SLOT_COUNT = 7;
    public static final int BAG_SLOT = INGREDIENT_SLOT_START + INGREDIENT_SLOT_COUNT;
    public static final int OUTPUT_SLOT = BAG_SLOT + 1;
    public static final int TOTAL_SLOTS = OUTPUT_SLOT + 1;
    public static final int MAX_PROGRESS = 120;

    private int activeRecipe = -1;
    private int progress;
    private final List<ItemStack> legacyOverflow = new ArrayList<>();

    private final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (activeRecipe >= 0 || !getStackInSlot(OUTPUT_SLOT).isEmpty()) {
                return false;
            }
            if (slot >= INGREDIENT_SLOT_START && slot < INGREDIENT_SLOT_START + INGREDIENT_SLOT_COUNT) {
                return MixerRecipes.isIngredient(stack);
            }
            return slot == BAG_SLOT && isEmptyBag(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MixerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        blockEntity.dropLegacyOverflow(level, pos);
        blockEntity.tryStartStoredBag();
        boolean active = blockEntity.activeRecipe >= 0;
        if (state.hasProperty(MixerBlock.LIT) && state.getValue(MixerBlock.LIT) != active) {
            level.setBlock(pos, state.setValue(MixerBlock.LIT, active), Block.UPDATE_CLIENTS);
        }
        blockEntity.continueMixing(level, pos);
    }

    public static boolean isAcceptedInput(ItemStack stack) {
        return !stack.isEmpty() && (MixerRecipes.isIngredient(stack) || isEmptyBag(stack));
    }

    public static boolean isEmptyBag(ItemStack stack) {
        return stack.is(ModItems.EMPTY_BAG.get());
    }

    public ItemStack insertIngredient(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !MixerRecipes.isIngredient(stack) || activeRecipe >= 0
                || !inventory.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return stack;
        }
        ItemStack remainder = stack.copy();
        for (int slot = INGREDIENT_SLOT_START;
             slot < INGREDIENT_SLOT_START + INGREDIENT_SLOT_COUNT && !remainder.isEmpty();
             slot++) {
            remainder = inventory.insertItem(slot, remainder, simulate);
        }
        return remainder;
    }

    public boolean startMixingWithBag(boolean simulate) {
        if (activeRecipe >= 0 || !inventory.getStackInSlot(BAG_SLOT).isEmpty()
                || !inventory.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return false;
        }
        int recipeIndex = findBestMatchingRecipe();
        if (recipeIndex < 0) {
            return false;
        }
        if (!simulate) {
            inventory.setStackInSlot(BAG_SLOT, new ItemStack(ModItems.EMPTY_BAG.get()));
            activeRecipe = recipeIndex;
            progress = 0;
            setChanged();
        }
        return true;
    }

    public boolean giveAllContentsTo(Player player) {
        boolean movedAny = false;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.extractItem(slot, Integer.MAX_VALUE, false);
            if (stack.isEmpty()) {
                continue;
            }
            movedAny = true;
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        if (activeRecipe >= 0 || progress != 0) {
            movedAny = true;
        }
        activeRecipe = -1;
        progress = 0;
        setChanged();
        return movedAny;
    }

    public int getSlots() {
        return inventory.getSlots();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    private void continueMixing(Level level, BlockPos pos) {
        if (activeRecipe < 0) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }
        List<MixerRecipeDisplay> recipes = MixerRecipes.all();
        if (activeRecipe >= recipes.size()) {
            cancelMixing();
            return;
        }
        MixerRecipeDisplay recipe = recipes.get(activeRecipe);
        if (!isEmptyBag(inventory.getStackInSlot(BAG_SLOT)) || !hasIngredients(recipe)
                || !canOutputAccept(inventory.getStackInSlot(OUTPUT_SLOT), recipe.output())) {
            cancelMixing();
            return;
        }
        progress++;
        if (progress == 1 || progress % 20 == 0) {
            level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.25F, 0.8F);
        }
        if (progress < MAX_PROGRESS) {
            setChanged();
            return;
        }
        consumeIngredients(recipe);
        inventory.extractItem(BAG_SLOT, 1, false);
        inventory.setStackInSlot(OUTPUT_SLOT, recipe.output().copy());
        activeRecipe = -1;
        progress = 0;
        setChanged();
    }

    private void tryStartStoredBag() {
        if (activeRecipe >= 0 || !inventory.getStackInSlot(OUTPUT_SLOT).isEmpty()
                || !isEmptyBag(inventory.getStackInSlot(BAG_SLOT))) {
            return;
        }
        int recipeIndex = findBestMatchingRecipe();
        if (recipeIndex >= 0) {
            activeRecipe = recipeIndex;
            progress = 0;
            setChanged();
        }
    }

    private int findBestMatchingRecipe() {
        List<MixerRecipeDisplay> recipes = MixerRecipes.all();
        int bestIndex = -1;
        int bestIngredientTypes = -1;
        int bestTotalCount = -1;
        for (int index = 0; index < recipes.size(); index++) {
            MixerRecipeDisplay recipe = recipes.get(index);
            if (!hasIngredients(recipe)) {
                continue;
            }
            int ingredientTypes = recipe.inputs().size();
            int totalCount = recipe.inputs().stream().mapToInt(ItemStack::getCount).sum();
            if (ingredientTypes > bestIngredientTypes
                    || ingredientTypes == bestIngredientTypes && totalCount > bestTotalCount) {
                bestIndex = index;
                bestIngredientTypes = ingredientTypes;
                bestTotalCount = totalCount;
            }
        }
        return bestIndex;
    }

    private boolean hasIngredients(MixerRecipeDisplay recipe) {
        for (ItemStack required : recipe.inputs()) {
            int available = 0;
            for (int slot = INGREDIENT_SLOT_START;
                 slot < INGREDIENT_SLOT_START + INGREDIENT_SLOT_COUNT;
                 slot++) {
                ItemStack stored = inventory.getStackInSlot(slot);
                if (stored.is(required.getItem())) {
                    available += stored.getCount();
                }
            }
            if (available < required.getCount()) {
                return false;
            }
        }
        return true;
    }

    private void consumeIngredients(MixerRecipeDisplay recipe) {
        for (ItemStack required : recipe.inputs()) {
            int remaining = required.getCount();
            for (int slot = INGREDIENT_SLOT_START;
                 slot < INGREDIENT_SLOT_START + INGREDIENT_SLOT_COUNT && remaining > 0;
                 slot++) {
                ItemStack stored = inventory.getStackInSlot(slot);
                if (!stored.is(required.getItem())) {
                    continue;
                }
                int extracted = Math.min(remaining, stored.getCount());
                inventory.extractItem(slot, extracted, false);
                remaining -= extracted;
            }
        }
    }

    private void cancelMixing() {
        activeRecipe = -1;
        progress = 0;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("ActiveRecipe", activeRecipe);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            CompoundTag inventoryTag = tag.getCompound("Inventory");
            int savedSize = inventoryTag.getInt("Size");
            inventory.deserializeNBT(registries, inventoryTag);
            if (savedSize != TOTAL_SLOTS) {
                migrateLegacyInventory(savedSize);
            } else {
                MachineInventoryHelper.ensureSize(inventory, TOTAL_SLOTS);
            }
        }
        activeRecipe = tag.contains("ActiveRecipe") ? tag.getInt("ActiveRecipe") : -1;
        progress = tag.getInt("Progress");
        if (activeRecipe < -1 || activeRecipe >= MixerRecipes.all().size()) {
            activeRecipe = -1;
            progress = 0;
        }
    }

    private void migrateLegacyInventory(int savedSize) {
        ItemStack[] saved = new ItemStack[Math.max(0, savedSize)];
        for (int slot = 0; slot < saved.length && slot < inventory.getSlots(); slot++) {
            saved[slot] = inventory.getStackInSlot(slot).copy();
        }
        MachineInventoryHelper.ensureSize(inventory, TOTAL_SLOTS);
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }

        if (saved.length == 11) {
            for (int slot = 0; slot < 9; slot++) {
                preserveLegacyIngredient(saved[slot]);
            }
            inventory.setStackInSlot(BAG_SLOT, saved[9]);
            inventory.setStackInSlot(OUTPUT_SLOT, saved[10]);
        } else if (saved.length == 7) {
            preserveLegacyIngredient(saved[0]);
            inventory.setStackInSlot(BAG_SLOT, saved[1]);
            inventory.setStackInSlot(OUTPUT_SLOT, saved[2]);
            preserveLegacyExtras(saved, 3);
        } else if (saved.length == 6) {
            preserveLegacyIngredient(saved[0]);
            inventory.setStackInSlot(OUTPUT_SLOT, saved[1]);
            preserveLegacyExtras(saved, 2);
        } else {
            preserveLegacyExtras(saved, 0);
        }
    }

    private void preserveLegacyExtras(ItemStack[] saved, int firstExtra) {
        for (int source = firstExtra; source < saved.length; source++) {
            preserveLegacyIngredient(saved[source]);
        }
    }

    private void preserveLegacyIngredient(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemStack remainder = stack.copy();
        for (int slot = INGREDIENT_SLOT_START;
             slot < INGREDIENT_SLOT_START + INGREDIENT_SLOT_COUNT && !remainder.isEmpty();
             slot++) {
            ItemStack stored = inventory.getStackInSlot(slot);
            if (stored.isEmpty()) {
                inventory.setStackInSlot(slot, remainder);
                return;
            }
            if (ItemStack.isSameItemSameComponents(stored, remainder)) {
                int moved = Math.min(remainder.getCount(), stored.getMaxStackSize() - stored.getCount());
                if (moved > 0) {
                    stored.grow(moved);
                    inventory.setStackInSlot(slot, stored);
                    remainder.shrink(moved);
                }
            }
        }
        if (!remainder.isEmpty()) {
            legacyOverflow.add(remainder);
        }
    }

    private void dropLegacyOverflow(Level level, BlockPos pos) {
        if (legacyOverflow.isEmpty()) {
            return;
        }
        for (ItemStack stack : legacyOverflow) {
            Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), stack);
        }
        legacyOverflow.clear();
    }

    private static boolean canOutputAccept(ItemStack outputStack, ItemStack resultStack) {
        if (resultStack.isEmpty()) {
            return false;
        }
        if (outputStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(outputStack, resultStack)) {
            return false;
        }
        return outputStack.getCount() + resultStack.getCount() <= outputStack.getMaxStackSize();
    }
}
