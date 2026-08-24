package com.industrialcrops.mixin;

import com.industrialcrops.item.FusionIngotPickaxeItem;
import net.minecraft.core.BlockPos;
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

/** Applies one adaptive harvest enchantment to the copy used by block loot tables. */
@Mixin(Block.class)
public abstract class BlockFusionPickaxeLootMixin {
    @Inject(method = "playerDestroy", at = @At("HEAD"))
    private void industrialcrops$selectFusionHarvestMode(Level level, Player player, BlockPos pos,
            BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        FusionIngotPickaxeItem.prepareHarvestTool(tool, level, state);
    }
}
