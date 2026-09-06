package com.industrialcrops.screen;

import com.industrialcrops.block.entity.CrystalSteelWorkbenchBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.recipe.CrystalWorkbenchRecipes;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.fluids.FluidStack;

/** Per-player crafting inputs follow vanilla workbench close/return behavior; the tank belongs to the block. */
public final class CrystalSteelWorkbenchMenu extends AbstractContainerMenu {
    public static final int GRID_SIZE = 5;
    public static final int INVENTORY_START = 26, HOTBAR_START = 53, END = 62;
    private final CraftingContainer inputs = new TransientCraftingContainer(this, GRID_SIZE, GRID_SIZE);
    private final ResultContainer result = new ResultContainer();
    private boolean consuming;
    private final Player player;
    private final CrystalSteelWorkbenchBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public CrystalSteelWorkbenchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, (CrystalSteelWorkbenchBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }
    public CrystalSteelWorkbenchMenu(int id, Inventory inventory, CrystalSteelWorkbenchBlockEntity entity) {
        super(ModMenus.CRYSTAL_STEEL_WORKBENCH.get(), id);
        this.player = inventory.player;
        this.blockEntity = entity;
        this.access = ContainerLevelAccess.create(player.level(), entity.getBlockPos());
        addSlot(new ResultSlot(player, inputs, result, 0, 138, 66) {
            @Override public boolean mayPickup(Player player) {
                var custom=CrystalWorkbenchRecipes.find(inputs);
                return super.mayPickup(player) && (custom==null || custom.hasFluid(fluid()));
            }
            @Override public void onTake(Player player, ItemStack stack) {
                var custom=CrystalWorkbenchRecipes.find(inputs);
                if(custom==null) { super.onTake(player,stack); return; }
                if(!custom.hasFluid(fluid())) return;
                checkTakeAchievements(stack);
                consuming=true;
                try {
                    blockEntity.getTank().drain(custom.fluid().getAmount(),FluidAction.EXECUTE);
                    for(int i=0;i<25;i++) if(!custom.inputs().get(i).isEmpty()) inputs.removeItem(i,1);
                } finally { consuming=false; }
                slotsChanged(inputs);
            }
        });
        for (int row = 0; row < GRID_SIZE; row++) for (int col = 0; col < GRID_SIZE; col++)
            addSlot(new Slot(inputs, col + row * GRID_SIZE, 14 + col * 18, 30 + row * 18));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 26 + col * 18, 150 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 26 + col * 18, 208));
    }
    public FluidStack fluid() { return blockEntity.getTank().getFluid(); }
    public int capacity() { return blockEntity.getTank().getCapacity(); }
    public CraftingContainer craftingInputs() { return inputs; }

    @Override public void slotsChanged(Container container) {
        if (consuming || !(player instanceof ServerPlayer serverPlayer)) return;
        CraftingInput input = inputs.asCraftInput();
        ItemStack output = ItemStack.EMPTY;
        var custom = CrystalWorkbenchRecipes.find(inputs);
        var recipe = player.level().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, player.level());
        if (custom != null) {
            result.setRecipeUsed(null);
            if(custom.hasFluid(fluid())) output=custom.output().copy();
        } else if (recipe.isPresent() && result.setRecipeUsed(player.level(), serverPlayer, recipe.get())) {
            ItemStack assembled = recipe.get().value().assemble(input, player.level().registryAccess());
            if (assembled.isItemEnabled(player.level().enabledFeatures())) output = assembled;
        }
        if (ItemStack.matches(result.getItem(0), output)) return;
        result.setItem(0, output);
        setRemoteSlot(0, output);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, output));
    }
    @Override public void broadcastChanges() {
        slotsChanged(inputs);
        super.broadcastChanges();
    }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        slotsChanged(inputs);
        super.clicked(slot, button, type, player);
    }
    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, inputs));
    }
    @Override public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && stillValid(access, player, ModBlocks.CRYSTAL_STEEL_WORKBENCH.get());
    }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != result && super.canTakeItemForPickAll(stack, slot);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        slotsChanged(inputs);
        Slot slot = slots.get(index);
        if (index == 0 && !slot.mayPickup(player)) return ItemStack.EMPTY;
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack moving = slot.getItem(), original = moving.copy();
        if (index == 0) {
            moving.getItem().onCraftedBy(moving, player.level(), player);
            if (!moveItemStackTo(moving, INVENTORY_START, END, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(moving, original);
        } else if (index < INVENTORY_START) {
            if (!moveItemStackTo(moving, INVENTORY_START, END, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(moving, 1, INVENTORY_START, false)) {
            if (index < HOTBAR_START) {
                if (!moveItemStackTo(moving, HOTBAR_START, END, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(moving, INVENTORY_START, HOTBAR_START, false)) return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        if (moving.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, moving);
        if (index == 0) player.drop(moving, false);
        return original;
    }
}
