package com.industrialcrops.screen;

import com.industrialcrops.block.entity.GoldPlasmaExtractorBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public final class GoldPlasmaExtractorMenu extends AbstractContainerMenu {
    private final GoldPlasmaExtractorBlockEntity extractor;
    private final BlockPos pos;
    private final ContainerData data;

    public GoldPlasmaExtractorMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, read(inventory, buffer));
    }
    private GoldPlasmaExtractorMenu(int id, Inventory inventory, GoldPlasmaExtractorBlockEntity extractor) {
        this(id, inventory, extractor, extractor.getBlockPos());
    }
    public GoldPlasmaExtractorMenu(int id, Inventory inventory, GoldPlasmaExtractorBlockEntity extractor, BlockPos pos) {
        super(ModMenus.GOLD_PLASMA_EXTRACTOR.get(), id);
        this.extractor = extractor;
        this.pos = pos;
        this.data = extractor.getData();
        addSlot(new SlotItemHandler(extractor.getInventory(), 0, 53, 35));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    private static GoldPlasmaExtractorBlockEntity read(Inventory inventory, FriendlyByteBuf buffer) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buffer.readBlockPos());
        if (!(entity instanceof GoldPlasmaExtractorBlockEntity extractor)) throw new IllegalStateException("Missing gold plasma extractor");
        return extractor;
    }
    public int energy() { return (data.get(0) & 0xFFFF) | ((data.get(1) & 0xFFFF) << 16); }
    public int progress() { return data.get(2); }
    public int maxProgress() { return data.get(3); }
    public int fluidAmount() { return data.get(4); }
    public int fluidCapacity() { return data.get(5); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return stack.getCount() == copy.getCount() ? ItemStack.EMPTY : copy;
    }
    @Override public boolean stillValid(Player player) { return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.GOLD_PLASMA_EXTRACTOR.get()); }
}
