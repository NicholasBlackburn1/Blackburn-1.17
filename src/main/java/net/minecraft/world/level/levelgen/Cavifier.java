package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class Cavifier implements NoiseModifier {
   private final int minCellY;
   private final NormalNoise layerNoiseSource;
   private final NormalNoise pillarNoiseSource;
   private final NormalNoise pillarRarenessModulator;
   private final NormalNoise pillarThicknessModulator;
   private final NormalNoise spaghetti2dNoiseSource;
   private final NormalNoise spaghetti2dElevationModulator;
   private final NormalNoise spaghetti2dRarityModulator;
   private final NormalNoise spaghetti2dThicknessModulator;
   private final NormalNoise spaghetti3dNoiseSource1;
   private final NormalNoise spaghetti3dNoiseSource2;
   private final NormalNoise spaghetti3dRarityModulator;
   private final NormalNoise spaghetti3dThicknessModulator;
   private final NormalNoise spaghettiRoughnessNoise;
   private final NormalNoise spaghettiRoughnessModulator;
   private final NormalNoise caveEntranceNoiseSource;
   private final NormalNoise cheeseNoiseSource;
   private static final int CHEESE_NOISE_RANGE = 128;
   private static final int SURFACE_DENSITY_THRESHOLD = 170;

   public Cavifier(RandomSource p_158115_, int p_158116_) {
      this.minCellY = p_158116_;
      this.pillarNoiseSource = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -7, 1.0D, 1.0D);
      this.pillarRarenessModulator = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -8, 1.0D);
      this.pillarThicknessModulator = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -8, 1.0D);
      this.spaghetti2dNoiseSource = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -7, 1.0D);
      this.spaghetti2dElevationModulator = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -8, 1.0D);
      this.spaghetti2dRarityModulator = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -11, 1.0D);
      this.spaghetti2dThicknessModulator = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -11, 1.0D);
      this.spaghetti3dNoiseSource1 = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -7, 1.0D);
      this.spaghetti3dNoiseSource2 = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -7, 1.0D);
      this.spaghetti3dRarityModulator = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -11, 1.0D);
      this.spaghetti3dThicknessModulator = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -8, 1.0D);
      this.spaghettiRoughnessNoise = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -5, 1.0D);
      this.spaghettiRoughnessModulator = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -8, 1.0D);
      this.caveEntranceNoiseSource = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -8, 1.0D, 1.0D, 1.0D);
      this.layerNoiseSource = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -8, 1.0D);
      this.cheeseNoiseSource = NormalNoise.create(new SimpleRandomSource(p_158115_.nextLong()), -8, 0.5D, 1.0D, 2.0D, 1.0D, 2.0D, 1.0D, 0.0D, 2.0D, 0.0D);
   }

   public double modifyNoise(double p_158151_, int p_158152_, int p_158153_, int p_158154_) {
      boolean flag = p_158151_ < 170.0D;
      double d0 = this.spaghettiRoughness(p_158154_, p_158152_, p_158153_);
      double d1 = this.getSpaghetti3d(p_158154_, p_158152_, p_158153_);
      if (flag) {
         return Math.min(p_158151_, (d1 + d0) * 128.0D * 5.0D);
      } else {
         double d2 = this.cheeseNoiseSource.getValue((double)p_158154_, (double)p_158152_ / 1.5D, (double)p_158153_);
         double d3 = Mth.clamp(d2 + 0.25D, -1.0D, 1.0D);
         double d4 = (double)((float)(30 - p_158152_) / 8.0F);
         double d5 = d3 + Mth.clampedLerp(0.5D, 0.0D, d4);
         double d6 = this.getLayerizedCaverns(p_158154_, p_158152_, p_158153_);
         double d7 = this.getSpaghetti2d(p_158154_, p_158152_, p_158153_);
         double d8 = d5 + d6;
         double d9 = Math.min(d8, Math.min(d1, d7) + d0);
         double d10 = Math.max(d9, this.getPillars(p_158154_, p_158152_, p_158153_));
         return 128.0D * Mth.clamp(d10, -1.0D, 1.0D);
      }
   }

   private double addEntrances(double p_158120_, int p_158121_, int p_158122_, int p_158123_) {
      double d0 = this.caveEntranceNoiseSource.getValue((double)(p_158121_ * 2), (double)p_158122_, (double)(p_158123_ * 2));
      d0 = NoiseUtils.biasTowardsExtreme(d0, 1.0D);
      int i = 0;
      double d1 = (double)(p_158122_ - 0) / 40.0D;
      d0 = d0 + Mth.clampedLerp(0.5D, p_158120_, d1);
      double d2 = 3.0D;
      d0 = 4.0D * d0 + 3.0D;
      return Math.min(p_158120_, d0);
   }

   private double getPillars(int p_158125_, int p_158126_, int p_158127_) {
      double d0 = 0.0D;
      double d1 = 2.0D;
      double d2 = NoiseUtils.sampleNoiseAndMapToRange(this.pillarRarenessModulator, (double)p_158125_, (double)p_158126_, (double)p_158127_, 0.0D, 2.0D);
      double d3 = 0.0D;
      double d4 = 1.1D;
      double d5 = NoiseUtils.sampleNoiseAndMapToRange(this.pillarThicknessModulator, (double)p_158125_, (double)p_158126_, (double)p_158127_, 0.0D, 1.1D);
      d5 = Math.pow(d5, 3.0D);
      double d6 = 25.0D;
      double d7 = 0.3D;
      double d8 = this.pillarNoiseSource.getValue((double)p_158125_ * 25.0D, (double)p_158126_ * 0.3D, (double)p_158127_ * 25.0D);
      d8 = d5 * (d8 * 2.0D - d2);
      return d8 > 0.03D ? d8 : Double.NEGATIVE_INFINITY;
   }

   private double getLayerizedCaverns(int p_158135_, int p_158136_, int p_158137_) {
      double d0 = this.layerNoiseSource.getValue((double)p_158135_, (double)(p_158136_ * 8), (double)p_158137_);
      return Mth.square(d0) * 4.0D;
   }

   private double getSpaghetti3d(int p_158139_, int p_158140_, int p_158141_) {
      double d0 = this.spaghetti3dRarityModulator.getValue((double)(p_158139_ * 2), (double)p_158140_, (double)(p_158141_ * 2));
      double d1 = Cavifier.QuantizedSpaghettiRarity.getSpaghettiRarity3D(d0);
      double d2 = 0.065D;
      double d3 = 0.088D;
      double d4 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti3dThicknessModulator, (double)p_158139_, (double)p_158140_, (double)p_158141_, 0.065D, 0.088D);
      double d5 = sampleWithRarity(this.spaghetti3dNoiseSource1, (double)p_158139_, (double)p_158140_, (double)p_158141_, d1);
      double d6 = Math.abs(d1 * d5) - d4;
      double d7 = sampleWithRarity(this.spaghetti3dNoiseSource2, (double)p_158139_, (double)p_158140_, (double)p_158141_, d1);
      double d8 = Math.abs(d1 * d7) - d4;
      return clampToUnit(Math.max(d6, d8));
   }

   private double getSpaghetti2d(int p_158143_, int p_158144_, int p_158145_) {
      double d0 = this.spaghetti2dRarityModulator.getValue((double)(p_158143_ * 2), (double)p_158144_, (double)(p_158145_ * 2));
      double d1 = Cavifier.QuantizedSpaghettiRarity.getSphaghettiRarity2D(d0);
      double d2 = 0.6D;
      double d3 = 1.3D;
      double d4 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2dThicknessModulator, (double)(p_158143_ * 2), (double)p_158144_, (double)(p_158145_ * 2), 0.6D, 1.3D);
      double d5 = sampleWithRarity(this.spaghetti2dNoiseSource, (double)p_158143_, (double)p_158144_, (double)p_158145_, d1);
      double d6 = 0.083D;
      double d7 = Math.abs(d1 * d5) - 0.083D * d4;
      int i = this.minCellY;
      int j = 8;
      double d8 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2dElevationModulator, (double)p_158143_, 0.0D, (double)p_158145_, (double)i, 8.0D);
      double d9 = Math.abs(d8 - (double)p_158144_ / 8.0D) - 1.0D * d4;
      d9 = d9 * d9 * d9;
      return clampToUnit(Math.max(d9, d7));
   }

   private double spaghettiRoughness(int p_158147_, int p_158148_, int p_158149_) {
      double d0 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghettiRoughnessModulator, (double)p_158147_, (double)p_158148_, (double)p_158149_, 0.0D, 0.1D);
      return (0.4D - Math.abs(this.spaghettiRoughnessNoise.getValue((double)p_158147_, (double)p_158148_, (double)p_158149_))) * d0;
   }

   private static double clampToUnit(double p_158118_) {
      return Mth.clamp(p_158118_, -1.0D, 1.0D);
   }

   private static double sampleWithRarity(NormalNoise p_158129_, double p_158130_, double p_158131_, double p_158132_, double p_158133_) {
      return p_158129_.getValue(p_158130_ / p_158133_, p_158131_ / p_158133_, p_158132_ / p_158133_);
   }

   static final class QuantizedSpaghettiRarity {
      private QuantizedSpaghettiRarity() {
      }

      static double getSphaghettiRarity2D(double p_158157_) {
         if (p_158157_ < -0.75D) {
            return 0.5D;
         } else if (p_158157_ < -0.5D) {
            return 0.75D;
         } else if (p_158157_ < 0.5D) {
            return 1.0D;
         } else {
            return p_158157_ < 0.75D ? 2.0D : 3.0D;
         }
      }

      static double getSpaghettiRarity3D(double p_158159_) {
         if (p_158159_ < -0.5D) {
            return 0.75D;
         } else if (p_158159_ < 0.0D) {
            return 1.0D;
         } else {
            return p_158159_ < 0.5D ? 1.5D : 2.0D;
         }
      }
   }
}