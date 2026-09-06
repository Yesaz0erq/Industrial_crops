package com.industrialcrops.recipe;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.registry.ModFluids;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.List;

/** Shared by server crafting and JEI so the displayed and consumed inputs stay identical. */
public final class CrystalWorkbenchRecipes {
    public record Recipe(List<ItemStack> inputs, ItemStack output, FluidStack fluid) {
        public boolean matchesItems(Container grid) {
            if (grid.getContainerSize()!=25) return false;
            for(int i=0;i<25;i++) {
                ItemStack expected=inputs.get(i), actual=grid.getItem(i);
                if(expected.isEmpty() ? !actual.isEmpty() : !actual.is(expected.getItem())) return false;
            }
            return true;
        }
        public boolean hasFluid(FluidStack stored) {
            return FluidStack.isSameFluidSameComponents(stored, fluid) && stored.getAmount()>=fluid.getAmount();
        }
    }
    private static final List<Recipe> RECIPES = List.of(wireless());
    private static Recipe wireless() {
        String pattern=" C C " + "C E C" + " C C " + " IBI " + "IIDII";
        var ingredients=new java.util.ArrayList<ItemStack>();
        for(char key:pattern.toCharArray()) ingredients.add(switch(key) {
            case 'C' -> new ItemStack(ModItems.COMET_FRUIT.get());
            case 'E' -> new ItemStack(ModItems.ENERGY_CRYSTAL.get());
            case 'I' -> new ItemStack(ModItems.CRYSTAL_INGOT.get());
            case 'B' -> new ItemStack(Items.BEACON);
            case 'D' -> new ItemStack(ModItems.AUTOMATIC_COMPONENT.get());
            default -> ItemStack.EMPTY;
        });
        return new Recipe(List.copyOf(ingredients), new ItemStack(ModItems.WIRELESS_ENERGY_TRANSMITTER.get()),
                new FluidStack(ModFluids.CONCENTRATED_PLASMA_JUICE.get(),1000));
    }
    public static List<Recipe> all() { return RECIPES; }
    public static Recipe find(Container grid) { return RECIPES.stream().filter(r->r.matchesItems(grid)).findFirst().orElse(null); }
}
