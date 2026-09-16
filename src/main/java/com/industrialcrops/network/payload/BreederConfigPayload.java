package com.industrialcrops.network.payload;
import com.industrialcrops.screen.AnimalBreederMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record BreederConfigPayload(int containerId,int limit,boolean wide,boolean enabled,boolean harvest,boolean slaughter) {
    public static void encode(BreederConfigPayload p, FriendlyByteBuf b) { b.writeInt(p.containerId); b.writeInt(p.limit); b.writeBoolean(p.wide); b.writeBoolean(p.enabled); b.writeBoolean(p.harvest); b.writeBoolean(p.slaughter); }
    public static BreederConfigPayload decode(FriendlyByteBuf b) { return new BreederConfigPayload(b.readInt(), b.readInt(), b.readBoolean(), b.readBoolean(), b.readBoolean(), b.readBoolean()); }
    public static void handle(BreederConfigPayload p, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get(); context.setPacketHandled(true);
        var player = context.getSender(); if (player == null) return;
if(player.containerMenu instanceof AnimalBreederMenu m&&m.containerId==p.containerId)m.configure(player,p.limit,p.wide,p.enabled,p.harvest,p.slaughter);
    }

}
