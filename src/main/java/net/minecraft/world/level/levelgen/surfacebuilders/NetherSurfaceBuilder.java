package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NetherSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
   private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
   private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
   protected long seed;
   protected PerlinNoise decorationNoise;

   public NetherSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_75072_) {
      super(p_75072_);
   }

   public void apply(Random p_164130_, ChunkAccess p_164131_, Biome p_164132_, int p_164133_, int p_164134_, int p_164135_, double p_164136_, BlockState p_164137_, BlockState p_164138_, int p_164139_, int p_164140_, long p_164141_, SurfaceBuilderBaseConfiguration p_164142_) {
      int i = p_164139_;
      int j = p_164133_ & 15;
      int k = p_164134_ & 15;
      double d0 = 0.03125D;
      boolean flag = this.decorationNoise.getValue((double)p_164133_ * 0.03125D, (double)p_164134_ * 0.03125D, 0.0D) * 75.0D + p_164130_.nextDouble() > 0.0D;
      boolean flag1 = this.decorationNoise.getValue((double)p_164133_ * 0.03125D, 109.0D, (double)p_164134_ * 0.03125D) * 75.0D + p_164130_.nextDouble() > 0.0D;
      int l = (int)(p_164136_ / 3.0D + 3.0D + p_164130_.nextDouble() * 0.25D);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int i1 = -1;
      BlockState blockstate = p_164142_.getTopMaterial();
      BlockState blockstate1 = p_164142_.getUnderMaterial();

      for(int j1 = 127; j1 >= p_164140_; --j1) {
         blockpos$mutableblockpos.set(j, j1, k);
         BlockState blockstate2 = p_164131_.getBlockState(blockpos$mutableblockpos);
         if (blockstate2.isAir()) {
            i1 = -1;
         } else if (blockstate2.is(p_164137_.getBlock())) {
            if (i1 == -1) {
               boolean flag2 = false;
               if (l <= 0) {
                  flag2 = true;
                  blockstate1 = p_164142_.getUnderMaterial();
               } else if (j1 >= i - 4 && j1 <= i + 1) {
                  blockstate = p_164142_.getTopMaterial();
                  blockstate1 = p_164142_.getUnderMaterial();
                  if (flag1) {
                     blockstate = GRAVEL;
                     blockstate1 = p_164142_.getUnderMaterial();
                  }

                  if (flag) {
                     blockstate = SOUL_SAND;
                     blockstate1 = SOUL_SAND;
                  }
               }

               if (j1 < i && flag2) {
                  blockstate = p_164138_;
               }

               i1 = l;
               if (j1 >= i - 1) {
                  p_164131_.setBlockState(blockpos$mutableblockpos, blockstate, false);
               } else {
                  p_164131_.setBlockState(blockpos$mutableblockpos, blockstate1, false);
               }
            } else if (i1 > 0) {
               --i1;
               p_164131_.setBlockState(blockpos$mutableblockpos, blockstate1, false);
            }
         }
      }

   }

   public void initNoise(long p_75074_) {
      if (this.seed != p_75074_ || this.decorationNoise == null) {
         this.decorationNoise = new PerlinNoise(new WorldgenRandom(p_75074_), IntStream.rangeClosed(-3, 0));
      }

      this.seed = p_75074_;
   }
}