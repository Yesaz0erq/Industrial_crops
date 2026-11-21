package com.industrialcrops.block.entity;

import com.industrialcrops.machine.MachineItemPort;
import com.industrialcrops.block.StructureBuilderBlock;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.StructureBuilderPlan;
import com.industrialcrops.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.common.util.BlockSnapshot;

public final class StructureBuilderBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MATERIAL_SLOTS=108, UPGRADE_START=108, UPGRADE_COUNT=4, SLOTS=112, MATERIAL_ROWS=10;
    public static final int IDLE=0, RUNNING=1, COMPLETE=2, MISSING=3, BLOCKED=4, UNLOADED=5,
        PROTECTED=6, STOPPED=7, BOUNDS=8, INVALID=9;
    private int type, tier, page, status, progress, total, cooldown;
    private boolean working, creative;
    private UUID owner;
    private StructureBuilderPlan plan;
    private StructureBuilderPlan.Target target;
    private int detectionFailure, demolitionCount;
    private final List<Item> materialTypes = new ArrayList<>();
    private final List<Integer> materialCounts = new ArrayList<>();
    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot < MATERIAL_SLOTS || SpeedUpgradeHelper.isSpeedUpgrade(stack); }
        @Override public int getSlotLimit(int slot) { return slot < MATERIAL_SLOTS ? 64 : 1; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return working ? stack : super.insertItem(slot, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return working ? ItemStack.EMPTY : super.extractItem(slot, amount, simulate);
        }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    public StructureBuilderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRUCTURE_BUILDER.get(), pos, state);
    }
    private final MachineItemPort automation = MachineItemPort.storage(inventory, MATERIAL_SLOTS);

    public MachineItemPort getAutomationHandler() { return automation; }

    public ItemStackHandler getInventory() { return inventory; }
    public boolean working() { return working; }
    public int getSlots() { return SLOTS; }
    public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
    public int value(int index) {
        if (index == 8+MATERIAL_ROWS*2) return demolitionCount;
        if (index == 7+MATERIAL_ROWS*2) return target == null || !target.adjacent() ? 0 : target.installedTier() == 0 ? 1 : 2;
        if (index == 6+MATERIAL_ROWS*2) return SpeedUpgradeHelper.tier(inventory,UPGRADE_START,UPGRADE_COUNT);
        if (index < 6) return switch(index) { case 0 -> target == null ? type : target.type().ordinal(); case 1 -> target == null ? tier : target.tier(); case 2 -> page;
            case 3 -> status; case 4 -> progress; default -> total; };
        int row=(index-6)/2;
        if (row >= materialTypes.size()) return 0;
        return (index-6)%2 == 0 ? BuiltInRegistries.ITEM.getId(materialTypes.get(row)) : materialCounts.get(row);
    }

    private StructureBuilderPlan.Target detectTarget() throws StructureBuilderPlan.Failure {
        return StructureBuilderPlan.detect(level, worldPosition, getBlockState().getValue(StructureBuilderBlock.FACING),
            ApStructure.Type.values()[type], tier);
    }

    public void refreshTargetIfChanged() {
        if (working || level == null || level.isClientSide() || level.getGameTime() % 10 != 0) return;
        try {
            if (!Objects.equals(target, detectTarget())) refresh();
        } catch (StructureBuilderPlan.Failure failure) {
            if (detectionFailure != failure.status) refresh();
        }
    }

    public void refresh() {
        if (working || level == null || level.isClientSide()) return;
        materialTypes.clear(); materialCounts.clear(); progress=total=demolitionCount=0;
        try {
            target=null;
            target=detectTarget();
            detectionFailure=0;
            plan=StructureBuilderPlan.create(level, worldPosition, target);
            for (Map.Entry<Item,Integer> entry : plan.materials.entrySet()) {
                materialTypes.add(entry.getKey()); materialCounts.add(entry.getValue());
            }
            demolitionCount=plan.demolitionCount;
            total=plan.steps.size(); status=total == 0 ? COMPLETE : IDLE;
        } catch (StructureBuilderPlan.Failure failure) { plan=null; status=failure.status; if (target==null) detectionFailure=status; }
        setChanged();
    }

    public boolean button(Player player, int id) {
        if (level == null || level.isClientSide() || player.isSpectator() || !player.mayBuild()) return false;
        if (id == 4 || id == 5) { page=Math.floorMod(page + (id==4 ? -1 : 1), 4); setChanged(); return true; }
        if (id == 3) { working=false; status=STOPPED; plan=null; setChanged(); return true; }
        if (working) return false;
        if (id == 0) {
            if (target == null || !target.adjacent()) type=(type+1)%ApStructure.Type.values().length;
        } else if (id == 1) {
            if (target == null || target.installedTier() == 0) tier=(tier+1)%4;
        } else if (id != 2 && id != 6) return false;
        refresh();
        if (id != 2 || plan == null || status == COMPLETE) return true;
        creative=player.getAbilities().instabuild;
        if (!creative) {
            for (Map.Entry<Item,Integer> entry : plan.materials.entrySet()) {
                if (count(entry.getKey()) < entry.getValue()) { status=MISSING; return true; }
            }
        }
        for (StructureBuilderPlan.Step step : plan.steps) {
            if (!permitted(player, step.pos()) || step.door() && !permitted(player, step.pos().above())) {
                status=PROTECTED; return true;
            }
        }
        owner=player.getUUID(); cooldown=0; working=true; status=RUNNING; setChanged(); return true;
    }

    private boolean permitted(Player player, BlockPos pos) {
        return level.mayInteract(player, pos) && player.mayUseItemAt(pos, Direction.UP, new ItemStack(getBlockState().getBlock()));
    }
    public int count(Item item) {
        int count=0;
        for (int i=0; i<MATERIAL_SLOTS; i++) if (inventory.getStackInSlot(i).is(item)) count+=inventory.getStackInSlot(i).getCount();
        return count;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StructureBuilderBlockEntity machine) {
        if (level.isClientSide() || !machine.working) return;
        machine.buildBatch();
    }

    private void buildBatch() {
        if (plan == null || owner == null || !(level.getPlayerByUUID(owner) instanceof ServerPlayer player)
            || !player.mayBuild() || player.isSpectator() || creative != player.getAbilities().instabuild) { stop(STOPPED); return; }
        if (cooldown > 0) { cooldown--; return; }
        try {
            if (!target.equals(detectTarget())) { stop(BLOCKED); return; }
        } catch (StructureBuilderPlan.Failure failure) { stop(failure.status); return; }
        int speed=SpeedUpgradeHelper.tier(inventory,UPGRADE_START,UPGRADE_COUNT);
        cooldown=switch(speed) { case 1 -> 4; case 2 -> 9; case 3 -> 1; case 4 -> 0; default -> 9; };
        boolean instantSmall=speed==4 && plan.layout.width()<=16 && plan.layout.depth()<=16;
        int limit=speed>=2 ? Integer.MAX_VALUE : speed==1 ? 8 : 1;
        long chunk=net.minecraft.world.level.ChunkPos.asLong(plan.steps.get(progress).pos());
        // Chunk groups bound the work of fast components; the largest builds never run in one giant loop.
        for (int batch=0; batch<limit && progress<plan.steps.size(); batch++) {
            if (speed>=2 && !instantSmall && net.minecraft.world.level.ChunkPos.asLong(plan.steps.get(progress).pos()) != chunk) break;
            StructureBuilderPlan.Step step=plan.steps.get(progress);
            try {
                if (!runStep(player, step)) return;
            } catch (StructureBuilderPlan.Failure failure) { stop(failure.status); return; }
            int row=materialTypes.indexOf(step.material());
            if (row >= 0) materialCounts.set(row, Math.max(0, materialCounts.get(row)-1));
            progress++;
        }
        if (progress >= plan.steps.size()) {
            boolean valid=plan.layout.check(level).formed()
                && level.getBlockEntity(plan.layout.controller()) instanceof ApControllerBlockEntity controller
                && controller.facing() == plan.layout.facing() && controller.structureType() == plan.layout.type()
                && controller.expansionTier() == plan.layout.tier();
            stop(valid ? COMPLETE : INVALID);
        }
        setChanged();
    }

    private boolean runStep(ServerPlayer player, StructureBuilderPlan.Step step) throws StructureBuilderPlan.Failure {
        BlockPos pos=step.pos();
        StructureBuilderPlan.available(level, pos);
        if (step.door()) StructureBuilderPlan.available(level, pos.above());
        if (step.satisfied(level)) return true;
        if (step.material() == Items.WATER_BUCKET && plan.layout.hasSourceWater(level)) return true;
        if (!permitted(player, pos) || step.door() && !permitted(player, pos.above())) throw new StructureBuilderPlan.Failure(PROTECTED);
        if (!creative && !step.clearance() && count(step.material()) == 0) throw new StructureBuilderPlan.Failure(MISSING);
        if (step.upgrade()) {
            if (!(level.getBlockEntity(pos) instanceof ApControllerBlockEntity controller)
                || controller.structureType() != plan.layout.type() || controller.facing() != plan.layout.facing()) throw new StructureBuilderPlan.Failure(INVALID);
            if (controller.expansionTier() != 0) throw new StructureBuilderPlan.Failure(BLOCKED);
            int slot=0;
            while (slot<controller.getUpgradeSlots().getSlots() && !controller.getUpgradeSlots().getStackInSlot(slot).isEmpty()) slot++;
            if (slot==controller.getUpgradeSlots().getSlots()) throw new StructureBuilderPlan.Failure(BLOCKED);
            controller.getUpgradeSlots().setStackInSlot(slot, new ItemStack(step.material()));
        } else {
            StructureBuilderPlan.editable(level, worldPosition, pos);
            if (step.door()) {
                StructureBuilderPlan.editable(level, worldPosition, pos.above());
                if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) throw new StructureBuilderPlan.Failure(BLOCKED);
            }
            if (step.material() == Items.WATER_BUCKET) StructureBuilderPlan.checkBasin(level, pos);
            demolish(player, step);
            if (step.clearance()) return true;
            if (!place(player, step)) return false;
        }
        if (!creative) consume(step.material());
        return true;
    }

    /** Expose the intended state to protection listeners while the original block and inventory are still intact. */
    private static final class PlannedPlacement extends BlockEvent.EntityPlaceEvent {
        private final BlockState intended;
        PlannedPlacement(BlockSnapshot before, BlockState support, ServerPlayer player, BlockState intended) {
            super(before, support, player);
            this.intended=intended;
        }
        @Override public BlockState getState() { return intended; }
        @Override public BlockState getPlacedBlock() { return intended; }
    }

    private boolean mayPlace(ServerPlayer player, BlockPos pos, BlockState intended, BlockState support) {
        BlockSnapshot before=BlockSnapshot.create(level.dimension(), level, pos);
        PlannedPlacement event=new PlannedPlacement(before, support, player, intended);
        return !NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    private boolean mayBreak(ServerPlayer player, BlockPos pos, BlockState old) {
        return old.isAir() || old.canEntityDestroy(level, pos, player) && !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, old, player)).isCanceled();
    }

    private void demolish(ServerPlayer player, StructureBuilderPlan.Step step) throws StructureBuilderPlan.Failure {
        BlockPos pos=step.pos();
        BlockPos upper=pos.above();
        BlockState before=level.getBlockState(pos);
        BlockState above=step.door() ? level.getBlockState(upper) : null;
        if (!mayBreak(player, pos, before) || step.door() && !mayBreak(player, upper, above)) throw new StructureBuilderPlan.Failure(PROTECTED);
        if (!step.clearance() && (!mayPlace(player, pos, step.state(), level.getBlockState(pos.below()))
            || step.door() && !mayPlace(player, upper, step.state().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), step.state()))) {
            throw new StructureBuilderPlan.Failure(PROTECTED);
        }
        // A protection listener can modify the site; only remove the states approved by the events.
        if (level.getBlockState(pos) != before || step.door() && level.getBlockState(upper) != above) throw new StructureBuilderPlan.Failure(BLOCKED);
        removeApproved(player, pos, before);
        // Normal neighbor updates can remove the upper half when a lower door half is broken.
        if (step.door() && !level.getBlockState(upper).isAir()) removeApproved(player, upper, above);
        if (!cleared(pos, before) || step.door() && !cleared(upper, above)) throw new StructureBuilderPlan.Failure(BLOCKED);
    }

    private void removeApproved(ServerPlayer player, BlockPos pos, BlockState before) throws StructureBuilderPlan.Failure {
        if (before.isAir()) return;
        if (level.getBlockState(pos) != before) throw new StructureBuilderPlan.Failure(BLOCKED);
        BlockEntity container=level.getBlockEntity(pos);
        before.getBlock().playerWillDestroy(level, pos, before, player);
        // Respect per-block removal vetoes before emitting loot. The loader also runs the normal removal callbacks.
        if (!before.onDestroyedByPlayer(level, pos, player, !creative, before.getFluidState()) || level.getBlockState(pos) == before)
            throw new StructureBuilderPlan.Failure(BLOCKED);
        before.getBlock().destroy(level, pos, before);
        if (!creative) before.getBlock().playerDestroy(level, player, pos, before, container, ItemStack.EMPTY);
    }

    private boolean cleared(BlockPos pos, BlockState before) {
        BlockState current=level.getBlockState(pos);
        return current.isAir() || current == before.getFluidState().createLegacyBlock();
    }

    /** Both breaks and placements have been approved. Roll back a partial door if a write fails. */
    private boolean place(ServerPlayer player, StructureBuilderPlan.Step step) {
        BlockSnapshot before=BlockSnapshot.create(level.dimension(), level, step.pos());
        BlockSnapshot above=step.door() ? BlockSnapshot.create(level.dimension(), level, step.pos().above()) : null;
        boolean placed=level.setBlock(step.pos(), step.state(), 2);
        if (placed && above != null) placed=level.setBlock(step.pos().above(), step.state()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
        if (!placed) {
            if (above != null) above.restore(2);
            before.restore(2); stop(PROTECTED); return false;
        }
        level.updateNeighborsAt(step.pos(), step.state().getBlock());
        if (above != null) level.updateNeighborsAt(step.pos().above(), step.state().getBlock());
        return true;
    }

    private void consume(Item material) {
        for (int i=0; i<MATERIAL_SLOTS; i++) {
            ItemStack stack=inventory.getStackInSlot(i);
            if (stack.is(material)) {
                ItemStack rest=stack.copy(); rest.shrink(1);
                inventory.setStackInSlot(i, material == Items.WATER_BUCKET ? new ItemStack(Items.BUCKET) : rest);
                return;
            }
        }
    }
    private void stop(int nextStatus) { working=false; status=nextStatus; plan=null; setChanged(); }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Type",type); tag.putInt("Tier",tier); tag.putInt("Page",page);
        tag.putInt("Status",working ? STOPPED : status);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);
        if (tag.contains("Inventory")) {
            CompoundTag saved=tag.getCompound("Inventory").copy(); saved.putInt("Size",SLOTS); inventory.deserializeNBT(registries,saved);
        }
        type=Math.max(0,Math.min(2,tag.getInt("Type"))); tier=Math.max(0,Math.min(3,tag.getInt("Tier")));
        page=Math.max(0,Math.min(3,tag.getInt("Page"))); status=tag.getInt("Status"); working=false; plan=null;
    }
    @Override public Component getDisplayName() { return Component.translatable("block.industrialcrops.automatic_structure_builder"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        refresh(); return new com.industrialcrops.screen.StructureBuilderMenu(id,inventory,this);
    }
}
