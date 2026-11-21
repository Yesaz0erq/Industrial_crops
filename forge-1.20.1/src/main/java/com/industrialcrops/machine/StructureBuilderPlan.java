package com.industrialcrops.machine;

import com.industrialcrops.block.ApControllerBlock;
import com.industrialcrops.block.entity.ApControllerBlockEntity;
import com.industrialcrops.registry.ModBlocks;
import com.industrialcrops.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Plans only missing parts of the same AP blueprint used by the controller and hologram. */
public final class StructureBuilderPlan {
    public record Target(BlockPos core, Direction facing, ApStructure.Type type, int tier,
                         boolean adjacent, int installedTier) {}

    public record Step(BlockPos pos, BlockState state, Item material, ApStructure.Part part) {
        public boolean upgrade() { return state == null; }
        public boolean door() { return part != null && part.requirement() == ApStructure.Requirement.DOOR_LOWER; }
        public boolean clearance() { return !upgrade() && state.isAir(); }

        public boolean satisfied(Level level) {
            if (upgrade()) return false;
            if (part != null) {
                if (!part.matches(level)) return false;
                if (!door()) return true;
                BlockState upper = level.getBlockState(pos.above());
                return upper.is(BlockTags.WOODEN_DOORS) && upper.hasProperty(DoorBlock.HALF)
                    && upper.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
            }
            if (material == Items.DIRT) return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP);
            return level.getBlockState(pos).equals(state);
        }
    }
    public static final class Failure extends Exception {
        public final int status;
        public Failure(int status) { this.status = status; }
    }
    public final ApStructure.Layout layout;
    public final List<Step> steps;
    public final Map<Item, Integer> materials;
    public final int demolitionCount;

    private StructureBuilderPlan(ApStructure.Layout layout, List<Step> steps, int demolitionCount) {
        this.layout = layout;
        this.demolitionCount = demolitionCount;
        this.steps = List.copyOf(steps);
        this.materials = new LinkedHashMap<>();
        for (Step step : steps) {
            if (!step.clearance()) materials.merge(step.material(), 1, Integer::sum);
        }
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

    public static Target detect(Level level, BlockPos builder, Direction facing, ApStructure.Type selected, int tier)
        throws Failure {
        Target target = null;
        for (Direction side : Direction.values()) {
            BlockPos neighbor = builder.relative(side);
            if (!level.hasChunkAt(neighbor)) throw new Failure(5);
            if (level.getBlockEntity(neighbor) instanceof ApControllerBlockEntity controller) {
                if (target != null) throw new Failure(10);
                int installed = controller.expansionTier();
                target = new Target(neighbor, controller.facing(), controller.structureType(),
                    installed == 0 ? tier : installed, true, installed);
            }
        }
        if (target != null) return target;
        BlockPos core = builder.relative(facing.getOpposite(), 3).above(selected == ApStructure.Type.RANCH ? 1 : 0);
        return new Target(core, facing, selected, tier, false, 0);
    }

    public static StructureBuilderPlan create(Level level, BlockPos builder, Direction facing,
                                               ApStructure.Type type, int tier) throws Failure {
        return create(level, builder, detect(level, builder, facing, type, tier));
    }

    public static StructureBuilderPlan create(Level level, BlockPos builder, Target target) throws Failure {
        BlockPos core = target.core();
        int tier = target.tier();
        ApStructure.Layout layout = ApStructure.layout(core, target.facing(), target.type(), tier);
        List<Step> steps = new ArrayList<>();
        Set<BlockPos> changed = new HashSet<>();
        for (ApStructure.Part part : layout.parts()) {
            available(level, part.pos());
            if (part.requirement() == ApStructure.Requirement.DOOR_UPPER) continue;
            Step step = new Step(part.pos(), part.example(), part.example().getBlock().asItem(), part);
            if (step.door()) available(level, part.pos().above());
            if (step.satisfied(level)) continue;
            editable(level, builder, part.pos());
            if (step.door()) {
                editable(level, builder, part.pos().above());
                changed.add(part.pos().above());
                BlockPos support = part.pos().below();
                available(level, support);
                if (!level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) {
                    editable(level, builder, support);
                    steps.add(new Step(support, Blocks.DIRT.defaultBlockState(), Items.DIRT, null));
                    changed.add(support);
                }
            }
            if (!step.clearance() && step.material() == Items.AIR) throw new Failure(9);
            steps.add(step);
            changed.add(part.pos());
        }
        available(level, core);
        BlockState state = controller(target.type()).defaultBlockState().setValue(ApControllerBlock.FACING, target.facing());
        ApControllerBlockEntity existing = null;
        if (level.getBlockState(core).is(state.getBlock())) {
            if (level.getBlockState(core).getValue(ApControllerBlock.FACING) != target.facing()) throw new Failure(4);
            if (!(level.getBlockEntity(core) instanceof ApControllerBlockEntity controller)) throw new Failure(9);
            existing = controller;
            if (existing.expansionTier() != 0 && existing.expansionTier() != tier) throw new Failure(4);
        } else {
            editable(level, builder, core);
            steps.add(new Step(core, state, state.getBlock().asItem(), null));
            changed.add(core);
        }
        steps.sort(Comparator.<Step>comparingInt(step -> step.pos().getX() >> 4)
            .thenComparingInt(step -> step.pos().getZ() >> 4).thenComparingInt(step -> step.pos().getY()));
        if (target.type() != ApStructure.Type.SLIME && !layout.hasSourceWater(level, changed)) {
            BlockPos water = layout.pos(2, -1, 2);
            editable(level, builder, water);
            checkBasin(level, water);
            changed.add(water);
            steps.add(new Step(water, Blocks.WATER.defaultBlockState(), Items.WATER_BUCKET, null));
        }
        if (tier > 0 && (existing == null || existing.expansionTier() != tier)) {
            if (existing != null) {
                boolean room = false;
                for (int i = 0; i < existing.getUpgradeSlots().getSlots(); i++) room |= existing.getUpgradeSlots().getStackInSlot(i).isEmpty();
                if (!room) throw new Failure(4);
            }
            steps.add(new Step(core, null, expansion(tier), null));
        }
        int demolitionCount = (int) changed.stream().filter(pos -> !level.getBlockState(pos).isAir()).count();
        return new StructureBuilderPlan(layout, steps, demolitionCount);
    }

    public static void checkBasin(Level level, BlockPos water) throws Failure {
        for (Direction side : Direction.values()) {
            if (side == Direction.UP) continue;
            BlockPos wall = water.relative(side);
            available(level, wall);
            if (!level.getBlockState(wall).isFaceSturdy(level, wall, side.getOpposite())) throw new Failure(4);
        }
    }

    public static void available(Level level, BlockPos pos) throws Failure {
        if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)) throw new Failure(8);
        if (!level.hasChunkAt(pos)) throw new Failure(5);
    }

    public static void editable(Level level, BlockPos builder, BlockPos pos) throws Failure {
        available(level, pos);
        if (pos.equals(builder)) throw new Failure(11);
        BlockState state = level.getBlockState(pos);
        if (state.getDestroySpeed(level, pos) < 0 || state.getBlock() instanceof GameMasterBlock
            || state.getBlock() instanceof LiquidBlock) throw new Failure(4);
    }
}
