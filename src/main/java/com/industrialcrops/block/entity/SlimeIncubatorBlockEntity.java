package com.industrialcrops.block.entity;

import com.industrialcrops.item.IncubatorBlockItem;
import com.industrialcrops.registry.ModBlockEntities;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.machine.SpeedUpgradeHelper;
import com.industrialcrops.machine.MachineInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public final class SlimeIncubatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int MAX_PROGRESS = 160;
    /** A Flux Slime buds one copy of its Flux Ore Gel every 30 seconds (before speed components). */
    public static final int FLUX_PROGRESS = 600;
    public static final int UPGRADE_SLOT_START = 2, UPGRADE_SLOT_COUNT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == INPUT_SLOT ? 1 : super.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && isAcceptedSlimeInput(stack)
                    || slot >= UPGRADE_SLOT_START && SpeedUpgradeHelper.isSpeedUpgrade(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (slot == INPUT_SLOT) {
                activeSlimeType = getInputSlimeType(getStackInSlot(INPUT_SLOT));
                progress = 0;
            }
            setChanged();
        }
    };

    private int activeSlimeType = IncubatorBlockEntity.SLIME_NONE;
    private int progress;

    public SlimeIncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLIME_INCUBATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SlimeIncubatorBlockEntity slime_conversion_device) {
        if (level.isClientSide()) {
            return;
        }

        ItemStack input = slime_conversion_device.inventory.getStackInSlot(INPUT_SLOT);
        int slimeType = getInputSlimeType(input);
        if (slimeType == IncubatorBlockEntity.SLIME_NONE) {
            slime_conversion_device.resetProgress();
            return;
        }

        if (slime_conversion_device.activeSlimeType != slimeType) {
            slime_conversion_device.activeSlimeType = slimeType;
            slime_conversion_device.progress = 0;
            slime_conversion_device.setChanged();
        }

        ItemStack result = slimeType == IncubatorBlockEntity.SLIME_FLUX ? fluxGel(input) : getSlimeDrop(slimeType);
        if (result.isEmpty() || !canOutputAccept(slime_conversion_device.inventory.getStackInSlot(OUTPUT_SLOT), result)) {
            return;
        }

        int required = requiredProgress(slimeType);
        slime_conversion_device.progress += SpeedUpgradeHelper.progressStep(slime_conversion_device.inventory, UPGRADE_SLOT_START, UPGRADE_SLOT_COUNT, required);
        if (slime_conversion_device.progress >= required) {
            slime_conversion_device.insertOutput(result);
            slime_conversion_device.progress = 0;
        }
        slime_conversion_device.setChanged();
    }

    /** Transfer the one captured slime into the visible left-hand input slot on placement. */
    public boolean installCapturedSlime(ItemStack machine) {
        if (!inventory.getStackInSlot(INPUT_SLOT).isEmpty()) return false;
        ItemStack egg = capturedSlimeEgg(machine);
        if (egg.isEmpty()) return false;
        inventory.setStackInSlot(INPUT_SLOT, egg);
        return true;
    }

    public static boolean isCultivatableType(int type) {
        return type >= IncubatorBlockEntity.SLIME_VANILLA && type <= IncubatorBlockEntity.SLIME_GOLD
                || type == IncubatorBlockEntity.SLIME_FLUX;
    }

    public static ItemStack capturedSlimeEgg(ItemStack captured) {
        int type = IncubatorBlockItem.getStoredType(captured);
        ItemStack egg = switch (type) {
            case IncubatorBlockEntity.SLIME_VANILLA -> new ItemStack(Items.SLIME_SPAWN_EGG);
            case IncubatorBlockEntity.SLIME_COPPER -> new ItemStack(ModItems.BROWN_CREATE_SLIME_SPAWN_EGG.get());
            case IncubatorBlockEntity.SLIME_IRON -> new ItemStack(ModItems.GRAY_GEAR_SLIME_SPAWN_EGG.get());
            case IncubatorBlockEntity.SLIME_GOLD -> new ItemStack(ModItems.GOLDEN_REDSTONE_LAMP_SLIME_SPAWN_EGG.get());
            case IncubatorBlockEntity.SLIME_FLUX -> new ItemStack(ModItems.FLUX_SLIME_SPAWN_EGG.get());
            default -> ItemStack.EMPTY;
        };
        if (egg.isEmpty()) return egg;
        int size = IncubatorBlockItem.getStoredSize(captured);
        var ores = IncubatorBlockItem.getStoredOres(captured);
        IncubatorBlockItem.setStoredSlime(egg, type, size, ores);
        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getKey(IncubatorBlockEntity.getSlimeEntityType(type)).toString());
        entityData.putInt("Size", size - 1);
        if (type == IncubatorBlockEntity.SLIME_FLUX) {
            entityData.put(com.industrialcrops.machine.FluxOreIndex.TAG_ORES,
                    com.industrialcrops.machine.FluxOreIndex.writeIds(ores));
        }
        egg.getOrCreateTag().put("EntityTag", entityData);
        return egg;
    }

    public static int requiredProgress(int slimeType) {
        return slimeType == IncubatorBlockEntity.SLIME_FLUX ? FLUX_PROGRESS : MAX_PROGRESS;
    }

    /** Flux Ore Gel carrying the ore data of the captured Flux Slime; empty when it knows no metals. */
    public static ItemStack fluxGel(ItemStack input) {
        java.util.Set<net.minecraft.resources.ResourceLocation> ores = IncubatorBlockItem.getStoredOres(input);
        if (ores.isEmpty()) return ItemStack.EMPTY;
        ItemStack gel = new ItemStack(ModItems.FLUX_ORE_GEL.get());
        com.industrialcrops.util.ItemStackNbt.update(gel,
                tag -> tag.put(com.industrialcrops.machine.FluxOreIndex.TAG_ORES, com.industrialcrops.machine.FluxOreIndex.writeIds(ores)));
        return gel;
    }

    public static boolean isAcceptedSlimeInput(ItemStack stack) {
        return getInputSlimeType(stack) != IncubatorBlockEntity.SLIME_NONE;
    }

    public static int getInputSlimeType(ItemStack stack) {
        if (stack.isEmpty()) {
            return IncubatorBlockEntity.SLIME_NONE;
        }
        if (stack.is(ModItems.INCUBATOR.get())) {
            int storedType = IncubatorBlockItem.getStoredType(stack);
            return isCultivatableType(storedType) ? storedType : IncubatorBlockEntity.SLIME_NONE;
        }
        if (stack.is(ModItems.FLUX_SLIME_SPAWN_EGG.get())) {
            return IncubatorBlockEntity.SLIME_FLUX;
        }
        if (stack.is(Items.SLIME_SPAWN_EGG)) {
            return IncubatorBlockEntity.SLIME_VANILLA;
        }
        if (stack.is(ModItems.BROWN_CREATE_SLIME_SPAWN_EGG.get())) {
            return IncubatorBlockEntity.SLIME_COPPER;
        }
        if (stack.is(ModItems.GRAY_GEAR_SLIME_SPAWN_EGG.get())) {
            return IncubatorBlockEntity.SLIME_IRON;
        }
        if (stack.is(ModItems.GOLDEN_REDSTONE_LAMP_SLIME_SPAWN_EGG.get())) {
            return IncubatorBlockEntity.SLIME_GOLD;
        }
        return IncubatorBlockEntity.SLIME_NONE;
    }

    public static ItemStack getSlimeDrop(int slimeType) {
        return switch (slimeType) {
            case IncubatorBlockEntity.SLIME_VANILLA -> new ItemStack(Items.SLIME_BALL);
            case IncubatorBlockEntity.SLIME_COPPER -> new ItemStack(Items.COPPER_INGOT);
            case IncubatorBlockEntity.SLIME_IRON -> new ItemStack(Items.IRON_INGOT);
            case IncubatorBlockEntity.SLIME_GOLD -> new ItemStack(Items.GOLD_INGOT);
            default -> ItemStack.EMPTY;
        };
    }

    public int getProgress() {
        return progress;
    }

    public int getActiveSlimeType() {
        return activeSlimeType;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getSlots() {
        return inventory.getSlots();
    }

    public ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("ActiveSlimeType", activeSlimeType);
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
            MachineInventoryHelper.ensureSize(inventory, UPGRADE_SLOT_START + UPGRADE_SLOT_COUNT);
        }
        activeSlimeType = tag.getInt("ActiveSlimeType");
        progress = tag.getInt("Progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcrops.slime_cultivation_device");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.industrialcrops.screen.SlimeIncubatorMenu(containerId, inventory, this, worldPosition);
    }

    private void insertOutput(ItemStack result) {
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
            inventory.setStackInSlot(OUTPUT_SLOT, output);
        }
    }

    private void resetProgress() {
        if (progress != 0 || activeSlimeType != IncubatorBlockEntity.SLIME_NONE) {
            progress = 0;
            activeSlimeType = IncubatorBlockEntity.SLIME_NONE;
            setChanged();
        }
    }

    private static boolean canOutputAccept(ItemStack outputStack, ItemStack resultStack) {
        if (outputStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(outputStack, resultStack)) {
            return false;
        }
        return outputStack.getCount() + resultStack.getCount() <= outputStack.getMaxStackSize();
    }
}
