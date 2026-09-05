package com.industrialcrops.screen;

import com.industrialcrops.block.entity.BasicCropStorageArrayBlockEntity;
import com.industrialcrops.recipe.ManipulatorIngredient;
import com.industrialcrops.recipe.ManipulatorRecipeDisplay;
import com.industrialcrops.recipe.ManipulatorRecipes;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BasicManipulatorMenu extends AbstractContainerMenu {
    private final BlockPos manipulatorPos;
    private final @Nullable BasicCropStorageArrayBlockEntity drive;
    private final List<ManipulatorRecipeDisplay> recipes;
    private final boolean advanced;

    public BasicManipulatorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, buffer.readBlockPos());
    }

    public BasicManipulatorMenu(int containerId, Inventory playerInventory, BlockPos manipulatorPos) {
        super(ModMenus.BASIC_MANIPULATOR.get(), containerId);
        this.manipulatorPos = manipulatorPos;
        this.advanced = playerInventory.player.level().getBlockState(manipulatorPos).is(ModBlocks.ADVANCED_MANIPULATOR.get());
        this.recipes = ManipulatorRecipes.forAdvanced(advanced);
        this.drive = playerInventory.player.level().isClientSide()
                ? null
                : BasicCropStorageArrayBlockEntity.findAttached(playerInventory.player.level(), manipulatorPos);
        addPlayerInventory(playerInventory, 70, 158);
        addPlayerHotbar(playerInventory, 70, 216);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= recipes.size()) {
            return false;
        }
        return craftRecipe(player, recipes.get(id));
    }

    public List<ManipulatorRecipeDisplay> recipes() {
        return recipes;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), manipulatorPos);
        return stillValid(access, player, ModBlocks.BASIC_MANIPULATOR.get())
                || stillValid(access, player, ModBlocks.ADVANCED_MANIPULATOR.get());
    }

    private boolean craftRecipe(Player player, ManipulatorRecipeDisplay recipe) {
        Inventory inventory = player.getInventory();
        for (ManipulatorIngredient ingredient : recipe.ingredients()) {
            if (countAcceptedItems(inventory, ingredient) < ingredient.count()) {
                return false;
            }
        }

        for (ManipulatorIngredient ingredient : recipe.ingredients()) {
            consumeAcceptedItems(inventory, ingredient, ingredient.count());
        }

        ItemStack result = recipe.output().copy();
        if (!inventory.add(result)) {
            player.drop(result, false);
        }

        inventory.setChanged();
        broadcastChanges();
        return true;
    }

    private int countAcceptedItems(Inventory inventory, ManipulatorIngredient ingredient) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (matchesAccepted(stack, ingredient.acceptedStacks())) {
                count += stack.getCount();
            }
        }

        if (ingredient.isExactItem() && drive != null) {
            count += drive.getStoredCount(ingredient.acceptedStacks().get(0).getItem());
        }
        return count;
    }

    private void consumeAcceptedItems(Inventory inventory, ManipulatorIngredient ingredient, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!matchesAccepted(stack, ingredient.acceptedStacks())) {
                continue;
            }

            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }

        if (remaining > 0 && ingredient.isExactItem() && drive != null) {
            drive.remove(ingredient.acceptedStacks().get(0).getItem(), remaining);
        }
    }

    private boolean matchesAccepted(ItemStack stack, java.util.List<ItemStack> acceptedStacks) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(ManipulatorRecipes.STRIPPED_LOGS)) {
            for (ItemStack acceptedStack : acceptedStacks) {
                if (acceptedStack.is(ManipulatorRecipes.STRIPPED_LOGS)) {
                    return true;
                }
            }
        }
        for (ItemStack acceptedStack : acceptedStacks) {
            if (ItemStack.isSameItemSameTags(stack, acceptedStack)) {
                return true;
            }
        }
        return false;
    }

    private void addPlayerInventory(Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory, int x, int y) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * 18, y));
        }
    }
}
