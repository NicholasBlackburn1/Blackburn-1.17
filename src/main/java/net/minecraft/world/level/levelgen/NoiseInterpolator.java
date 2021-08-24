package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class NoiseInterpolator {
   private double[][] slice0;
   private double[][] slice1;
   private final int cellCountY;
   private final int cellCountZ;
   private final int cellNoiseMinY;
   private final NoiseInterpolator.NoiseColumnFiller noiseColumnFiller;
   private double noise000;
   private double noise001;
   private double noise100;
   private double noise101;
   private double noise010;
   private double noise011;
   private double noise110;
   private double noise111;
   private double valueXZ00;
   private double valueXZ10;
   private double valueXZ01;
   private double valueXZ11;
   private double valueZ0;
   private double valueZ1;
   private final int firstCellXInChunk;
   private final int firstCellZInChunk;

   public NoiseInterpolator(int p_158595_, int p_158596_, int p_158597_, ChunkPos p_158598_, int p_158599_, NoiseInterpolator.NoiseColumnFiller p_158600_) {
      this.cellCountY = p_158596_;
      this.cellCountZ = p_158597_;
      this.cellNoiseMinY = p_158599_;
      this.noiseColumnFiller = p_158600_;
      this.slice0 = allocateSlice(p_158596_, p_158597_);
      this.slice1 = allocateSlice(p_158596_, p_158597_);
      this.firstCellXInChunk = p_158598_.x * p_158595_;
      this.firstCellZInChunk = p_158598_.z * p_158597_;
   }

   private static double[][] allocateSlice(int p_158616_, int p_158617_) {
      int i = p_158617_ + 1;
      int j = p_158616_ + 1;
      double[][] adouble = new double[i][j];

      for(int k = 0; k < i; ++k) {
         adouble[k] = new double[j];
      }

      return adouble;
   }

   public void initializeForFirstCellX() {
      this.fillSlice(this.slice0, this.firstCellXInChunk);
   }

   public void advanceCellX(int p_158605_) {
      this.fillSlice(this.slice1, this.firstCellXInChunk + p_158605_ + 1);
   }

   private void fillSlice(double[][] p_158610_, int p_158611_) {
      for(int i = 0; i < this.cellCountZ + 1; ++i) {
         int j = this.firstCellZInChunk + i;
         this.noiseColumnFiller.fillNoiseColumn(p_158610_[i], p_158611_, j, this.cellNoiseMinY, this.cellCountY);
      }

   }

   public void selectCellYZ(int p_158607_, int p_158608_) {
      this.noise000 = this.slice0[p_158608_][p_158607_];
      this.noise001 = this.slice0[p_158608_ + 1][p_158607_];
      this.noise100 = this.slice1[p_158608_][p_158607_];
      this.noise101 = this.slice1[p_158608_ + 1][p_158607_];
      this.noise010 = this.slice0[p_158608_][p_158607_ + 1];
      this.noise011 = this.slice0[p_158608_ + 1][p_158607_ + 1];
      this.noise110 = this.slice1[p_158608_][p_158607_ + 1];
      this.noise111 = this.slice1[p_158608_ + 1][p_158607_ + 1];
   }

   public void updateForY(double p_158603_) {
      this.valueXZ00 = Mth.lerp(p_158603_, this.noise000, this.noise010);
      this.valueXZ10 = Mth.lerp(p_158603_, this.noise100, this.noise110);
      this.valueXZ01 = Mth.lerp(p_158603_, this.noise001, this.noise011);
      this.valueXZ11 = Mth.lerp(p_158603_, this.noise101, this.noise111);
   }

   public void updateForX(double p_158614_) {
      this.valueZ0 = Mth.lerp(p_158614_, this.valueXZ00, this.valueXZ10);
      this.valueZ1 = Mth.lerp(p_158614_, this.valueXZ01, this.valueXZ11);
   }

   public double calculateValue(double p_158619_) {
      return Mth.lerp(p_158619_, this.valueZ0, this.valueZ1);
   }

   public void swapSlices() {
      double[][] adouble = this.slice0;
      this.slice0 = this.slice1;
      this.slice1 = adouble;
   }

   @FunctionalInterface
   public interface NoiseColumnFiller {
      void fillNoiseColumn(double[] p_158621_, int p_158622_, int p_158623_, int p_158624_, int p_158625_);
   }
}