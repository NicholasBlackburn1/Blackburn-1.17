package net.minecraft.world.level.levelgen.synth;

import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public class BlendedNoise {
   private final PerlinNoise minLimitNoise;
   private final PerlinNoise maxLimitNoise;
   private final PerlinNoise mainNoise;

   public BlendedNoise(PerlinNoise p_164294_, PerlinNoise p_164295_, PerlinNoise p_164296_) {
      this.minLimitNoise = p_164294_;
      this.maxLimitNoise = p_164295_;
      this.mainNoise = p_164296_;
   }

   public BlendedNoise(RandomSource p_164292_) {
      this(new PerlinNoise(p_164292_, IntStream.rangeClosed(-15, 0)), new PerlinNoise(p_164292_, IntStream.rangeClosed(-15, 0)), new PerlinNoise(p_164292_, IntStream.rangeClosed(-7, 0)));
   }

   public double sampleAndClampNoise(int p_164298_, int p_164299_, int p_164300_, double p_164301_, double p_164302_, double p_164303_, double p_164304_) {
      double d0 = 0.0D;
      double d1 = 0.0D;
      double d2 = 0.0D;
      boolean flag = true;
      double d3 = 1.0D;

      for(int i = 0; i < 8; ++i) {
         ImprovedNoise improvednoise = this.mainNoise.getOctaveNoise(i);
         if (improvednoise != null) {
            d2 += improvednoise.noise(PerlinNoise.wrap((double)p_164298_ * p_164303_ * d3), PerlinNoise.wrap((double)p_164299_ * p_164304_ * d3), PerlinNoise.wrap((double)p_164300_ * p_164303_ * d3), p_164304_ * d3, (double)p_164299_ * p_164304_ * d3) / d3;
         }

         d3 /= 2.0D;
      }

      double d8 = (d2 / 10.0D + 1.0D) / 2.0D;
      boolean flag1 = d8 >= 1.0D;
      boolean flag2 = d8 <= 0.0D;
      d3 = 1.0D;

      for(int j = 0; j < 16; ++j) {
         double d4 = PerlinNoise.wrap((double)p_164298_ * p_164301_ * d3);
         double d5 = PerlinNoise.wrap((double)p_164299_ * p_164302_ * d3);
         double d6 = PerlinNoise.wrap((double)p_164300_ * p_164301_ * d3);
         double d7 = p_164302_ * d3;
         if (!flag1) {
            ImprovedNoise improvednoise1 = this.minLimitNoise.getOctaveNoise(j);
            if (improvednoise1 != null) {
               d0 += improvednoise1.noise(d4, d5, d6, d7, (double)p_164299_ * d7) / d3;
            }
         }

         if (!flag2) {
            ImprovedNoise improvednoise2 = this.maxLimitNoise.getOctaveNoise(j);
            if (improvednoise2 != null) {
               d1 += improvednoise2.noise(d4, d5, d6, d7, (double)p_164299_ * d7) / d3;
            }
         }

         d3 /= 2.0D;
      }

      return Mth.clampedLerp(d0 / 512.0D, d1 / 512.0D, d8);
   }
}