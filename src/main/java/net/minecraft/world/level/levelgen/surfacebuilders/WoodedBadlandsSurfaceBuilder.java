package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class WoodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
   private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
   private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
   private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

   public WoodedBadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_75294_) {
      super(p_75294_);
   }

   public void apply(Random p_164261_, ChunkAccess p_164262_, Biome p_164263_, int p_164264_, int p_164265_, int p_164266_, double p_164267_, BlockState p_164268_, BlockState p_164269_, int p_164270_, int p_164271_, long p_164272_, SurfaceBuilderBaseConfiguration p_164273_) {
      int i = p_164264_ & 15;
      int j = p_164265_ & 15;
      BlockState blockstate = WHITE_TERRACOTTA;
      SurfaceBuilderConfiguration surfacebuilderconfiguration = p_164263_.getGenerationSettings().getSurfaceBuilderConfig();
      BlockState blockstate1 = surfacebuilderconfiguration.getUnderMaterial();
      BlockState blockstate2 = surfacebuilderconfiguration.getTopMaterial();
      BlockState blockstate3 = blockstate1;
      int k = (int)(p_164267_ / 3.0D + 3.0D + p_164261_.nextDouble() * 0.25D);
      boolean flag = Math.cos(p_164267_ / 3.0D * Math.PI) > 0.0D;
      int l = -1;
      boolean flag1 = false;
      int i1 = 0;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j1 = p_164266_; j1 >= p_164271_; --j1) {
         if (i1 < 15) {
            blockpos$mutableblockpos.set(i, j1, j);
            BlockState blockstate4 = p_164262_.getBlockState(blockpos$mutableblockpos);
            if (blockstate4.isAir()) {
               l = -1;
            } else if (blockstate4.is(p_164268_.getBlock())) {
               if (l == -1) {
                  flag1 = false;
                  if (k <= 0) {
                     blockstate = Blocks.AIR.defaultBlockState();
                     blockstate3 = p_164268_;
                  } else if (j1 >= p_164270_ - 4 && j1 <= p_164270_ + 1) {
                     blockstate = WHITE_TERRACOTTA;
                     blockstate3 = blockstate1;
                  }

                  if (j1 < p_164270_ && (blockstate == null || blockstate.isAir())) {
                     blockstate = p_164269_;
                  }

                  l = k + Math.max(0, j1 - p_164270_);
                  if (j1 >= p_164270_ - 1) {
                     if (j1 > 86 + k * 2) {
                        if (flag) {
                           p_164262_.setBlockState(blockpos$mutableblockpos, Blocks.COARSE_DIRT.defaultBlockState(), false);
                        } else {
                           p_164262_.setBlockState(blockpos$mutableblockpos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
                        }
                     } else if (j1 > p_164270_ + 3 + k) {
                        BlockState blockstate5;
                        if (j1 >= 64 && j1 <= 127) {
                           if (flag) {
                              blockstate5 = TERRACOTTA;
                           } else {
                              blockstate5 = this.getBand(p_164264_, j1, p_164265_);
                           }
                        } else {
                           blockstate5 = ORANGE_TERRACOTTA;
                        }

                        p_164262_.setBlockState(blockpos$mutableblockpos, blockstate5, false);
                     } else {
                        p_164262_.setBlockState(blockpos$mutableblockpos, blockstate2, false);
                        flag1 = true;
                     }
                  } else {
                     p_164262_.setBlockState(blockpos$mutableblockpos, blockstate3, false);
                     if (blockstate3 == WHITE_TERRACOTTA) {
                        p_164262_.setBlockState(blockpos$mutableblockpos, ORANGE_TERRACOTTA, false);
                     }
                  }
               } else if (l > 0) {
                  --l;
                  if (flag1) {
                     p_164262_.setBlockState(blockpos$mutableblockpos, ORANGE_TERRACOTTA, false);
                  } else {
                     p_164262_.setBlockState(blockpos$mutableblockpos, this.getBand(p_164264_, j1, p_164265_), false);
                  }
               }

               ++i1;
            }
         }
      }

   }
}