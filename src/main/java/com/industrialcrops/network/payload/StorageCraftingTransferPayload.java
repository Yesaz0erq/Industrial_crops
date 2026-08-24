package com.industrialcrops.network.payload;

import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.network.NetworkEvent;

public record StorageCraftingTransferPayload(ResourceLocation recipeId) {
    public static void encode(StorageCraftingTransferPayload p, FriendlyByteBuf b) { b.writeResourceLocation(p.recipeId); }
    public static StorageCraftingTransferPayload decode(FriendlyByteBuf b) { return new StorageCraftingTransferPayload(b.readResourceLocation()); }
    public static void handle(StorageCraftingTransferPayload p, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null && player.containerMenu instanceof AdvancedIndustrialStorageMenu menu) {
            player.level().getRecipeManager().byKey(p.recipeId).ifPresent(recipe -> {
                if (recipe instanceof CraftingRecipe crafting) menu.transferCraftingRecipe(crafting);
            });
        }
        supplier.get().setPacketHandled(true);
    }
}
