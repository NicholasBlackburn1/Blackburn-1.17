package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class DefaultFlowerFeature extends AbstractFlowerFeature<RandomPatchConfiguration> {
   public DefaultFlowerFeature(Codec<RandomPatchConfiguration> p_65517_) {
      super(p_65517_);
   }

   public boolean isValid(LevelAccessor p_65523_, BlockPos p_65524_, RandomPatchConfiguration p_65525_) {
      return !p_65525_.blacklist.contains(p_65523_.getBlockState(p_65524_));
   }

   public int getCount(RandomPatchConfiguration p_65529_) {
      return p_65529_.tries;
   }

   public BlockPos getPos(Random p_65535_, BlockPos p_65536_, RandomPatchConfiguration p_65537_) {
      return p_65536_.offset(p_65535_.nextInt(p_65537_.xspread) - p_65535_.nextInt(p_65537_.xspread), p_65535_.nextInt(p_65537_.yspread) - p_65535_.nextInt(p_65537_.yspread), p_65535_.nextInt(p_65537_.zspread) - p_65535_.nextInt(p_65537_.zspread));
   }

   public BlockState getRandomFlower(Random p_65543_, BlockPos p_65544_, RandomPatchConfiguration p_65545_) {
      return p_65545_.stateProvider.getState(p_65543_, p_65544_);
   }
}