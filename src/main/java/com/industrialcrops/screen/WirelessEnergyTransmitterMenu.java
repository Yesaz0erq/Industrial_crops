package com.industrialcrops.screen;

import com.industrialcrops.block.entity.WirelessEnergyTransmitterBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class WirelessEnergyTransmitterMenu extends AbstractContainerMenu {
    private final WirelessEnergyTransmitterBlockEntity machine;
    private final BlockPos pos;
    private final ContainerData data;

    public WirelessEnergyTransmitterMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), null, new SimpleContainerData(WirelessEnergyTransmitterBlockEntity.DATA_COUNT));
    }
    public WirelessEnergyTransmitterMenu(int id, Inventory inventory, WirelessEnergyTransmitterBlockEntity machine) {
        this(id, inventory, machine.getBlockPos(), machine, machine.getData());
    }
    private WirelessEnergyTransmitterMenu(int id, Inventory inventory, BlockPos pos, WirelessEnergyTransmitterBlockEntity machine, ContainerData data) {
        super(ModMenus.WIRELESS_ENERGY_TRANSMITTER.get(), id);
        this.pos = pos; this.machine = machine; this.data = data;
        addDataSlots(data);
    }
    public int energy() { return (data.get(0) & 0xffff) | ((data.get(1) & 0xffff) << 16); }
    public int radius() { return data.get(2); }
    public int targets() { return (data.get(3) & 0xffff) | ((data.get(4) & 0xffff) << 16); }
    public int sent() { return (data.get(5) & 0xffff) | ((data.get(6) & 0xffff) << 16); }
    public boolean active() { return data.get(7) != 0; }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (machine == null || player.level().isClientSide() || !stillValid(player) || button < 0 || button > 1) return false;
        machine.setRangeRadius(button);
        broadcastChanges();
        return true;
    }
    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.WIRELESS_ENERGY_TRANSMITTER.get());
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
