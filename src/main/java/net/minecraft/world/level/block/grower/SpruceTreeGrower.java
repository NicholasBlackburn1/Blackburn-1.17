package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class SpruceTreeGrower extends AbstractMegaTreeGrower {
   @Nullable
   protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random p_60044_, boolean p_60045_) {
      return Features.SPRUCE;
   }

   @Nullable
   protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random p_60042_) {
      return p_60042_.nextBoolean() ? Features.MEGA_SPRUCE : Features.MEGA_PINE;
   }
}