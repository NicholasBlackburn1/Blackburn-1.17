package net.minecraft.world.level.levelgen;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class NoiseSampler {
   private static final int OLD_CELL_COUNT_Y = 32;
   private static final float[] BIOME_WEIGHTS = Util.make(new float[25], (p_158687_) -> {
      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            float f = 10.0F / Mth.sqrt((float)(i * i + j * j) + 0.2F);
            p_158687_[i + 2 + (j + 2) * 5] = f;
         }
      }

   });
   private final BiomeSource biomeSource;
   private final int cellWidth;
   private final int cellHeight;
   private final int cellCountY;
   private final NoiseSettings noiseSettings;
   private final BlendedNoise blendedNoise;
   @Nullable
   private final SimplexNoise islandNoise;
   private final PerlinNoise depthNoise;
   private final double topSlideTarget;
   private final double topSlideSize;
   private final double topSlideOffset;
   private final double bottomSlideTarget;
   private final double bottomSlideSize;
   private final double bottomSlideOffset;
   private final double dimensionDensityFactor;
   private final double dimensionDensityOffset;
   private final NoiseModifier caveNoiseModifier;

   public NoiseSampler(BiomeSource p_158658_, int p_158659_, int p_158660_, int p_158661_, NoiseSettings p_158662_, BlendedNoise p_158663_, @Nullable SimplexNoise p_158664_, PerlinNoise p_158665_, NoiseModifier p_158666_) {
      this.cellWidth = p_158659_;
      this.cellHeight = p_158660_;
      this.biomeSource = p_158658_;
      this.cellCountY = p_158661_;
      this.noiseSettings = p_158662_;
      this.blendedNoise = p_158663_;
      this.islandNoise = p_158664_;
      this.depthNoise = p_158665_;
      this.topSlideTarget = (double)p_158662_.topSlideSettings().target();
      this.topSlideSize = (double)p_158662_.topSlideSettings().size();
      this.topSlideOffset = (double)p_158662_.topSlideSettings().offset();
      this.bottomSlideTarget = (double)p_158662_.bottomSlideSettings().target();
      this.bottomSlideSize = (double)p_158662_.bottomSlideSettings().size();
      this.bottomSlideOffset = (double)p_158662_.bottomSlideSettings().offset();
      this.dimensionDensityFactor = p_158662_.densityFactor();
      this.dimensionDensityOffset = p_158662_.densityOffset();
      this.caveNoiseModifier = p_158666_;
   }

   public void fillNoiseColumn(double[] p_158679_, int p_158680_, int p_158681_, NoiseSettings p_158682_, int p_158683_, int p_158684_, int p_158685_) {
      double d0;
      double d1;
      if (this.islandNoise != null) {
         d0 = (double)(TheEndBiomeSource.getHeightValue(this.islandNoise, p_158680_, p_158681_) - 8.0F);
         if (d0 > 0.0D) {
            d1 = 0.25D;
         } else {
            d1 = 1.0D;
         }
      } else {
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         int i = 2;
         int j = p_158683_;
         float f3 = this.biomeSource.getNoiseBiome(p_158680_, p_158683_, p_158681_).getDepth();

         for(int k = -2; k <= 2; ++k) {
            for(int l = -2; l <= 2; ++l) {
               Biome biome = this.biomeSource.getNoiseBiome(p_158680_ + k, j, p_158681_ + l);
               float f4 = biome.getDepth();
               float f5 = biome.getScale();
               float f6;
               float f7;
               if (p_158682_.isAmplified() && f4 > 0.0F) {
                  f6 = 1.0F + f4 * 2.0F;
                  f7 = 1.0F + f5 * 4.0F;
               } else {
                  f6 = f4;
                  f7 = f5;
               }

               float f8 = f4 > f3 ? 0.5F : 1.0F;
               float f9 = f8 * BIOME_WEIGHTS[k + 2 + (l + 2) * 5] / (f6 + 2.0F);
               f += f7 * f9;
               f1 += f6 * f9;
               f2 += f9;
            }
         }

         float f10 = f1 / f2;
         float f11 = f / f2;
         double d6 = (double)(f10 * 0.5F - 0.125F);
         double d8 = (double)(f11 * 0.9F + 0.1F);
         d0 = d6 * 0.265625D;
         d1 = 96.0D / d8;
      }

      double d2 = 684.412D * p_158682_.noiseSamplingSettings().xzScale();
      double d3 = 684.412D * p_158682_.noiseSamplingSettings().yScale();
      double d4 = d2 / p_158682_.noiseSamplingSettings().xzFactor();
      double d5 = d3 / p_158682_.noiseSamplingSettings().yFactor();
      double d7 = p_158682_.randomDensityOffset() ? this.getRandomDensity(p_158680_, p_158681_) : 0.0D;

      for(int i1 = 0; i1 <= p_158685_; ++i1) {
         int j1 = i1 + p_158684_;
         double d9 = this.blendedNoise.sampleAndClampNoise(p_158680_, j1, p_158681_, d2, d3, d4, d5);
         double d10 = this.computeInitialDensity(j1, d0, d1, d7) + d9;
         d10 = this.caveNoiseModifier.modifyNoise(d10, j1 * this.cellHeight, p_158681_ * this.cellWidth, p_158680_ * this.cellWidth);
         d10 = this.applySlide(d10, j1);
         p_158679_[i1] = d10;
      }

   }

   private double computeInitialDensity(int p_158671_, double p_158672_, double p_158673_, double p_158674_) {
      double d0 = 1.0D - (double)p_158671_ * 2.0D / 32.0D + p_158674_;
      double d1 = d0 * this.dimensionDensityFactor + this.dimensionDensityOffset;
      double d2 = (d1 + p_158672_) * p_158673_;
      return d2 * (double)(d2 > 0.0D ? 4 : 1);
   }

   private double applySlide(double p_158668_, int p_158669_) {
      int i = Mth.intFloorDiv(this.noiseSettings.minY(), this.cellHeight);
      int j = p_158669_ - i;
      if (this.topSlideSize > 0.0D) {
         double d0 = ((double)(this.cellCountY - j) - this.topSlideOffset) / this.topSlideSize;
         p_158668_ = Mth.clampedLerp(this.topSlideTarget, p_158668_, d0);
      }

      if (this.bottomSlideSize > 0.0D) {
         double d1 = ((double)j - this.bottomSlideOffset) / this.bottomSlideSize;
         p_158668_ = Mth.clampedLerp(this.bottomSlideTarget, p_158668_, d1);
      }

      return p_158668_;
   }

   private double getRandomDensity(int p_158676_, int p_158677_) {
      double d0 = this.depthNoise.getValue((double)(p_158676_ * 200), 10.0D, (double)(p_158677_ * 200), 1.0D, 0.0D, true);
      double d1;
      if (d0 < 0.0D) {
         d1 = -d0 * 0.3D;
      } else {
         d1 = d0;
      }

      double d2 = d1 * 24.575625D - 2.0D;
      return d2 < 0.0D ? d2 * 0.009486607142857142D : Math.min(d2, 1.0D) * 0.006640625D;
   }
}