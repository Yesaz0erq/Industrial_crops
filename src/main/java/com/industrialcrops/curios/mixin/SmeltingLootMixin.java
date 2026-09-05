package com.industrialcrops.curios.mixin;

import com.industrialcrops.curios.CarroteCuriosEffects;
import com.industrialcrops.curios.CarroteCuriosItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public abstract class SmeltingLootMixin {
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void carroteCurios$smelt(BlockState state, ServerLevel level, BlockPos pos,
            BlockEntity blockEntity, Entity breaker, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!(breaker instanceof Player player) || !state.is(Tags.Blocks.ORES)
                || !CarroteCuriosEffects.has(player, CarroteCuriosItems.SMELTING)) return;
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack input : cir.getReturnValue()) {
            var inventory = new SimpleContainer(input);
            var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, inventory, level);
            ItemStack output = recipe.map(value -> value.assemble(inventory, level.registryAccess())).orElse(ItemStack.EMPTY);
            if (output.isEmpty()) {
                drops.add(input);
                continue;
            }
            int remaining = input.getCount() * output.getCount();
            while (remaining > 0) {
                int count = Math.min(remaining, output.getMaxStackSize());
                drops.add(output.copyWithCount(count));
                remaining -= count;
            }
        }
        cir.setReturnValue(drops);
    }
}
