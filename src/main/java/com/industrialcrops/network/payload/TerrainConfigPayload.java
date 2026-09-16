package com.industrialcrops.network.payload;

import com.industrialcrops.screen.TerrainProcessorMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TerrainConfigPayload(int containerId,int chunkX,int chunkZ,int startY,int endY,boolean fill,boolean replaceOnly,int action) implements CustomPacketPayload {
    public static final Type<TerrainConfigPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("industrialcrops","terrain_config"));
    public static final StreamCodec<ByteBuf,TerrainConfigPayload> STREAM_CODEC=new StreamCodec<>() {
        @Override public TerrainConfigPayload decode(ByteBuf b){return new TerrainConfigPayload(b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readBoolean(),b.readBoolean(),b.readInt());}
        @Override public void encode(ByteBuf b,TerrainConfigPayload p){b.writeInt(p.containerId);b.writeInt(p.chunkX);b.writeInt(p.chunkZ);b.writeInt(p.startY);b.writeInt(p.endY);b.writeBoolean(p.fill);b.writeBoolean(p.replaceOnly);b.writeInt(p.action);}
    };
    public static void handle(TerrainConfigPayload payload,IPayloadContext context) {
        if(context.player().containerMenu instanceof TerrainProcessorMenu menu&&menu.containerId==payload.containerId)
            menu.configure(context.player(),payload.chunkX,payload.chunkZ,payload.startY,payload.endY,payload.fill,payload.replaceOnly,payload.action);
    }
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
