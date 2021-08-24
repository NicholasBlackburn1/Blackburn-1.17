package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class AbstractFlowerFeature<U extends FeatureConfiguration> extends Feature<U> {
   public AbstractFlowerFeature(Codec<U> p_65075_) {
      super(p_65075_);
   }

   public boolean place(FeaturePlaceContext<U> p_159434_) {
      Random random = p_159434_.random();
      BlockPos blockpos = p_159434_.origin();
      WorldGenLevel worldgenlevel = p_159434_.level();
      U u = p_159434_.config();
      BlockState blockstate = this.getRandomFlower(random, blockpos, u);
      int i = 0;

      for(int j = 0; j < this.getCount(u); ++j) {
         BlockPos blockpos1 = this.getPos(random, blockpos, u);
         if (worldgenlevel.isEmptyBlock(blockpos1) && blockstate.canSurvive(worldgenlevel, blockpos1) && this.isValid(worldgenlevel, blockpos1, u)) {
            worldgenlevel.setBlock(blockpos1, blockstate, 2);
            ++i;
         }
      }

      return i > 0;
   }

   public abstract boolean isValid(LevelAccessor p_65076_, BlockPos p_65077_, U p_65078_);

   public abstract int getCount(U p_65085_);

   public abstract BlockPos getPos(Random p_65086_, BlockPos p_65087_, U p_65088_);

   public abstract BlockState getRandomFlower(Random p_65089_, BlockPos p_65090_, U p_65091_);
}