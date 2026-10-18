package com.industrialcrops.block;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

/** A plantable cultivated carrot with a fixed harvest range, independent of crop genetics and Fortune. */
public final class CultivatedCarrotCropBlock extends CarrotBlock {
    private final Supplier<? extends ItemLike> carrot;

    public CultivatedCarrotCropBlock(Properties properties, Supplier<? extends ItemLike> carrot) {
        super(properties);
        this.carrot = carrot;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return carrot.get();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int count = isMaxAge(state) ? 2 + builder.getLevel().random.nextInt(3) : 1;
        return List.of(new ItemStack(carrot.get().asItem(), count));
    }
}
