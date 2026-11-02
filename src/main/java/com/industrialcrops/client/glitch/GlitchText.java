package com.industrialcrops.client.glitch;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

/**
 * Glitch look for Carrote item names: characters flip to block glyphs, fall into the obfuscated font and
 * flash red/cyan, mostly quietly with short corruption bursts. Purely client-side and time-driven, so the
 * name animates wherever it is drawn every frame.
 */
public final class GlitchText {
   static final int RED = 0xFF3355;
   static final int CYAN = 0x33FFF0;
   private static final char[] GLYPHS = "▓▒░█▌▐▄▀#$%&@*¤¦?/\\<>".toCharArray();
   /** Frame length in milliseconds; a burst covers three frames. */
   static final long FRAME_MS = 70L;

   private GlitchText() {
   }

   public static boolean isCarrote(ItemStack stack) {
      if (stack.isEmpty()) return false;
      String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
      return "carrote".equals(namespace) || "carrote_curios".equals(namespace);
   }

   static long frame() {
      return Util.getMillis() / FRAME_MS;
   }

   static int mix(long value) {
      long h = value * 0x9E3779B97F4A7C15L;
      h ^= h >>> 31;
      h *= 0xBF58476D1CE4E5B9L;
      h ^= h >>> 29;
      return (int) (h & 0x7FFFFFFF);
   }

   /** Roughly one frame in seven starts a three-frame corruption burst. */
   static boolean burst(long frame, int seed) {
      return mix((frame / 3) * 31L + seed) % 100 < 15;
   }

   static int baseColor(Component name) {
      TextColor[] found = new TextColor[1];
      name.visit((style, text) -> {
         if (found[0] == null && style.getColor() != null) found[0] = style.getColor();
         return java.util.Optional.empty();
      }, Style.EMPTY);
      return found[0] != null ? found[0].getValue() : 0xFF55FF;
   }

   /** The name for this frame, with corrupted characters and an untouched base colour. */
   public static MutableComponent corrupt(Component name, int seed) {
      return corrupt(name, seed, frame());
   }

   static MutableComponent corrupt(Component name, int seed, long frame) {
      String text = name.getString();
      int base = baseColor(name);
      boolean burst = burst(frame, seed);
      Style baseStyle = name.getStyle().withColor(base);
      MutableComponent out = Component.empty();
      StringBuilder run = new StringBuilder();
      for (int index = 0; index < text.length(); index++) {
         char c = text.charAt(index);
         int roll = mix(frame * 131L + index * 7919L + seed) % 1000;
         int chance = burst ? 280 : 25;
         if (c == ' ' || roll >= chance) {
            run.append(c);
            continue;
         }
         if (run.length() > 0) {
            out.append(Component.literal(run.toString()).withStyle(baseStyle));
            run.setLength(0);
         }
         int pick = mix(roll * 31L + frame);
         int color = switch (pick % 3) {
            case 0 -> RED;
            case 1 -> CYAN;
            default -> 0xFFFFFF;
         };
         Style style = baseStyle.withColor(color);
         String shown;
         if (pick % 4 == 0) {
            shown = String.valueOf(c);
            style = style.withObfuscated(true);
         } else {
            shown = String.valueOf(GLYPHS[pick % GLYPHS.length]);
         }
         out.append(Component.literal(shown).withStyle(style));
      }
      if (run.length() > 0) out.append(Component.literal(run.toString()).withStyle(baseStyle));
      return out;
   }

   public static int seed(ItemStack stack) {
      return BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
   }
}
