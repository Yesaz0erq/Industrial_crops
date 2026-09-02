package com.industrialcrops.screen;

import com.industrialcrops.block.MatterMachineBlock;
import com.industrialcrops.block.entity.ItemNetworkTerminalBlockEntity;
import com.industrialcrops.block.entity.MatterMachineBlockEntity;
import com.industrialcrops.registry.ModMenus;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MatterMachineMenu extends AbstractContainerMenu {
    public static final int BUTTON_OPERATE = 0;
    public static final int BUTTON_SIDE_BASE = 10;
    public static final int BUTTON_ALL_SIDES_ON = 20;
    public static final int BUTTON_ALL_SIDES_OFF = 21;
    public static final int BUTTON_SET_PAGE_BASE = 100;
    public static final int BUTTON_SELECT_BASE = 200;
    public static final int RELATIVE_SIDE_COUNT = 6;
    public static final int TERMINAL_ROWS = 3;
    public static final int TERMINAL_SLOTS = TERMINAL_ROWS * 9;
    public static final int UPGRADE_SLOT_X = -68;
    public static final int UPGRADE_SLOT_Y = 47;

    private final MatterMachineBlockEntity blockEntity;
    private final BlockPos pos;
    private final ContainerData machineData;
    private final int firstPhysicalMachineSlot;
    private final int physicalMachineSlots;
    private final int upgradeMenuStart;
    private final int playerInventoryStart;
    private final int playerInventoryEnd;
    private final int playerHotbarStart;
    private final int playerHotbarEnd;
    private final int[] counts = new int[TERMINAL_SLOTS];
    private int page;
    private int syncedPage;
    private int totalPages = 1;
    private int selected = -1;
    private int syncedSelected = -1;
    private String searchQuery = "";
    private boolean upgradeSlotsVisible;

    public MatterMachineMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, read(inventory, buffer));
    }

    private MatterMachineMenu(int id, Inventory inventory, MatterMachineBlockEntity entity) {
        this(id, inventory, entity, entity.getBlockPos());
    }

    public MatterMachineMenu(int id, Inventory playerInventory, MatterMachineBlockEntity entity, BlockPos pos) {
        super(ModMenus.MATTER_MACHINE.get(), id);
        blockEntity = entity;
        this.pos = pos;
        machineData = entity.getData();
        boolean digitizer = entity.getKind() == MatterMachineBlockEntity.Kind.DIGITIZER;
        int mainPhysicalSlots;

        if (digitizer) {
            firstPhysicalMachineSlot = 0;
            addDigitizerSlots(entity);
            mainPhysicalSlots = MatterMachineBlockEntity.DIGITIZER_MAIN_SLOT_COUNT;
        } else {
            firstPhysicalMachineSlot = TERMINAL_SLOTS;
            selected = entity.getSelectedNetworkIndex();
            addTerminalSlots();
            if (entity.getKind() == MatterMachineBlockEntity.Kind.RECONSTRUCTOR) {
                mainPhysicalSlots = 1;
                addSlot(new SlotItemHandler(entity.getInventory(), MatterMachineBlockEntity.OUTPUT_SLOT, 116, 82) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                });
            } else mainPhysicalSlots = 0;
        }
        upgradeMenuStart = firstPhysicalMachineSlot + mainPhysicalSlots;
        addUpgradeSlots(entity);
        physicalMachineSlots = mainPhysicalSlots + MatterMachineBlockEntity.UPGRADE_SLOT_COUNT;

        playerInventoryStart = firstPhysicalMachineSlot + physicalMachineSlots;
        playerInventoryEnd = playerInventoryStart + 27;
        playerHotbarStart = playerInventoryEnd;
        playerHotbarEnd = playerHotbarStart + 9;
        addDataSlots(machineData);
        if (!digitizer) addTerminalDataSlots();

        int inventoryY = 109;
        int hotbarY = 167;
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, inventoryY + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
    }

    private void addDigitizerSlots(MatterMachineBlockEntity entity) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;
                addSlot(new SlotItemHandler(entity.getInventory(), slot, 8 + column * 18, 20 + row * 18));
            }
        }
    }

    private void addUpgradeSlots(MatterMachineBlockEntity entity) {
        int inventoryStart = entity.getUpgradeSlotStart();
        for (int index = 0; index < MatterMachineBlockEntity.UPGRADE_SLOT_COUNT; index++) {
            int x = UPGRADE_SLOT_X + index % 2 * 22;
            int y = UPGRADE_SLOT_Y + index / 2 * 22;
            addSlot(new SlotItemHandler(entity.getInventory(), inventoryStart + index, x, y) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return MatterMachineBlockEntity.isMachineUpgrade(stack) && super.mayPlace(stack);
                }
                @Override public boolean isActive() { return upgradeSlotsVisible; }
            });
        }
    }

    public void setUpgradeSlotsVisible(boolean visible) { upgradeSlotsVisible = visible; }

    private void addTerminalSlots() {
        SimpleContainer clientProjection = new SimpleContainer(TERMINAL_SLOTS);
        for (int row = 0; row < TERMINAL_ROWS; row++) for (int col = 0; col < 9; col++) {
            int index = row * 9 + col;
            addSlot(new TerminalSlot(clientProjection, index, 8 + col * 18, 20 + row * 18));
        }
    }

    private void addTerminalDataSlots() {
        addDataSlot(new DataSlot() {
            @Override public int get() {
                page = Math.max(0, Math.min(page, getServerPageCount() - 1));
                return page;
            }
            @Override public void set(int value) { syncedPage = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return getServerPageCount(); }
            @Override public void set(int value) { totalPages = Math.max(1, value); }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return selectedVisibleIndex(); }
            @Override public void set(int value) { syncedSelected = value; }
        });
        for (int index = 0; index < TERMINAL_SLOTS; index++) {
            final int slot = index;
            addDataSlot(new DataSlot() {
                @Override public int get() {
                    ItemNetworkTerminalBlockEntity terminal = blockEntity.findConnectedTerminal();
                    return terminal == null ? 0 : (int) Math.min(Integer.MAX_VALUE, terminal.count(toAbsoluteIndex(slot)));
                }
                @Override public void set(int value) { counts[slot] = value; }
            });
        }
    }

    public int energy() { return (machineData.get(0) & 0xFFFF) | ((machineData.get(1) & 0xFFFF) << 16); }
    public int progress() { return machineData.get(2); }
    public int maxProgress() { return machineData.get(3); }
    public MatterMachineBlockEntity.Kind kind() {
        return MatterMachineBlockEntity.Kind.values()[Math.max(0, Math.min(2, machineData.get(4)))];
    }
    public boolean isEnergySideEnabled(Direction direction) { return (machineData.get(5) & (1 << direction.ordinal())) != 0; }
    public boolean isRelativeEnergySideEnabled(int relativeSide) { return isEnergySideEnabled(toWorldDirection(relativeSide)); }
    public Direction worldDirectionForRelative(int relativeSide) { return toWorldDirection(relativeSide); }
    public boolean hasConnectedTerminal() { return machineData.get(6) != 0; }
    public boolean canStartOperation() { return machineData.get(7) != 0; }
    public boolean isOperating() { return machineData.get(8) != 0; }
    public boolean isAutomaticCopying() { return machineData.get(9) != 0; }
    public int page() { return syncedPage; }
    public int totalPages() { return totalPages; }
    public int selectedVisible() { return syncedSelected; }
    public int count(int slot) { return slot >= 0 && slot < TERMINAL_SLOTS ? counts[slot] : 0; }
    public BlockPos getBlockPos() { return pos; }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query;
        page = 0;
        selected = -1;
        broadcastChanges();
    }

    public boolean selectVisibleSlot(int visibleIndex) {
        if (kind() == MatterMachineBlockEntity.Kind.DIGITIZER
                || visibleIndex < 0 || visibleIndex >= TERMINAL_SLOTS) return false;
        int absolute = toAbsoluteIndex(visibleIndex);
        ItemNetworkTerminalBlockEntity terminal = blockEntity.findConnectedTerminal();
        if (absolute < 0 || terminal == null) return false;
        selected = absolute;
        terminal.setSelectedIndex(absolute);
        if (!blockEntity.selectNetworkEntry(absolute)) return false;
        broadcastChanges();
        return true;
    }

    public boolean operateVisibleSlot(int visibleIndex, Player player) {
        if (blockEntity.getKind() == MatterMachineBlockEntity.Kind.DIGITIZER
                || visibleIndex < 0 || visibleIndex >= TERMINAL_SLOTS
                || !stillValid(player)) return false;
        int absolute = toAbsoluteIndex(visibleIndex);
        ItemNetworkTerminalBlockEntity terminal = blockEntity.findConnectedTerminal();
        if (absolute < 0 || terminal == null) return false;
        selected = absolute;
        terminal.setSelectedIndex(absolute);
        boolean started = blockEntity.requestNetworkOperation(absolute);
        broadcastChanges();
        return started;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_OPERATE) return blockEntity.performNetworkOperation(player);
        if (id >= BUTTON_SIDE_BASE && id < BUTTON_SIDE_BASE + RELATIVE_SIDE_COUNT) {
            blockEntity.toggleEnergyInput(toWorldDirection(id - BUTTON_SIDE_BASE));
            broadcastChanges();
            return true;
        }
        if (id == BUTTON_ALL_SIDES_ON || id == BUTTON_ALL_SIDES_OFF) {
            blockEntity.setAllEnergyInputs(id == BUTTON_ALL_SIDES_ON);
            broadcastChanges();
            return true;
        }
        if (id >= BUTTON_SELECT_BASE && id < BUTTON_SELECT_BASE + TERMINAL_SLOTS
                && kind() != MatterMachineBlockEntity.Kind.DIGITIZER) {
            return selectVisibleSlot(id - BUTTON_SELECT_BASE);
        }
        if (id >= BUTTON_SET_PAGE_BASE && id < BUTTON_SELECT_BASE
                && kind() != MatterMachineBlockEntity.Kind.DIGITIZER) {
            page = Math.max(0, Math.min(getServerPageCount() - 1, id - BUTTON_SET_PAGE_BASE));
            selected = -1;
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        if (kind() != MatterMachineBlockEntity.Kind.DIGITIZER && slot >= 0 && slot < TERMINAL_SLOTS) {
            int absolute = toAbsoluteIndex(slot);
            ItemNetworkTerminalBlockEntity terminal = blockEntity.findConnectedTerminal();
            if (absolute >= 0 && terminal != null) {
                selected = absolute;
                terminal.setSelectedIndex(absolute);
                blockEntity.selectNetworkEntry(absolute);
                broadcastChanges();
            }
            return;
        }
        super.clicked(slot, button, type, player);
    }

    private Direction toWorldDirection(int relativeSide) {
        Direction front = blockEntity.getBlockState().getValue(MatterMachineBlock.FACING);
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        if (kind() != MatterMachineBlockEntity.Kind.DIGITIZER && index < TERMINAL_SLOTS) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        boolean isPhysicalMachineSlot = index >= firstPhysicalMachineSlot
                && index < firstPhysicalMachineSlot + physicalMachineSlots;
        if (isPhysicalMachineSlot) {
            if (!moveItemStackTo(stack, playerInventoryStart, playerHotbarEnd, true)) return ItemStack.EMPTY;
        } else if (MatterMachineBlockEntity.isMachineUpgrade(stack)) {
            if (!moveItemStackTo(stack, upgradeMenuStart,
                    upgradeMenuStart + MatterMachineBlockEntity.UPGRADE_SLOT_COUNT, false)) return ItemStack.EMPTY;
        } else if (kind() == MatterMachineBlockEntity.Kind.DIGITIZER && blockEntity.isItemValid(0, stack)) {
            if (!moveItemStackTo(stack, firstPhysicalMachineSlot,
                    firstPhysicalMachineSlot + MatterMachineBlockEntity.DIGITIZER_MAIN_SLOT_COUNT, false)) return ItemStack.EMPTY;
        } else if (index >= playerInventoryStart && index < playerInventoryEnd) {
            if (!moveItemStackTo(stack, playerHotbarStart, playerHotbarEnd, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) == blockEntity
                && player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64;
    }

    private int getServerPageCount() {
        ItemNetworkTerminalBlockEntity terminal = blockEntity.findConnectedTerminal();
        if (terminal == null) return 1;
        int rows = (terminal.matchingIndices(searchQuery).size() + 8) / 9;
        return Math.max(1, rows - TERMINAL_ROWS + 1);
    }

    private int toAbsoluteIndex(int visibleIndex) {
        ItemNetworkTerminalBlockEntity terminal = blockEntity.findConnectedTerminal();
        if (terminal == null) return -1;
        List<Integer> matches = terminal.matchingIndices(searchQuery);
        int filtered = page * 9 + visibleIndex;
        return filtered >= 0 && filtered < matches.size() ? matches.get(filtered) : -1;
    }

    private int selectedVisibleIndex() {
        if (selected < 0) return -1;
        ItemNetworkTerminalBlockEntity terminal = blockEntity.findConnectedTerminal();
        if (terminal == null) return -1;
        List<Integer> matches = terminal.matchingIndices(searchQuery);
        int filtered = matches.indexOf(selected);
        int firstVisible = page * 9;
        return filtered < firstVisible || filtered >= firstVisible + TERMINAL_SLOTS ? -1 : filtered - firstVisible;
    }

    private static MatterMachineBlockEntity read(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof MatterMachineBlockEntity machine) return machine;
        throw new IllegalStateException("Missing matter machine at " + pos);
    }

    private final class TerminalSlot extends Slot {
        private TerminalSlot(SimpleContainer container, int index, int x, int y) { super(container, index, x, y); }
        @Override public ItemStack getItem() {
            ItemNetworkTerminalBlockEntity terminal = blockEntity.findConnectedTerminal();
            return terminal != null && terminal.getLevel() != null && !terminal.getLevel().isClientSide()
                    ? terminal.displayStack(toAbsoluteIndex(getSlotIndex())) : super.getItem();
        }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
