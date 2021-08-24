package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseDependantDecoratorConfiguration implements DecoratorConfiguration {
   public static final Codec<NoiseDependantDecoratorConfiguration> CODEC = RecordCodecBuilder.create((p_67805_) -> {
      return p_67805_.group(Codec.DOUBLE.fieldOf("noise_level").forGetter((p_160998_) -> {
         return p_160998_.noiseLevel;
      }), Codec.INT.fieldOf("below_noise").forGetter((p_160996_) -> {
         return p_160996_.belowNoise;
      }), Codec.INT.fieldOf("above_noise").forGetter((p_160994_) -> {
         return p_160994_.aboveNoise;
      })).apply(p_67805_, NoiseDependantDecoratorConfiguration::new);
   });
   public final double noiseLevel;
   public final int belowNoise;
   public final int aboveNoise;

   public NoiseDependantDecoratorConfiguration(double p_67799_, int p_67800_, int p_67801_) {
      this.noiseLevel = p_67799_;
      this.belowNoise = p_67800_;
      this.aboveNoise = p_67801_;
   }
}