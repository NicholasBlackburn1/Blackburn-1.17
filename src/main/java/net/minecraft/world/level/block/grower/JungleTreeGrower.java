package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class JungleTreeGrower extends AbstractMegaTreeGrower {
   @Nullable
   protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random p_60034_, boolean p_60035_) {
      return Features.JUNGLE_TREE_NO_VINE;
   }

   @Nullable
   protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random p_60032_) {
      return Features.MEGA_JUNGLE_TREE;
   }
}