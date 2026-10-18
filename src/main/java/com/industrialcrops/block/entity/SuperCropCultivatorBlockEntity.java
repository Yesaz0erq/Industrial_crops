package com.industrialcrops.block.entity;

import com.industrialcrops.block.SuperCropCultivatorBlock;
import com.industrialcrops.recipe.SuperCultivationRecipes;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.SuperCropCultivatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Super Crop Cultivator: a Super-quality industrial crop and two materials grow a new plant variety
 * over one minute (see {@link SuperCultivationRecipes}).
 */
public final class SuperCropCultivatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CROP_SLOT = 0;
    public static final int MATERIAL_A_SLOT = 1;
    public static final int MATERIAL_B_SLOT = 2;
    public static final int MATERIAL_C_SLOT = 3;
    public static final int OUTPUT_SLOT = 4;
    /** Returned containers, e.g. the bottle left over from Dragon's Breath. */
    public static final int BYPRODUCT_SLOT = 5;
    public static final int SLOT_COUNT = 6;

    public static boolean isMaterialSlot(int slot) {
        return slot >= MATERIAL_A_SLOT && slot <= MATERIAL_C_SLOT;
    }

    /** Status codes shown by the screen. */
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_NOT_SUPER = 1;
    public static final int STATUS_MISSING_MATERIALS = 2;
    public static final int STATUS_OUTPUT_FULL = 3;
    public static final int STATUS_GROWING = 4;

    private int progress;
    private int maxProgress = SuperCultivationRecipes.DEFAULT_DURATION;
    private int status;
    /** Counts finished harvests so the screen can play its harvest flash. */
    private int harvests;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case CROP_SLOT -> SuperCultivationRecipes.isCultivationCrop(stack);
                case MATERIAL_A_SLOT, MATERIAL_B_SLOT, MATERIAL_C_SLOT -> SuperCultivationRecipes.isMaterial(stack);
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** Automation: crops and materials go in, only the harvest comes out. */
    private final IItemHandler automationHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == OUTPUT_SLOT || slot == BYPRODUCT_SLOT || !isItemValid(slot, stack)) return stack;
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == OUTPUT_SLOT || slot == BYPRODUCT_SLOT ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == CROP_SLOT) {
                return SuperCultivationRecipes.isCultivationCrop(stack) && SuperCultivationRecipes.isSuperQuality(stack);
            }
            if (isMaterialSlot(slot)) {
                // Each material item lives in one slot only, so pipes cannot block a recipe.
                if (!SuperCultivationRecipes.isMaterial(stack)) return false;
                for (int other = MATERIAL_A_SLOT; other <= MATERIAL_C_SLOT; other++) {
                    if (other == slot) continue;
                    ItemStack inOther = inventory.getStackInSlot(other);
                    if (!inOther.isEmpty() && ItemStack.isSameItemSameComponents(inOther, stack)) return false;
                }
                return true;
            }
            return false;
        }
    };

    public SuperCropCultivatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SUPER_CROP_CULTIVATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SuperCropCultivatorBlockEntity blockEntity) {
        if (!level.isClientSide()) {
            blockEntity.serverTick((ServerLevel) level, pos, state);
        }
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        ItemStack crop = inventory.getStackInSlot(CROP_SLOT);
        SuperCultivationRecipes.Recipe recipe = SuperCultivationRecipes.find(crop, materialStacks());
        int newStatus = statusFor(crop, recipe);

        if (newStatus != STATUS_GROWING) {
            if (progress != 0 || status != newStatus) {
                progress = 0;
                status = newStatus;
                setChanged();
            }
            setActive(level, pos, state, false);
            return;
        }

        status = STATUS_GROWING;
        maxProgress = Math.max(1, recipe.duration());
        progress++;
        setActive(level, pos, state, true);
        if (progress % 40 == 0) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35F,
                    0.8F + 0.6F * progress / maxProgress);
        }
        if (progress >= maxProgress) {
            harvest(level, pos, recipe);
        }
        setChanged();
    }

    private int statusFor(ItemStack crop, @Nullable SuperCultivationRecipes.Recipe recipe) {
        if (crop.isEmpty()) return STATUS_IDLE;
        if (!SuperCultivationRecipes.isSuperQuality(crop)) return STATUS_NOT_SUPER;
        if (recipe == null) return STATUS_MISSING_MATERIALS;
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        ItemStack result = recipe.resultStack();
        boolean fits = output.isEmpty()
                || ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize();
        if (fits) {
            int needed = 0;
            ItemStack byproduct = inventory.getStackInSlot(BYPRODUCT_SLOT);
            for (ItemStack remainder : recipe.remainders()) {
                if (!byproduct.isEmpty() && !ItemStack.isSameItemSameComponents(byproduct, remainder)) {
                    fits = false;
                }
                needed += remainder.getCount();
            }
            if (fits && needed > 0) {
                int room = byproduct.isEmpty() ? 64 : byproduct.getMaxStackSize() - byproduct.getCount();
                fits = room >= needed;
            }
        }
        return fits ? STATUS_GROWING : STATUS_OUTPUT_FULL;
    }

    private void harvest(ServerLevel level, BlockPos pos, SuperCultivationRecipes.Recipe recipe) {
        inventory.extractItem(CROP_SLOT, 1, false);
        for (int slot = MATERIAL_A_SLOT; slot <= MATERIAL_C_SLOT; slot++) {
            ItemStack used = inventory.getStackInSlot(slot);
            if (used.isEmpty()) continue;
            // Keep containers such as the Dragon's Breath bottle.
            ItemStack remainder = used.getCraftingRemainingItem().copy();
            inventory.extractItem(slot, 1, false);
            if (!remainder.isEmpty()) {
                ItemStack byproduct = inventory.getStackInSlot(BYPRODUCT_SLOT);
                if (byproduct.isEmpty()) {
                    inventory.setStackInSlot(BYPRODUCT_SLOT, remainder);
                } else {
                    byproduct.grow(remainder.getCount());
                    inventory.setStackInSlot(BYPRODUCT_SLOT, byproduct);
                }
            }
        }
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, recipe.resultStack());
        } else {
            output.grow(1);
            inventory.setStackInSlot(OUTPUT_SLOT, output);
        }
        progress = 0;
        harvests = (harvests + 1) & 0x7FFF;
        level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 0.9F, 1.2F);
        level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.4F, 1.6F);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
                24, 0.3D, 0.25D, 0.3D, 0.04D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                10, 0.35D, 0.2D, 0.35D, 0.0D);
    }

    private void setActive(Level level, BlockPos pos, BlockState state, boolean active) {
        if (state.hasProperty(SuperCropCultivatorBlock.ACTIVE) && state.getValue(SuperCropCultivatorBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(SuperCropCultivatorBlock.ACTIVE, active), 3);
        }
    }

    private java.util.List<ItemStack> materialStacks() {
        return java.util.List.of(inventory.getStackInSlot(MATERIAL_A_SLOT), inventory.getStackInSlot(MATERIAL_B_SLOT),
                inventory.getStackInSlot(MATERIAL_C_SLOT));
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public IItemHandler getAutomationHandler() {
        return automationHandler;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getStatus() {
        return status;
    }

    public int getHarvests() {
        return harvests;
    }

    public java.util.List<ItemStack> collectDrops() {
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) drops.add(stack.copy());
        }
        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putInt("Harvests", harvests);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
            com.industrialcrops.machine.MachineInventoryHelper.ensureSize(inventory, SLOT_COUNT);
        }
        progress = Math.max(0, tag.getInt("Progress"));
        maxProgress = tag.contains("MaxProgress") ? Math.max(1, tag.getInt("MaxProgress")) : SuperCultivationRecipes.DEFAULT_DURATION;
        harvests = tag.getInt("Harvests");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.super_crop_cultivator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SuperCropCultivatorMenu(containerId, playerInventory, this, worldPosition);
    }
}
