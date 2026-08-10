package com.industrialcrops.block;

import com.industrialcrops.block.entity.CropGeneticsBlockEntity;
import com.industrialcrops.crop.CropGenetics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class IndustrialFruitBlock extends Block implements EntityBlock {
    public IndustrialFruitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockPos pos = BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN));
        CropGenetics.Genes genes = null;
        BlockEntity droppedBlockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (droppedBlockEntity instanceof CropGeneticsBlockEntity genetics) {
            genes = genetics.getOrCreateGenes(builder.getLevel().random);
        } else if (builder.getLevel().getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics) {
            genes = genetics.getOrCreateGenes(builder.getLevel().random);
        }

        double multiplier = genes == null ? 1.0D : genes.dominantQuality().yieldMultiplier();
        int minimum = (int) Math.round(4 * multiplier);
        int maximum = (int) Math.round(6 * multiplier);
        int count = minimum + builder.getLevel().random.nextInt(maximum - minimum + 1);
        ItemStack result = new ItemStack(asItem(), count);
        if (genes != null) {
            CropGenetics.write(result, genes);
        }
        return List.of(result);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics) {
            genetics.initialize(CropGenetics.read(stack), level.random);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack result = new ItemStack(asItem());
        if (level.getBlockEntity(pos) instanceof CropGeneticsBlockEntity genetics
                && genetics.isInitialized()) {
            CropGenetics.write(result, genetics.getGenes());
        }
        return result;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CropGeneticsBlockEntity(pos, state);
    }
}
