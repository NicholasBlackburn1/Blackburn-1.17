package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import net.minecraft.world.level.levelgen.RandomSource;

public class NormalNoise {
   private static final double INPUT_FACTOR = 1.0181268882175227D;
   private static final double TARGET_DEVIATION = 0.3333333333333333D;
   private final double valueFactor;
   private final PerlinNoise first;
   private final PerlinNoise second;

   public static NormalNoise create(RandomSource p_164355_, int p_164356_, double... p_164357_) {
      return new NormalNoise(p_164355_, p_164356_, new DoubleArrayList(p_164357_));
   }

   public static NormalNoise create(RandomSource p_164351_, int p_164352_, DoubleList p_164353_) {
      return new NormalNoise(p_164351_, p_164352_, p_164353_);
   }

   private NormalNoise(RandomSource p_164347_, int p_164348_, DoubleList p_164349_) {
      this.first = PerlinNoise.create(p_164347_, p_164348_, p_164349_);
      this.second = PerlinNoise.create(p_164347_, p_164348_, p_164349_);
      int i = Integer.MAX_VALUE;
      int j = Integer.MIN_VALUE;
      DoubleListIterator doublelistiterator = p_164349_.iterator();

      while(doublelistiterator.hasNext()) {
         int k = doublelistiterator.nextIndex();
         double d0 = doublelistiterator.nextDouble();
         if (d0 != 0.0D) {
            i = Math.min(i, k);
            j = Math.max(j, k);
         }
      }

      this.valueFactor = 0.16666666666666666D / expectedDeviation(j - i);
   }

   private static double expectedDeviation(int p_75385_) {
      return 0.1D * (1.0D + 1.0D / (double)(p_75385_ + 1));
   }

   public double getValue(double p_75381_, double p_75382_, double p_75383_) {
      double d0 = p_75381_ * 1.0181268882175227D;
      double d1 = p_75382_ * 1.0181268882175227D;
      double d2 = p_75383_ * 1.0181268882175227D;
      return (this.first.getValue(p_75381_, p_75382_, p_75383_) + this.second.getValue(d0, d1, d2)) * this.valueFactor;
   }
}