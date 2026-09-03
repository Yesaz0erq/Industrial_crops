package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ReinforcedControlDeviceBlockEntity extends AdvancedIndustrialStorageBlockEntity {
    public ReinforcedControlDeviceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REINFORCED_CONTROL_DEVICE.get(), pos, state);
    }

    /**
     * Reinforced controllers use connected reinforced storage components as
     * their page-backed storage. Keep the newer internal-cell fallback for
     * controllers that have no connected component blocks.
     */
    private List<ReinforcedIndustrialStorageArrayBlockEntity> connectedComponents() {
        return level == null ? List.of()
                : ReinforcedIndustrialStorageArrayBlockEntity.findAllAttached(level, worldPosition);
    }

    private ItemStack connectedStack(int slot) {
        List<ReinforcedIndustrialStorageArrayBlockEntity> components = connectedComponents();
        if (components.isEmpty()) return null;
        if (slot < 0 || slot >= components.size() * ReinforcedIndustrialStorageArrayBlockEntity.SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        int componentIndex = slot / ReinforcedIndustrialStorageArrayBlockEntity.SLOT_COUNT;
        int componentSlot = slot % ReinforcedIndustrialStorageArrayBlockEntity.SLOT_COUNT;
        return components.get(componentIndex).getStackInSlot(componentSlot);
    }

    @Override
    public int getUnlockedStorageSlots() {
        List<ReinforcedIndustrialStorageArrayBlockEntity> components = connectedComponents();
        return components.isEmpty() ? super.getUnlockedStorageSlots()
                : components.size() * ReinforcedIndustrialStorageArrayBlockEntity.SLOT_COUNT;
    }

    @Override
    public ItemStack getStorageStack(int slot) {
        ItemStack connected = connectedStack(slot);
        return connected == null ? super.getStorageStack(slot) : connected;
    }

    @Override
    public ItemStack extractFromStorageSlot(int slot, int amount) {
        List<ReinforcedIndustrialStorageArrayBlockEntity> components = connectedComponents();
        if (components.isEmpty()) return super.extractFromStorageSlot(slot, amount);
        ItemStack current = connectedStack(slot);
        if (current == null || current.isEmpty()) return ItemStack.EMPTY;
        int componentIndex = slot / ReinforcedIndustrialStorageArrayBlockEntity.SLOT_COUNT;
        int componentSlot = slot % ReinforcedIndustrialStorageArrayBlockEntity.SLOT_COUNT;
        return components.get(componentIndex).extractFromSlot(componentSlot, amount);
    }

    @Override
    public int insertIntoStorageSlot(int slot, ItemStack stack) {
        List<ReinforcedIndustrialStorageArrayBlockEntity> components = connectedComponents();
        if (components.isEmpty()) return super.insertIntoStorageSlot(slot, stack);
        if (slot < 0 || slot >= getUnlockedStorageSlots()) return 0;
        int componentIndex = slot / ReinforcedIndustrialStorageArrayBlockEntity.SLOT_COUNT;
        int componentSlot = slot % ReinforcedIndustrialStorageArrayBlockEntity.SLOT_COUNT;
        return components.get(componentIndex).insertIntoSlot(componentSlot, stack);
    }

    @Override
    public int insertIntoStorage(ItemStack stack) {
        List<ReinforcedIndustrialStorageArrayBlockEntity> components = connectedComponents();
        if (components.isEmpty()) return super.insertIntoStorage(stack);
        int inserted = 0;
        for (ReinforcedIndustrialStorageArrayBlockEntity component : components) {
            if (stack.isEmpty()) break;
            int amount = component.insertStack(stack);
            if (amount > 0) {
                stack.shrink(amount);
                inserted += amount;
            }
        }
        return inserted;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.industrialcrops.screen.AdvancedIndustrialStorageMenu(
                containerId, inventory, this, worldPosition,
                com.industrialcrops.screen.AdvancedIndustrialStorageMenu.MAX_ROWS);
    }
}
