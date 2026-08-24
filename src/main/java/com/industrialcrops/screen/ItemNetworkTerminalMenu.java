package com.industrialcrops.screen;

import com.industrialcrops.block.entity.ItemNetworkTerminalBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.Optional;

public final class ItemNetworkTerminalMenu extends AbstractContainerMenu {
    public static final int MIN_ROWS = 3;
    public static final int MAX_ROWS = 6;
    public static final int BUTTON_PREVIOUS = 0;
    public static final int BUTTON_NEXT = 1;
    public static final int BUTTON_COPY = 2;
    public static final int BUTTON_RECONSTRUCT = 3;
    public static final int BUTTON_SET_PAGE_BASE = 100;

    private final ItemNetworkTerminalBlockEntity terminal;
    private final BlockPos pos;
    private final int visibleRows;
    private final int visibleSlots;
    private final int[] counts;
    private final Player player;
    private final ContainerLevelAccess access;
    private final CraftingContainer craftingSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final int resultSlotIndex;
    private final int craftingSlotStart;
    private final int craftingSlotEnd;
    private final int playerInventoryStart;
    private final int playerInventoryEnd;
    private final int playerHotbarStart;
    private final int playerHotbarEnd;
    private int page;
    private int syncedPage;
    private int totalPages = 1;
    private int selected = -1;
    private int syncedSelected = -1;
    private String searchQuery = "";

    public ItemNetworkTerminalMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, readOpenData(inventory, buffer));
    }

    private ItemNetworkTerminalMenu(int id, Inventory inventory, OpenData data) {
        this(id, inventory, data.terminal(), data.terminal().getBlockPos(), data.rows());
    }

    public ItemNetworkTerminalMenu(int id, Inventory inventory, ItemNetworkTerminalBlockEntity terminal, BlockPos pos) {
        this(id, inventory, terminal, pos, MIN_ROWS);
    }

    public ItemNetworkTerminalMenu(int id, Inventory inventory, ItemNetworkTerminalBlockEntity terminal, BlockPos pos, int requestedRows) {
        super(ModMenus.ITEM_NETWORK_TERMINAL.get(), id);
        this.terminal = terminal;
        this.pos = pos;
        this.visibleRows = Math.max(MIN_ROWS, Math.min(MAX_ROWS, requestedRows));
        this.visibleSlots = visibleRows * 9;
        this.counts = new int[visibleSlots];
        this.player = inventory.player;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);

        SimpleContainer placeholder = new SimpleContainer(visibleSlots);
        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                addSlot(new NetworkSlot(placeholder, index, 8 + col * 18, 20 + row * 18));
            }
        }
        this.resultSlotIndex = visibleSlots;
        this.craftingSlotStart = resultSlotIndex + 1;
        this.craftingSlotEnd = craftingSlotStart + 9;
        this.playerInventoryStart = craftingSlotEnd;
        this.playerInventoryEnd = playerInventoryStart + 27;
        this.playerHotbarStart = playerInventoryEnd;
        this.playerHotbarEnd = playerHotbarStart + 9;
        addSlot(new ResultSlot(inventory.player, craftingSlots, resultSlots, 0, 129, getCraftingGridY() + 18));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(craftingSlots, col + row * 3, 25 + col * 18, getCraftingGridY() + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 7 + col * 18, getPlayerInventoryY() + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 7 + col * 18, getPlayerHotbarY()));
        }

        addDataSlot(new DataSlot() {
            public int get() {
                page = Math.max(0, Math.min(page, getServerPageCount() - 1));
                return page;
            }
            public void set(int value) { syncedPage = value; }
        });
        addDataSlot(new DataSlot() {
            public int get() { return getServerPageCount(); }
            public void set(int value) { totalPages = Math.max(1, value); }
        });
        addDataSlot(new DataSlot() {
            public int get() { return selectedVisibleIndex(); }
            public void set(int value) { syncedSelected = value; }
        });
        for (int index = 0; index < visibleSlots; index++) {
            final int slot = index;
            addDataSlot(new DataSlot() {
                public int get() { return (int) Math.min(Integer.MAX_VALUE, terminal.count(toAbsoluteIndex(slot))); }
                public void set(int value) { counts[slot] = value; }
            });
        }
    }

    public int getVisibleRows() { return visibleRows; }
    public int getVisibleSlotCount() { return visibleSlots; }
    public int getActionPanelY() { return 77 + (visibleRows - MIN_ROWS) * 18; }
    public int getCraftingGridY() { return getActionPanelY() + 1; }
    public int getPlayerInventoryY() { return 147 + (visibleRows - MIN_ROWS) * 18; }
    public int getPlayerHotbarY() { return getPlayerInventoryY() + 58; }
    public int getImageHeight() { return getPlayerHotbarY() + 24; }
    public BlockPos getBlockPos() { return pos; }
    public int page() { return syncedPage; }
    public int totalPages() { return totalPages; }
    public int selectedVisible() { return syncedSelected; }
    public int count(int slot) { return slot >= 0 && slot < visibleSlots ? counts[slot] : 0; }
    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query;
        page = 0;
        selected = -1;
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_PREVIOUS && page > 0) {
            page--;
            selected = -1;
            broadcastChanges();
            return true;
        }
        int pages = getServerPageCount();
        if (id == BUTTON_NEXT && page < pages - 1) {
            page++;
            selected = -1;
            broadcastChanges();
            return true;
        }
        if (id == BUTTON_COPY && selected >= 0) {
            boolean copied = terminal.copySelected(selected);
            broadcastChanges();
            return copied;
        }
        if (id == BUTTON_RECONSTRUCT && selected >= 0) {
            boolean reconstructed = terminal.reconstructSelected(selected, player);
            if (selected >= terminal.size()) selected = -1;
            broadcastChanges();
            return reconstructed;
        }
        if (id >= BUTTON_SET_PAGE_BASE) {
            page = Math.max(0, Math.min(pages - 1, id - BUTTON_SET_PAGE_BASE));
            selected = -1;
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        if (slot >= 0 && slot < visibleSlots) {
            int absolute = toAbsoluteIndex(slot);
            if (absolute >= 0) {
                selected = absolute;
                terminal.setSelectedIndex(absolute);
                broadcastChanges();
            }
            return;
        }
        super.clicked(slot, button, type, player);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < resultSlotIndex || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == resultSlotIndex) {
            access.execute((level, ignored) -> stack.getItem().onCraftedBy(stack, level, player));
            if (!moveItemStackTo(stack, playerInventoryStart, playerHotbarEnd, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, copy);
        } else if (index >= craftingSlotStart && index < craftingSlotEnd) {
            if (!moveItemStackTo(stack, playerInventoryStart, playerHotbarEnd, false)) return ItemStack.EMPTY;
        } else if (index >= playerInventoryStart && index < playerInventoryEnd) {
            if (!moveItemStackTo(stack, playerHotbarStart, playerHotbarEnd, false)) return ItemStack.EMPTY;
        } else if (index >= playerHotbarStart && index < playerHotbarEnd) {
            if (!moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }
    @Override public void slotsChanged(Container container) {
        if (container == craftingSlots) access.execute((level, ignored) -> updateCraftingResult(level));
    }
    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level, ignored) -> clearContainer(player, craftingSlots));
    }
    @Override public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, ModBlocks.ITEM_NETWORK_TERMINAL.get());
    }

    private static OpenData readOpenData(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int rows = buffer.readableBytes() > 0 ? buffer.readVarInt() : MIN_ROWS;
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ItemNetworkTerminalBlockEntity terminal) {
            return new OpenData(terminal, Math.max(MIN_ROWS, Math.min(MAX_ROWS, rows)));
        }
        throw new IllegalStateException("Missing item network terminal at " + pos);
    }

    private record OpenData(ItemNetworkTerminalBlockEntity terminal, int rows) {}

    private void updateCraftingResult(Level level) {
        if (level.isClientSide || level.getServer() == null || !(player instanceof ServerPlayer serverPlayer)) return;
        ItemStack result = ItemStack.EMPTY;
        Optional<CraftingRecipe> optional = level.getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftingSlots, level);
        if (optional.isPresent()) {
            CraftingRecipe recipe = optional.get();
            resultSlots.setRecipeUsed(recipe);
            ItemStack assembled = recipe.assemble(craftingSlots, level.registryAccess());
            if (assembled.isItemEnabled(level.enabledFeatures())) result = assembled;
        }
        resultSlots.setItem(0, result);
        setRemoteSlot(resultSlotIndex, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), resultSlotIndex, result));
    }

    private int getServerPageCount() {
        int size = terminal.matchingIndices(searchQuery).size();
        int totalRows = (size + 8) / 9;
        return Math.max(1, totalRows - visibleRows + 1);
    }

    private int toAbsoluteIndex(int visibleIndex) {
        int filtered = page * 9 + visibleIndex;
        java.util.List<Integer> matches = terminal.matchingIndices(searchQuery);
        return filtered >= 0 && filtered < matches.size() ? matches.get(filtered) : -1;
    }

    private int selectedVisibleIndex() {
        if (selected < 0) return -1;
        java.util.List<Integer> matches = terminal.matchingIndices(searchQuery);
        int filtered = matches.indexOf(selected);
        int firstVisible = page * 9;
        return filtered < firstVisible || filtered >= firstVisible + visibleSlots
                ? -1 : filtered - firstVisible;
    }

    private final class NetworkSlot extends Slot {
        private NetworkSlot(SimpleContainer container, int index, int x, int y) { super(container, index, x, y); }
        @Override public ItemStack getItem() {
            return terminal.getLevel() != null && !terminal.getLevel().isClientSide()
                    ? terminal.displayStack(toAbsoluteIndex(getSlotIndex()))
                    : super.getItem();
        }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
