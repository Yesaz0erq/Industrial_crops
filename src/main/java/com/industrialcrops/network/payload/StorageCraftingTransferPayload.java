package com.industrialcrops.network.payload;

import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StorageCraftingTransferPayload(ResourceLocation recipeId) implements CustomPacketPayload {
    public static final Type<StorageCraftingTransferPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(IndustrialCrops.MOD_ID, "storage_crafting_transfer"));
    public static final StreamCodec<ByteBuf, StorageCraftingTransferPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256).map(ResourceLocation::parse, ResourceLocation::toString),
            StorageCraftingTransferPayload::recipeId,
            StorageCraftingTransferPayload::new);

    public static void handle(StorageCraftingTransferPayload payload, IPayloadContext context) {
        if (!(context.player().containerMenu instanceof AdvancedIndustrialStorageMenu menu)) return;
        context.player().level().getRecipeManager().byKey(payload.recipeId).ifPresent(holder -> {
            if (holder.value() instanceof CraftingRecipe recipe) menu.transferCraftingRecipe(recipe);
        });
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
