package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class DecoratedFeature extends Feature<DecoratedFeatureConfiguration> {
   public DecoratedFeature(Codec<DecoratedFeatureConfiguration> p_65495_) {
      super(p_65495_);
   }

   public boolean place(FeaturePlaceContext<DecoratedFeatureConfiguration> p_159545_) {
      MutableBoolean mutableboolean = new MutableBoolean();
      WorldGenLevel worldgenlevel = p_159545_.level();
      DecoratedFeatureConfiguration decoratedfeatureconfiguration = p_159545_.config();
      ChunkGenerator chunkgenerator = p_159545_.chunkGenerator();
      Random random = p_159545_.random();
      BlockPos blockpos = p_159545_.origin();
      ConfiguredFeature<?, ?> configuredfeature = decoratedfeatureconfiguration.feature.get();
      decoratedfeatureconfiguration.decorator.getPositions(new DecorationContext(worldgenlevel, chunkgenerator), random, blockpos).forEach((p_159543_) -> {
         if (configuredfeature.place(worldgenlevel, chunkgenerator, random, p_159543_)) {
            mutableboolean.setTrue();
         }

      });
      return mutableboolean.isTrue();
   }

   public String toString() {
      return String.format("< %s [%s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this));
   }
}