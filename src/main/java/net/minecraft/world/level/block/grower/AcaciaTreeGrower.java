package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class AcaciaTreeGrower extends AbstractTreeGrower {
   @Nullable
   protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random p_60018_, boolean p_60019_) {
      return Features.ACACIA;
   }
}