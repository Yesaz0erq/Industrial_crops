package com.industrialcrops.mixin;

import com.industrialcrops.item.FusionIngotPickaxeItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies one adaptive harvest enchantment to the copy used by block loot tables. */
@Mixin(Block.class)
public abstract class BlockFusionPickaxeLootMixin {
    @Inject(method = "playerDestroy", at = @At("HEAD"))
    private void industrialcrops$selectFusionHarvestMode(Level level, Player player, BlockPos pos,
            BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        FusionIngotPickaxeItem.prepareHarvestTool(tool, level, state);
    }
    // Evaluate loot only once. Empty random loot is not a reason to create a second drop.
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true)
    private static void industrialcrops$naturalUnbreakableDrop(BlockState state, ServerLevel level,
            BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool,
            CallbackInfoReturnable<List<ItemStack>> callback) {
        if (tool.getItem() instanceof FusionIngotPickaxeItem
                && FusionIngotPickaxeItem.canBreak(state)
                && state.getDestroySpeed(level, pos) < 0.0F
                && callback.getReturnValue().isEmpty()
                && state.getBlock().asItem() != Items.AIR) {
            callback.setReturnValue(List.of(new ItemStack(state.getBlock().asItem())));
        }
    }
}
