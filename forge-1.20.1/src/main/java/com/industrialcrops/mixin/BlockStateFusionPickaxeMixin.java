package com.industrialcrops.mixin;

import com.industrialcrops.item.FusionIngotPickaxeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateFusionPickaxeMixin {
    @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
    private void industrialcrops$allowNaturalUnbreakableBlocks(
            Player player,
            BlockGetter level,
            BlockPos pos,
            CallbackInfoReturnable<Float> callback
    ) {
        BlockState state = (BlockState) (Object) this;
        if (!(player.getMainHandItem().getItem() instanceof FusionIngotPickaxeItem)) {
            return;
        }

        FusionIngotPickaxeItem.ensureEnchantments(player.getMainHandItem(), player.level());
        if (state.getDestroySpeed(level, pos) == -1.0F
                && FusionIngotPickaxeItem.canBreak(state)) {
            callback.setReturnValue(0.1F);
        }
    }
}
