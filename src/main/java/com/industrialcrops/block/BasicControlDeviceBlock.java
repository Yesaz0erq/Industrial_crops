package com.industrialcrops.block;

import com.industrialcrops.block.entity.BasicControlDeviceBlockEntity;
import com.industrialcrops.machine.DimensionUpgradeHelper;
import com.industrialcrops.screen.ReinforcedControlDeviceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class BasicControlDeviceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public BasicControlDeviceBlock(Properties properties) {
        super(properties);
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasicControlDeviceBlockEntity(pos, state);
    }

    @Override public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
@Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack held = player.getItemInHand(hand);
        if (DimensionUpgradeHelper.isDimensionUpgrade(held)) {
            if (!(level.getBlockEntity(pos) instanceof BasicControlDeviceBlockEntity controller)) return InteractionResult.FAIL;
            if (!level.isClientSide() && controller.installDimensionUpgrade(held) && !player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof BasicControlDeviceBlockEntity controller) {
            if (!level.isClientSide()) {
                ItemStack removed = controller.removeDimensionUpgrade();
                if (!removed.isEmpty() && !player.getInventory().add(removed)) popResource(level, pos, removed);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide()) {
            net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player,new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.industrialcrops.basic_control_device");
                }

                @Override
                public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                    // Use the paginated storage controller interface.  The
                    // old two-crop button menu is kept registered for
                    // compatibility, but the placed basic controller now
                    // exposes every attached storage array page.
                    return new ReinforcedControlDeviceMenu(containerId, playerInventory, pos);
                }
            }, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BasicControlDeviceBlockEntity controller) {
            controller.releaseTicket();
            if (!level.isClientSide()) {
                ItemStack upgrade = controller.removeDimensionUpgrade();
                if (!upgrade.isEmpty()) popResource(level, pos, upgrade);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
