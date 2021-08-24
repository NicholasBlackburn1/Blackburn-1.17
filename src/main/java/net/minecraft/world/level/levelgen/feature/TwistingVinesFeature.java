package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TwistingVinesFeature extends Feature<NoneFeatureConfiguration> {
   public TwistingVinesFeature(Codec<NoneFeatureConfiguration> p_67292_) {
      super(p_67292_);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> p_160558_) {
      return place(p_160558_.level(), p_160558_.random(), p_160558_.origin(), 8, 4, 8);
   }

   public static boolean place(LevelAccessor p_67307_, Random p_67308_, BlockPos p_67309_, int p_67310_, int p_67311_, int p_67312_) {
      if (isInvalidPlacementLocation(p_67307_, p_67309_)) {
         return false;
      } else {
         placeTwistingVines(p_67307_, p_67308_, p_67309_, p_67310_, p_67311_, p_67312_);
         return true;
      }
   }

   private static void placeTwistingVines(LevelAccessor p_67326_, Random p_67327_, BlockPos p_67328_, int p_67329_, int p_67330_, int p_67331_) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < p_67329_ * p_67329_; ++i) {
         blockpos$mutableblockpos.set(p_67328_).move(Mth.nextInt(p_67327_, -p_67329_, p_67329_), Mth.nextInt(p_67327_, -p_67330_, p_67330_), Mth.nextInt(p_67327_, -p_67329_, p_67329_));
         if (findFirstAirBlockAboveGround(p_67326_, blockpos$mutableblockpos) && !isInvalidPlacementLocation(p_67326_, blockpos$mutableblockpos)) {
            int j = Mth.nextInt(p_67327_, 1, p_67331_);
            if (p_67327_.nextInt(6) == 0) {
               j *= 2;
            }

            if (p_67327_.nextInt(5) == 0) {
               j = 1;
            }

            int k = 17;
            int l = 25;
            placeWeepingVinesColumn(p_67326_, p_67327_, blockpos$mutableblockpos, j, 17, 25);
         }
      }

   }

   private static boolean findFirstAirBlockAboveGround(LevelAccessor p_67294_, BlockPos.MutableBlockPos p_67295_) {
      do {
         p_67295_.move(0, -1, 0);
         if (p_67294_.isOutsideBuildHeight(p_67295_)) {
            return false;
         }
      } while(p_67294_.getBlockState(p_67295_).isAir());

      p_67295_.move(0, 1, 0);
      return true;
   }

   public static void placeWeepingVinesColumn(LevelAccessor p_67300_, Random p_67301_, BlockPos.MutableBlockPos p_67302_, int p_67303_, int p_67304_, int p_67305_) {
      for(int i = 1; i <= p_67303_; ++i) {
         if (p_67300_.isEmptyBlock(p_67302_)) {
            if (i == p_67303_ || !p_67300_.isEmptyBlock(p_67302_.above())) {
               p_67300_.setBlock(p_67302_, Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(p_67301_, p_67304_, p_67305_))), 2);
               break;
            }

            p_67300_.setBlock(p_67302_, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
         }

         p_67302_.move(Direction.UP);
      }

   }

   private static boolean isInvalidPlacementLocation(LevelAccessor p_67297_, BlockPos p_67298_) {
      if (!p_67297_.isEmptyBlock(p_67298_)) {
         return true;
      } else {
         BlockState blockstate = p_67297_.getBlockState(p_67298_.below());
         return !blockstate.is(Blocks.NETHERRACK) && !blockstate.is(Blocks.WARPED_NYLIUM) && !blockstate.is(Blocks.WARPED_WART_BLOCK);
      }
   }
}