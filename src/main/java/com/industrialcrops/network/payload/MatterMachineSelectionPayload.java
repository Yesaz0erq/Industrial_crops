package com.industrialcrops.network.payload;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.screen.MatterMachineMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Reliable C2S selection for the virtual terminal slots shown by matter machines. */
public record MatterMachineSelectionPayload(int containerId, int visibleIndex, boolean operate) implements CustomPacketPayload {
    public static final Type<MatterMachineSelectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "matter_machine_selection"));
    public static final StreamCodec<ByteBuf, MatterMachineSelectionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MatterMachineSelectionPayload::containerId,
            ByteBufCodecs.VAR_INT, MatterMachineSelectionPayload::visibleIndex,
            ByteBufCodecs.BOOL, MatterMachineSelectionPayload::operate,
            MatterMachineSelectionPayload::new);

    public static void handle(MatterMachineSelectionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof MatterMachineMenu menu
                    && menu.containerId == payload.containerId) {
                if (payload.operate) menu.operateVisibleSlot(payload.visibleIndex, context.player());
                else menu.selectVisibleSlot(payload.visibleIndex);
            }
        });
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
