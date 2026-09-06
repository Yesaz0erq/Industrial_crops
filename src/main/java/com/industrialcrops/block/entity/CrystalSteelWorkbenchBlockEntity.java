package com.industrialcrops.block.entity;

import com.industrialcrops.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class CrystalSteelWorkbenchBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    public static final int CAPACITY = 16_000;
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                BlockState state = getBlockState();
                level.sendBlockUpdated(worldPosition, state, state, 3);
            }
        }
    };

    public CrystalSteelWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_STEEL_WORKBENCH.get(), pos, state);
    }

    @Override public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.industrialcrops.crystal_steel_workbench");
    }
    @Override public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id,
            net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player) {
        return new com.industrialcrops.screen.CrystalSteelWorkbenchMenu(id, inventory, this);
    }

    public FluidTank getTank() { return tank; }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Tank")) tank.readFromNBT(registries, tag.getCompound("Tank"));
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
