package com.industrialcrops.network.payload;

import com.industrialcrops.screen.AnimalBreederMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BreederConfigPayload(int containerId,int limit,boolean wide,boolean enabled,boolean harvest,boolean slaughter) implements CustomPacketPayload {
    public static final Type<BreederConfigPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("industrialcrops","breeder_config"));
    public static final StreamCodec<ByteBuf,BreederConfigPayload> STREAM_CODEC=new StreamCodec<>(){
        public BreederConfigPayload decode(ByteBuf b){return new BreederConfigPayload(b.readInt(),b.readInt(),b.readBoolean(),b.readBoolean(),b.readBoolean(),b.readBoolean());}
        public void encode(ByteBuf b,BreederConfigPayload p){b.writeInt(p.containerId).writeInt(p.limit).writeBoolean(p.wide).writeBoolean(p.enabled).writeBoolean(p.harvest).writeBoolean(p.slaughter);}
    };
    public static void handle(BreederConfigPayload p,IPayloadContext ctx){if(ctx.player().containerMenu instanceof AnimalBreederMenu m&&m.containerId==p.containerId)m.configure(ctx.player(),p.limit,p.wide,p.enabled,p.harvest,p.slaughter);}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
