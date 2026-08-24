package com.industrialcrops.network.payload;

import com.industrialcrops.screen.MatterMachineMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record MatterMachineSelectionPayload(int containerId, int visibleIndex, boolean operate) {
    public static void encode(MatterMachineSelectionPayload p, FriendlyByteBuf b) {
        b.writeVarInt(p.containerId); b.writeVarInt(p.visibleIndex); b.writeBoolean(p.operate);
    }
    public static MatterMachineSelectionPayload decode(FriendlyByteBuf b) {
        return new MatterMachineSelectionPayload(b.readVarInt(), b.readVarInt(), b.readBoolean());
    }
    public static void handle(MatterMachineSelectionPayload p, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null && player.containerMenu instanceof MatterMachineMenu menu
                && menu.containerId == p.containerId) {
            if (p.operate) menu.operateVisibleSlot(p.visibleIndex, player);
            else menu.selectVisibleSlot(p.visibleIndex);
        }
        supplier.get().setPacketHandled(true);
    }
}
