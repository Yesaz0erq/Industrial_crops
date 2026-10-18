package com.industrialcrops.screen;

import com.industrialcrops.block.entity.SuperCropCultivatorBlockEntity;
import com.industrialcrops.recipe.SuperCultivationRecipes;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public final class SuperCropCultivatorMenu extends AbstractContainerMenu {
    public static final int CROP_X = 14, CROP_Y = 28;
    public static final int MATERIAL_X = 14;
    /** Item y of material slots A, B and C. */
    public static final int[] MATERIAL_Y = {58, 82, 106};
    public static final int OUTPUT_X = 150, OUTPUT_Y = 58;
    public static final int BYPRODUCT_X = 150, BYPRODUCT_Y = 94;
    public static final int INVENTORY_Y = 142, HOTBAR_Y = 200;
    private static final int MACHINE_SLOT_COUNT = SuperCropCultivatorBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private final BlockPos pos;
    private int progress;
    private int maxProgress = SuperCultivationRecipes.DEFAULT_DURATION;
    private int status;
    private int harvests;

    public SuperCropCultivatorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, readBlockEntity(playerInventory, buffer));
    }

    private SuperCropCultivatorMenu(int containerId, Inventory playerInventory, SuperCropCultivatorBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.getBlockPos());
    }

    public SuperCropCultivatorMenu(int containerId, Inventory playerInventory, SuperCropCultivatorBlockEntity blockEntity, BlockPos pos) {
        super(ModMenus.SUPER_CROP_CULTIVATOR.get(), containerId);
        this.pos = pos;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), SuperCropCultivatorBlockEntity.CROP_SLOT, CROP_X, CROP_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SuperCultivationRecipes.isCultivationCrop(stack);
            }
        });
        for (int i = 0; i < MATERIAL_Y.length; i++) {
            addSlot(new SlotItemHandler(blockEntity.getInventory(), SuperCropCultivatorBlockEntity.MATERIAL_A_SLOT + i, MATERIAL_X, MATERIAL_Y[i]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return SuperCultivationRecipes.isMaterial(stack);
                }
            });
        }
        addSlot(new SlotItemHandler(blockEntity.getInventory(), SuperCropCultivatorBlockEntity.OUTPUT_SLOT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getInventory(), SuperCropCultivatorBlockEntity.BYPRODUCT_SLOT, BYPRODUCT_X, BYPRODUCT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, HOTBAR_Y));
        }

        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int value) { progress = value & 0xFFFF; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getMaxProgress(); }
            @Override public void set(int value) { maxProgress = Math.max(1, value & 0xFFFF); }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getStatus(); }
            @Override public void set(int value) { status = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getHarvests(); }
            @Override public void set(int value) { harvests = value & 0x7FFF; }
        });
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public float getProgressFraction() {
        return Math.max(0.0F, Math.min(1.0F, progress / (float) Math.max(1, maxProgress)));
    }

    public int getStatus() {
        return status;
    }

    public int getHarvests() {
        return harvests;
    }

    /** Seconds left in the current cultivation. */
    public int getSecondsLeft() {
        return Math.max(0, (maxProgress - progress + 19) / 20);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END,
                    slotIndex >= SuperCropCultivatorBlockEntity.OUTPUT_SLOT)) {
                return ItemStack.EMPTY;
            }
            if (slotIndex == SuperCropCultivatorBlockEntity.OUTPUT_SLOT) slot.onQuickCraft(stack, copy);
        } else if (SuperCultivationRecipes.isCultivationCrop(stack)) {
            if (!moveItemStackTo(stack, SuperCropCultivatorBlockEntity.CROP_SLOT, SuperCropCultivatorBlockEntity.CROP_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (SuperCultivationRecipes.isMaterial(stack)) {
            if (!moveMaterial(stack)) return ItemStack.EMPTY;
        } else if (slotIndex < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) return ItemStack.EMPTY;
        } else if (slotIndex < PLAYER_HOTBAR_END) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    /** Shift-click materials into the slot that already holds the same item, else the first empty one. */
    private boolean moveMaterial(ItemStack stack) {
        int first = SuperCropCultivatorBlockEntity.MATERIAL_A_SLOT;
        int last = SuperCropCultivatorBlockEntity.MATERIAL_C_SLOT;
        for (int slot = first; slot <= last; slot++) {
            ItemStack in = slots.get(slot).getItem();
            if (!in.isEmpty() && ItemStack.isSameItemSameTags(in, stack)) return moveItemStackTo(stack, slot, slot + 1, false);
        }
        for (int slot = first; slot <= last; slot++) {
            if (slots.get(slot).getItem().isEmpty()) return moveItemStackTo(stack, slot, slot + 1, false);
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.SUPER_CROP_CULTIVATOR.get());
    }

    private static SuperCropCultivatorBlockEntity readBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SuperCropCultivatorBlockEntity cultivator) return cultivator;
        throw new IllegalStateException("Missing super crop cultivator block entity at " + pos);
    }
}
