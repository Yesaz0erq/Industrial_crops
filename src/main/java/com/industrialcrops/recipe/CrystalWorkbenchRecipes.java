package com.industrialcrops.recipe;
import com.industrialcrops.registry.ModItems;
import com.industrialcrops.registry.ModFluids;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraftforge.fluids.FluidStack;
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
            return fluid.isEmpty() || stored.isFluidEqual(fluid) && stored.getAmount()>=fluid.getAmount();
        }
    }
    private static final List<Recipe> RECIPES = List.of(wireless(), logistics(), processor(), terrain(), breeder());
    private static Recipe wireless() {
        String pattern=" C C " + "C E C" + " C C " + " IBI " + "IIDII";
        var ingredients=new java.util.ArrayList<ItemStack>();
        for(char key:pattern.toCharArray()) ingredients.add(switch(key) {
            case 'C' -> new ItemStack(ModItems.COMET_FRUIT.get());
            case 'E' -> new ItemStack(Items.ENDER_PEARL);
            case 'I' -> new ItemStack(ModItems.CRYSTAL_INGOT.get());
            case 'B' -> new ItemStack(Items.BEACON);
            case 'D' -> new ItemStack(ModItems.AUTOMATIC_COMPONENT.get());
            default -> ItemStack.EMPTY;
        });
        return new Recipe(List.copyOf(ingredients), new ItemStack(ModItems.WIRELESS_ENERGY_TRANSMITTER.get()),
                new FluidStack(ModFluids.CONCENTRATED_PLASMA_JUICE.get(),1000));
    }
    private static Recipe logistics() {
        return dry("     " + " IEI " + " ECE " + " IEI " + "IIKII", ModItems.CRYSTAL_LOGISTICS.get());
    }
    private static Recipe breeder() {
        return dry(" III " + "ISSSI" + "IKOKI" + "IRGRI" + " III ", ModItems.ANIMAL_BREEDER.get());
    }
    private static Recipe terrain() {
        return dry(" III " + "ISDSI" + "IRPRI" + "IKCKI" + " III ", ModItems.TERRAIN_PROCESSOR.get());
    }
    private static Recipe processor() {
        return dry("IIIII" + "IKWKI" + "IKHKI" + "IKCKI" + "IRSRI", ModItems.CRAFTING_PROCESSOR.get());
    }
    private static Recipe dry(String pattern, Item result) {
        var inputs=new java.util.ArrayList<ItemStack>();
        for(char key:pattern.toCharArray()) inputs.add(switch(key) {
            case 'I' -> new ItemStack(ModItems.CRYSTAL_INGOT.get());
            case 'C' -> new ItemStack(ModItems.CRYSTAL_STEEL_DEVICE_CASING.get());
            case 'K' -> new ItemStack(ModItems.COMET_FRUIT.get());
            case 'P' -> new ItemStack(ModItems.COMPONENT_SUBSTRATE.get());
            case 'G' -> new ItemStack(ModItems.GUIDANCE_COMPONENT.get());
            case 'D' -> new ItemStack(Items.DIAMOND_PICKAXE);
            case 'W' -> new ItemStack(Items.CRAFTING_TABLE);
            case 'E' -> new ItemStack(Items.ENDER_PEARL);
            case 'H' -> new ItemStack(Items.HOPPER);
            case 'R' -> new ItemStack(Items.REPEATER);
            case 'S' -> new ItemStack(Items.REDSTONE);
            case 'O' -> new ItemStack(Items.DROPPER);
            default -> ItemStack.EMPTY;
        });
        return new Recipe(List.copyOf(inputs),new ItemStack(result),FluidStack.EMPTY);
    }
    public static List<Recipe> all() { return RECIPES; }
    public static Recipe find(Container grid) { return RECIPES.stream().filter(r->r.matchesItems(grid)).findFirst().orElse(null); }
}
