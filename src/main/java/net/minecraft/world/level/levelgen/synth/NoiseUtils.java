package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;

public class NoiseUtils {
   public static double sampleNoiseAndMapToRange(NormalNoise p_164338_, double p_164339_, double p_164340_, double p_164341_, double p_164342_, double p_164343_) {
      double d0 = p_164338_.getValue(p_164339_, p_164340_, p_164341_);
      return Mth.map(d0, -1.0D, 1.0D, p_164342_, p_164343_);
   }

   public static double biasTowardsExtreme(double p_164335_, double p_164336_) {
      return p_164335_ + Math.sin(Math.PI * p_164335_) * p_164336_ / Math.PI;
   }
}