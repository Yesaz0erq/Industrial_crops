package com.industrialcrops.network.payload;

import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.ItemNetworkTerminalBlockEntity;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public record ResizeStorageMenuPayload(BlockPos pos, int rows, boolean terminal) {
    public static void encode(ResizeStorageMenuPayload p, FriendlyByteBuf b) {
        b.writeBlockPos(p.pos); b.writeVarInt(p.rows); b.writeBoolean(p.terminal);
    }
    public static ResizeStorageMenuPayload decode(FriendlyByteBuf b) {
        return new ResizeStorageMenuPayload(b.readBlockPos(), b.readVarInt(), b.readBoolean());
    }
    public static void handle(ResizeStorageMenuPayload p, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        int rows = Math.max(3, Math.min(6, p.rows));
        if (player == null || player.distanceToSqr(p.pos.getX() + .5, p.pos.getY() + .5, p.pos.getZ() + .5) > 64) return;
        if (p.terminal && player.containerMenu instanceof ItemNetworkTerminalMenu current
                && current.getBlockPos().equals(p.pos)
                && player.level().getBlockEntity(p.pos) instanceof ItemNetworkTerminalBlockEntity be) {
            NetworkHooks.openScreen(player, provider(be.getDisplayName(),
                    (id, inv, ignored) -> new ItemNetworkTerminalMenu(id, inv, be, p.pos, rows)),
                    b -> { b.writeBlockPos(p.pos); b.writeVarInt(rows); });
        } else if (!p.terminal && player.containerMenu instanceof AdvancedIndustrialStorageMenu current
                && current.getBlockPos().equals(p.pos)
                && player.level().getBlockEntity(p.pos) instanceof AdvancedIndustrialStorageBlockEntity be) {
            NetworkHooks.openScreen(player, provider(be.getDisplayName(),
                    (id, inv, ignored) -> new AdvancedIndustrialStorageMenu(id, inv, be, p.pos, rows)),
                    b -> { b.writeBlockPos(p.pos); b.writeVarInt(rows); });
        }
        supplier.get().setPacketHandled(true);
    }
    private static MenuProvider provider(Component title, Factory factory) {
        return new MenuProvider() {
            @Override public Component getDisplayName() { return title; }
            @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return factory.create(id, inv, player);
            }
        };
    }
    @FunctionalInterface private interface Factory { AbstractContainerMenu create(int id, Inventory inv, Player player); }
}
