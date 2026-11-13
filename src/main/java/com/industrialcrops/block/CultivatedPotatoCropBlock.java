package com.industrialcrops.block;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

/** A plantable cultivated potato with a fixed harvest range, independent of crop genetics and Fortune. */
public final class CultivatedPotatoCropBlock extends PotatoBlock {
    private final Supplier<? extends ItemLike> potato;

    public CultivatedPotatoCropBlock(Properties properties, Supplier<? extends ItemLike> potato) {
        super(properties);
        this.potato = potato;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return potato.get();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int count = isMaxAge(state) ? 2 + builder.getLevel().random.nextInt(3) : 1;
        return List.of(new ItemStack(potato.get().asItem(), count));
    }
}
