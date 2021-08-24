package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class BadlandsSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   protected static final int MAX_CLAY_DEPTH = 15;
   private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
   private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
   private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
   private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
   private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
   private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
   private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
   protected BlockState[] clayBands;
   protected long seed;
   protected PerlinSimplexNoise pillarNoise;
   protected PerlinSimplexNoise pillarRoofNoise;
   protected PerlinSimplexNoise clayBandsOffsetNoise;

   public BadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_74716_) {
      super(p_74716_);
   }

   public void apply(Random p_163821_, ChunkAccess p_163822_, Biome p_163823_, int p_163824_, int p_163825_, int p_163826_, double p_163827_, BlockState p_163828_, BlockState p_163829_, int p_163830_, int p_163831_, long p_163832_, SurfaceBuilderBaseConfiguration p_163833_) {
      int i = p_163824_ & 15;
      int j = p_163825_ & 15;
      BlockState blockstate = WHITE_TERRACOTTA;
      SurfaceBuilderConfiguration surfacebuilderconfiguration = p_163823_.getGenerationSettings().getSurfaceBuilderConfig();
      BlockState blockstate1 = surfacebuilderconfiguration.getUnderMaterial();
      BlockState blockstate2 = surfacebuilderconfiguration.getTopMaterial();
      BlockState blockstate3 = blockstate1;
      int k = (int)(p_163827_ / 3.0D + 3.0D + p_163821_.nextDouble() * 0.25D);
      boolean flag = Math.cos(p_163827_ / 3.0D * Math.PI) > 0.0D;
      int l = -1;
      boolean flag1 = false;
      int i1 = 0;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j1 = p_163826_; j1 >= p_163831_; --j1) {
         if (i1 < 15) {
            blockpos$mutableblockpos.set(i, j1, j);
            BlockState blockstate4 = p_163822_.getBlockState(blockpos$mutableblockpos);
            if (blockstate4.isAir()) {
               l = -1;
            } else if (blockstate4.is(p_163828_.getBlock())) {
               if (l == -1) {
                  flag1 = false;
                  if (k <= 0) {
                     blockstate = Blocks.AIR.defaultBlockState();
                     blockstate3 = p_163828_;
                  } else if (j1 >= p_163830_ - 4 && j1 <= p_163830_ + 1) {
                     blockstate = WHITE_TERRACOTTA;
                     blockstate3 = blockstate1;
                  }

                  if (j1 < p_163830_ && (blockstate == null || blockstate.isAir())) {
                     blockstate = p_163829_;
                  }

                  l = k + Math.max(0, j1 - p_163830_);
                  if (j1 >= p_163830_ - 1) {
                     if (j1 > p_163830_ + 3 + k) {
                        BlockState blockstate5;
                        if (j1 >= 64 && j1 <= 127) {
                           if (flag) {
                              blockstate5 = TERRACOTTA;
                           } else {
                              blockstate5 = this.getBand(p_163824_, j1, p_163825_);
                           }
                        } else {
                           blockstate5 = ORANGE_TERRACOTTA;
                        }

                        p_163822_.setBlockState(blockpos$mutableblockpos, blockstate5, false);
                     } else {
                        p_163822_.setBlockState(blockpos$mutableblockpos, blockstate2, false);
                        flag1 = true;
                     }
                  } else {
                     p_163822_.setBlockState(blockpos$mutableblockpos, blockstate3, false);
                     if (blockstate3.is(Blocks.WHITE_TERRACOTTA) || blockstate3.is(Blocks.ORANGE_TERRACOTTA) || blockstate3.is(Blocks.MAGENTA_TERRACOTTA) || blockstate3.is(Blocks.LIGHT_BLUE_TERRACOTTA) || blockstate3.is(Blocks.YELLOW_TERRACOTTA) || blockstate3.is(Blocks.LIME_TERRACOTTA) || blockstate3.is(Blocks.PINK_TERRACOTTA) || blockstate3.is(Blocks.GRAY_TERRACOTTA) || blockstate3.is(Blocks.LIGHT_GRAY_TERRACOTTA) || blockstate3.is(Blocks.CYAN_TERRACOTTA) || blockstate3.is(Blocks.PURPLE_TERRACOTTA) || blockstate3.is(Blocks.BLUE_TERRACOTTA) || blockstate3.is(Blocks.BROWN_TERRACOTTA) || blockstate3.is(Blocks.GREEN_TERRACOTTA) || blockstate3.is(Blocks.RED_TERRACOTTA) || blockstate3.is(Blocks.BLACK_TERRACOTTA)) {
                        p_163822_.setBlockState(blockpos$mutableblockpos, ORANGE_TERRACOTTA, false);
                     }
                  }
               } else if (l > 0) {
                  --l;
                  if (flag1) {
                     p_163822_.setBlockState(blockpos$mutableblockpos, ORANGE_TERRACOTTA, false);
                  } else {
                     p_163822_.setBlockState(blockpos$mutableblockpos, this.getBand(p_163824_, j1, p_163825_), false);
                  }
               }

               ++i1;
            }
         }
      }

   }

   public void initNoise(long p_74722_) {
      if (this.seed != p_74722_ || this.clayBands == null) {
         this.generateBands(p_74722_);
      }

      if (this.seed != p_74722_ || this.pillarNoise == null || this.pillarRoofNoise == null) {
         WorldgenRandom worldgenrandom = new WorldgenRandom(p_74722_);
         this.pillarNoise = new PerlinSimplexNoise(worldgenrandom, IntStream.rangeClosed(-3, 0));
         this.pillarRoofNoise = new PerlinSimplexNoise(worldgenrandom, ImmutableList.of(0));
      }

      this.seed = p_74722_;
   }

   protected void generateBands(long p_74750_) {
      this.clayBands = new BlockState[64];
      Arrays.fill(this.clayBands, TERRACOTTA);
      WorldgenRandom worldgenrandom = new WorldgenRandom(p_74750_);
      this.clayBandsOffsetNoise = new PerlinSimplexNoise(worldgenrandom, ImmutableList.of(0));

      for(int l1 = 0; l1 < 64; ++l1) {
         l1 += worldgenrandom.nextInt(5) + 1;
         if (l1 < 64) {
            this.clayBands[l1] = ORANGE_TERRACOTTA;
         }
      }

      int i2 = worldgenrandom.nextInt(4) + 2;

      for(int i = 0; i < i2; ++i) {
         int j = worldgenrandom.nextInt(3) + 1;
         int k = worldgenrandom.nextInt(64);

         for(int l = 0; k + l < 64 && l < j; ++l) {
            this.clayBands[k + l] = YELLOW_TERRACOTTA;
         }
      }

      int j2 = worldgenrandom.nextInt(4) + 2;

      for(int k2 = 0; k2 < j2; ++k2) {
         int i3 = worldgenrandom.nextInt(3) + 2;
         int l3 = worldgenrandom.nextInt(64);

         for(int i1 = 0; l3 + i1 < 64 && i1 < i3; ++i1) {
            this.clayBands[l3 + i1] = BROWN_TERRACOTTA;
         }
      }

      int l2 = worldgenrandom.nextInt(4) + 2;

      for(int j3 = 0; j3 < l2; ++j3) {
         int i4 = worldgenrandom.nextInt(3) + 1;
         int k4 = worldgenrandom.nextInt(64);

         for(int j1 = 0; k4 + j1 < 64 && j1 < i4; ++j1) {
            this.clayBands[k4 + j1] = RED_TERRACOTTA;
         }
      }

      int k3 = worldgenrandom.nextInt(3) + 3;
      int j4 = 0;

      for(int l4 = 0; l4 < k3; ++l4) {
         int i5 = 1;
         j4 += worldgenrandom.nextInt(16) + 4;

         for(int k1 = 0; j4 + k1 < 64 && k1 < 1; ++k1) {
            this.clayBands[j4 + k1] = WHITE_TERRACOTTA;
            if (j4 + k1 > 1 && worldgenrandom.nextBoolean()) {
               this.clayBands[j4 + k1 - 1] = LIGHT_GRAY_TERRACOTTA;
            }

            if (j4 + k1 < 63 && worldgenrandom.nextBoolean()) {
               this.clayBands[j4 + k1 + 1] = LIGHT_GRAY_TERRACOTTA;
            }
         }
      }

   }

   protected BlockState getBand(int p_74718_, int p_74719_, int p_74720_) {
      int i = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)p_74718_ / 512.0D, (double)p_74720_ / 512.0D, false) * 2.0D);
      return this.clayBands[(p_74719_ + i + 64) % 64];
   }
}