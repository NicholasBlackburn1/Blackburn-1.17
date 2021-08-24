package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;

public class DecoratedFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<DecoratedFeatureConfiguration> CODEC = RecordCodecBuilder.create((p_67586_) -> {
      return p_67586_.group(ConfiguredFeature.CODEC.fieldOf("feature").forGetter((p_160729_) -> {
         return p_160729_.feature;
      }), ConfiguredDecorator.CODEC.fieldOf("decorator").forGetter((p_160727_) -> {
         return p_160727_.decorator;
      })).apply(p_67586_, DecoratedFeatureConfiguration::new);
   });
   public final Supplier<ConfiguredFeature<?, ?>> feature;
   public final ConfiguredDecorator<?> decorator;

   public DecoratedFeatureConfiguration(Supplier<ConfiguredFeature<?, ?>> p_67581_, ConfiguredDecorator<?> p_67582_) {
      this.feature = p_67581_;
      this.decorator = p_67582_;
   }

   public String toString() {
      return String.format("< %s [%s | %s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this.feature.get().feature()), this.decorator);
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return this.feature.get().getFeatures();
   }
}