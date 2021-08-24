package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherForestSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected long seed;
   private PerlinNoise decorationNoise;

   public NetherForestSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_75036_) {
      super(p_75036_);
   }

   public void apply(Random p_164102_, ChunkAccess p_164103_, Biome p_164104_, int p_164105_, int p_164106_, int p_164107_, double p_164108_, BlockState p_164109_, BlockState p_164110_, int p_164111_, int p_164112_, long p_164113_, SurfaceBuilderBaseConfiguration p_164114_) {
      int i = p_164111_;
      int j = p_164105_ & 15;
      int k = p_164106_ & 15;
      double d0 = this.decorationNoise.getValue((double)p_164105_ * 0.1D, (double)p_164111_, (double)p_164106_ * 0.1D);
      boolean flag = d0 > 0.15D + p_164102_.nextDouble() * 0.35D;
      double d1 = this.decorationNoise.getValue((double)p_164105_ * 0.1D, 109.0D, (double)p_164106_ * 0.1D);
      boolean flag1 = d1 > 0.25D + p_164102_.nextDouble() * 0.9D;
      int l = (int)(p_164108_ / 3.0D + 3.0D + p_164102_.nextDouble() * 0.25D);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int i1 = -1;
      BlockState blockstate = p_164114_.getUnderMaterial();

      for(int j1 = 127; j1 >= p_164112_; --j1) {
         blockpos$mutableblockpos.set(j, j1, k);
         BlockState blockstate1 = p_164114_.getTopMaterial();
         BlockState blockstate2 = p_164103_.getBlockState(blockpos$mutableblockpos);
         if (blockstate2.isAir()) {
            i1 = -1;
         } else if (blockstate2.is(p_164109_.getBlock())) {
            if (i1 == -1) {
               boolean flag2 = false;
               if (l <= 0) {
                  flag2 = true;
                  blockstate = p_164114_.getUnderMaterial();
               }

               if (flag) {
                  blockstate1 = p_164114_.getUnderMaterial();
               } else if (flag1) {
                  blockstate1 = p_164114_.getUnderwaterMaterial();
               }

               if (j1 < i && flag2) {
                  blockstate1 = p_164110_;
               }

               i1 = l;
               if (j1 >= i - 1) {
                  p_164103_.setBlockState(blockpos$mutableblockpos, blockstate1, false);
               } else {
                  p_164103_.setBlockState(blockpos$mutableblockpos, blockstate, false);
               }
            } else if (i1 > 0) {
               --i1;
               p_164103_.setBlockState(blockpos$mutableblockpos, blockstate, false);
            }
         }
      }

   }

   public void initNoise(long p_75038_) {
      if (this.seed != p_75038_ || this.decorationNoise == null) {
         this.decorationNoise = new PerlinNoise(new WorldgenRandom(p_75038_), ImmutableList.of(0));
      }

      this.seed = p_75038_;
   }
}