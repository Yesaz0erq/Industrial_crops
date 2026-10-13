package com.industrialcrops.screen;

import com.industrialcrops.block.entity.CrystalSteelWorkbenchBlockEntity;
import com.industrialcrops.recipe.CrystalWorkbenchRecipes;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public final class CrystalSteelWorkbenchMenu extends AbstractContainerMenu {
   public static final int GRID_SIZE = 5;
   public static final int INVENTORY_START = 26;
   public static final int HOTBAR_START = 53;
   public static final int END = 62;
   private final CraftingContainer inputs = new TransientCraftingContainer(this, 5, 5);
   private final ResultContainer result = new ResultContainer();
   private boolean consuming;
   private final Player player;
   private final CrystalSteelWorkbenchBlockEntity blockEntity;
   private final ContainerLevelAccess access;

   public CrystalSteelWorkbenchMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
      this(id, inventory, (CrystalSteelWorkbenchBlockEntity)inventory.player.level().getBlockEntity(buffer.readBlockPos()));
   }

   public CrystalSteelWorkbenchMenu(int id, Inventory inventory, CrystalSteelWorkbenchBlockEntity entity) {
      super((MenuType)ModMenus.CRYSTAL_STEEL_WORKBENCH.get(), id);
      this.player = inventory.player;
      this.blockEntity = entity;
      this.access = ContainerLevelAccess.create(this.player.level(), entity.getBlockPos());
      this.addSlot(new ResultSlot(this.player, this.inputs, this.result, 0, 138, 66) {
         public boolean mayPickup(Player player) {
            CrystalWorkbenchRecipes.Recipe custom = CrystalWorkbenchRecipes.find(CrystalSteelWorkbenchMenu.this.inputs);
            return super.mayPickup(player) && (custom == null || custom.hasFluid(CrystalSteelWorkbenchMenu.this.fluid()));
         }

         public void onTake(Player player, ItemStack stack) {
            CrystalWorkbenchRecipes.Recipe custom = CrystalWorkbenchRecipes.find(CrystalSteelWorkbenchMenu.this.inputs);
            if (custom == null) {
               super.onTake(player, stack);
            } else if (custom.hasFluid(CrystalSteelWorkbenchMenu.this.fluid())) {
               this.checkTakeAchievements(stack);
               CrystalSteelWorkbenchMenu.this.consuming = true;

               try {
                  CrystalSteelWorkbenchMenu.this.blockEntity.getTank().drain(custom.fluid().getAmount(), FluidAction.EXECUTE);

                  for (int i = 0; i < 25; i++) {
                     if (!custom.inputs().get(i).isEmpty()) {
                        CrystalSteelWorkbenchMenu.this.inputs.removeItem(i, 1);
                     }
                  }
               } finally {
                  CrystalSteelWorkbenchMenu.this.consuming = false;
               }

               CrystalSteelWorkbenchMenu.this.slotsChanged(CrystalSteelWorkbenchMenu.this.inputs);
            }
         }
      });

      for (int row = 0; row < 5; row++) {
         for (int col = 0; col < 5; col++) {
            this.addSlot(new Slot(this.inputs, col + row * 5, 14 + col * 18, 30 + row * 18));
         }
      }

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, 26 + col * 18, 150 + row * 18));
         }
      }

      for (int col = 0; col < 9; col++) {
         this.addSlot(new Slot(inventory, col, 26 + col * 18, 208));
      }
   }

   public FluidStack fluid() {
      return this.blockEntity.getTank().getFluid();
   }

   public int capacity() {
      return this.blockEntity.getTank().getCapacity();
   }

   public CraftingContainer craftingInputs() {
      return this.inputs;
   }

   public void slotsChanged(Container container) {
      if (!this.consuming && this.player instanceof ServerPlayer serverPlayer) {
         CraftingInput var8 = this.inputs.asCraftInput();
         ItemStack output = ItemStack.EMPTY;
         CrystalWorkbenchRecipes.Recipe custom = CrystalWorkbenchRecipes.find(this.inputs);
         Optional recipe = this.player.level().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, var8, this.player.level());
         if (custom != null) {
            this.result.setRecipeUsed(null);
            if (custom.hasFluid(this.fluid())) {
               output = CrystalWorkbenchRecipes.assemble(custom, this.inputs);
            }
         } else if (recipe.isPresent() && this.result.setRecipeUsed(this.player.level(), serverPlayer, (RecipeHolder)recipe.get())) {
            ItemStack assembled = ((CraftingRecipe)((RecipeHolder)recipe.get()).value()).assemble(var8, this.player.level().registryAccess());
            if (assembled.isItemEnabled(this.player.level().enabledFeatures())) {
               output = assembled;
            }
         }

         if (!ItemStack.matches(this.result.getItem(0), output)) {
            this.result.setItem(0, output);
            this.setRemoteSlot(0, output);
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, output));
         }
      }
   }

   public void broadcastChanges() {
      this.slotsChanged(this.inputs);
      super.broadcastChanges();
   }

   public void clicked(int slot, int button, ClickType type, Player player) {
      this.slotsChanged(this.inputs);
      super.clicked(slot, button, type, player);
   }

   public void removed(Player player) {
      super.removed(player);
      this.access.execute((level, pos) -> this.clearContainer(player, this.inputs));
   }

   public boolean stillValid(Player player) {
      return !this.blockEntity.isRemoved() && stillValid(this.access, player, (Block)ModBlocks.CRYSTAL_STEEL_WORKBENCH.get());
   }

   public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
      return slot.container != this.result && super.canTakeItemForPickAll(stack, slot);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      if (index >= 0 && index < this.slots.size()) {
         this.slotsChanged(this.inputs);
         Slot slot = (Slot)this.slots.get(index);
         if (index == 0 && !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
         } else if (!slot.hasItem()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack moving = slot.getItem();
            ItemStack original = moving.copy();
            if (index == 0) {
               moving.getItem().onCraftedBy(moving, player.level(), player);
               if (!this.moveItemStackTo(moving, 26, 62, true)) {
                  return ItemStack.EMPTY;
               }

               slot.onQuickCraft(moving, original);
            } else if (index < 26) {
               if (!this.moveItemStackTo(moving, 26, 62, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(moving, 1, 26, false)) {
               if (index < 53) {
                  if (!this.moveItemStackTo(moving, 53, 62, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.moveItemStackTo(moving, 26, 53, false)) {
                  return ItemStack.EMPTY;
               }
            }

            if (moving.isEmpty()) {
               slot.setByPlayer(ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }

            if (moving.getCount() == original.getCount()) {
               return ItemStack.EMPTY;
            } else {
               slot.onTake(player, moving);
               if (index == 0) {
                  player.drop(moving, false);
               }

               return original;
            }
         }
      } else {
         return ItemStack.EMPTY;
      }
   }
}
