package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ErodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
   private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
   private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
   private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

   public ErodedBadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_74834_) {
      super(p_74834_);
   }

   public void apply(Random p_163934_, ChunkAccess p_163935_, Biome p_163936_, int p_163937_, int p_163938_, int p_163939_, double p_163940_, BlockState p_163941_, BlockState p_163942_, int p_163943_, int p_163944_, long p_163945_, SurfaceBuilderBaseConfiguration p_163946_) {
      double d0 = 0.0D;
      double d1 = Math.min(Math.abs(p_163940_), this.pillarNoise.getValue((double)p_163937_ * 0.25D, (double)p_163938_ * 0.25D, false) * 15.0D);
      if (d1 > 0.0D) {
         double d2 = 0.001953125D;
         double d3 = Math.abs(this.pillarRoofNoise.getValue((double)p_163937_ * 0.001953125D, (double)p_163938_ * 0.001953125D, false));
         d0 = d1 * d1 * 2.5D;
         double d4 = Math.ceil(d3 * 50.0D) + 14.0D;
         if (d0 > d4) {
            d0 = d4;
         }

         d0 = d0 + 64.0D;
      }

      int i1 = p_163937_ & 15;
      int i = p_163938_ & 15;
      BlockState blockstate4 = WHITE_TERRACOTTA;
      SurfaceBuilderConfiguration surfacebuilderconfiguration = p_163936_.getGenerationSettings().getSurfaceBuilderConfig();
      BlockState blockstate5 = surfacebuilderconfiguration.getUnderMaterial();
      BlockState blockstate = surfacebuilderconfiguration.getTopMaterial();
      BlockState blockstate1 = blockstate5;
      int j = (int)(p_163940_ / 3.0D + 3.0D + p_163934_.nextDouble() * 0.25D);
      boolean flag = Math.cos(p_163940_ / 3.0D * Math.PI) > 0.0D;
      int k = -1;
      boolean flag1 = false;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int l = Math.max(p_163939_, (int)d0 + 1); l >= p_163944_; --l) {
         blockpos$mutableblockpos.set(i1, l, i);
         if (p_163935_.getBlockState(blockpos$mutableblockpos).isAir() && l < (int)d0) {
            p_163935_.setBlockState(blockpos$mutableblockpos, p_163941_, false);
         }

         BlockState blockstate2 = p_163935_.getBlockState(blockpos$mutableblockpos);
         if (blockstate2.isAir()) {
            k = -1;
         } else if (blockstate2.is(p_163941_.getBlock())) {
            if (k == -1) {
               flag1 = false;
               if (j <= 0) {
                  blockstate4 = Blocks.AIR.defaultBlockState();
                  blockstate1 = p_163941_;
               } else if (l >= p_163943_ - 4 && l <= p_163943_ + 1) {
                  blockstate4 = WHITE_TERRACOTTA;
                  blockstate1 = blockstate5;
               }

               if (l < p_163943_ && (blockstate4 == null || blockstate4.isAir())) {
                  blockstate4 = p_163942_;
               }

               k = j + Math.max(0, l - p_163943_);
               if (l >= p_163943_ - 1) {
                  if (l <= p_163943_ + 3 + j) {
                     p_163935_.setBlockState(blockpos$mutableblockpos, blockstate, false);
                     flag1 = true;
                  } else {
                     BlockState blockstate3;
                     if (l >= 64 && l <= 127) {
                        if (flag) {
                           blockstate3 = TERRACOTTA;
                        } else {
                           blockstate3 = this.getBand(p_163937_, l, p_163938_);
                        }
                     } else {
                        blockstate3 = ORANGE_TERRACOTTA;
                     }

                     p_163935_.setBlockState(blockpos$mutableblockpos, blockstate3, false);
                  }
               } else {
                  p_163935_.setBlockState(blockpos$mutableblockpos, blockstate1, false);
                  if (blockstate1.is(Blocks.WHITE_TERRACOTTA) || blockstate1.is(Blocks.ORANGE_TERRACOTTA) || blockstate1.is(Blocks.MAGENTA_TERRACOTTA) || blockstate1.is(Blocks.LIGHT_BLUE_TERRACOTTA) || blockstate1.is(Blocks.YELLOW_TERRACOTTA) || blockstate1.is(Blocks.LIME_TERRACOTTA) || blockstate1.is(Blocks.PINK_TERRACOTTA) || blockstate1.is(Blocks.GRAY_TERRACOTTA) || blockstate1.is(Blocks.LIGHT_GRAY_TERRACOTTA) || blockstate1.is(Blocks.CYAN_TERRACOTTA) || blockstate1.is(Blocks.PURPLE_TERRACOTTA) || blockstate1.is(Blocks.BLUE_TERRACOTTA) || blockstate1.is(Blocks.BROWN_TERRACOTTA) || blockstate1.is(Blocks.GREEN_TERRACOTTA) || blockstate1.is(Blocks.RED_TERRACOTTA) || blockstate1.is(Blocks.BLACK_TERRACOTTA)) {
                     p_163935_.setBlockState(blockpos$mutableblockpos, ORANGE_TERRACOTTA, false);
                  }
               }
            } else if (k > 0) {
               --k;
               if (flag1) {
                  p_163935_.setBlockState(blockpos$mutableblockpos, ORANGE_TERRACOTTA, false);
               } else {
                  p_163935_.setBlockState(blockpos$mutableblockpos, this.getBand(p_163937_, l, p_163938_), false);
               }
            }
         }
      }

   }
}