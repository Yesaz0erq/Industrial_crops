package com.industrialcrops.block.entity;

import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.screen.AutomaticPlanterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Plants compatible crop block-items in the loaded 3x3 chunk area centered on this machine.
 * The internal seed drawer is consumed before any of the six adjacent inventories.
 */
public final class AutomaticPlanterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SEED_SLOT_COUNT = 27;
    public static final int ENERGY_CAPACITY = 100_000;
    public static final int ENERGY_PER_PLANT = 10;
    private static final int ENERGY_RECEIVE_RATE = 5_000;
    private static final int CHUNK_SIDE = 3;
    private static final int AREA_SIDE = CHUNK_SIDE * 16;
    private static final int AREA_SIZE = AREA_SIDE * AREA_SIDE;
    private static final int PLANT_INTERVAL = 4;
    private static final int SCAN_ATTEMPTS = 64;

    private final ItemStackHandler inventory = new ItemStackHandler(SEED_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isPlantableSeed(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 999;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final TrackedEnergyStorage energy = new TrackedEnergyStorage();

    private int scanIndex;
    private int cooldown;
    private int plantedCount;

    public AutomaticPlanterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTOMATIC_PLANTER.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public EnergyStorage getEnergyStorage(@Nullable Direction side) {
        return energy;
    }

    public int getPlantedCount() {
        return plantedCount;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AutomaticPlanterBlockEntity planter) {
        if (level.isClientSide()) {
            return;
        }
        PoweredMachineSupport.pullEnergy(level, pos, planter.energy, ENERGY_RECEIVE_RATE);
        if (++planter.cooldown < PLANT_INTERVAL) {
            return;
        }
        planter.cooldown = 0;
        planter.tryPlantNextPosition();
    }

    private void tryPlantNextPosition() {
        if (level == null || energy.getEnergyStored() < ENERGY_PER_PLANT || !hasAnySeedSource()) {
            return;
        }

        for (int attempt = 0; attempt < SCAN_ATTEMPTS; attempt++) {
            BlockPos target = nextTarget();
            if (!isOpenFarmland(target)) {
                continue;
            }
            if (plantFrom(inventory, target)) {
                energy.consume(ENERGY_PER_PLANT);
                plantedCount++;
                setChanged();
                return;
            }
            for (Direction direction : Direction.values()) {
                IItemHandler handler = level.getCapability(
                        Capabilities.ItemHandler.BLOCK,
                        worldPosition.relative(direction),
                        direction.getOpposite()
                );
                if (handler != null && plantFrom(handler, target)) {
                    energy.consume(ENERGY_PER_PLANT);
                    plantedCount++;
                    setChanged();
                    return;
                }
            }
        }
    }

    private boolean hasAnySeedSource() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (isPlantableSeed(inventory.getStackInSlot(slot))) {
                return true;
            }
        }
        if (level == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            IItemHandler handler = level.getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    worldPosition.relative(direction), direction.getOpposite());
            if (handler == null) continue;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                if (isPlantableSeed(handler.getStackInSlot(slot))) {
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos nextTarget() {
        int current = scanIndex++;
        if (scanIndex >= AREA_SIZE) {
            scanIndex = 0;
        }

        int chunkX = worldPosition.getX() >> 4;
        int chunkZ = worldPosition.getZ() >> 4;
        int x = (chunkX - 1) * 16 + current % AREA_SIDE;
        int z = (chunkZ - 1) * 16 + current / AREA_SIDE;
        if (level == null || !level.hasChunk(x >> 4, z >> 4)) {
            return worldPosition;
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }

    private boolean isOpenFarmland(BlockPos target) {
        if (level == null || target.equals(worldPosition)
                || target.getY() < level.getMinBuildHeight() || target.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        return level.getBlockState(target).isAir() && !level.getBlockState(target.below()).isAir();
    }

    private boolean plantFrom(IItemHandler handler, BlockPos target) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack candidate = handler.extractItem(slot, 1, true);
            if (!isPlantableSeed(candidate) || !tryPlaceSeed(candidate, target)) {
                continue;
            }
            ItemStack extracted = handler.extractItem(slot, 1, false);
            if (extracted.isEmpty()) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean tryPlaceSeed(ItemStack seed, BlockPos target) {
        if (level == null || !(seed.getItem() instanceof BlockItem)) {
            return false;
        }
        BlockPos soil = target.below();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(soil), Direction.UP, soil, false);
        UseOnContext context = new UseOnContext(level, null, InteractionHand.MAIN_HAND, seed, hit);
        InteractionResult result = seed.getItem().useOn(context);
        return result.consumesAction();
    }

    public static boolean isPlantableSeed(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof CropBlock;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("ScanIndex", scanIndex);
        tag.putInt("PlantedCount", plantedCount);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        scanIndex = Math.floorMod(tag.getInt("ScanIndex"), AREA_SIZE);
        plantedCount = Math.max(0, tag.getInt("PlantedCount"));
        energy.setStored(tag.getInt("Energy"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.automatic_planter");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AutomaticPlanterMenu(containerId, playerInventory, this, worldPosition);
    }

    private final class TrackedEnergyStorage extends EnergyStorage {
        private TrackedEnergyStorage() {
            super(ENERGY_CAPACITY, ENERGY_RECEIVE_RATE, 0);
        }

        private void setStored(int value) {
            energy = Math.max(0, Math.min(capacity, value));
        }

        private void consume(int amount) {
            energy = Math.max(0, energy - amount);
        }

        @Override
        public int receiveEnergy(int amount, boolean simulate) {
            int accepted = super.receiveEnergy(amount, simulate);
            if (accepted > 0 && !simulate) {
                setChanged();
            }
            return accepted;
        }
    }
}
