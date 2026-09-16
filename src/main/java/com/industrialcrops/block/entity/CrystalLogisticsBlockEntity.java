package com.industrialcrops.block.entity;

import com.industrialcrops.block.CrystalLogisticsBlock;
import com.industrialcrops.machine.DimensionUpgradeHelper;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.CrystalLogisticsMenu;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.*;
import java.util.*;

/** Loaded endpoints only; dimension upgrades use the existing persistent chunk-ticket controller. */
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public final class CrystalLogisticsBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 16000;
    private static final Map<MinecraftServer, Set<GlobalPos>> ENDPOINTS = new WeakHashMap<>();
    private String channel = "";
    private boolean output, ticket;
    private int cursor, connections;
    private boolean dirty;
    private final ItemStackHandler inventory = new ItemStackHandler(9) {
        @Override protected void onContentsChanged(int slot) { changed(); }
    };
    private final ItemStackHandler upgrade = new ItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return DimensionUpgradeHelper.isDimensionUpgrade(stack); }
        @Override protected void onContentsChanged(int slot) { refreshTicket(); changed(); }
    };
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override protected void onContentsChanged() { changed(); }
    };
    private final IItemHandler items = new IItemHandler() {
        public int getSlots() { return 9; }
        public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return output ? stack : inventory.insertItem(slot, stack, simulate); }
        public ItemStack extractItem(int slot, int amount, boolean simulate) { return output ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY; }
        public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
        public boolean isItemValid(int slot, ItemStack stack) { return !output; }
    };
    private final IFluidHandler fluids = new IFluidHandler() {
        public int getTanks() { return 1; }
        public FluidStack getFluidInTank(int index) { return tank.getFluid(); }
        public int getTankCapacity(int index) { return CAPACITY; }
        public boolean isFluidValid(int index, FluidStack fluid) { return !output; }
        public int fill(FluidStack fluid, FluidAction action) { return output ? 0 : tank.fill(fluid, action); }
        public FluidStack drain(FluidStack fluid, FluidAction action) { return output ? tank.drain(fluid, action) : FluidStack.EMPTY; }
        public FluidStack drain(int amount, FluidAction action) { return output ? tank.drain(amount, action) : FluidStack.EMPTY; }
    };
    public CrystalLogisticsBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.CRYSTAL_LOGISTICS.get(), pos, state); }
    public ItemStackHandler inventory() { return inventory; }
    public ItemStackHandler upgrade() { return upgrade; }
    public ItemStack getDimensionUpgrade() { return upgrade.getStackInSlot(0); }
    public FluidTank tank() { return tank; }
    public IItemHandler itemCapability() { return items; }
    public IFluidHandler fluidCapability() { return fluids; }
    public String channel() { return channel; }
    public boolean output() { return output; }
    public int connections() { return connections; }
    public int tier() { return DimensionUpgradeHelper.isInfinite(getDimensionUpgrade()) ? 2 : getDimensionUpgrade().isEmpty() ? 0 : 1; }
    private void changed() { setChanged(); dirty = true; }
    public void configure(String name, boolean isOutput) {
        channel = name.strip().replaceAll("[\\p{Cntrl}§]", "");
        if (channel.length() > 32) channel = channel.substring(0, 32);
        output = isOutput;
        connections = 0;
        if (level != null && !level.isClientSide) level.setBlock(worldPosition, getBlockState().setValue(CrystalLogisticsBlock.OUTPUT, output), 3);
        register(); changed(); sync();
    }
    private void register() {
        if (level instanceof ServerLevel server) ENDPOINTS.computeIfAbsent(server.getServer(), k -> new LinkedHashSet<>()).add(GlobalPos.of(level.dimension(), worldPosition));
    }
    private void unregister() {
        if (level instanceof ServerLevel server) {
            var positions = ENDPOINTS.get(server.getServer());
            if (positions != null) positions.remove(GlobalPos.of(level.dimension(), worldPosition));
        }
    }
    @Override public void onLoad() { super.onLoad(); register(); refreshTicket(); }
    @Override public void setRemoved() { unregister(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { unregister(); super.onChunkUnloaded(); }
    private void refreshTicket() {
        if (!(level instanceof ServerLevel)) return;
        boolean requested = !getDimensionUpgrade().isEmpty();
        if (requested != ticket) { DimensionUpgradeHelper.forceOwnerChunk(this, requested); ticket = requested; }
    }
    public void dropContents() {
        if (level == null || level.isClientSide) return;
        DimensionUpgradeHelper.forceOwnerChunk(this, false); ticket = false;
        for (int i = 0; i < 9; i++) Containers.dropItemStack(level, worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5, inventory.extractItem(i, 64, false));
        Containers.dropItemStack(level, worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5, upgrade.extractItem(0, 1, false));
    }
    public boolean canLink(CrystalLogisticsBlockEntity other) {
        if (other == this || other.isRemoved() || level == null || other.level == null || channel.isEmpty() || !channel.equals(other.channel) || output == other.output) return false;
        if (!level.dimension().equals(other.level.dimension())) return tier() == 2 && other.tier() == 2;
        return (tier() >= 1 && other.tier() >= 1) || worldPosition.distSqr(other.worldPosition) <= 128D*128D;
    }
    private List<CrystalLogisticsBlockEntity> peers() {
        List<CrystalLogisticsBlockEntity> result = new ArrayList<>();
        if (!(level instanceof ServerLevel server) || channel.isEmpty()) return result;
        var positions = ENDPOINTS.get(server.getServer());
        if (positions == null) return result;
        for (GlobalPos point : List.copyOf(positions)) {
            ServerLevel targetLevel = server.getServer().getLevel(point.dimension());
            if (targetLevel == null || targetLevel.getChunkSource().getChunkNow(point.pos().getX()>>4, point.pos().getZ()>>4) == null) continue;
            if (targetLevel.getBlockEntity(point.pos()) instanceof CrystalLogisticsBlockEntity other && canLink(other)) result.add(other);
        }
        return result;
    }
    public static void tick(Level level, BlockPos pos, BlockState state, CrystalLogisticsBlockEntity machine) {
        if (level.getGameTime() % 5 != 0) return;
        machine.transferCycle();
    }
    public void transferCycle() {
        if (!(level instanceof ServerLevel) || isRemoved()) return;
        register(); refreshTicket();
        List<CrystalLogisticsBlockEntity> peers = peers();
        if (connections != peers.size()) { connections = peers.size(); changed(); }
        if (!channel.isEmpty()) {
            if (output) exchangeNeighbors();
            else {
                exchangeNeighbors();
                int itemBudget = 64, fluidBudget = 1000;
                for (int i = 0; i < peers.size(); i++) {
                    var peer = peers.get(Math.floorMod(cursor + i, peers.size()));
                    itemBudget -= moveItems(inventory, peer.inventory, itemBudget);
                    fluidBudget -= moveFluid(tank, peer.tank, fluidBudget);
                    if (itemBudget == 0 && fluidBudget == 0) break;
                }
                if (!peers.isEmpty()) cursor = (cursor + 1) % peers.size();
            }
        }
        if (dirty) sync();
    }
    private void exchangeNeighbors() {
        int itemBudget = 64, fluidBudget = 1000;
        for (Direction side : Direction.values()) {
            BlockPos neighbor = worldPosition.relative(side);
            if (!level.hasChunkAt(neighbor) || level.getBlockEntity(neighbor) instanceof CrystalLogisticsBlockEntity) continue;
            var itemHandler = com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ITEM_HANDLER, neighbor, side.getOpposite());
            var fluidHandler = com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.FLUID_HANDLER, neighbor, side.getOpposite());
            if (itemHandler != null && itemBudget > 0) itemBudget -= output ? moveItems(inventory, itemHandler, itemBudget) : moveItems(itemHandler, inventory, itemBudget);
            if (fluidHandler != null && fluidBudget > 0) fluidBudget -= output ? moveFluid(tank, fluidHandler, fluidBudget) : moveFluid(fluidHandler, tank, fluidBudget);
        }
    }
    private static int moveItems(IItemHandler from, IItemHandler to, int limit) {
        int moved = 0;
        for (int slot = 0; slot < from.getSlots() && moved < limit; slot++) {
            ItemStack sample = from.extractItem(slot, limit-moved, true);
            if (sample.isEmpty()) continue;
            int accepted = sample.getCount() - ItemHandlerHelper.insertItemStacked(to, sample, true).getCount();
            if (accepted <= 0) continue;
            // Extraction and insertion execute on the same server thread, after both sides simulate.
            ItemStack extracted = from.extractItem(slot, accepted, false);
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(to, extracted, false);
            moved += extracted.getCount() - remainder.getCount();
            if (!remainder.isEmpty()) from.insertItem(slot, remainder, false);
        }
        return moved;
    }
    private static int moveFluid(IFluidHandler from, IFluidHandler to, int limit) {
        if (limit <= 0) return 0;
        FluidStack sample = from.drain(limit, IFluidHandler.FluidAction.SIMULATE);
        if (sample.isEmpty()) return 0;
        int accepted = to.fill(sample, IFluidHandler.FluidAction.SIMULATE);
        if (accepted == 0) return 0;
        FluidStack extracted = from.drain(new FluidStack(sample, accepted), IFluidHandler.FluidAction.EXECUTE);
        int moved = to.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
        if (moved < extracted.getAmount()) from.fill(new FluidStack(extracted, extracted.getAmount()-moved), IFluidHandler.FluidAction.EXECUTE);
        return moved;
    }
    private void sync() { if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); dirty = false; }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Channel", channel); tag.putBoolean("Output", output);
        tag.put("Items", inventory.serializeNBT()); tag.put("Upgrade", upgrade.serializeNBT()); tag.put("Tank", tank.writeToNBT( new CompoundTag()));
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        channel = tag.getString("Channel"); output = tag.getBoolean("Output");
        inventory.deserializeNBT( tag.getCompound("Items")); upgrade.deserializeNBT( tag.getCompound("Upgrade")); tank.readFromNBT( tag.getCompound("Tank"));
        connections = tag.getInt("Connections");
    }
    @Override public CompoundTag getUpdateTag() { var tag = saveWithoutMetadata(); tag.putInt("Connections", connections); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public Component getDisplayName() { return Component.translatable("block.industrialcrops.infinite_logistics_transfer_device"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new CrystalLogisticsMenu(id, inventory, this); }

    private LazyOptional<net.minecraftforge.items.IItemHandler> forgeCapability0 = LazyOptional.of(this::itemCapability);
    private LazyOptional<net.minecraftforge.fluids.capability.IFluidHandler> forgeCapability1 = LazyOptional.of(this::fluidCapability);
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @org.jetbrains.annotations.Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return forgeCapability0.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return forgeCapability1.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps();
        forgeCapability0.invalidate();
        forgeCapability1.invalidate();
    }
    @Override public void reviveCaps() {
        super.reviveCaps();
        forgeCapability0 = LazyOptional.of(this::itemCapability);
        forgeCapability1 = LazyOptional.of(this::fluidCapability);
    }
}
