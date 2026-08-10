package com.industrialcrops.network.payload;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.block.entity.ItemNetworkTerminalBlockEntity;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import com.industrialcrops.screen.ItemNetworkTerminalMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record ResizeStorageMenuPayload(BlockPos pos, int rows, boolean terminal) implements CustomPacketPayload {
    public static final Type<ResizeStorageMenuPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "resize_storage_menu"));
    public static final StreamCodec<ByteBuf, ResizeStorageMenuPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override public ResizeStorageMenuPayload decode(ByteBuf buffer) {
            return new ResizeStorageMenuPayload(BlockPos.STREAM_CODEC.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), ByteBufCodecs.BOOL.decode(buffer));
        }
        @Override public void encode(ByteBuf buffer, ResizeStorageMenuPayload payload) {
            BlockPos.STREAM_CODEC.encode(buffer, payload.pos);
            ByteBufCodecs.VAR_INT.encode(buffer, payload.rows);
            ByteBufCodecs.BOOL.encode(buffer, payload.terminal);
        }
    };

    public static void handle(ResizeStorageMenuPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        int rows = Math.max(3, Math.min(6, payload.rows));
        if (player.distanceToSqr(payload.pos.getX() + .5, payload.pos.getY() + .5, payload.pos.getZ() + .5) > 64) return;
        if (payload.terminal) {
            if (!(player.containerMenu instanceof ItemNetworkTerminalMenu current)
                    || !current.getBlockPos().equals(payload.pos)
                    || !(player.level().getBlockEntity(payload.pos) instanceof ItemNetworkTerminalBlockEntity blockEntity)) return;
            player.openMenu(provider(
                    blockEntity.getDisplayName(),
                    (id, inventory, menuPlayer) -> new ItemNetworkTerminalMenu(id, inventory, blockEntity, payload.pos, rows)
            ), buffer -> { buffer.writeBlockPos(payload.pos); buffer.writeVarInt(rows); });
        } else {
            if (!(player.containerMenu instanceof AdvancedIndustrialStorageMenu current)
                    || !current.getBlockPos().equals(payload.pos)
                    || !(player.level().getBlockEntity(payload.pos) instanceof AdvancedIndustrialStorageBlockEntity blockEntity)) return;
            player.openMenu(provider(
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

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    @FunctionalInterface private interface Factory { AbstractContainerMenu create(int id, Inventory inventory, Player player); }
}
