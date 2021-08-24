package net.minecraft.world.level.levelgen;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoodleCavifier {
   private static final int NOODLES_MAX_Y = 30;
   private static final double SPACING_AND_STRAIGHTNESS = 1.5D;
   private static final double XZ_FREQUENCY = 2.6666666666666665D;
   private static final double Y_FREQUENCY = 2.6666666666666665D;
   private final NormalNoise toggleNoiseSource;
   private final NormalNoise thicknessNoiseSource;
   private final NormalNoise noodleANoiseSource;
   private final NormalNoise noodleBNoiseSource;

   public NoodleCavifier(long p_158731_) {
      Random random = new Random(p_158731_);
      this.toggleNoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -8, 1.0D);
      this.thicknessNoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -8, 1.0D);
      this.noodleANoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -7, 1.0D);
      this.noodleBNoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -7, 1.0D);
   }

   public void fillToggleNoiseColumn(double[] p_158743_, int p_158744_, int p_158745_, int p_158746_, int p_158747_) {
      this.fillNoiseColumn(p_158743_, p_158744_, p_158745_, p_158746_, p_158747_, this.toggleNoiseSource, 1.0D);
   }

   public void fillThicknessNoiseColumn(double[] p_158766_, int p_158767_, int p_158768_, int p_158769_, int p_158770_) {
      this.fillNoiseColumn(p_158766_, p_158767_, p_158768_, p_158769_, p_158770_, this.thicknessNoiseSource, 1.0D);
   }

   public void fillRidgeANoiseColumn(double[] p_158772_, int p_158773_, int p_158774_, int p_158775_, int p_158776_) {
      this.fillNoiseColumn(p_158772_, p_158773_, p_158774_, p_158775_, p_158776_, this.noodleANoiseSource, 2.6666666666666665D, 2.6666666666666665D);
   }

   public void fillRidgeBNoiseColumn(double[] p_158778_, int p_158779_, int p_158780_, int p_158781_, int p_158782_) {
      this.fillNoiseColumn(p_158778_, p_158779_, p_158780_, p_158781_, p_158782_, this.noodleBNoiseSource, 2.6666666666666665D, 2.6666666666666665D);
   }

   public void fillNoiseColumn(double[] p_158749_, int p_158750_, int p_158751_, int p_158752_, int p_158753_, NormalNoise p_158754_, double p_158755_) {
      this.fillNoiseColumn(p_158749_, p_158750_, p_158751_, p_158752_, p_158753_, p_158754_, p_158755_, p_158755_);
   }

   public void fillNoiseColumn(double[] p_158757_, int p_158758_, int p_158759_, int p_158760_, int p_158761_, NormalNoise p_158762_, double p_158763_, double p_158764_) {
      int i = 8;
      int j = 4;

      for(int k = 0; k < p_158761_; ++k) {
         int l = k + p_158760_;
         int i1 = p_158758_ * 4;
         int j1 = l * 8;
         int k1 = p_158759_ * 4;
         double d0;
         if (j1 < 38) {
            d0 = NoiseUtils.sampleNoiseAndMapToRange(p_158762_, (double)i1 * p_158763_, (double)j1 * p_158764_, (double)k1 * p_158763_, -1.0D, 1.0D);
         } else {
            d0 = 1.0D;
         }

         p_158757_[k] = d0;
      }

   }

   public double noodleCavify(double p_158733_, int p_158734_, int p_158735_, int p_158736_, double p_158737_, double p_158738_, double p_158739_, double p_158740_, int p_158741_) {
      if (p_158735_ <= 30 && p_158735_ >= p_158741_ + 4) {
         if (p_158733_ < 0.0D) {
            return p_158733_;
         } else if (p_158737_ < 0.0D) {
            return p_158733_;
         } else {
            double d0 = 0.05D;
            double d1 = 0.1D;
            double d2 = Mth.clampedMap(p_158738_, -1.0D, 1.0D, 0.05D, 0.1D);
            double d3 = Math.abs(1.5D * p_158739_) - d2;
            double d4 = Math.abs(1.5D * p_158740_) - d2;
            double d5 = Math.max(d3, d4);
            return Math.min(p_158733_, d5);
         }
      } else {
         return p_158733_;
      }
   }
}