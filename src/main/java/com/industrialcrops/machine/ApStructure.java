package com.industrialcrops.machine;
import com.industrialcrops.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
public final class ApStructure {
   public static final int CASINGS = 8;
   public static final int FRAMES = 8;
   private ApStructure() {
   }
   public static BlockPos center(BlockPos var0, Direction var1) {
      return var0.relative(var1.getOpposite());
   }
   public static List<ApStructure.Part> parts(BlockPos var0, Direction var1, Block var2) {
      BlockPos var3 = center(var0, var1);
      ArrayList var4 = new ArrayList(18);
      for (int var5 = -1; var5 <= 1; var5++) {
         for (int var6 = -1; var6 <= 1; var6++) {
            BlockPos var7 = var3.offset(var5, 0, var6);
            if (!var7.equals(var0)) {
               var4.add(new ApStructure.Part(var7, (Block)ModBlocks.AP_STEEL_CASING.get(), ApStructure.Kind.CASING));
            }
            if (var5 != 0 && var6 != 0) {
               for (int var8 = 1; var8 <= 2; var8++) {
                  var4.add(new ApStructure.Part(var7.above(var8), (Block)ModBlocks.AP_STEEL_FRAME.get(), ApStructure.Kind.FRAME));
               }
            }
         }
      }
      var4.add(new ApStructure.Part(var3.above(), var2, ApStructure.Kind.CORE));
      var4.add(new ApStructure.Part(var3.above(2), (Block)ModBlocks.AP_STRUCTURAL_STEEL_BLOCK.get(), ApStructure.Kind.CAP));
      return var4;
   }
   public static ApStructure.Status check(Level var0, BlockPos var1, Direction var2, Block var3) {
      int var4 = 0;
      int var5 = 0;
      boolean var6 = false;
      boolean var7 = false;
      for (ApStructure.Part var9 : parts(var1, var2, var3)) {
         if (var0.getBlockState(var9.pos()).is(var9.block())) {
            switch (var9.kind()) {
               case CASING:
                  var4++;
                  break;
               case FRAME:
                  var5++;
                  break;
               case CORE:
                  var6 = true;
                  break;
               case CAP:
                  var7 = true;
            }
         }
      }
      return new ApStructure.Status(var4, var5, var6, var7);
   }
   public static enum Kind {
      CASING,
      FRAME,
      CORE,
      CAP;
   }
   public static record Part(BlockPos pos, Block block, ApStructure.Kind kind) {
   }
   public static record Status(int casings, int frames, boolean core, boolean cap) {
      public boolean formed() {
         return this.casings == 8 && this.frames == 8 && this.core && this.cap;
      }
      public int pack() {
         return this.casings | this.frames << 4 | (this.core ? 256 : 0) | (this.cap ? 512 : 0);
      }
      public static ApStructure.Status unpack(int var0) {
         return new ApStructure.Status(var0 & 15, var0 >> 4 & 15, (var0 & 256) != 0, (var0 & 512) != 0);
      }
   }
}
