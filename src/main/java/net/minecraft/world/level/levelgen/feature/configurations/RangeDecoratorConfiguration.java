package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class RangeDecoratorConfiguration implements DecoratorConfiguration, FeatureConfiguration {
   public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create((p_161080_) -> {
      return p_161080_.group(HeightProvider.CODEC.fieldOf("height").forGetter((p_161082_) -> {
         return p_161082_.height;
      })).apply(p_161080_, RangeDecoratorConfiguration::new);
   });
   public final HeightProvider height;

   public RangeDecoratorConfiguration(HeightProvider p_161078_) {
      this.height = p_161078_;
   }
}