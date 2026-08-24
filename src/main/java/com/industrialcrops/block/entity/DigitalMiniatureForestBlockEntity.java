package com.industrialcrops.block.entity;

import com.industrialcrops.block.TransportPipeBlock;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.machine.MachineInventoryHelper;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.DigitalMiniatureForestMenu;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class DigitalMiniatureForestBlockEntity extends BlockEntity implements MenuProvider {
    public static final int PROCESS_TICKS = 1_200;
    public static final int UPGRADE_COUNT = 4;
    public static final int ENERGY_CAPACITY = 100_000;
    private static final int ENERGY_PER_BASE_TICK = 10;
    private static final int RECEIVE_RATE = 5_000;

    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_COUNT) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return SpeedUpgradeHelper.isSpeedUpgrade(stack); }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final TrackedEnergyStorage energy = new TrackedEnergyStorage();
    private ItemStack activeSapling = ItemStack.EMPTY;
    private int progress;
    // The terminal lookup is intentionally cached for one tick only. This
    // keeps repeated client/menu state queries cheap while still observing
    // pipe changes on the next server tick.
    private long terminalLookupTick = Long.MIN_VALUE;
    private @Nullable BlockPos cachedTerminalPos;
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored() & 0xFFFF;
                case 1 -> energy.getEnergyStored() >>> 16;
                case 2 -> progress;
                case 3 -> PROCESS_TICKS;
                case 4 -> treeId(activeSapling);
                case 5 -> SpeedUpgradeHelper.tier(inventory, 0, UPGRADE_COUNT);
                case 6 -> findConnectedTerminal() == null ? 0 : 1;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) energy.setStored((energy.getEnergyStored() & 0xFFFF0000) | (value & 0xFFFF));
            else if (index == 1) energy.setStored((energy.getEnergyStored() & 0xFFFF) | ((value & 0xFFFF) << 16));
            else if (index == 2) progress = value;
        }
        @Override public int getCount() { return 7; }
    };

    public DigitalMiniatureForestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DIGITAL_MINIATURE_FOREST.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DigitalMiniatureForestBlockEntity forest) {
        if (level.isClientSide()) return;
        PoweredMachineSupport.pullEnergy(level, pos, forest.energy, RECEIVE_RATE);
        ItemNetworkTerminalBlockEntity terminal = forest.findConnectedTerminal();
        if (forest.activeSapling.isEmpty()) {
            if (terminal == null) return;
            forest.activeSapling = terminal.extractOneMatching(DigitalMiniatureForestBlockEntity::isSupportedSapling);
            if (forest.activeSapling.isEmpty()) return;
            forest.progress = 0;
            forest.setChanged();
        }
        if (forest.progress >= PROCESS_TICKS) {
            if (terminal != null) forest.finishTree(terminal);
            return;
        }
        int step = SpeedUpgradeHelper.progressStep(forest.inventory, 0, UPGRADE_COUNT, PROCESS_TICKS);
        int advance = Math.min(step, PROCESS_TICKS - forest.progress);
        int cost = ENERGY_PER_BASE_TICK * advance;
        if (forest.energy.getEnergyStored() < cost) return;
        forest.energy.consume(cost);
        forest.progress += advance;
        if (forest.progress >= PROCESS_TICKS && terminal != null) forest.finishTree(terminal);
        forest.setChanged();
    }

    private void finishTree(ItemNetworkTerminalBlockEntity terminal) {
        TreeYield yield = yieldFor(activeSapling);
        if (yield == null) { activeSapling = ItemStack.EMPTY; progress = 0; return; }
        terminal.addGenerated(new ItemStack(yield.log()), yield.count());
        activeSapling = ItemStack.EMPTY;
        progress = 0;
        setChanged();
    }

    public static boolean isSupportedSapling(ItemStack stack) { return yieldFor(stack) != null; }
    private static @Nullable TreeYield yieldFor(ItemStack stack) {
        if (stack.is(Items.OAK_SAPLING) || stack.is(Items.AZALEA) || stack.is(Items.FLOWERING_AZALEA)) return new TreeYield(Items.OAK_LOG, 5);
        if (stack.is(Items.BIRCH_SAPLING)) return new TreeYield(Items.BIRCH_LOG, 5);
        if (stack.is(Items.SPRUCE_SAPLING)) return new TreeYield(Items.SPRUCE_LOG, 7);
        if (stack.is(Items.JUNGLE_SAPLING)) return new TreeYield(Items.JUNGLE_LOG, 10);
        if (stack.is(Items.ACACIA_SAPLING)) return new TreeYield(Items.ACACIA_LOG, 6);
        if (stack.is(Items.DARK_OAK_SAPLING)) return new TreeYield(Items.DARK_OAK_LOG, 8);
        if (stack.is(Items.CHERRY_SAPLING)) return new TreeYield(Items.CHERRY_LOG, 7);
        if (stack.is(Items.MANGROVE_PROPAGULE)) return new TreeYield(Items.MANGROVE_LOG, 8);
        return null;
    }
    private static int treeId(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(Items.OAK_SAPLING) || stack.is(Items.AZALEA) || stack.is(Items.FLOWERING_AZALEA)) return 1;
        if (stack.is(Items.BIRCH_SAPLING)) return 2;
        if (stack.is(Items.SPRUCE_SAPLING)) return 3;
        if (stack.is(Items.JUNGLE_SAPLING)) return 4;
        if (stack.is(Items.ACACIA_SAPLING)) return 5;
        if (stack.is(Items.DARK_OAK_SAPLING)) return 6;
        if (stack.is(Items.CHERRY_SAPLING)) return 7;
        if (stack.is(Items.MANGROVE_PROPAGULE)) return 8;
        return 0;
    }

    public ItemStackHandler getInventory() { return inventory; }
    public EnergyStorage getEnergyStorage(@Nullable Direction side) { return energy; }
    public ContainerData getData() { return data; }
    public @Nullable ItemNetworkTerminalBlockEntity findConnectedTerminal() {
        if (level == null) return null;
        long gameTime = level.getGameTime();
        if (terminalLookupTick == gameTime) {
            if (cachedTerminalPos == null) return null;
            BlockEntity cached = level.getBlockEntity(cachedTerminalPos);
            return cached instanceof ItemNetworkTerminalBlockEntity terminal ? terminal : null;
        }
        terminalLookupTick = gameTime;
        cachedTerminalPos = null;
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(); Set<BlockPos> visited = new HashSet<>();
        queue.add(worldPosition); visited.add(worldPosition);
        while (!queue.isEmpty() && visited.size() <= 512) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!level.hasChunkAt(next) || !visited.add(next)) continue;
                BlockEntity entity = level.getBlockEntity(next);
                if (entity instanceof ItemNetworkTerminalBlockEntity terminal) {
                    cachedTerminalPos = next.immutable();
                    return terminal;
                }
                if (level.getBlockState(next).getBlock() instanceof TransportPipeBlock) queue.addLast(next);
            }
        }
        return null;
    }
    @Override public Component getDisplayName() { return Component.translatable("block.industrialcrops.digital_miniature_forest"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new DigitalMiniatureForestMenu(id, inv, this, worldPosition);
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Energy", energy.getEnergyStored()); tag.putInt("Progress", progress);
        if (!activeSapling.isEmpty()) tag.put("ActiveSapling", activeSapling.save(new CompoundTag()));
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
        MachineInventoryHelper.ensureSize(inventory, UPGRADE_COUNT);
        energy.setStored(tag.getInt("Energy")); progress = Math.max(0, Math.min(PROCESS_TICKS, tag.getInt("Progress")));
        activeSapling = tag.contains("ActiveSapling") ? ItemStack.of(tag.getCompound("ActiveSapling")) : ItemStack.EMPTY;
    }
    private record TreeYield(net.minecraft.world.item.Item log, int count) { }
    private final class TrackedEnergyStorage extends EnergyStorage {
        private TrackedEnergyStorage() { super(ENERGY_CAPACITY, RECEIVE_RATE, 0); }
        private void setStored(int value) { energy = Math.max(0, Math.min(capacity, value)); }
        private void consume(int value) { energy = Math.max(0, energy - value); }
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int accepted = super.receiveEnergy(amount, simulate); if (accepted > 0 && !simulate) setChanged(); return accepted;
        }
    }
}
