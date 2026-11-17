package com.industrialcrops.machine;

import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.block.entity.ApControllerBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Uses the same AP blueprint as the controller and hologram, including all expansion tiers. */
public final class StructureBuilderPlan {
    public record Step(BlockPos pos, BlockState state, Item material, boolean door, boolean soil) {
        public boolean upgrade() { return state == null; }
    }
    public static final class Failure extends Exception {
        public final int status;
        public Failure(int status) { this.status = status; }
    }
    public final ApStructure.Layout layout;
    public final List<Step> steps;
    public final Map<Item, Integer> materials;

    private StructureBuilderPlan(ApStructure.Layout layout, List<Step> steps) {
        this.layout = layout;
        this.steps = List.copyOf(steps);
        this.materials = new LinkedHashMap<>();
        for (Step step : steps) materials.merge(step.material(), 1, Integer::sum);
    }

    public static Block controller(ApStructure.Type type) {
        return switch (type) {
            case FARM -> ModBlocks.AP_AGRICULTURE_CONTROLLER.get();
            case RANCH -> ModBlocks.AP_LIVESTOCK_CONTROLLER.get();
            case SLIME -> ModBlocks.AP_SLIME_FARM.get();
        };
    }

    public static Item expansion(int tier) {
        return switch (tier) {
            case 1 -> ModItems.AP_EXPANSION_COMPONENT_1.get();
            case 2 -> ModItems.AP_EXPANSION_COMPONENT_2.get();
            case 3 -> ModItems.AP_EXPANSION_COMPONENT_3.get();
            default -> Items.AIR;
        };
    }

    public static StructureBuilderPlan create(Level level, BlockPos builder, Direction facing,
                                               ApStructure.Type type, int tier) throws Failure {
        BlockPos core = builder.relative(facing.getOpposite(), 3).above(type == ApStructure.Type.RANCH ? 1 : 0);
        ApStructure.Layout layout = ApStructure.layout(core, facing, type, tier);
        List<Step> steps = new ArrayList<>();
        for (ApStructure.Part part : layout.parts()) {
            available(level, part.pos());
            if (part.requirement() == ApStructure.Requirement.DOOR_UPPER) continue;
            if (part.requirement() == ApStructure.Requirement.DOOR_LOWER) {
                available(level, part.pos().above());
                ApStructure.Part upper = new ApStructure.Part(part.pos().above(), part.example()
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), part.category(), ApStructure.Requirement.DOOR_UPPER);
                if (part.matches(level) && upper.matches(level)) continue;
                if (!replaceable(level, part.pos(), false) || !replaceable(level, part.pos().above(), false)) throw new Failure(4);
                BlockPos support = part.pos().below();
                available(level, support);
                if (!level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
                    if (!replaceable(level, support, false)) throw new Failure(4);
                    steps.add(new Step(support, Blocks.DIRT.defaultBlockState(), Items.DIRT, false, false));
                }
                steps.add(new Step(part.pos(), part.example(), Items.OAK_DOOR, true, false));
                continue;
            }
            if (part.matches(level)) continue;
            if (part.requirement() == ApStructure.Requirement.AIRSPACE
                || part.requirement() == ApStructure.Requirement.CROP_SPACE) throw new Failure(4);
            boolean soil = part.pos().getY() < core.getY() - (type == ApStructure.Type.RANCH ? 1 : 0);
            if (!replaceable(level, part.pos(), soil)) throw new Failure(4);
            Item material = part.example().getBlock().asItem();
            if (material == Items.AIR) throw new Failure(9);
            steps.add(new Step(part.pos(), part.example(), material, false, soil));
        }
        steps.sort(Comparator.comparingInt(step -> step.pos().getY()));
        // One sunken source supplies irrigation/drinking water without flooding the work plane.
        if (type != ApStructure.Type.SLIME && !layout.hasSourceWater(level)) {
            BlockPos water = layout.pos(2, -1, 2);
            available(level, water);
            if (!replaceable(level, water, true)) throw new Failure(4);
            // Require a contained hole: the builder never pours an uncontrolled waterfall into a void.
            for (Direction side : new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                BlockPos wall = water.relative(side);
                available(level, wall);
                if (!level.getBlockState(wall).isFaceSturdy(level, wall, side.getOpposite())) throw new Failure(4);
            }
            steps.add(new Step(water, Blocks.WATER.defaultBlockState(), Items.WATER_BUCKET, false, true));
        }
        available(level, core);
        BlockState state = controller(type).defaultBlockState().setValue(ApControllerBlock.FACING, facing);
        if (level.getBlockState(core).is(controller(type))) {
            if (level.getBlockState(core).getValue(ApControllerBlock.FACING) != facing) throw new Failure(4);
            if (!(level.getBlockEntity(core) instanceof ApControllerBlockEntity entity)) throw new Failure(9);
            if (entity.expansionTier() != 0 && entity.expansionTier() != tier) throw new Failure(4);
        } else {
            if (!replaceable(level, core, false)) throw new Failure(4);
            steps.add(new Step(core, state, controller(type).asItem(), false, false));
        }
        boolean installed = level.getBlockEntity(core) instanceof ApControllerBlockEntity entity && entity.expansionTier() == tier;
        if (tier > 0 && !installed) {
            if (level.getBlockEntity(core) instanceof ApControllerBlockEntity entity) {
                boolean room = false;
                for (int i = 0; i < entity.getUpgradeSlots().getSlots(); i++) room |= entity.getUpgradeSlots().getStackInSlot(i).isEmpty();
                if (!room) throw new Failure(4);
            }
            steps.add(new Step(core, null, expansion(tier), false, false));
        }
        steps.sort(Comparator.<Step>comparingInt(step -> step.pos().getX() >> 4)
            .thenComparingInt(step -> step.pos().getZ() >> 4).thenComparingInt(step -> step.pos().getY()));
        return new StructureBuilderPlan(layout, steps);
    }

    public static void available(Level level, BlockPos pos) throws Failure {
        if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)) throw new Failure(8);
        if (!level.hasChunkAt(pos)) throw new Failure(5);
    }

    public static boolean replaceable(Level level, BlockPos pos, boolean soil) {
        BlockState state = level.getBlockState(pos);
        return level.getBlockEntity(pos) == null && state.getFluidState().isEmpty()
            && (state.canBeReplaced() || soil && state.is(BlockTags.DIRT));
    }
}
