package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class NoiseCountFactorDecoratorConfiguration implements DecoratorConfiguration {
   public static final Codec<NoiseCountFactorDecoratorConfiguration> CODEC = RecordCodecBuilder.create((p_70816_) -> {
      return p_70816_.group(Codec.INT.fieldOf("noise_to_count_ratio").forGetter((p_162268_) -> {
         return p_162268_.noiseToCountRatio;
      }), Codec.DOUBLE.fieldOf("noise_factor").forGetter((p_162266_) -> {
         return p_162266_.noiseFactor;
      }), Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0D).forGetter((p_162264_) -> {
         return p_162264_.noiseOffset;
      })).apply(p_70816_, NoiseCountFactorDecoratorConfiguration::new);
   });
   public final int noiseToCountRatio;
   public final double noiseFactor;
   public final double noiseOffset;

   public NoiseCountFactorDecoratorConfiguration(int p_70812_, double p_70813_, double p_70814_) {
      this.noiseToCountRatio = p_70812_;
      this.noiseFactor = p_70813_;
      this.noiseOffset = p_70814_;
   }
}