package com.industrialcrops.screen;

import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.ReinforcedControlDeviceBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AdvancedIndustrialStorageMenu extends AbstractContainerMenu {
    public static final int MIN_ROWS = 3;
    public static final int MAX_ROWS = 6;
    public static final int CELL_SLOT_COUNT = AdvancedIndustrialStorageBlockEntity.CELL_SLOT_COUNT;
    public static final int BUTTON_PREVIOUS_PAGE = 0;
    public static final int BUTTON_NEXT_PAGE = 1;
    public static final int BUTTON_SET_SCROLL_ROW_BASE = 100;
    public static final int STORAGE_SLOTS_X = 8;
    public static final int STORAGE_SLOTS_Y = 20;
    public static final int CELL_SLOTS_X = -76;
    public static final int CELL_SLOTS_Y = 25;
    public static final int CRAFTING_GRID_X = 25;
    private static final int MAX_SYNCED_COUNT = 10_000;
    /** The RS2 output well is 26 px wide; a 16 px item is centered at x=143. */
    public static final int resultSlot_X = 143;
    public static final int PLAYER_INVENTORY_X = 7;

    private final AdvancedIndustrialStorageBlockEntity blockEntity;
    private final BlockPos pos;
    private final ContainerLevelAccess access;
    private final Player player;
    private final int visibleRows;
    private final int storageSlotCount;
    private final int storageSlotEnd;
    private final int cellSlotStart;
    private final int cellSlotEnd;
    private final int resultSlot;
    private final int craftingSlotStart;
    private final int craftingSlotEnd;
    private final int playerInventoryStart;
    private final int playerInventoryEnd;
    private final int playerHotbarStart;
    private final int playerHotbarEnd;
    private final boolean remoteAccess;
    private final CraftingContainer craftingSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private int unlockedStorageSlots;
    private int scrollRow;
    private int syncedScrollRow;
    private int syncedScrollPositions = 1;
    private String searchQuery = "";
    private boolean cellSlotsVisible;
    private final int[] syncedCounts;

    public AdvancedIndustrialStorageMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, readOpenData(inventory, buffer));
    }

    private AdvancedIndustrialStorageMenu(int containerId, Inventory inventory, OpenData data) {
        this(containerId, inventory, data.blockEntity(), data.blockEntity().getBlockPos(), data.rows(), data.remoteAccess());
    }

    public AdvancedIndustrialStorageMenu(int containerId, Inventory inventory, AdvancedIndustrialStorageBlockEntity blockEntity, BlockPos pos) {
        this(containerId, inventory, blockEntity, pos, MIN_ROWS);
    }

    public AdvancedIndustrialStorageMenu(int containerId, Inventory inventory, AdvancedIndustrialStorageBlockEntity blockEntity, BlockPos pos, int requestedRows) {
        this(containerId, inventory, blockEntity, pos, requestedRows, false);
    }

    public AdvancedIndustrialStorageMenu(int containerId, Inventory inventory, AdvancedIndustrialStorageBlockEntity blockEntity,
                                         BlockPos pos, int requestedRows, boolean remoteAccess) {
        super(ModMenus.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = pos;
        this.remoteAccess = remoteAccess;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);
        this.player = inventory.player;
        this.visibleRows = Math.max(MIN_ROWS, Math.min(MAX_ROWS, requestedRows));
        this.storageSlotCount = visibleRows * 9;
        this.storageSlotEnd = storageSlotCount;
        this.syncedCounts = new int[storageSlotCount];
        this.cellSlotStart = storageSlotEnd;
        this.cellSlotEnd = cellSlotStart + CELL_SLOT_COUNT;
        this.resultSlot = cellSlotEnd;
        this.craftingSlotStart = resultSlot + 1;
        this.craftingSlotEnd = craftingSlotStart + 9;
        this.playerInventoryStart = craftingSlotEnd;
        this.playerInventoryEnd = playerInventoryStart + 27;
        this.playerHotbarStart = playerInventoryEnd;
        this.playerHotbarEnd = playerHotbarStart + 9;

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getUnlockedStorageSlots();
            }

            @Override
            public void set(int value) {
                unlockedStorageSlots = value;
            }
        });
        for (int index = 0; index < storageSlotCount; index++) {
            final int visibleSlot = index;
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    int absoluteSlot = toAbsoluteStorageSlot(visibleSlot);
                    ItemStack stack = absoluteSlot < 0 ? ItemStack.EMPTY : blockEntity.getStorageStack(absoluteSlot);
                    return Math.min(MAX_SYNCED_COUNT, stack.getCount());
                }

                @Override
                public void set(int value) {
                    syncedCounts[visibleSlot] = Math.max(0, value);
                }
            });
        }
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                scrollRow = clampScrollRow(scrollRow);
                return scrollRow;
            }

            @Override
            public void set(int value) {
                syncedScrollRow = Math.max(0, value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getScrollPositionCount();
            }

            @Override
            public void set(int value) {
                syncedScrollPositions = Math.max(1, value);
            }
        });

        addStorageSlots();
        addCellSlots();
        addSlot(new ResultSlot(inventory.player, craftingSlots, resultSlots, 0, resultSlot_X, getResultSlotY()));
        addCraftingSlots();
        addPlayerInventory(inventory, PLAYER_INVENTORY_X, getPlayerInventoryY());
        addPlayerHotbar(inventory, PLAYER_INVENTORY_X, getPlayerHotbarY());
    }

    public int getVisibleRows() { return visibleRows; }
    public int getVisibleSlotCount() { return storageSlotCount; }
    public int getCraftingGridY() { return 78 + (visibleRows - MIN_ROWS) * 18; }
    public int getResultSlotY() { return getCraftingGridY() + 18; }
    public int getCellSlotsY() { return CELL_SLOTS_Y; }
    public int getPlayerInventoryY() { return 165 + (visibleRows - MIN_ROWS) * 18; }
    public int getPlayerHotbarY() { return getPlayerInventoryY() + 58; }
    public int getImageHeight() { return getPlayerHotbarY() + 24; }
    public BlockPos getBlockPos() { return pos; }
    public int getCraftingSlotStart() { return craftingSlotStart; }
    public int getCraftingSlotEnd() { return craftingSlotEnd; }
    public int getPlayerInventoryStart() { return playerInventoryStart; }
    public int getPlayerInventoryEndWithHotbar() { return playerHotbarEnd; }
    public void setCellSlotsVisible(boolean visible) { cellSlotsVisible = visible; }

    /**
     * Server-side JEI transfer that can source ingredients from the complete
     * controller storage, including slots that are not on the current page.
     */
    public boolean transferCraftingRecipe(CraftingRecipe recipe) {
        List<CraftingTarget> targets = craftingTargets(recipe);
        if (targets.isEmpty()) return false;
        List<ItemStack> requested = resolveRequestedStacks(targets);
        if (requested == null || !clearCraftingGrid()) return false;

        for (int index = 0; index < targets.size(); index++) {
            ItemStack extracted = extractMatching(requested.get(index));
            if (extracted.isEmpty()) {
                slotsChanged(craftingSlots);
                broadcastChanges();
                return false;
            }
            craftingSlots.setItem(targets.get(index).slot(), extracted);
        }
        slotsChanged(craftingSlots);
        broadcastChanges();
        return true;
    }

    public int getUnlockedStorageSlots() {
        // The synchronized field is populated by DataSlot#set on the client.
        // On the server it remains zero, so paging must read the authoritative
        // component inventory directly.
        if (!player.level().isClientSide()) {
            return blockEntity.getUnlockedStorageSlots();
        }
        return Math.max(unlockedStorageSlots, blockEntity.getUnlockedStorageSlots());
    }

    public int getStoredCount(int visibleSlot) {
        return visibleSlot >= 0 && visibleSlot < syncedCounts.length ? syncedCounts[visibleSlot] : 0;
    }

    public int getScrollRow() {
        return Math.min(syncedScrollRow, getScrollPositions() - 1);
    }

    public int getScrollPositions() {
        int positions = Math.max(1, syncedScrollPositions);
        // Keep the client usable even while the initial scroll-range data slot
        // is catching up after a controller resize or remote-open handshake.
        if (player.level().isClientSide() && searchQuery.isEmpty()) {
            // The client proxy also receives the six cell slots through the
            // menu. Use that capacity as a fallback while the dedicated data
            // slot is still synchronizing after opening or resizing.
            int clientUnlocked = Math.max(unlockedStorageSlots, blockEntity.getUnlockedStorageSlots());
            positions = Math.max(positions, scrollPositionCountForSlots(clientUnlocked));
        }
        return positions;
    }

    public int getMaxPages() {
        return Math.max(1, (AdvancedIndustrialStorageBlockEntity.STORAGE_SLOT_COUNT + storageSlotCount - 1)
                / storageSlotCount);
    }

    public boolean isStorageSlotUnlocked(int slot) {
        if (slot < 0 || slot >= storageSlotCount) {
            return false;
        }
        int absolute = toAbsoluteStorageSlot(slot);
        return absolute >= 0 && (remoteAccess
                ? absolute < getUnlockedStorageSlots()
                : blockEntity.isStorageSlotUnlocked(absolute));
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query;
        scrollRow = 0;
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_PREVIOUS_PAGE && scrollRow > 0) {
            scrollRow = Math.max(0, scrollRow - visibleRows);
            broadcastChanges();
            return true;
        }
        if (id == BUTTON_NEXT_PAGE && scrollRow < getScrollPositionCount() - 1) {
            scrollRow = Math.min(getScrollPositionCount() - 1, scrollRow + visibleRows);
            broadcastChanges();
            return true;
        }
        if (id >= BUTTON_SET_SCROLL_ROW_BASE) {
            scrollRow = Math.max(0, Math.min(
                    getScrollPositionCount() - 1, id - BUTTON_SET_SCROLL_ROW_BASE));
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        if (slotIndex >= 0 && slotIndex < storageSlotEnd) {
            int storageSlot = slotIndex;
            if (clickType == ClickType.PICKUP) {
                handleStorageClick(storageSlot, button);
                return;
            }
            if (clickType == ClickType.QUICK_MOVE && button == 0) {
                quickMoveStorageToPlayer(storageSlot);
            }
            return;
        }

        super.clicked(slotIndex, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        if (slotIndex >= 0 && slotIndex < storageSlotEnd) {
            return quickMoveStorageToPlayer(slotIndex);
        }

        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex == resultSlot) {
            access.execute((level, ignoredPos) -> stack.getItem().onCraftedBy(stack, level, player));
            if (!moveItemStackTo(stack, playerInventoryStart, playerHotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (slotIndex >= cellSlotStart && slotIndex < cellSlotEnd) {
            if (!slot.mayPickup(player)) {
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(stack, playerInventoryStart, playerHotbarEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= craftingSlotStart && slotIndex < craftingSlotEnd) {
            if (!moveItemStackTo(stack, playerInventoryStart, playerHotbarEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= playerInventoryStart && slotIndex < playerHotbarEnd) {
            if (AdvancedIndustrialStorageBlockEntity.isStorageCell(stack)
                    && moveItemStackTo(stack, cellSlotStart, cellSlotEnd, false)) {
                // Prefer configuring storage cells before pushing cells into the virtual storage grid.
            } else {
                int inserted = blockEntity.insertIntoStorage(stack.copy());
                if (inserted > 0) {
                    stack.shrink(inserted);
                } else if (slotIndex < playerInventoryEnd) {
                    if (!moveItemStackTo(stack, playerHotbarStart, playerHotbarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        broadcastChanges();
        return copy;
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == craftingSlots) {
            access.execute((level, ignoredPos) -> updateCraftingResult(level));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, ignoredPos) -> clearContainer(player, craftingSlots));
    }

    @Override
    public boolean stillValid(Player player) {
        if (remoteAccess) {
            return player.level().hasChunkAt(pos)
                    && player.level().getBlockState(pos).is(ModBlocks.REINFORCED_CONTROL_DEVICE.get());
        }
        return stillValid(access, player, ModBlocks.ADVANCED_INDUSTRIAL_STORAGE_DEVICE.get())
                || stillValid(access, player, ModBlocks.REINFORCED_CONTROL_DEVICE.get());
    }

    private void addStorageSlots() {
        SimpleContainer placeholder = new SimpleContainer(storageSlotCount);
        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = col + row * 9;
                addSlot(new StorageSlot(placeholder, slot, STORAGE_SLOTS_X + col * 18, STORAGE_SLOTS_Y + row * 18));
            }
        }
    }

    private void addCellSlots() {
        for (int slot = 0; slot < CELL_SLOT_COUNT; slot++) {
            final int cellSlot = slot;
            int x = CELL_SLOTS_X + slot % 3 * 18;
            int y = CELL_SLOTS_Y + slot / 3 * 18;
            addSlot(new SlotItemHandler(blockEntity.getCellInventory(), slot, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return AdvancedIndustrialStorageBlockEntity.isStorageCell(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return getItem().isEmpty() || blockEntity.canRemoveStorageCell(cellSlot);
                }

                @Override
                public boolean isActive() {
                    return cellSlotsVisible;
                }
            });
        }
    }

    private void addCraftingSlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(craftingSlots, col + row * 3, CRAFTING_GRID_X + col * 18, getCraftingGridY() + row * 18));
            }
        }
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

    private List<CraftingTarget> craftingTargets(CraftingRecipe recipe) {
        List<CraftingTarget> targets = new ArrayList<>();
        List<Ingredient> ingredients = recipe.getIngredients();
        if (recipe instanceof ShapedRecipe shaped) {
            int width = shaped.getWidth();
            for (int index = 0; index < ingredients.size(); index++) {
                Ingredient ingredient = ingredients.get(index);
                if (!ingredient.isEmpty()) targets.add(new CraftingTarget(index / width * 3 + index % width, ingredient));
            }
        } else {
            int slot = 0;
            for (Ingredient ingredient : ingredients) {
                if (!ingredient.isEmpty()) targets.add(new CraftingTarget(slot++, ingredient));
            }
        }
        return targets;
    }

    private List<ItemStack> resolveRequestedStacks(List<CraftingTarget> targets) {
        List<ItemStack> supplies = new ArrayList<>();
        for (int slot = 0; slot < craftingSlots.getContainerSize(); slot++) addSupply(supplies, craftingSlots.getItem(slot));
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) addSupply(supplies, inventory.getItem(slot));
        for (int slot = 0; slot < blockEntity.getUnlockedStorageSlots(); slot++) addSupply(supplies, blockEntity.getStorageStack(slot));

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < targets.size(); i++) order.add(i);
        order.sort(Comparator.comparingInt(index -> candidateCount(targets.get(index).ingredient(), supplies)));
        ItemStack[] selected = new ItemStack[targets.size()];
        return assignIngredients(0, order, targets, supplies, selected) ? List.of(selected) : null;
    }

    private static boolean assignIngredients(int depth, List<Integer> order, List<CraftingTarget> targets,
                                             List<ItemStack> supplies, ItemStack[] selected) {
        if (depth >= order.size()) return true;
        int targetIndex = order.get(depth);
        Ingredient ingredient = targets.get(targetIndex).ingredient();
        for (ItemStack supply : supplies) {
            if (supply.isEmpty() || !ingredient.test(supply)) continue;
            selected[targetIndex] = supply.copyWithCount(1);
            supply.shrink(1);
            if (assignIngredients(depth + 1, order, targets, supplies, selected)) return true;
            supply.grow(1);
        }
        selected[targetIndex] = ItemStack.EMPTY;
        return false;
    }

    private static int candidateCount(Ingredient ingredient, List<ItemStack> supplies) {
        int count = 0;
        for (ItemStack stack : supplies) if (!stack.isEmpty() && ingredient.test(stack)) count++;
        return count;
    }

    private static void addSupply(List<ItemStack> supplies, ItemStack source) {
        if (source.isEmpty()) return;
        for (ItemStack existing : supplies) {
            if (ItemStack.isSameItemSameComponents(existing, source)) {
                existing.setCount((int) Math.min(Integer.MAX_VALUE, (long) existing.getCount() + source.getCount()));
                return;
            }
        }
        supplies.add(source.copy());
    }

    private boolean clearCraftingGrid() {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < craftingSlots.getContainerSize(); slot++) {
            ItemStack stack = craftingSlots.removeItemNoUpdate(slot);
            if (stack.isEmpty()) continue;
            int inserted = blockEntity.insertIntoStorage(stack.copy());
            stack.shrink(inserted);
            if (!stack.isEmpty()) inventory.add(stack);
            if (!stack.isEmpty()) {
                craftingSlots.setItem(slot, stack);
                return false;
            }
        }
        return true;
    }

    private ItemStack extractMatching(ItemStack requested) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, requested)) continue;
            return inventory.removeItem(slot, 1);
        }
        for (int slot = 0; slot < blockEntity.getUnlockedStorageSlots(); slot++) {
            ItemStack stack = blockEntity.getStorageStack(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, requested)) {
                return blockEntity.extractFromStorageSlot(slot, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private record CraftingTarget(int slot, Ingredient ingredient) {}

    private void handleStorageClick(int storageSlot, int button) {
        int absoluteStorageSlot = toAbsoluteStorageSlot(storageSlot);
        if (!blockEntity.isStorageSlotUnlocked(absoluteStorageSlot)) {
            return;
        }

        ItemStack cursor = getCarried();
        ItemStack slotStack = blockEntity.getStorageStack(absoluteStorageSlot);

        if (cursor.isEmpty()) {
            if (slotStack.isEmpty()) {
                return;
            }
            int amount = button == 1 ? 1 : Math.min(slotStack.getMaxStackSize(), slotStack.getCount());
            setCarried(blockEntity.extractFromStorageSlot(absoluteStorageSlot, amount));
            broadcastChanges();
            return;
        }

        int amount = button == 1 ? 1 : cursor.getCount();
        int inserted = blockEntity.insertIntoStorageSlot(absoluteStorageSlot, cursor.copyWithCount(amount));
        if (inserted <= 0) {
            return;
        }

        cursor.shrink(inserted);
        setCarried(cursor.isEmpty() ? ItemStack.EMPTY : cursor);
        broadcastChanges();
    }

    private ItemStack quickMoveStorageToPlayer(int storageSlot) {
        int absoluteStorageSlot = toAbsoluteStorageSlot(storageSlot);
        if (!blockEntity.isStorageSlotUnlocked(absoluteStorageSlot)) {
            return ItemStack.EMPTY;
        }

        ItemStack stored = blockEntity.getStorageStack(absoluteStorageSlot);
        if (stored.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int requested = Math.min(stored.getCount(), stored.getMaxStackSize());
        ItemStack moving = stored.copyWithCount(requested);
        ItemStack original = moving.copy();
        moveItemStackTo(moving, playerInventoryStart, playerHotbarEnd, true);

        int moved = requested - moving.getCount();
        if (moved <= 0) {
            return ItemStack.EMPTY;
        }

        blockEntity.extractFromStorageSlot(absoluteStorageSlot, moved);
        broadcastChanges();
        return original;
    }

    private void updateCraftingResult(Level level) {
        if (level.isClientSide || level.getServer() == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CraftingInput craftingInput = craftingSlots.asCraftInput();
        ItemStack result = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> optional = level.getServer()
                .getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftingInput, level, (RecipeHolder<CraftingRecipe>) null);
        if (optional.isPresent()) {
            RecipeHolder<CraftingRecipe> holder = optional.get();
            CraftingRecipe recipe = holder.value();
            if (resultSlots.setRecipeUsed(level, serverPlayer, holder)) {
                ItemStack assembled = recipe.assemble(craftingInput, level.registryAccess());
                if (assembled.isItemEnabled(level.enabledFeatures())) {
                    result = assembled;
                }
            }
        }

        resultSlots.setItem(0, result);
        setRemoteSlot(resultSlot, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), resultSlot, result));
    }

    private static OpenData readOpenData(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int rows = buffer.readableBytes() > 0 ? buffer.readVarInt() : MIN_ROWS;
        boolean remoteAccess = buffer.readableBytes() > 0 && buffer.readBoolean();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (!(blockEntity instanceof AdvancedIndustrialStorageBlockEntity) && remoteAccess) {
            blockEntity = new ReinforcedControlDeviceBlockEntity(pos,
                    ModBlocks.REINFORCED_CONTROL_DEVICE.get().defaultBlockState());
        }
        if (blockEntity instanceof AdvancedIndustrialStorageBlockEntity storage) {
            return new OpenData(storage, Math.max(MIN_ROWS, Math.min(MAX_ROWS, rows)), remoteAccess);
        }
        throw new IllegalStateException("Missing advanced industrial storage block entity at " + pos);
    }

    private record OpenData(AdvancedIndustrialStorageBlockEntity blockEntity, int rows, boolean remoteAccess) {}

    private int toAbsoluteStorageSlot(int visibleSlot) {
        if (visibleSlot < 0 || visibleSlot >= storageSlotCount) {
            return -1;
        }
        if (searchQuery.isEmpty()) return clampScrollRow(scrollRow) * 9 + visibleSlot;
        java.util.List<Integer> matches = matchingStorageSlots();
        int filtered = clampScrollRow(scrollRow) * 9 + visibleSlot;
        return filtered < matches.size() ? matches.get(filtered) : -1;
    }

    private int clampScrollRow(int requestedRow) {
        return Math.max(0, Math.min(requestedRow, getScrollPositionCount() - 1));
    }

    private int getScrollPositionCount() {
        int size = searchQuery.isEmpty() ? getUnlockedStorageSlots() : matchingStorageSlots().size();
        return scrollPositionCountForSlots(size);
    }

    private int scrollPositionCountForSlots(int slots) {
        int totalRows = (Math.max(0, slots) + 8) / 9;
        int maxScrollRow = Math.max(0, totalRows - visibleRows);
        return maxScrollRow + 1;
    }

    private java.util.List<Integer> matchingStorageSlots() {
        java.util.List<Integer> matches = new java.util.ArrayList<>();
        String query = searchQuery.toLowerCase(java.util.Locale.ROOT);
        for (int slot = 0; slot < getUnlockedStorageSlots(); slot++) {
            ItemStack stack = blockEntity.getStorageStack(slot);
            if (stack.isEmpty()) continue;
            String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(java.util.Locale.ROOT);
            String name = stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT);
            if (id.contains(query) || name.contains(query)) matches.add(slot);
        }
        return matches;
    }

    private final class StorageSlot extends Slot {
        private StorageSlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public ItemStack getItem() {
            // On the client the proxy/container contents are populated by the
            // server slot packets.  The server must always read the real
            // controller block entity so remote extraction shows the actual
            // stored stack instead of an empty placeholder.  Use the player
            // level rather than the block entity level because remote clients
            // intentionally use a level-less proxy block entity.
            if (player.level().isClientSide()) {
                return super.getItem();
            }
            return blockEntity.getStorageStack(toAbsoluteStorageSlot(getSlotIndex()));
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}

