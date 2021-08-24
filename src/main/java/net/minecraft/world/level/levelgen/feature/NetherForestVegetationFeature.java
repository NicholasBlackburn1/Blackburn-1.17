package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class NetherForestVegetationFeature extends Feature<BlockPileConfiguration> {
   public NetherForestVegetationFeature(Codec<BlockPileConfiguration> p_66361_) {
      super(p_66361_);
   }

   public boolean place(FeaturePlaceContext<BlockPileConfiguration> p_160068_) {
      return place(p_160068_.level(), p_160068_.random(), p_160068_.origin(), p_160068_.config(), 8, 4);
   }

   public static boolean place(LevelAccessor p_66363_, Random p_66364_, BlockPos p_66365_, BlockPileConfiguration p_66366_, int p_66367_, int p_66368_) {
      BlockState blockstate = p_66363_.getBlockState(p_66365_.below());
      if (!blockstate.is(BlockTags.NYLIUM)) {
         return false;
      } else {
         int i = p_66365_.getY();
         if (i >= p_66363_.getMinBuildHeight() + 1 && i + 1 < p_66363_.getMaxBuildHeight()) {
            int j = 0;

            for(int k = 0; k < p_66367_ * p_66367_; ++k) {
               BlockPos blockpos = p_66365_.offset(p_66364_.nextInt(p_66367_) - p_66364_.nextInt(p_66367_), p_66364_.nextInt(p_66368_) - p_66364_.nextInt(p_66368_), p_66364_.nextInt(p_66367_) - p_66364_.nextInt(p_66367_));
               BlockState blockstate1 = p_66366_.stateProvider.getState(p_66364_, blockpos);
               if (p_66363_.isEmptyBlock(blockpos) && blockpos.getY() > p_66363_.getMinBuildHeight() && blockstate1.canSurvive(p_66363_, blockpos)) {
                  p_66363_.setBlock(blockpos, blockstate1, 2);
                  ++j;
               }
            }

            return j > 0;
         } else {
            return false;
         }
      }
   }
}