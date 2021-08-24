package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_74788_) {
      super(p_74788_);
   }

   public void apply(Random p_163891_, ChunkAccess p_163892_, Biome p_163893_, int p_163894_, int p_163895_, int p_163896_, double p_163897_, BlockState p_163898_, BlockState p_163899_, int p_163900_, int p_163901_, long p_163902_, SurfaceBuilderBaseConfiguration p_163903_) {
      this.apply(p_163891_, p_163892_, p_163893_, p_163894_, p_163895_, p_163896_, p_163897_, p_163898_, p_163899_, p_163903_.getTopMaterial(), p_163903_.getUnderMaterial(), p_163903_.getUnderwaterMaterial(), p_163900_, p_163901_);
   }

   protected void apply(Random p_163919_, ChunkAccess p_163920_, Biome p_163921_, int p_163922_, int p_163923_, int p_163924_, double p_163925_, BlockState p_163926_, BlockState p_163927_, BlockState p_163928_, BlockState p_163929_, BlockState p_163930_, int p_163931_, int p_163932_) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int i = (int)(p_163925_ / 3.0D + 3.0D + p_163919_.nextDouble() * 0.25D);
      if (i == 0) {
         boolean flag = false;

         for(int j = p_163924_; j >= p_163932_; --j) {
            blockpos$mutableblockpos.set(p_163922_, j, p_163923_);
            BlockState blockstate = p_163920_.getBlockState(blockpos$mutableblockpos);
            if (blockstate.isAir()) {
               flag = false;
            } else if (blockstate.is(p_163926_.getBlock())) {
               if (!flag) {
                  BlockState blockstate1;
                  if (j >= p_163931_) {
                     blockstate1 = Blocks.AIR.defaultBlockState();
                  } else if (j == p_163931_ - 1) {
                     blockstate1 = p_163921_.getTemperature(blockpos$mutableblockpos) < 0.15F ? Blocks.ICE.defaultBlockState() : p_163927_;
                  } else if (j >= p_163931_ - (7 + i)) {
                     blockstate1 = p_163926_;
                  } else {
                     blockstate1 = p_163930_;
                  }

                  p_163920_.setBlockState(blockpos$mutableblockpos, blockstate1, false);
               }

               flag = true;
            }
         }
      } else {
         BlockState blockstate3 = p_163929_;
         int k = -1;

         for(int l = p_163924_; l >= p_163932_; --l) {
            blockpos$mutableblockpos.set(p_163922_, l, p_163923_);
            BlockState blockstate4 = p_163920_.getBlockState(blockpos$mutableblockpos);
            if (blockstate4.isAir()) {
               k = -1;
            } else if (blockstate4.is(p_163926_.getBlock())) {
               if (k == -1) {
                  k = i;
                  BlockState blockstate2;
                  if (l >= p_163931_ + 2) {
                     blockstate2 = p_163928_;
                  } else if (l >= p_163931_ - 1) {
                     blockstate3 = p_163929_;
                     blockstate2 = p_163928_;
                  } else if (l >= p_163931_ - 4) {
                     blockstate3 = p_163929_;
                     blockstate2 = p_163929_;
                  } else if (l >= p_163931_ - (7 + i)) {
                     blockstate2 = blockstate3;
                  } else {
                     blockstate3 = p_163926_;
                     blockstate2 = p_163930_;
                  }

                  p_163920_.setBlockState(blockpos$mutableblockpos, blockstate2, false);
               } else if (k > 0) {
                  --k;
                  p_163920_.setBlockState(blockpos$mutableblockpos, blockstate3, false);
                  if (k == 0 && blockstate3.is(Blocks.SAND) && i > 1) {
                     k = p_163919_.nextInt(4) + Math.max(0, l - p_163931_);
                     blockstate3 = blockstate3.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                  }
               }
            }
         }
      }

   }
}