package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.NetherForestVegetationFeature;
import net.minecraft.world.level.levelgen.feature.TwistingVinesFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;

public class NyliumBlock extends Block implements BonemealableBlock {
   protected NyliumBlock(BlockBehaviour.Properties p_55057_) {
      super(p_55057_);
   }

   private static boolean canBeNylium(BlockState p_55079_, LevelReader p_55080_, BlockPos p_55081_) {
      BlockPos blockpos = p_55081_.above();
      BlockState blockstate = p_55080_.getBlockState(blockpos);
      int i = LayerLightEngine.getLightBlockInto(p_55080_, p_55079_, p_55081_, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(p_55080_, blockpos));
      return i < p_55080_.getMaxLightLevel();
   }

   public void randomTick(BlockState p_55074_, ServerLevel p_55075_, BlockPos p_55076_, Random p_55077_) {
      if (!canBeNylium(p_55074_, p_55075_, p_55076_)) {
         p_55075_.setBlockAndUpdate(p_55076_, Blocks.NETHERRACK.defaultBlockState());
      }

   }

   public boolean isValidBonemealTarget(BlockGetter p_55064_, BlockPos p_55065_, BlockState p_55066_, boolean p_55067_) {
      return p_55064_.getBlockState(p_55065_.above()).isAir();
   }

   public boolean isBonemealSuccess(Level p_55069_, Random p_55070_, BlockPos p_55071_, BlockState p_55072_) {
      return true;
   }

   public void performBonemeal(ServerLevel p_55059_, Random p_55060_, BlockPos p_55061_, BlockState p_55062_) {
      BlockState blockstate = p_55059_.getBlockState(p_55061_);
      BlockPos blockpos = p_55061_.above();
      if (blockstate.is(Blocks.CRIMSON_NYLIUM)) {
         NetherForestVegetationFeature.place(p_55059_, p_55060_, blockpos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
      } else if (blockstate.is(Blocks.WARPED_NYLIUM)) {
         NetherForestVegetationFeature.place(p_55059_, p_55060_, blockpos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
         NetherForestVegetationFeature.place(p_55059_, p_55060_, blockpos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);
         if (p_55060_.nextInt(8) == 0) {
            TwistingVinesFeature.place(p_55059_, p_55060_, blockpos, 3, 1, 2);
         }
      }

   }
}