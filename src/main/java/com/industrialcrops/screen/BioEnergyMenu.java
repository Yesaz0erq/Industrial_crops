package com.industrialcrops.screen;

import com.industrialcrops.block.entity.BioEnergyMachineBlockEntity;
import com.industrialcrops.block.BioEnergyMachineBlock;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class BioEnergyMenu extends AbstractContainerMenu implements UpgradeableMenu {
    public static final int UPGRADE_X=-68,UPGRADE_Y=28,UPGRADE_SPACING=22;
    public static final int BUTTON_SIDE_BASE = 10;
    public static final int BUTTON_ALL_SIDES_ON = 20;
    public static final int BUTTON_ALL_SIDES_OFF = 21;
    public static final int RELATIVE_SIDE_COUNT = 6;
    private final BioEnergyMachineBlockEntity blockEntity;
    private final BlockPos pos;
    private final ContainerData data;
    private final int machineSlots;
    private final int playerInventoryStart;
    private boolean upgradeSlotsVisible;

    public BioEnergyMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, read(inventory, buffer));
    }

    private BioEnergyMenu(int id, Inventory inventory, BioEnergyMachineBlockEntity entity) {
        this(id, inventory, entity, entity.getBlockPos());
    }

    public BioEnergyMenu(int id, Inventory inventory, BioEnergyMachineBlockEntity entity, BlockPos pos) {
        super(ModMenus.BIO_ENERGY_MACHINE.get(), id);
        blockEntity = entity;
        this.pos = pos;
        data = entity.getData();
        machineSlots = entity.getKind() == BioEnergyMachineBlockEntity.Kind.BATTERY ? 0
                : entity.getKind() == BioEnergyMachineBlockEntity.Kind.GENERATOR ? 5 : 1;
        if (machineSlots > 0) {
            addSlot(new SlotItemHandler(entity.getInventory(), 0, 18, 35) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return entity.getInventory().isItemValid(0, stack);
                }
            });
        }
        if (entity.getKind() == BioEnergyMachineBlockEntity.Kind.GENERATOR) for(int i=0;i<4;i++){
            int x=UPGRADE_X+i%2*22,y=UPGRADE_Y+i/2*22;addSlot(new SlotItemHandler(entity.getInventory(),1+i,x,y){
                @Override public boolean mayPlace(ItemStack stack){return SpeedUpgradeHelper.isSpeedUpgrade(stack);}
                @Override public boolean isActive(){return upgradeSlotsVisible;}
            });
        }
        playerInventoryStart = machineSlots;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        addDataSlots(data);
    }

    public BioEnergyMachineBlockEntity.Kind kind() {
        int ordinal = Math.max(0, Math.min(BioEnergyMachineBlockEntity.Kind.values().length - 1, data.get(10)));
        return BioEnergyMachineBlockEntity.Kind.values()[ordinal];
    }
    public int energy() { return (data.get(0) & 0xFFFF) | ((data.get(1) & 0xFFFF) << 16); }
    public int energyCapacity() { return (data.get(2) & 0xFFFF) | ((data.get(3) & 0xFFFF) << 16); }
    public int progress() { return data.get(4); }
    public int maxProgress() { return data.get(5); }
    public int residue() { return data.get(6); }
    public int residueCapacity() { return data.get(7); }
    public int burnTime() { return data.get(8); }
    public int burnTimeTotal() { return data.get(9); }
    public int currentYield() { return data.get(11); }
    public boolean isEnergySideEnabled(Direction direction) { return (data.get(12) & (1 << direction.ordinal())) != 0; }
    public boolean isRelativeEnergySideEnabled(int relativeSide) { return isEnergySideEnabled(toWorldDirection(relativeSide)); }
    public BioEnergyMachineBlockEntity.EnergySideMode relativeEnergySideMode(int relativeSide) {
        Direction direction = toWorldDirection(relativeSide);
        int bit = 1 << direction.ordinal();
        if ((data.get(12) & bit) != 0) return BioEnergyMachineBlockEntity.EnergySideMode.OUTPUT;
        if ((data.get(13) & bit) != 0) return BioEnergyMachineBlockEntity.EnergySideMode.INPUT;
        return BioEnergyMachineBlockEntity.EnergySideMode.NONE;
    }
    public Direction worldDirectionForRelative(int relativeSide) { return toWorldDirection(relativeSide); }
    public int scaledProgress(int width) {
        return maxProgress() <= 0 ? 0 : Math.max(0, Math.min(width, progress() * width / maxProgress()));
    }
    public void setUpgradeSlotsVisible(boolean visible){upgradeSlotsVisible=visible;}

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int inventoryEnd = playerInventoryStart + 27;
        int hotbarEnd = inventoryEnd + 9;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, playerInventoryStart, hotbarEnd, true)) return ItemStack.EMPTY;
        } else if (blockEntity.getKind()==BioEnergyMachineBlockEntity.Kind.GENERATOR && SpeedUpgradeHelper.isSpeedUpgrade(stack)) {
            if(!moveItemStackTo(stack,1,5,false))return ItemStack.EMPTY;
        } else if (machineSlots > 0 && blockEntity.getInventory().isItemValid(0, stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (index < inventoryEnd) {
            if (!moveItemStackTo(stack, inventoryEnd, hotbarEnd, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, playerInventoryStart, inventoryEnd, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override public boolean stillValid(Player player) {
        Block expected = switch (blockEntity.getKind()) {
            case GENERATOR -> ModBlocks.BIO_ENERGY_GENERATOR.get();
            case BATTERY -> ModBlocks.ENERGY_BATTERY.get();
            case INCINERATOR -> ModBlocks.RESIDUE_INCINERATOR.get();
        };
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(player.level(), pos), player, expected);
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (blockEntity.getKind() != BioEnergyMachineBlockEntity.Kind.BATTERY) return false;
        if (id >= BUTTON_SIDE_BASE && id < BUTTON_SIDE_BASE + RELATIVE_SIDE_COUNT) {
            blockEntity.cycleEnergySide(toWorldDirection(id - BUTTON_SIDE_BASE));
            broadcastChanges();
            return true;
        }
        if (id == BUTTON_ALL_SIDES_ON || id == BUTTON_ALL_SIDES_OFF) {
            blockEntity.setAllEnergyOutputs(id == BUTTON_ALL_SIDES_ON);
            broadcastChanges();
            return true;
        }
        return false;
    }

    private Direction toWorldDirection(int relativeSide) {
        Direction front = blockEntity.getBlockState().getValue(BioEnergyMachineBlock.FACING);
        return switch (relativeSide) {
            case 0 -> Direction.UP;
            case 1 -> Direction.DOWN;
            case 2 -> front;
            case 3 -> front.getOpposite();
            case 4 -> front.getCounterClockWise();
            case 5 -> front.getClockWise();
            default -> front;
        };
    }

    private static BioEnergyMachineBlockEntity read(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof BioEnergyMachineBlockEntity machine) return machine;
        throw new IllegalStateException("Missing bio-energy machine at " + pos);
    }
}
