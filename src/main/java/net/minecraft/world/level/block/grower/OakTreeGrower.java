package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class OakTreeGrower extends AbstractTreeGrower {
   @Nullable
   protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random p_60038_, boolean p_60039_) {
      if (p_60038_.nextInt(10) == 0) {
         return p_60039_ ? Features.FANCY_OAK_BEES_005 : Features.FANCY_OAK;
      } else {
         return p_60039_ ? Features.OAK_BEES_005 : Features.OAK;
      }
   }
}