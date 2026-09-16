package com.industrialcrops.machine;

import com.industrialcrops.block.entity.*;
import java.util.*;
import java.util.function.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Slot adapters are restricted to this mod's storage; arbitrary capabilities cannot break the transaction. */
public final class CraftingInventorySource {
    public record Slot(Supplier<ItemStack> read, IntFunction<ItemStack> extract, Consumer<ItemStack> restore) {}
    private final List<Slot> slots = new ArrayList<>();
    public static boolean supported(BlockEntity be) {
        return be instanceof BasicControlDeviceBlockEntity || be instanceof AdvancedIndustrialStorageBlockEntity || be instanceof ItemNetworkTerminalBlockEntity;
    }
    public CraftingInventorySource(BlockEntity be) {
        if (be instanceof ReinforcedControlDeviceBlockEntity && addDrives(be,true)) return;
        if (be instanceof AdvancedIndustrialStorageBlockEntity storage) {
            for (int i=0;i<Math.min(8192,storage.getUnlockedStorageSlots());i++) {
                int index=i;
                slots.add(new Slot(()->storage.getStorageStack(index), n->storage.extractFromStorageSlot(index,n), s->storage.insertIntoStorageSlot(index,s)));
            }
        } else if (be instanceof BasicControlDeviceBlockEntity basic && basic.getLevel()!=null) {
            addDrives(basic,false);
        } else if (be instanceof ItemNetworkTerminalBlockEntity terminal) {
            for (int i=0;i<Math.min(8192,terminal.size());i++) {
                ItemStack key=terminal.displayStack(i).copy();
                slots.add(new Slot(()->terminal.craftingStored(key), n->terminal.craftingExtract(key,n), s->terminal.addGenerated(s,s.getCount())));
            }
        }
    }
    private boolean addDrives(BlockEntity controller,boolean reinforced) {
        var level=controller.getLevel(); if(level==null) return false;
        var queue=new ArrayDeque<net.minecraft.core.BlockPos>();
        var visited=new HashSet<net.minecraft.core.BlockPos>();
        queue.add(controller.getBlockPos()); visited.add(controller.getBlockPos());
        while(!queue.isEmpty() && visited.size()<4096 && slots.size()<8192) {
            var pos=queue.removeFirst();
            for(var direction:net.minecraft.core.Direction.values()) {
                var next=pos.relative(direction);
                if(!visited.add(next) || !level.hasChunkAt(next)) continue;
                var drive=level.getBlockEntity(next);
                net.minecraftforge.items.ItemStackHandler inventory=null;
                if(reinforced && drive instanceof ReinforcedIndustrialStorageArrayBlockEntity array) inventory=array.getInventory();
                if(!reinforced && drive instanceof BasicCropStorageArrayBlockEntity array) inventory=array.getInventory();
                if(inventory==null) continue;
                queue.add(next); var handler=inventory;
                for(int i=0;i<handler.getSlots() && slots.size()<8192;i++) {
                    int index=i;
                    slots.add(new Slot(()->handler.getStackInSlot(index),n->handler.extractItem(index,n,false),s->handler.insertItem(index,s,false)));
                }
            }
        }
        return !slots.isEmpty();
    }
    public List<ItemStack> snapshot() { return slots.stream().map(s->s.read.get().copy()).toList(); }
    public boolean commit(List<ItemStack> items) {
        if (items.size()!=slots.size()) return false;
        for (int i=0;i<items.size();i++) {
            ItemStack required=items.get(i), current=slots.get(i).read.get();
            if (!required.isEmpty() && (!ItemStack.isSameItemSameTags(required,current)||current.getCount()<required.getCount())) return false;
        }
        List<ItemStack> taken=new ArrayList<>();
        for (int i=0;i<items.size();i++) {
            ItemStack required=items.get(i);
            ItemStack extracted=ItemStack.EMPTY;
            int remaining=required.getCount();
            while(remaining>0) {
                ItemStack part=slots.get(i).extract.apply(remaining);
                if(part.isEmpty()) break;
                if(extracted.isEmpty()) extracted=part.copy(); else extracted.grow(part.getCount());
                remaining-=part.getCount();
            }
            taken.add(extracted);
            if (!required.isEmpty() && (!ItemStack.isSameItemSameTags(required,extracted)||required.getCount()!=extracted.getCount())) {
                for(int j=0;j<taken.size();j++) if(!taken.get(j).isEmpty()) slots.get(j).restore.accept(taken.get(j));
                return false;
            }
        }
        return true;
    }
}
