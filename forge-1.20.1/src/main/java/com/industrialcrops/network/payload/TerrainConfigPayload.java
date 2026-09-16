package com.industrialcrops.network.payload;
import com.industrialcrops.screen.TerrainProcessorMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record TerrainConfigPayload(int containerId,int chunkX,int chunkZ,int startY,int endY,boolean fill,boolean replaceOnly,int action) {
    public static void encode(TerrainConfigPayload p, FriendlyByteBuf b) { b.writeInt(p.containerId); b.writeInt(p.chunkX); b.writeInt(p.chunkZ); b.writeInt(p.startY); b.writeInt(p.endY); b.writeBoolean(p.fill); b.writeBoolean(p.replaceOnly); b.writeInt(p.action); }
    public static TerrainConfigPayload decode(FriendlyByteBuf b) { return new TerrainConfigPayload(b.readInt(), b.readInt(), b.readInt(), b.readInt(), b.readInt(), b.readBoolean(), b.readBoolean(), b.readInt()); }
    public static void handle(TerrainConfigPayload payload, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get(); context.setPacketHandled(true);
        var player = context.getSender(); if (player == null) return;

        if(player.containerMenu instanceof TerrainProcessorMenu menu&&menu.containerId==payload.containerId)
            menu.configure(player,payload.chunkX,payload.chunkZ,payload.startY,payload.endY,payload.fill,payload.replaceOnly,payload.action);

    }

}
