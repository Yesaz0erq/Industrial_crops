package com.industrialcrops.block.entity;

import com.industrialcrops.registry.*;
import com.industrialcrops.machine.PoweredMachineSupport;
import com.industrialcrops.screen.AnimalBreederMenu;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.*;
import java.util.*;

/** Loaded-chunk-only husbandry. Nine food slots and nine output slots. */
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public final class AnimalBreederBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENERGY_PER_BIRTH=100, ENERGY_CAPACITY=100_000, TANK_CAPACITY=8000;
    private static final GameProfile PROFILE=new GameProfile(UUID.fromString("dd7b5a08-6a90-42ac-ab1a-a3b54ceea9b7"),"[Animal Breeder]");
    private boolean wide, enabled=true, harvest=true, slaughter=true;
    private int limit=16, population, adults, status, scanCursor, scanSection, births, culled;
    private final Power energy=new Power();
    private final ItemStackHandler items=new ItemStackHandler(18) {
        @Override protected void onContentsChanged(int slot){setChanged();}
    };
    private final FluidTank tank=new FluidTank(TANK_CAPACITY,s->s.getFluid()==ModFluids.CONCENTRATED_PLASMA_JUICE.get()) {
        @Override protected void onContentsChanged(){setChanged();}
    };
    private final IItemHandler automation=new IItemHandler() {
        public int getSlots(){return 18;}
        public ItemStack getStackInSlot(int slot){return items.getStackInSlot(slot);}
        public ItemStack insertItem(int slot,ItemStack s,boolean simulate){return slot<9?items.insertItem(slot,s,simulate):s;}
        public ItemStack extractItem(int slot,int amount,boolean simulate){return slot>=9?items.extractItem(slot,amount,simulate):ItemStack.EMPTY;}
        public int getSlotLimit(int slot){return 64;}
        public boolean isItemValid(int slot,ItemStack s){return slot<9;}
    };
    private List<Animal> cachedAnimals=List.of();
    private int clientFluid;
    private final ContainerData data=new ContainerData() {
        public int getCount(){return 12;}
        public int get(int i){return switch(i){case 0->energy.getEnergyStored()&65535;case 1->energy.getEnergyStored()>>>16;
            case 2->level!=null&&level.isClientSide?clientFluid:tank.getFluidAmount();case 3->limit;case 4->population;case 5->adults;
            case 6->wide?1:0;case 7->enabled?1:0;case 8->harvest?1:0;case 9->slaughter?1:0;case 10->status;case 11->births;default->0;};}
        public void set(int i,int v){switch(i){case 0->energy.set((energy.getEnergyStored()&0xffff0000)|(v&65535));case 1->energy.set((energy.getEnergyStored()&65535)|((v&65535)<<16));case 2->clientFluid=v&65535;case 3->limit=v&65535;case 4->population=v&65535;case 5->adults=v&65535;case 6->wide=v!=0;case 7->enabled=v!=0;case 8->harvest=v!=0;case 9->slaughter=v!=0;case 10->status=v;case 11->births=v&65535;}}
    };
    public AnimalBreederBlockEntity(BlockPos pos,BlockState state){super(ModBlockEntities.ANIMAL_BREEDER.get(),pos,state);}
    public ItemStackHandler inventory(){return items;}
    public IItemHandler itemCapability(){return automation;}
    public EnergyStorage energy(){return energy;}
    public FluidTank tank(){return tank;}
    public ContainerData data(){return data;}
    public int limit(){return limit;}
    public boolean wide(){return wide;}
    public boolean enabled(){return enabled;}
    public boolean harvest(){return harvest;}
    public boolean slaughter(){return slaughter;}
    public void configure(int count,boolean area,boolean on,boolean crops,boolean kill){
        limit=net.minecraft.util.Mth.clamp(count,2,4096);wide=area;enabled=on;harvest=crops;slaughter=kill;scanCursor=scanSection=0;setChanged();
    }
    public AABB bounds(){int r=wide?1:0,cx=worldPosition.getX()>>4,cz=worldPosition.getZ()>>4;
        return new AABB((cx-r)*16,level.getMinBuildHeight(),(cz-r)*16,(cx+r+1)*16,level.getMaxBuildHeight(),(cz+r+1)*16);}
    private boolean loaded(){int r=wide?1:0;for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++)
        if(!level.hasChunk((worldPosition.getX()>>4)+x,(worldPosition.getZ()>>4)+z))return false;return true;}
    public static boolean livestock(Animal a){return a.isAlive()&&!a.hasCustomName()
            && !(a instanceof TamableAnimal t&&t.isTame()) && !(a instanceof AbstractHorse h&&h.isTamed());}
    private List<Animal> animals(){AABB b=bounds();return level.getEntitiesOfClass(Animal.class,b,a->livestock(a)&&b.contains(a.position()));}
    private boolean feedBag(ItemStack s){return s.is(ModItems.FEED_BAG_BASIC.get())||s.is(ModItems.FEED_BAG_FAST_BREEDING.get());}
    private boolean food(Animal a,ItemStack s){return !s.isEmpty()&&(a.isFood(s)||feedBag(s));}
    private boolean anyFood(List<Animal> animals,ItemStack s){return animals.stream().anyMatch(a->food(a,s));}
    private ItemStack insert(ItemStack stack,int from,int to,boolean simulate){
        ItemStack rest=stack.copy();for(int i=from;i<to&&!rest.isEmpty();i++)rest=items.insertItem(i,rest,simulate);return rest;
    }
    private void output(ItemStack stack){var rest=insert(stack,9,18,false);if(!rest.isEmpty())Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,rest);}
    private void refill(List<Animal> animals){
        for(int i=9;i<18;i++){
            var stack=items.getStackInSlot(i);if(anyFood(animals,stack))items.setStackInSlot(i,insert(stack,0,9,false));
        }
        for(Direction side:Direction.values()){
            BlockPos pos=worldPosition.relative(side);if(!level.hasChunkAt(pos))continue;
            IItemHandler source=com.industrialcrops.util.ForgeCapabilityUtil.find(level, ForgeCapabilities.ITEM_HANDLER,pos,side.getOpposite());if(source==null)continue;
            for(int slot=0;slot<Math.min(source.getSlots(),256);slot++){
                ItemStack sample=source.extractItem(slot,16,true);if(!anyFood(animals,sample))continue;
                int amount=sample.getCount()-insert(sample,0,9,true).getCount();if(amount<=0)continue;
                ItemStack taken=source.extractItem(slot,amount,false);var rest=insert(taken,0,9,false);if(!rest.isEmpty())output(rest);
            }
        }
    }
    private int[] feedingPlan(Animal a,Animal b){
        for(int i=0;i<9;i++)if(food(a,items.getStackInSlot(i)))for(int j=0;j<9;j++)
            if(food(b,items.getStackInSlot(j))&&(i!=j||items.getStackInSlot(i).getCount()>=2))return new int[]{i,j};
        return null;
    }
    private void consumeFood(int slot){ItemStack consumed=items.extractItem(slot,1,false);if(feedBag(consumed))output(new ItemStack(ModItems.EMPTY_BAG.get()));
        else if(consumed.hasCraftingRemainingItem())output(consumed.getCraftingRemainingItem());}
    private boolean breed(ServerLevel server,List<Animal> animals){
        if(energy.getEnergyStored()<ENERGY_PER_BIRTH){status=2;return false;}
        boolean missingFood=false;
        for(int i=0;i<animals.size();i++){
            Animal a=animals.get(i);if(a.getAge()!=0||a.isInLove())continue;
            for(int j=i+1;j<animals.size();j++){
                Animal b=animals.get(j);if(b.getAge()!=0||b.isInLove()||a.getType()!=b.getType())continue;
                int[] plan=feedingPlan(a,b);if(plan==null){missingFood=true;continue;}
                a.setInLove(null);b.setInLove(null);
                if(!a.canMate(b)||!b.canMate(a)){a.resetLove();b.resetLove();continue;}
                var event=new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(a,b,a.getBreedOffspring(server,b));
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
                var child=event.getChild();
                if(event.isCanceled()||child==null){a.resetLove();b.resetLove();continue;}
                child.setBaby(true);child.moveTo(a.getX(),a.getY(),a.getZ(),0,0);
                if(!server.addFreshEntity(child)){a.resetLove();b.resetLove();continue;}
                consumeFood(plan[0]);consumeFood(plan[1]);energy.consume(ENERGY_PER_BIRTH);
                a.finalizeSpawnChildFromBreeding(server,b,child);
                births++;population++;status=3;setChanged();return true;
            }
        }
        status=missingFood?4:5;return false;
    }
    private boolean outputRoom(){for(int i=9;i<18;i++)if(items.getStackInSlot(i).isEmpty())return true;return false;}
    private void cull(ServerLevel server,List<Animal> animals){
        if(!slaughter||tank.getFluidAmount()<100){status=6;return;}
        if(!outputRoom()){status=7;return;}
        Animal target=animals.stream().filter(a->!a.isBaby()).findFirst().orElse(null);
        if(target==null){status=8;return;}
        var actor=FakePlayerFactory.get(server,PROFILE);actor.setPos(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5);
        // Normal death and loot events remain authoritative; no direct remove/loot-table duplication.
        AABB box=target.getBoundingBox().inflate(2);Set<UUID> before=new HashSet<>();
        server.getEntitiesOfClass(ItemEntity.class,box).forEach(e->before.add(e.getUUID()));
        target.hurt(server.damageSources().playerAttack(actor),Float.MAX_VALUE);
        if(target.isAlive()){status=9;return;}
        tank.drain(100,IFluidHandler.FluidAction.EXECUTE);culled++;population--;adults--;status=10;
        for(ItemEntity drop:server.getEntitiesOfClass(ItemEntity.class,box,e->!before.contains(e.getUUID()))){
            var rest=insert(drop.getItem(),9,18,false);if(rest.isEmpty())drop.discard();else drop.setItem(rest);
        }
        setChanged();
    }
    /** Incremental section scan skips empty sections; never loads chunks or searches a whole volume in one tick. */
    private void scanCrops(ServerLevel server,List<Animal> animals){
        if(!harvest||animals.isEmpty()||population>=limit||energy.getEnergyStored()<ENERGY_PER_BIRTH)return;
        int width=wide?3:1,r=wide?1:0,sections=server.getSectionsCount();
        for(int budget=0;budget<512;budget++){
            int group=Math.floorMod(scanSection,width*width*sections),ci=group/sections,sy=group%sections;
            int cx=(worldPosition.getX()>>4)-r+ci%width,cz=(worldPosition.getZ()>>4)-r+ci/width;
            var chunk=server.getChunkSource().getChunkNow(cx,cz);if(chunk==null)return;
            var section=chunk.getSection(sy);
            if(section.hasOnlyAir()){scanSection=(group+1)%(width*width*sections);scanCursor=0;continue;}
            int n=scanCursor++;if(scanCursor>=4096){scanCursor=0;scanSection=(group+1)%(width*width*sections);}
            BlockPos pos=new BlockPos(cx*16+(n&15),server.getMinBuildHeight()+sy*16+(n>>8),cz*16+((n>>4)&15));
            if(harvestCrop(server,pos,animals))return;
        }
    }
    public boolean harvestCrop(ServerLevel server,BlockPos pos,List<Animal> animals){
        if(!bounds().contains(pos.getCenter())||!server.hasChunkAt(pos))return false;
        BlockState state=server.getBlockState(pos),reset;
        Item seed=Items.AIR;
        if(state.getBlock() instanceof CropBlock crop){
            if(!crop.isMaxAge(state))return false;
            reset=crop.getStateForAge(0);seed=crop.getCloneItemStack(server,pos,state).getItem();
        }else if(state.is(Blocks.SWEET_BERRY_BUSH)&&state.getValue(SweetBerryBushBlock.AGE)>=2){
            reset=state.setValue(SweetBerryBushBlock.AGE,0);
        }else if((state.is(Blocks.CAVE_VINES)||state.is(Blocks.CAVE_VINES_PLANT))&&state.getValue(CaveVines.BERRIES)){
            reset=state.setValue(CaveVines.BERRIES,false);
        }else return false;
        var actor=FakePlayerFactory.get(server,PROFILE);
        if(!server.getWorldBorder().isWithinBounds(pos)||!server.mayInteract(actor,pos))return false;
        var drops=Block.getDrops(state,server,pos,server.getBlockEntity(pos),actor,ItemStack.EMPTY);
        if(drops.stream().noneMatch(s->anyFood(animals,s)))return false;
        // The reset retains one planted seed, which is removed from the harvest.
        for(ItemStack s:drops)if(s.is(seed)){s.shrink(1);break;}
        ItemStackHandler simulation=new ItemStackHandler(18);
        for(int i=0;i<18;i++)simulation.setStackInSlot(i,items.getStackInSlot(i).copy());
        for(ItemStack s:drops){var rest=s.copy();for(int i=anyFood(animals,s)?0:9;i<18&&!rest.isEmpty();i++)rest=simulation.insertItem(i,rest,false);if(!rest.isEmpty())return false;}
        if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.BlockEvent.BreakEvent(server,pos,state,actor)))return false;
        if(!server.setBlock(pos,reset,3))return false;
        for(int i=0;i<18;i++)items.setStackInSlot(i,simulation.getStackInSlot(i));
        setChanged();return true;
    }
    public void workCycle(){
        if(!(level instanceof ServerLevel server))return;
        if(!enabled){status=0;return;}if(!loaded()){status=1;return;}
        var animals=animals();cachedAnimals=animals;population=animals.size();adults=(int)animals.stream().filter(a->!a.isBaby()).count();
        if(energy.getEnergyStored()==0){status=2;return;}
        if(population>=limit){cull(server,animals);return;}
        if(energy.getEnergyStored()<ENERGY_PER_BIRTH){status=2;return;}
        refill(animals);breed(server,animals);
    }
    public static void tick(Level level,BlockPos pos,BlockState state,AnimalBreederBlockEntity m){
        if(!(level instanceof ServerLevel server))return;
        PoweredMachineSupport.pullEnergy(level,pos,m.energy,5000);
        if(level.getGameTime()%20==0)m.workCycle();
        if(m.enabled&&m.loaded()&&level.getGameTime()%2==0)m.scanCrops(server,m.cachedAnimals.stream().filter(AnimalBreederBlockEntity::livestock).toList());
    }
    public void dropContents(){for(int i=0;i<18;i++)Containers.dropItemStack(level,worldPosition.getX(),worldPosition.getY(),worldPosition.getZ(),items.getStackInSlot(i));}
    @Override public Component getDisplayName(){return Component.translatable("block.industrialcrops.automatic_animal_breeder");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player player){return new AnimalBreederMenu(id,inv,this);}
    @Override protected void saveAdditional(CompoundTag tag){super.saveAdditional(tag);
        tag.put("Items",items.serializeNBT());tag.put("Plasma",tank.writeToNBT(new CompoundTag()));tag.putInt("Energy",energy.getEnergyStored());
        tag.putInt("Limit",limit);tag.putBoolean("Wide",wide);tag.putBoolean("Enabled",enabled);tag.putBoolean("Harvest",harvest);tag.putBoolean("Slaughter",slaughter);tag.putInt("Births",births);tag.putInt("Culled",culled);}
    @Override public void load(CompoundTag tag){super.load(tag);
        if(tag.contains("Items"))items.deserializeNBT(tag.getCompound("Items"));if(tag.contains("Plasma"))tank.readFromNBT(tag.getCompound("Plasma"));energy.set(tag.getInt("Energy"));
        if(tag.contains("Limit"))configure(tag.getInt("Limit"),tag.getBoolean("Wide"),tag.getBoolean("Enabled"),tag.getBoolean("Harvest"),tag.getBoolean("Slaughter"));births=tag.getInt("Births");culled=tag.getInt("Culled");}
    private final class Power extends EnergyStorage {
        Power(){super(ENERGY_CAPACITY,5000,0);}void set(int v){energy=net.minecraft.util.Mth.clamp(v,0,capacity);}void consume(int v){energy-=v;setChanged();}
        @Override public int receiveEnergy(int amount,boolean simulate){int n=super.receiveEnergy(amount,simulate);if(n>0&&!simulate)setChanged();return n;}
    }

    private LazyOptional<net.minecraftforge.items.IItemHandler> forgeCapability0 = LazyOptional.of(this::itemCapability);
    private LazyOptional<net.minecraftforge.energy.IEnergyStorage> forgeCapability1 = LazyOptional.of(this::energy);
    private LazyOptional<net.minecraftforge.fluids.capability.IFluidHandler> forgeCapability2 = LazyOptional.of(this::tank);
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap, @org.jetbrains.annotations.Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return forgeCapability0.cast();
        if (cap == ForgeCapabilities.ENERGY) return forgeCapability1.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER) return forgeCapability2.cast();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps();
        forgeCapability0.invalidate();
        forgeCapability1.invalidate();
        forgeCapability2.invalidate();
    }
    @Override public void reviveCaps() {
        super.reviveCaps();
        forgeCapability0 = LazyOptional.of(this::itemCapability);
        forgeCapability1 = LazyOptional.of(this::energy);
        forgeCapability2 = LazyOptional.of(this::tank);
    }
}
