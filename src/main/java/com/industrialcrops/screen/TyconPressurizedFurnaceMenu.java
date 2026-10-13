package com.industrialcrops.screen;
import com.industrialcrops.block.entity.TyconPressurizedFurnaceBlockEntity;
import com.industrialcrops.recipe.TyconForgeRecipes;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
public final class TyconPressurizedFurnaceMenu extends AbstractContainerMenu {
   private static final int MACHINE_SLOTS = 5;
   public static final int MACHINE_X = 118;
   public static final int INPUT_X = 132;
   public static final int INPUT_Y = 36;
   public static final int OUTPUT_X = 221;
   public static final int OUTPUT_Y = 45;
   public static final int INVENTORY_X = 128;
   public static final int INVENTORY_Y = 112;
   public static final int HOTBAR_Y = 170;
   public static final int SELECT_RECIPE_BASE = 100;
   private final TyconPressurizedFurnaceBlockEntity furnace;
   private final BlockPos pos;
   private final ContainerData data;
   public TyconPressurizedFurnaceMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
      this(id, inventory, read(inventory, buffer));
   }
   private TyconPressurizedFurnaceMenu(int id, Inventory inventory, TyconPressurizedFurnaceBlockEntity furnace) {
      this(id, inventory, furnace, furnace.getBlockPos());
   }
   public TyconPressurizedFurnaceMenu(int id, Inventory playerInventory, TyconPressurizedFurnaceBlockEntity furnace, BlockPos pos) {
      super((MenuType)ModMenus.TYCON_PRESSURIZED_FURNACE.get(), id);
      this.furnace = furnace;
      this.pos = pos;
      this.data = furnace.getData();
      for (int i = 0; i < 4; i++) {
         this.addSlot(new SlotItemHandler(furnace.getInventory(), 0 + i, 132 + i % 2 * 18, 36 + i / 2 * 18));
      }
      this.addSlot(new SlotItemHandler(furnace.getInventory(), 4, 221, 45) {
         public boolean mayPlace(ItemStack stack) {
            return false;
         }
      });
      for (int row = 0; row < 3; row++) {
         for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 128 + column * 18, 112 + row * 18));
         }
      }
      for (int column = 0; column < 9; column++) {
         this.addSlot(new Slot(playerInventory, column, 128 + column * 18, 170));
      }
      this.addDataSlots(this.data);
   }
   public int energy() {
      return this.data.get(0) & 65535 | (this.data.get(1) & 65535) << 16;
   }
   public int progress() {
      return this.data.get(2);
   }
   public int maxProgress() {
      return this.data.get(3);
   }
   public int selectedRecipe() {
      return this.data.get(4);
   }
   public boolean hasHeatSource() {
      return this.data.get(5) != 0;
   }
   public TyconForgeRecipes.Recipe selectedRecipeData() {
      return TyconForgeRecipes.get(this.selectedRecipe());
   }
   public int inputCount(ItemStack item) {
      int total = 0;
      for (int i = 0; i < 4; i++) {
         ItemStack stack = ((Slot)this.slots.get(i)).getItem();
         if (ItemStack.isSameItem(stack, item)) {
            total += stack.getCount();
         }
      }
      return total;
   }
   public boolean clickMenuButton(Player player, int id) {
      if (id == 0) {
         this.furnace.selectRecipe(-1);
      } else if (id == 1) {
         this.furnace.selectRecipe(1);
      } else {
         if (id < 100 || id >= 100 + TyconForgeRecipes.count()) {
            return false;
         }
         this.furnace.selectRecipeIndex(id - 100);
      }
      this.broadcastChanges();
      return true;
   }
   public ItemStack quickMoveStack(Player player, int index) {
      if (index >= 0 && index < this.slots.size()) {
         Slot slot = (Slot)this.slots.get(index);
         if (!slot.hasItem()) {
            return ItemStack.EMPTY;
         } else {
            ItemStack stack = slot.getItem();
            ItemStack copy = stack.copy();
            if (index < 5) {
               if (!this.moveItemStackTo(stack, 5, this.slots.size(), true)) {
                  return ItemStack.EMPTY;
               }
            } else {
               if (!TyconPressurizedFurnaceBlockEntity.isSupportedIngredient(stack)) {
                  return ItemStack.EMPTY;
               }
               if (!this.moveItemStackTo(stack, 0, 4, false)) {
                  return ItemStack.EMPTY;
               }
            }
            if (stack.isEmpty()) {
               slot.set(ItemStack.EMPTY);
            } else {
               slot.setChanged();
            }
            return stack.getCount() == copy.getCount() ? ItemStack.EMPTY : copy;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }
   public boolean stillValid(Player player) {
      return stillValid(ContainerLevelAccess.create(player.level(), this.pos), player, (Block)ModBlocks.TYCON_PRESSURIZED_FURNACE.get());
   }
   private static TyconPressurizedFurnaceBlockEntity read(Inventory inventory, FriendlyByteBuf buffer) {
      BlockEntity blockEntity = inventory.player.level().getBlockEntity(buffer.readBlockPos());
      if (blockEntity instanceof TyconPressurizedFurnaceBlockEntity) {
         return (TyconPressurizedFurnaceBlockEntity)blockEntity;
      } else {
         throw new IllegalStateException("Missing Tycon pressurized furnace");
      }
   }
}
