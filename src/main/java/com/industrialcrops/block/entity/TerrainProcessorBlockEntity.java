package com.industrialcrops.block.entity;

import com.industrialcrops.machine.CraftingInventorySource;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.screen.TerrainProcessorMenu;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.*;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.*;

/** Bounded, resumable work in one loaded chunk. Sample 0, supply 1, harvested blocks 2..10. */
public final class TerrainProcessorBlockEntity extends BlockEntity implements MenuProvider {
    private static final UUID MACHINE_OWNER = UUID.fromString("a6e9fda7-70f0-4fc7-a906-934972aa30a1");
    private final ItemStackHandler items = new ItemStackHandler(11) {
        @Override public int getSlotLimit(int slot) { return slot == 0 ? 1 : 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return isBuildingMaterial(stack); }
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };
    private final ItemStackHandler upgrades = new ItemStackHandler(4) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return isUpgrade(stack); }
        @Override protected void onContentsChanged(int slot) { update(); }
    };
    public static boolean isUpgrade(ItemStack stack) { return SpeedUpgradeHelper.isSpeedUpgrade(stack) || stack.is(ModItems.MINING_UPGRADE_COMPONENT.get()); }
    public ItemStackHandler upgrades() { return upgrades; }
    public int speedTier() { return SpeedUpgradeHelper.tier(upgrades,0,4); }
    public int cycleInterval() { return speedTier() >= 3 ? 1 : 4; }
    public boolean miningUpgrade() {
        for(int i=0;i<4;i++) if(upgrades.getStackInSlot(i).is(ModItems.MINING_UPGRADE_COMPONENT.get())) return true;
        return false;
    }
    private boolean autoSupply = true;
    public boolean autoSupply() { return autoSupply; }
    private boolean initialized, running, fill, replaceOnly = true;
    private int chunkX, chunkZ, startY, endY, cursor, changed;
    private String status = "idle";
    private UUID owner = MACHINE_OWNER;

    public TerrainProcessorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.TERRAIN_PROCESSOR.get(), pos, state); }
    public ItemStackHandler inventory() { return items; }
    public int chunkX() { return chunkX; }
    public int chunkZ() { return chunkZ; }
    public int startY() { return startY; }
    public int endY() { return endY; }
    public int cursor() { return cursor; }
    public int changed() { return changed; }
    public int total() { return (Math.abs(endY-startY)+1)*256; }
    public boolean running() { return running; }
    public boolean fill() { return fill; }
    public boolean replaceOnly() { return replaceOnly; }
    public String status() { return status; }
    @Override public void onLoad() { super.onLoad(); if (!initialized && level != null) resetDefaults(); }
    public void resetDefaults() {
        if (level == null) return;
        chunkX = worldPosition.getX() >> 4; chunkZ = worldPosition.getZ() >> 4;
        startY = Math.clamp(worldPosition.getY()-1, level.getMinBuildHeight(), level.getMaxBuildHeight()-1);
        endY = level.getMinBuildHeight(); fill = false; replaceOnly = true; autoSupply = true;
        cursor = changed = 0; running = false; initialized = true; status = "idle"; update();
    }
    public void configure(Player player, int cx, int cz, int y1, int y2, boolean filling, boolean onlyReplaceable, int action) {
        if (level == null || level.isClientSide) return;
        if (action == 3 || action == 4) { autoSupply = action == 4; update(); return; }
        if (action == 1) { running = false; status = "paused"; update(); return; }
        if (action == 2) { resetDefaults(); return; }
        if (action != 0) return;
        if (cx < -1874999 || cx > 1874999 || cz < -1874999 || cz > 1874999
                || y1 < level.getMinBuildHeight() || y1 >= level.getMaxBuildHeight()
                || y2 < level.getMinBuildHeight() || y2 >= level.getMaxBuildHeight()) {
            running = false; status = "invalid"; update(); return;
        }
        boolean newJob = cx != chunkX || cz != chunkZ || y1 != startY || y2 != endY || filling != fill || onlyReplaceable != replaceOnly;
        chunkX = cx; chunkZ = cz; startY = y1; endY = y2; fill = filling; replaceOnly = onlyReplaceable;
        if (newJob || cursor >= total()) cursor = changed = 0;
        owner = player.getUUID(); initialized = true; running = true; status = "working"; update();
    }
    public static boolean isBuildingMaterial(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem item)) return false;
        Block block = item.getBlock();
        // These need extra positions, a block entity, or falling physics outside the selected work volume.
        return !(block instanceof EntityBlock || block instanceof FallingBlock || block instanceof BedBlock
                || block instanceof DoorBlock || block instanceof DoublePlantBlock || block instanceof LiquidBlock)
                && block != Blocks.BEDROCK && block != Blocks.AIR;
    }
    private boolean matchesSample(ItemStack stack) {
        return isBuildingMaterial(stack) && (items.getStackInSlot(0).isEmpty()
                || ItemStack.isSameItemSameComponents(items.getStackInSlot(0),stack));
    }
    private boolean refillController(BlockEntity source) {
        var storage = new CraftingInventorySource(source);
        var snapshot = storage.snapshot();
        for (int slot=0;slot<snapshot.size();slot++) {
            var available = snapshot.get(slot);
            if (!matchesSample(available)) continue;
            int amount = Math.min(64, available.getCount());
            var withdrawal = new ArrayList<ItemStack>(Collections.nCopies(snapshot.size(), ItemStack.EMPTY));
            withdrawal.set(slot,available.copyWithCount(amount));
            if (storage.commit(withdrawal)) { items.setStackInSlot(1,available.copyWithCount(amount)); return true; }
        }
        return false;
    }
    private boolean refill() {
        if (!items.getStackInSlot(1).isEmpty()) return matchesSample(items.getStackInSlot(1));
        if (!autoSupply) return false;
        for (int slot=2;slot<11;slot++) {
            ItemStack candidate=items.getStackInSlot(slot);
            if(matchesSample(candidate)) { items.setStackInSlot(1,items.extractItem(slot,64,false)); return true; }
        }
        for (Direction side : Direction.values()) {
            BlockPos pos = worldPosition.relative(side);
            if (!level.hasChunkAt(pos)) continue;
            var be = level.getBlockEntity(pos);
            if (CraftingInventorySource.supported(be)) {
                if (refillController(be)) return true;
                continue;
            }
            var handler = level.getCapability(Capabilities.ItemHandler.BLOCK,pos,side.getOpposite());
            if (handler == null) continue;
            for (int slot=0;slot<Math.min(handler.getSlots(),8192);slot++) {
                ItemStack candidate = handler.extractItem(slot,64,true);
                if (!matchesSample(candidate)) continue;
                ItemStack extracted = handler.extractItem(slot,Math.min(64,candidate.getCount()),false);
                if (!extracted.isEmpty()) { items.setStackInSlot(1,extracted); return matchesSample(extracted); }
            }
        }
        // The same nearby controller/storage integration used by the crafting processor.
        BlockEntity nearest = null; double best = 65;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-8,-8,-8),worldPosition.offset(8,8,8))) {
            double distance = pos.distSqr(worldPosition);
            if (distance > 64 || distance >= best || !level.hasChunkAt(pos)) continue;
            var be = level.getBlockEntity(pos);
            if (CraftingInventorySource.supported(be)) { nearest = be; best = distance; }
        }
        return nearest != null && refillController(nearest);
    }
    private void exportDrops() {
        if (fill) return;
        for (Direction side : Direction.values()) {
            BlockPos pos = worldPosition.relative(side);
            if (!level.hasChunkAt(pos) || CraftingInventorySource.supported(level.getBlockEntity(pos))) continue;
            var target = level.getCapability(Capabilities.ItemHandler.BLOCK,pos,side.getOpposite());
            if (target == null) continue;
            for (int slot=2;slot<11;slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                ItemStack rest = ItemHandlerHelper.insertItemStacked(target,stack.copy(),false);
                if (rest.getCount() != stack.getCount()) items.setStackInSlot(slot,rest);
            }
        }
    }
    private enum Result { SKIP, CHANGED, WAIT }
    private Result process(ServerLevel server, BlockPos pos, FakePlayer player) {
        BlockState old = server.getBlockState(pos);
        if (!server.getWorldBorder().isWithinBounds(pos) || !server.mayInteract(player,pos)
                || ((old.is(Blocks.BEDROCK) || old.getDestroySpeed(server,pos)<0) && (fill || !miningUpgrade())) || old.hasBlockEntity()) return Result.SKIP;
        if (!fill && (old.isAir() || old.getBlock() instanceof LiquidBlock)) return Result.SKIP;
        if (fill && replaceOnly && !old.canBeReplaced()) return Result.SKIP;
        BlockState replacement = Blocks.AIR.defaultBlockState();
        if (fill) {
            if (!refill()) { status = "materials"; return Result.WAIT; }
            ItemStack material = items.getStackInSlot(1);
            var context = new BlockPlaceContext(server,player,InteractionHand.MAIN_HAND,material.copyWithCount(1),
                    new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)) {
                @Override public BlockPos getClickedPos() { return pos; }
            };
            // Read placement properties without allowing a multi-block item to write outside the selected volume.
            replacement = ((BlockItem)material.getItem()).getBlock().getStateForPlacement(context);
            if (replacement == null || !replacement.canSurvive(server,pos) || replacement.hasBlockEntity()) return Result.SKIP;
            if (replacement.equals(old)) return Result.SKIP;
        }
        if (!old.isAir() && NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(server,pos,old,player)).isCanceled()) {
            status = "protected"; return Result.WAIT;
        }
        var drops = old.isAir() || old.getBlock() instanceof LiquidBlock ? List.<ItemStack>of()
                : Block.getDrops(old,server,pos,null,player,new ItemStack(Items.NETHERITE_PICKAXE));
        if (!fill && miningUpgrade() && old.getDestroySpeed(server,pos)<0 && drops.isEmpty() && old.getBlock().asItem()!=Items.AIR)
            drops=List.of(new ItemStack(old.getBlock()));
        var staged = new ItemStackHandler(9);
        for (int i=0;i<9;i++) staged.setStackInSlot(i,items.getStackInSlot(i+2).copy());
        for (ItemStack drop : drops) if (!ItemHandlerHelper.insertItemStacked(staged,drop.copy(),false).isEmpty()) {
            status = "full"; return Result.WAIT;
        }
        var snapshot = BlockSnapshot.create(server.dimension(),server,pos);
        if (!server.setBlock(pos,replacement,3)) return Result.SKIP;
        if (fill && EventHooks.onBlockPlace(player,snapshot,Direction.UP)) {
            snapshot.restore(); status = "protected"; return Result.WAIT;
        }
        if (fill) items.extractItem(1,1,false);
        for (int i=0;i<9;i++) items.setStackInSlot(i+2,staged.getStackInSlot(i));
        return Result.CHANGED;
    }
    public static void tick(Level level, BlockPos pos, BlockState state, TerrainProcessorBlockEntity machine) {
        if (!(level instanceof ServerLevel server) || level.getGameTime()%machine.cycleInterval() != 0) return;
        machine.workCycle(server);
    }
    public void workCycle(ServerLevel server) {
        if (!initialized) resetDefaults();
        exportDrops();
        if (!running) return;
        if (!server.hasChunk(chunkX,chunkZ)) { status="unloaded"; update(); return; }
        FakePlayer player = FakePlayerFactory.get(server,new GameProfile(owner,"[Terrain]"));
        player.setPos(worldPosition.getCenter());
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.NETHERITE_PICKAXE));
        int edits=0;
        int tier=speedTier();
        int boundary=tier>=4?total():tier>=2?Math.min(total(),(cursor/256+1)*256):total();
        int scanLimit=tier>=2?boundary-cursor:64;
        int editLimit=tier>=2?scanLimit:tier==1?16:4;
        for (int scanned=0;scanned<scanLimit && edits<editLimit && cursor<boundary;scanned++) {
            int layer=cursor/256, cell=cursor%256;
            BlockPos target=new BlockPos(chunkX*16+(cell%16),startY+Integer.signum(endY-startY)*layer,chunkZ*16+cell/16);
            Result result=process(server,target,player);
            if (result==Result.WAIT && status.equals("full")) { exportDrops(); result=process(server,target,player); }
            if (result==Result.WAIT) { update(); return; }
            cursor++;
            if (result==Result.CHANGED) { changed++; edits++; }
        }
        status=cursor>=total()?"done":"working";
        if (cursor>=total()) running=false;
        setChanged();
        if (!running || server.getGameTime()%20==0) sync();
    }
    public IItemHandler itemCapability() {
        return new IItemHandler() {
            public int getSlots(){return 10;}
            public ItemStack getStackInSlot(int slot){return items.getStackInSlot(slot+1);}
            public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return slot==0?items.insertItem(1,stack,simulate):stack;}
            public ItemStack extractItem(int slot,int amount,boolean simulate){return slot==0?ItemStack.EMPTY:items.extractItem(slot+1,amount,simulate);}
            public int getSlotLimit(int slot){return 64;}
            public boolean isItemValid(int slot,ItemStack stack){return slot==0&&isBuildingMaterial(stack);}
        };
    }
    public void dropContents() { if(level!=null) for(int i=0;i<11;i++) Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,items.getStackInSlot(i));
        if(level!=null) for(int i=0;i<4;i++) Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,upgrades.getStackInSlot(i)); }
    private void update(){setChanged();sync();}
    private void sync(){if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);
        tag.put("Upgrades",upgrades.serializeNBT(registries));tag.putBoolean("AutoSupply",autoSupply);
        tag.put("Items",items.serializeNBT(registries));tag.putBoolean("Initialized",initialized);
        tag.putInt("ChunkX",chunkX);tag.putInt("ChunkZ",chunkZ);tag.putInt("StartY",startY);tag.putInt("EndY",endY);
        tag.putInt("Cursor",cursor);tag.putInt("Changed",changed);tag.putBoolean("Running",running);
        tag.putBoolean("Fill",fill);tag.putBoolean("ReplaceOnly",replaceOnly);tag.putString("Status",status);tag.putUUID("Owner",owner);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);items.deserializeNBT(registries,tag.getCompound("Items"));
        if(tag.contains("Upgrades")) upgrades.deserializeNBT(registries,tag.getCompound("Upgrades"));
        autoSupply=!tag.contains("AutoSupply") || tag.getBoolean("AutoSupply");
        initialized=tag.getBoolean("Initialized");chunkX=tag.getInt("ChunkX");chunkZ=tag.getInt("ChunkZ");
        startY=tag.getInt("StartY");endY=tag.getInt("EndY");cursor=Math.max(0,tag.getInt("Cursor"));changed=tag.getInt("Changed");
        running=tag.getBoolean("Running");fill=tag.getBoolean("Fill");
        replaceOnly=!tag.contains("ReplaceOnly")||tag.getBoolean("ReplaceOnly");status=tag.getString("Status");
        owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):MACHINE_OWNER;
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveWithoutMetadata(registries);}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public Component getDisplayName(){return Component.translatable("block.industrialcrops.terrain_processing_device");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){if(!initialized)resetDefaults();sync();return new TerrainProcessorMenu(id,inventory,this);}
}
