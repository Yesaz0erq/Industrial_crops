package com.industrialcrops.block.entity;

import com.industrialcrops.block.StructureBuilderBlock;
import com.industrialcrops.machine.ApStructure;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.StructureBuilderPlan;
import com.industrialcrops.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

public final class StructureBuilderBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MATERIAL_SLOTS=108, UPGRADE_START=108, UPGRADE_COUNT=4, SLOTS=112, MATERIAL_ROWS=10;
    public static final int IDLE=0, RUNNING=1, COMPLETE=2, MISSING=3, BLOCKED=4, UNLOADED=5,
        PROTECTED=6, STOPPED=7, BOUNDS=8, INVALID=9;
    private int type, tier, page, status, progress, total, cooldown;
    private boolean working, creative;
    private UUID owner;
    private StructureBuilderPlan plan;
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
    public ItemStackHandler getInventory() { return inventory; }
    public boolean working() { return working; }
    public int getSlots() { return SLOTS; }
    public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
    public int value(int index) {
        if (index == 6+MATERIAL_ROWS*2) return SpeedUpgradeHelper.tier(inventory,UPGRADE_START,UPGRADE_COUNT);
        if (index < 6) return switch(index) { case 0 -> type; case 1 -> tier; case 2 -> page;
            case 3 -> status; case 4 -> progress; default -> total; };
        int row=(index-6)/2;
        if (row >= materialTypes.size()) return 0;
        return (index-6)%2 == 0 ? BuiltInRegistries.ITEM.getId(materialTypes.get(row)) : materialCounts.get(row);
    }

    public void refresh() {
        if (working || level == null || level.isClientSide()) return;
        materialTypes.clear(); materialCounts.clear(); progress=total=0;
        try {
            plan=StructureBuilderPlan.create(level, worldPosition, getBlockState().getValue(StructureBuilderBlock.FACING),
                ApStructure.Type.values()[type], tier);
            for (Map.Entry<Item,Integer> entry : plan.materials.entrySet()) {
                materialTypes.add(entry.getKey()); materialCounts.add(entry.getValue());
            }
            total=plan.steps.size(); status=total == 0 ? COMPLETE : IDLE;
        } catch (StructureBuilderPlan.Failure failure) { plan=null; status=failure.status; }
        setChanged();
    }

    public boolean button(Player player, int id) {
        if (level == null || level.isClientSide() || player.isSpectator() || !player.mayBuild()) return false;
        if (id == 4 || id == 5) { page=Math.floorMod(page + (id==4 ? -1 : 1), 4); setChanged(); return true; }
        if (id == 3) { working=false; status=STOPPED; plan=null; setChanged(); return true; }
        if (working) return false;
        if (id == 0) type=(type+1)%ApStructure.Type.values().length;
        else if (id == 1) tier=(tier+1)%4;
        else if (id != 2 && id != 6) return false;
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
        int speed=SpeedUpgradeHelper.tier(inventory,UPGRADE_START,UPGRADE_COUNT);
        cooldown=switch(speed) { case 1 -> 4; case 2 -> 9; case 3 -> 1; case 4 -> 0; default -> 9; };
        boolean instantSmall=speed==4 && plan.layout.width()<=16 && plan.layout.depth()<=16;
        int limit=speed>=2 ? Integer.MAX_VALUE : speed==1 ? 8 : 1;
        long chunk=net.minecraft.world.level.ChunkPos.asLong(plan.steps.get(progress).pos());
        // Chunk groups bound the work of fast components; the largest builds never run in one giant loop.
        for (int batch=0; batch<limit && progress<plan.steps.size(); batch++) {
            if (speed>=2 && !instantSmall && net.minecraft.world.level.ChunkPos.asLong(plan.steps.get(progress).pos()) != chunk) break;
            StructureBuilderPlan.Step step=plan.steps.get(progress);
            try { StructureBuilderPlan.available(level, step.pos()); }
            catch (StructureBuilderPlan.Failure failure) { stop(failure.status); return; }
            if (!permitted(player, step.pos()) || step.door() && !permitted(player, step.pos().above())) { stop(PROTECTED); return; }
            // A builder never overwrites a new obstruction placed after the initial check.
            if (!step.upgrade() && !StructureBuilderPlan.replaceable(level, step.pos(), step.soil())
                || step.door() && !StructureBuilderPlan.replaceable(level, step.pos().above(), false)) { stop(BLOCKED); return; }
            if (!creative && count(step.material()) == 0) { stop(MISSING); return; }
            if (step.upgrade()) {
                if (!(level.getBlockEntity(step.pos()) instanceof ApControllerBlockEntity controller)) { stop(INVALID); return; }
                boolean installed=false;
                if (controller.expansionTier() != 0) { stop(BLOCKED); return; }
                for (int slot=0; slot<controller.getUpgradeSlots().getSlots(); slot++) {
                    if (controller.getUpgradeSlots().getStackInSlot(slot).isEmpty()) {
                        controller.getUpgradeSlots().setStackInSlot(slot, new ItemStack(step.material())); installed=true; break;
                    }
                }
                if (!installed) { stop(BLOCKED); return; }
            } else if (!place(player, step)) return;
            if (!creative) consume(step.material());
            int row=materialTypes.indexOf(step.material());
            if (row >= 0) materialCounts.set(row, Math.max(0, materialCounts.get(row)-1));
            progress++;
        }
        if (progress >= plan.steps.size()) {
            boolean valid=plan.layout.check(level).formed()
                && level.getBlockEntity(plan.layout.controller()) instanceof ApControllerBlockEntity controller
                && controller.expansionTier() == tier;
            stop(valid ? COMPLETE : INVALID);
        }
        setChanged();
    }

    /** Place before the loader's normal protection event, rolling back both halves if cancelled. */
    private boolean place(ServerPlayer player, StructureBuilderPlan.Step step) {
        BlockSnapshot before=BlockSnapshot.create(level.dimension(), level, step.pos());
        BlockSnapshot above=step.door() ? BlockSnapshot.create(level.dimension(), level, step.pos().above()) : null;
        boolean placed=level.setBlock(step.pos(), step.state(), 2);
        if (placed && above != null) placed=level.setBlock(step.pos().above(), step.state()
            .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
        boolean cancelled=!placed || EventHooks.onBlockPlace(player, before, Direction.UP)
            || above != null && EventHooks.onBlockPlace(player, above, Direction.UP);
        if (cancelled) {
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
