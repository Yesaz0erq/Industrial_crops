package com.industrialcrops.network.payload;
import com.industrialcrops.IndustrialCrops;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record StorageCraftingTransferPayload(ResourceLocation recipeId) {
    public static void encode(StorageCraftingTransferPayload p, FriendlyByteBuf b) { b.writeResourceLocation(p.recipeId); }
    public static StorageCraftingTransferPayload decode(FriendlyByteBuf b) { return new StorageCraftingTransferPayload(b.readResourceLocation()); }
    public static void handle(StorageCraftingTransferPayload payload, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get(); context.setPacketHandled(true);
        var player = context.getSender(); if (player == null) return;

        if (player.containerMenu instanceof com.industrialcrops.screen.CraftingProcessorMenu processor) {
            if (!processor.stillValid(player)) return;
            player.level().getRecipeManager().byKey(payload.recipeId).ifPresent(holder -> {
                if (holder instanceof CraftingRecipe recipe && !recipe.isSpecial() && recipe.canCraftInDimensions(5,5))
                    processor.machine().selectTarget(recipe.getResultItem(player.level().registryAccess()));
            });
            return;
        }
        if (!(player.containerMenu instanceof AdvancedIndustrialStorageMenu menu) || !menu.stillValid(player)) return;
        player.level().getRecipeManager().byKey(payload.recipeId).ifPresent(holder -> {
            if (holder instanceof CraftingRecipe recipe) menu.transferCraftingRecipe(recipe);
        });

    }


}
