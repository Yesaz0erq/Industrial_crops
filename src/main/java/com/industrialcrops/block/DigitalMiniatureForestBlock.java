package com.industrialcrops.block;

import com.industrialcrops.block.entity.DigitalMiniatureForestBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class DigitalMiniatureForestBlock extends GoldPoweredMachineBlock {
    public static final MapCodec<DigitalMiniatureForestBlock> CODEC = simpleCodec(DigitalMiniatureForestBlock::new);
    // Match the model's 2-pixel base and 1-pixel glass walls/roof. The closed
    // exterior still blocks entry, but inset faces must not sample a solid
    // neighbour's light as they do with a full-cube collision shape.
    private static final VoxelShape COLLISION = Shapes.or(
            Block.box(0, 0, 0, 16, 2, 16),
            Block.box(0, 2, 0, 16, 16, 1),
            Block.box(0, 2, 15, 16, 16, 16),
            Block.box(0, 2, 1, 1, 16, 15),
            Block.box(15, 2, 1, 16, 16, 15),
            Block.box(1, 15, 1, 15, 16, 15)
    );

    public DigitalMiniatureForestBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends GoldPoweredMachineBlock> codec() { return CODEC; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }
    @Override protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
    @Override protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    @Override protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
    @Override protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DigitalMiniatureForestBlockEntity(pos, state); }
}
