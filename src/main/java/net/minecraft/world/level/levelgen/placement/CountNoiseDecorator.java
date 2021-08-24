package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;

public class CountNoiseDecorator extends RepeatingDecorator<NoiseDependantDecoratorConfiguration> {
   public CountNoiseDecorator(Codec<NoiseDependantDecoratorConfiguration> p_70504_) {
      super(p_70504_);
   }

   protected int count(Random p_162151_, NoiseDependantDecoratorConfiguration p_162152_, BlockPos p_162153_) {
      double d0 = Biome.BIOME_INFO_NOISE.getValue((double)p_162153_.getX() / 200.0D, (double)p_162153_.getZ() / 200.0D, false);
      return d0 < p_162152_.noiseLevel ? p_162152_.belowNoise : p_162152_.aboveNoise;
   }
}