package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GrowingPlantConfiguration implements FeatureConfiguration {
   public static final Codec<GrowingPlantConfiguration> CODEC = RecordCodecBuilder.create((p_160918_) -> {
      return p_160918_.group(SimpleWeightedRandomList.wrappedCodec(IntProvider.CODEC).fieldOf("height_distribution").forGetter((p_160928_) -> {
         return p_160928_.heightDistribution;
      }), Direction.CODEC.fieldOf("direction").forGetter((p_160926_) -> {
         return p_160926_.direction;
      }), BlockStateProvider.CODEC.fieldOf("body_provider").forGetter((p_160924_) -> {
         return p_160924_.bodyProvider;
      }), BlockStateProvider.CODEC.fieldOf("head_provider").forGetter((p_160922_) -> {
         return p_160922_.headProvider;
      }), Codec.BOOL.fieldOf("allow_water").forGetter((p_160920_) -> {
         return p_160920_.allowWater;
      })).apply(p_160918_, GrowingPlantConfiguration::new);
   });
   public final SimpleWeightedRandomList<IntProvider> heightDistribution;
   public final Direction direction;
   public final BlockStateProvider bodyProvider;
   public final BlockStateProvider headProvider;
   public final boolean allowWater;

   public GrowingPlantConfiguration(SimpleWeightedRandomList<IntProvider> p_160912_, Direction p_160913_, BlockStateProvider p_160914_, BlockStateProvider p_160915_, boolean p_160916_) {
      this.heightDistribution = p_160912_;
      this.direction = p_160913_;
      this.bodyProvider = p_160914_;
      this.headProvider = p_160915_;
      this.allowWater = p_160916_;
   }
}