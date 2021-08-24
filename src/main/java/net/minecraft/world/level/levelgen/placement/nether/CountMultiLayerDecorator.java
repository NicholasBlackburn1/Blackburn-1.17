package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class CountMultiLayerDecorator extends FeatureDecorator<CountConfiguration> {
   public CountMultiLayerDecorator(Codec<CountConfiguration> p_70892_) {
      super(p_70892_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_70902_, Random p_70903_, CountConfiguration p_70904_, BlockPos p_70905_) {
      List<BlockPos> list = Lists.newArrayList();
      int i = 0;

      boolean flag;
      do {
         flag = false;

         for(int j = 0; j < p_70904_.count().sample(p_70903_); ++j) {
            int k = p_70903_.nextInt(16) + p_70905_.getX();
            int l = p_70903_.nextInt(16) + p_70905_.getZ();
            int i1 = p_70902_.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
            int j1 = findOnGroundYPosition(p_70902_, k, i1, l, i);
            if (j1 != Integer.MAX_VALUE) {
               list.add(new BlockPos(k, j1, l));
               flag = true;
            }
         }

         ++i;
      } while(flag);

      return list.stream();
   }

   private static int findOnGroundYPosition(DecorationContext p_70896_, int p_70897_, int p_70898_, int p_70899_, int p_70900_) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(p_70897_, p_70898_, p_70899_);
      int i = 0;
      BlockState blockstate = p_70896_.getBlockState(blockpos$mutableblockpos);

      for(int j = p_70898_; j >= p_70896_.getMinBuildHeight() + 1; --j) {
         blockpos$mutableblockpos.setY(j - 1);
         BlockState blockstate1 = p_70896_.getBlockState(blockpos$mutableblockpos);
         if (!isEmpty(blockstate1) && isEmpty(blockstate) && !blockstate1.is(Blocks.BEDROCK)) {
            if (i == p_70900_) {
               return blockpos$mutableblockpos.getY() + 1;
            }

            ++i;
         }

         blockstate = blockstate1;
      }

      return Integer.MAX_VALUE;
   }

   private static boolean isEmpty(BlockState p_70894_) {
      return p_70894_.isAir() || p_70894_.is(Blocks.WATER) || p_70894_.is(Blocks.LAVA);
   }
}