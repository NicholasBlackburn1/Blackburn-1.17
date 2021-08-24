package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;

public class RandomFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<RandomFeatureConfiguration> CODEC = RecordCodecBuilder.create((p_67898_) -> {
      return p_67898_.apply2(RandomFeatureConfiguration::new, WeightedConfiguredFeature.CODEC.listOf().fieldOf("features").forGetter((p_161053_) -> {
         return p_161053_.features;
      }), ConfiguredFeature.CODEC.fieldOf("default").forGetter((p_161051_) -> {
         return p_161051_.defaultFeature;
      }));
   });
   public final List<WeightedConfiguredFeature> features;
   public final Supplier<ConfiguredFeature<?, ?>> defaultFeature;

   public RandomFeatureConfiguration(List<WeightedConfiguredFeature> p_67886_, ConfiguredFeature<?, ?> p_67887_) {
      this(p_67886_, () -> {
         return p_67887_;
      });
   }

   private RandomFeatureConfiguration(List<WeightedConfiguredFeature> p_67889_, Supplier<ConfiguredFeature<?, ?>> p_67890_) {
      this.features = p_67889_;
      this.defaultFeature = p_67890_;
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return Stream.concat(this.features.stream().flatMap((p_67894_) -> {
         return p_67894_.feature.get().getFeatures();
      }), this.defaultFeature.get().getFeatures());
   }
}