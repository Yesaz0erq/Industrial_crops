package com.industrialcrops.network.payload;
import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.ItemNetworkTerminalBlockEntity;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record ResizeStorageMenuPayload(BlockPos pos, int rows, boolean terminal) {
    public static void encode(ResizeStorageMenuPayload p, FriendlyByteBuf b) { b.writeBlockPos(p.pos); b.writeInt(p.rows); b.writeBoolean(p.terminal); }
    public static ResizeStorageMenuPayload decode(FriendlyByteBuf b) { return new ResizeStorageMenuPayload(b.readBlockPos(), b.readInt(), b.readBoolean()); }
    public static void handle(ResizeStorageMenuPayload payload, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get(); context.setPacketHandled(true);
        var player = context.getSender(); if (player == null) return;


        int rows = Math.max(3, Math.min(6, payload.rows));

        if (payload.terminal) {
            if (!(player.containerMenu instanceof ItemNetworkTerminalMenu current)
                    || !current.getBlockPos().equals(payload.pos)
                    || !current.stillValid(player)) return;
            var blockEntity=current.terminal();
            net.minecraftforge.network.NetworkHooks.openScreen(player, provider(
                    blockEntity.getDisplayName(),
                    (id, inventory, menuPlayer) -> new ItemNetworkTerminalMenu(id, inventory, blockEntity, payload.pos, rows, current.isRemoteAccess())
            ), buffer -> { buffer.writeBlockPos(payload.pos); buffer.writeVarInt(rows); buffer.writeBoolean(current.isRemoteAccess()); });
        } else {
            if (player.distanceToSqr(payload.pos.getX()+.5,payload.pos.getY()+.5,payload.pos.getZ()+.5)>64) return;
            if (!(player.containerMenu instanceof AdvancedIndustrialStorageMenu current)
                    || !current.getBlockPos().equals(payload.pos)
                    || !(player.level().getBlockEntity(payload.pos) instanceof AdvancedIndustrialStorageBlockEntity blockEntity)) return;
            net.minecraftforge.network.NetworkHooks.openScreen(player, provider(
                    blockEntity.getDisplayName(),
                    (id, inventory, menuPlayer) -> new AdvancedIndustrialStorageMenu(id, inventory, blockEntity, payload.pos, rows)
            ), buffer -> { buffer.writeBlockPos(payload.pos); buffer.writeVarInt(rows); });
        }

    }

    private static MenuProvider provider(Component title, Factory factory) {
        return new MenuProvider() {
            @Override public Component getDisplayName() { return title; }
            @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return factory.create(id, inventory, player);
            }
        };
    }


    @FunctionalInterface private interface Factory { AbstractContainerMenu create(int id, Inventory inventory, Player player); }
}
