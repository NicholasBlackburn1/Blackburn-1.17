package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface Aquifer {
   int ALWAYS_LAVA_AT_OR_BELOW_Y_INDEX = 9;
   int ALWAYS_USE_SEA_LEVEL_WHEN_ABOVE = 30;

   static Aquifer create(ChunkPos p_157960_, NormalNoise p_157961_, NormalNoise p_157962_, NormalNoise p_157963_, NoiseGeneratorSettings p_157964_, NoiseSampler p_157965_, int p_157966_, int p_157967_) {
      return new Aquifer.NoiseBasedAquifer(p_157960_, p_157961_, p_157962_, p_157963_, p_157964_, p_157965_, p_157966_, p_157967_);
   }

   static Aquifer createDisabled(final int p_157957_, final BlockState p_157958_) {
      return new Aquifer() {
         public BlockState computeState(BaseStoneSource p_157980_, int p_157981_, int p_157982_, int p_157983_, double p_157984_) {
            if (p_157984_ > 0.0D) {
               return p_157980_.getBaseBlock(p_157981_, p_157982_, p_157983_);
            } else {
               return p_157982_ >= p_157957_ ? Blocks.AIR.defaultBlockState() : p_157958_;
            }
         }

         public boolean shouldScheduleFluidUpdate() {
            return false;
         }
      };
   }

   BlockState computeState(BaseStoneSource p_157968_, int p_157969_, int p_157970_, int p_157971_, double p_157972_);

   boolean shouldScheduleFluidUpdate();

   public static class NoiseBasedAquifer implements Aquifer {
      private static final int X_RANGE = 10;
      private static final int Y_RANGE = 9;
      private static final int Z_RANGE = 10;
      private static final int X_SEPARATION = 6;
      private static final int Y_SEPARATION = 3;
      private static final int Z_SEPARATION = 6;
      private static final int X_SPACING = 16;
      private static final int Y_SPACING = 12;
      private static final int Z_SPACING = 16;
      private final NormalNoise barrierNoise;
      private final NormalNoise waterLevelNoise;
      private final NormalNoise lavaNoise;
      private final NoiseGeneratorSettings noiseGeneratorSettings;
      private final Aquifer.NoiseBasedAquifer.AquiferStatus[] aquiferCache;
      private final long[] aquiferLocationCache;
      private boolean shouldScheduleFluidUpdate;
      private final NoiseSampler sampler;
      private final int minGridX;
      private final int minGridY;
      private final int minGridZ;
      private final int gridSizeX;
      private final int gridSizeZ;

      NoiseBasedAquifer(ChunkPos p_158008_, NormalNoise p_158009_, NormalNoise p_158010_, NormalNoise p_158011_, NoiseGeneratorSettings p_158012_, NoiseSampler p_158013_, int p_158014_, int p_158015_) {
         this.barrierNoise = p_158009_;
         this.waterLevelNoise = p_158010_;
         this.lavaNoise = p_158011_;
         this.noiseGeneratorSettings = p_158012_;
         this.sampler = p_158013_;
         this.minGridX = this.gridX(p_158008_.getMinBlockX()) - 1;
         int i = this.gridX(p_158008_.getMaxBlockX()) + 1;
         this.gridSizeX = i - this.minGridX + 1;
         this.minGridY = this.gridY(p_158014_) - 1;
         int j = this.gridY(p_158014_ + p_158015_) + 1;
         int k = j - this.minGridY + 1;
         this.minGridZ = this.gridZ(p_158008_.getMinBlockZ()) - 1;
         int l = this.gridZ(p_158008_.getMaxBlockZ()) + 1;
         this.gridSizeZ = l - this.minGridZ + 1;
         int i1 = this.gridSizeX * k * this.gridSizeZ;
         this.aquiferCache = new Aquifer.NoiseBasedAquifer.AquiferStatus[i1];
         this.aquiferLocationCache = new long[i1];
         Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
      }

      private int getIndex(int p_158028_, int p_158029_, int p_158030_) {
         int i = p_158028_ - this.minGridX;
         int j = p_158029_ - this.minGridY;
         int k = p_158030_ - this.minGridZ;
         return (j * this.gridSizeZ + k) * this.gridSizeX + i;
      }

      public BlockState computeState(BaseStoneSource p_158034_, int p_158035_, int p_158036_, int p_158037_, double p_158038_) {
         if (p_158038_ <= 0.0D) {
            double d0;
            BlockState blockstate;
            boolean flag;
            if (this.isLavaLevel(p_158036_)) {
               blockstate = Blocks.LAVA.defaultBlockState();
               d0 = 0.0D;
               flag = false;
            } else {
               int i = Math.floorDiv(p_158035_ - 5, 16);
               int j = Math.floorDiv(p_158036_ + 1, 12);
               int k = Math.floorDiv(p_158037_ - 5, 16);
               int l = Integer.MAX_VALUE;
               int i1 = Integer.MAX_VALUE;
               int j1 = Integer.MAX_VALUE;
               long k1 = 0L;
               long l1 = 0L;
               long i2 = 0L;

               for(int j2 = 0; j2 <= 1; ++j2) {
                  for(int k2 = -1; k2 <= 1; ++k2) {
                     for(int l2 = 0; l2 <= 1; ++l2) {
                        int i3 = i + j2;
                        int j3 = j + k2;
                        int k3 = k + l2;
                        int l3 = this.getIndex(i3, j3, k3);
                        long j4 = this.aquiferLocationCache[l3];
                        long i4;
                        if (j4 != Long.MAX_VALUE) {
                           i4 = j4;
                        } else {
                           WorldgenRandom worldgenrandom = new WorldgenRandom(Mth.getSeed(i3, j3 * 3, k3) + 1L);
                           i4 = BlockPos.asLong(i3 * 16 + worldgenrandom.nextInt(10), j3 * 12 + worldgenrandom.nextInt(9), k3 * 16 + worldgenrandom.nextInt(10));
                           this.aquiferLocationCache[l3] = i4;
                        }

                        int j5 = BlockPos.getX(i4) - p_158035_;
                        int k4 = BlockPos.getY(i4) - p_158036_;
                        int l4 = BlockPos.getZ(i4) - p_158037_;
                        int i5 = j5 * j5 + k4 * k4 + l4 * l4;
                        if (l >= i5) {
                           i2 = l1;
                           l1 = k1;
                           k1 = i4;
                           j1 = i1;
                           i1 = l;
                           l = i5;
                        } else if (i1 >= i5) {
                           i2 = l1;
                           l1 = i4;
                           j1 = i1;
                           i1 = i5;
                        } else if (j1 >= i5) {
                           i2 = i4;
                           j1 = i5;
                        }
                     }
                  }
               }

               Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus = this.getAquiferStatus(k1);
               Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus1 = this.getAquiferStatus(l1);
               Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus2 = this.getAquiferStatus(i2);
               double d6 = this.similarity(l, i1);
               double d7 = this.similarity(l, j1);
               double d8 = this.similarity(i1, j1);
               flag = d6 > 0.0D;
               if (aquifer$noisebasedaquifer$aquiferstatus.fluidLevel >= p_158036_ && aquifer$noisebasedaquifer$aquiferstatus.fluidType.is(Blocks.WATER) && this.isLavaLevel(p_158036_ - 1)) {
                  d0 = 1.0D;
               } else if (d6 > -1.0D) {
                  double d9 = 1.0D + (this.barrierNoise.getValue((double)p_158035_, (double)p_158036_, (double)p_158037_) + 0.05D) / 4.0D;
                  double d10 = this.calculatePressure(p_158036_, d9, aquifer$noisebasedaquifer$aquiferstatus, aquifer$noisebasedaquifer$aquiferstatus1);
                  double d11 = this.calculatePressure(p_158036_, d9, aquifer$noisebasedaquifer$aquiferstatus, aquifer$noisebasedaquifer$aquiferstatus2);
                  double d1 = this.calculatePressure(p_158036_, d9, aquifer$noisebasedaquifer$aquiferstatus1, aquifer$noisebasedaquifer$aquiferstatus2);
                  double d2 = Math.max(0.0D, d6);
                  double d3 = Math.max(0.0D, d7);
                  double d4 = Math.max(0.0D, d8);
                  double d5 = 2.0D * d2 * Math.max(d10, Math.max(d11 * d3, d1 * d4));
                  d0 = Math.max(0.0D, d5);
               } else {
                  d0 = 0.0D;
               }

               blockstate = p_158036_ >= aquifer$noisebasedaquifer$aquiferstatus.fluidLevel ? Blocks.AIR.defaultBlockState() : aquifer$noisebasedaquifer$aquiferstatus.fluidType;
            }

            if (p_158038_ + d0 <= 0.0D) {
               this.shouldScheduleFluidUpdate = flag;
               return blockstate;
            }
         }

         this.shouldScheduleFluidUpdate = false;
         return p_158034_.getBaseBlock(p_158035_, p_158036_, p_158037_);
      }

      public boolean shouldScheduleFluidUpdate() {
         return this.shouldScheduleFluidUpdate;
      }

      private boolean isLavaLevel(int p_158018_) {
         return p_158018_ - this.noiseGeneratorSettings.noiseSettings().minY() <= 9;
      }

      private double similarity(int p_158025_, int p_158026_) {
         double d0 = 25.0D;
         return 1.0D - (double)Math.abs(p_158026_ - p_158025_) / 25.0D;
      }

      private double calculatePressure(int p_158020_, double p_158021_, Aquifer.NoiseBasedAquifer.AquiferStatus p_158022_, Aquifer.NoiseBasedAquifer.AquiferStatus p_158023_) {
         if (p_158020_ <= p_158022_.fluidLevel && p_158020_ <= p_158023_.fluidLevel && p_158022_.fluidType != p_158023_.fluidType) {
            return 1.0D;
         } else {
            int i = Math.abs(p_158022_.fluidLevel - p_158023_.fluidLevel);
            double d0 = 0.5D * (double)(p_158022_.fluidLevel + p_158023_.fluidLevel);
            double d1 = Math.abs(d0 - (double)p_158020_ - 0.5D);
            return 0.5D * (double)i * p_158021_ - d1;
         }
      }

      private int gridX(int p_158040_) {
         return Math.floorDiv(p_158040_, 16);
      }

      private int gridY(int p_158046_) {
         return Math.floorDiv(p_158046_, 12);
      }

      private int gridZ(int p_158048_) {
         return Math.floorDiv(p_158048_, 16);
      }

      private Aquifer.NoiseBasedAquifer.AquiferStatus getAquiferStatus(long p_158032_) {
         int i = BlockPos.getX(p_158032_);
         int j = BlockPos.getY(p_158032_);
         int k = BlockPos.getZ(p_158032_);
         int l = this.gridX(i);
         int i1 = this.gridY(j);
         int j1 = this.gridZ(k);
         int k1 = this.getIndex(l, i1, j1);
         Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus = this.aquiferCache[k1];
         if (aquifer$noisebasedaquifer$aquiferstatus != null) {
            return aquifer$noisebasedaquifer$aquiferstatus;
         } else {
            Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus1 = this.computeAquifer(i, j, k);
            this.aquiferCache[k1] = aquifer$noisebasedaquifer$aquiferstatus1;
            return aquifer$noisebasedaquifer$aquiferstatus1;
         }
      }

      private Aquifer.NoiseBasedAquifer.AquiferStatus computeAquifer(int p_158042_, int p_158043_, int p_158044_) {
         int i = this.noiseGeneratorSettings.seaLevel();
         if (p_158043_ > 30) {
            return new Aquifer.NoiseBasedAquifer.AquiferStatus(i, Blocks.WATER.defaultBlockState());
         } else {
            int j = 64;
            int k = -10;
            int l = 40;
            double d0 = this.waterLevelNoise.getValue((double)Math.floorDiv(p_158042_, 64), (double)Math.floorDiv(p_158043_, 40) / 1.4D, (double)Math.floorDiv(p_158044_, 64)) * 30.0D + -10.0D;
            boolean flag = false;
            if (Math.abs(d0) > 8.0D) {
               d0 *= 4.0D;
            }

            int i1 = Math.floorDiv(p_158043_, 40) * 40 + 20;
            int j1 = i1 + Mth.floor(d0);
            if (i1 == -20) {
               double d1 = this.lavaNoise.getValue((double)Math.floorDiv(p_158042_, 64), (double)Math.floorDiv(p_158043_, 40) / 1.4D, (double)Math.floorDiv(p_158044_, 64));
               flag = Math.abs(d1) > (double)0.22F;
            }

            return new Aquifer.NoiseBasedAquifer.AquiferStatus(Math.min(56, j1), flag ? Blocks.LAVA.defaultBlockState() : Blocks.WATER.defaultBlockState());
         }
      }

      static final class AquiferStatus {
         final int fluidLevel;
         final BlockState fluidType;

         public AquiferStatus(int p_158052_, BlockState p_158053_) {
            this.fluidLevel = p_158052_;
            this.fluidType = p_158053_;
         }
      }
   }
}