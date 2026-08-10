package com.industrialcrops.item;

import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.block.entity.AdvancedIndustrialStorageBlockEntity;
import com.industrialcrops.screen.ReinforcedControlDeviceMenu;
import com.industrialcrops.screen.AdvancedIndustrialStorageMenu;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** A handheld link terminal for accessing a loaded control device from a distance. */
public final class RemoteAccessDeviceItem extends Item {
    private static final String DIMENSION_TAG = "RemoteDimension";
    private static final String POS_TAG = "RemotePos";
    private static final String KIND_TAG = "RemoteKind";
    private static final String BASIC_KIND = "basic";
    private static final String REINFORCED_KIND = "reinforced";

    public RemoteAccessDeviceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        String kind = controlKind(state);
        if (kind == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(DIMENSION_TAG, level.dimension().location().toString());
            tag.putLong(POS_TAG, pos.asLong());
            tag.putString(KIND_TAG, kind);
        });
        Player player = context.getPlayer();
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.industrialcrops.remote_access.bound"), true);
        }
        level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.7F, 1.25F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Binding binding = readBinding(stack);
        if (binding == null) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.industrialcrops.remote_access.unbound"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (!level.dimension().location().equals(binding.dimension())) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.industrialcrops.remote_access.wrong_dimension"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.hasChunkAt(binding.pos())) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.industrialcrops.remote_access.unloaded"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        BlockState state = level.getBlockState(binding.pos());
        if (!binding.kind().equals(controlKind(state))) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.industrialcrops.remote_access.invalid"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide()) {
            openRemoteMenu(player, binding);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static void openRemoteMenu(Player player, Binding binding) {
        BlockPos pos = binding.pos();
        if (BASIC_KIND.equals(binding.kind())) {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.industrialcrops.basic_control_device");
                }

                @Override
                public @Nullable net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                        int id, Inventory inventory, Player menuPlayer) {
                    return new ReinforcedControlDeviceMenu(id, inventory, pos, true);
                }
            }, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeBoolean(true);
            });
        } else {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.industrialcrops.reinforced_control_device");
                }

                @Override
                public @Nullable net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                        int id, Inventory inventory, Player menuPlayer) {
                    if (!(player.level().getBlockEntity(pos) instanceof AdvancedIndustrialStorageBlockEntity storage)) {
                        return null;
                    }
                    return new AdvancedIndustrialStorageMenu(id, inventory, storage, pos,
                            AdvancedIndustrialStorageMenu.MAX_ROWS, true);
                }
            }, buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeVarInt(AdvancedIndustrialStorageMenu.MAX_ROWS);
                buffer.writeBoolean(true);
            });
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        Binding binding = readBinding(stack);
        if (binding == null) {
            tooltip.add(Component.translatable("tooltip.industrialcrops.remote_access.unbound"));
        } else {
            tooltip.add(Component.translatable("tooltip.industrialcrops.remote_access.bound"));
        }
    }

    /** Returns the bound controller position for the client tooltip overlay. */
    public static @Nullable BlockPos getBoundPosition(ItemStack stack) {
        Binding binding = readBinding(stack);
        return binding == null ? null : binding.pos();
    }

    private static @Nullable String controlKind(BlockState state) {
        if (state.is(ModBlocks.CARROT_CONTROL_DEVICE.get())) return BASIC_KIND;
        if (state.is(ModBlocks.REINFORCED_CONTROL_DEVICE.get())) return REINFORCED_KIND;
        return null;
    }

    private static @Nullable Binding readBinding(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag();
        String dimension = tag.getString(DIMENSION_TAG);
        String kind = tag.getString(KIND_TAG);
        if (dimension.isEmpty() || !tag.contains(POS_TAG, Tag.TAG_LONG) ||
                (!BASIC_KIND.equals(kind) && !REINFORCED_KIND.equals(kind))) {
            return null;
        }
        ResourceLocation location = ResourceLocation.tryParse(dimension);
        return location == null ? null : new Binding(location, BlockPos.of(tag.getLong(POS_TAG)), kind);
    }

    private record Binding(ResourceLocation dimension, BlockPos pos, String kind) {}
}
