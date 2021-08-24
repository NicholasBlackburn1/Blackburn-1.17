package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
   @Nullable
   protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random p_60028_, boolean p_60029_) {
      return null;
   }

   @Nullable
   protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random p_60026_) {
      return Features.DARK_OAK;
   }
}