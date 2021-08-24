package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class NoiseBasedDecorator extends RepeatingDecorator<NoiseCountFactorDecoratorConfiguration> {
   public NoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> p_70794_) {
      super(p_70794_);
   }

   protected int count(Random p_162260_, NoiseCountFactorDecoratorConfiguration p_162261_, BlockPos p_162262_) {
      double d0 = Biome.BIOME_INFO_NOISE.getValue((double)p_162262_.getX() / p_162261_.noiseFactor, (double)p_162262_.getZ() / p_162261_.noiseFactor, false);
      return (int)Math.ceil((d0 + p_162261_.noiseOffset) * (double)p_162261_.noiseToCountRatio);
   }
}