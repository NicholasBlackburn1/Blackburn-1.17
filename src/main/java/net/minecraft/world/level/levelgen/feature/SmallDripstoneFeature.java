package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SmallDripstoneConfiguration;

public class SmallDripstoneFeature extends Feature<SmallDripstoneConfiguration> {
   public SmallDripstoneFeature(Codec<SmallDripstoneConfiguration> p_160345_) {
      super(p_160345_);
   }

   public boolean place(FeaturePlaceContext<SmallDripstoneConfiguration> p_160362_) {
      WorldGenLevel worldgenlevel = p_160362_.level();
      BlockPos blockpos = p_160362_.origin();
      Random random = p_160362_.random();
      SmallDripstoneConfiguration smalldripstoneconfiguration = p_160362_.config();
      if (!DripstoneUtils.isEmptyOrWater(worldgenlevel, blockpos)) {
         return false;
      } else {
         int i = Mth.randomBetweenInclusive(random, 1, smalldripstoneconfiguration.maxPlacements);
         boolean flag = false;

         for(int j = 0; j < i; ++j) {
            BlockPos blockpos1 = randomOffset(random, blockpos, smalldripstoneconfiguration);
            if (searchAndTryToPlaceDripstone(worldgenlevel, random, blockpos1, smalldripstoneconfiguration)) {
               flag = true;
            }
         }

         return flag;
      }
   }

   private static boolean searchAndTryToPlaceDripstone(WorldGenLevel p_160351_, Random p_160352_, BlockPos p_160353_, SmallDripstoneConfiguration p_160354_) {
      Direction direction = Direction.getRandom(p_160352_);
      Direction direction1 = p_160352_.nextBoolean() ? Direction.UP : Direction.DOWN;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = p_160353_.mutable();

      for(int i = 0; i < p_160354_.emptySpaceSearchRadius; ++i) {
         if (!DripstoneUtils.isEmptyOrWater(p_160351_, blockpos$mutableblockpos)) {
            return false;
         }

         if (tryToPlaceDripstone(p_160351_, p_160352_, blockpos$mutableblockpos, direction1, p_160354_)) {
            return true;
         }

         if (tryToPlaceDripstone(p_160351_, p_160352_, blockpos$mutableblockpos, direction1.getOpposite(), p_160354_)) {
            return true;
         }

         blockpos$mutableblockpos.move(direction);
      }

      return false;
   }

   private static boolean tryToPlaceDripstone(WorldGenLevel p_160356_, Random p_160357_, BlockPos p_160358_, Direction p_160359_, SmallDripstoneConfiguration p_160360_) {
      if (!DripstoneUtils.isEmptyOrWater(p_160356_, p_160358_)) {
         return false;
      } else {
         BlockPos blockpos = p_160358_.relative(p_160359_.getOpposite());
         BlockState blockstate = p_160356_.getBlockState(blockpos);
         if (!DripstoneUtils.isDripstoneBase(blockstate)) {
            return false;
         } else {
            createPatchOfDripstoneBlocks(p_160356_, p_160357_, blockpos);
            int i = p_160357_.nextFloat() < p_160360_.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(p_160356_, p_160358_.relative(p_160359_)) ? 2 : 1;
            DripstoneUtils.growPointedDripstone(p_160356_, p_160358_, p_160359_, i, false);
            return true;
         }
      }
   }

   private static void createPatchOfDripstoneBlocks(WorldGenLevel p_160347_, Random p_160348_, BlockPos p_160349_) {
      DripstoneUtils.placeDripstoneBlockIfPossible(p_160347_, p_160349_);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (!(p_160348_.nextFloat() < 0.3F)) {
            BlockPos blockpos = p_160349_.relative(direction);
            DripstoneUtils.placeDripstoneBlockIfPossible(p_160347_, blockpos);
            if (!p_160348_.nextBoolean()) {
               BlockPos blockpos1 = blockpos.relative(Direction.getRandom(p_160348_));
               DripstoneUtils.placeDripstoneBlockIfPossible(p_160347_, blockpos1);
               if (!p_160348_.nextBoolean()) {
                  BlockPos blockpos2 = blockpos1.relative(Direction.getRandom(p_160348_));
                  DripstoneUtils.placeDripstoneBlockIfPossible(p_160347_, blockpos2);
               }
            }
         }
      }

   }

   private static BlockPos randomOffset(Random p_160364_, BlockPos p_160365_, SmallDripstoneConfiguration p_160366_) {
      return p_160365_.offset(Mth.randomBetweenInclusive(p_160364_, -p_160366_.maxOffsetFromOrigin, p_160366_.maxOffsetFromOrigin), Mth.randomBetweenInclusive(p_160364_, -p_160366_.maxOffsetFromOrigin, p_160366_.maxOffsetFromOrigin), Mth.randomBetweenInclusive(p_160364_, -p_160366_.maxOffsetFromOrigin, p_160366_.maxOffsetFromOrigin));
   }
}