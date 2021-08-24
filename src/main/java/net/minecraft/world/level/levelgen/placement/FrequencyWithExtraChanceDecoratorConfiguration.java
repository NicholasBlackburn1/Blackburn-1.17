package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyWithExtraChanceDecoratorConfiguration implements DecoratorConfiguration {
   public static final Codec<FrequencyWithExtraChanceDecoratorConfiguration> CODEC = RecordCodecBuilder.create((p_70733_) -> {
      return p_70733_.group(Codec.INT.fieldOf("count").forGetter((p_162186_) -> {
         return p_162186_.count;
      }), Codec.FLOAT.fieldOf("extra_chance").forGetter((p_162184_) -> {
         return p_162184_.extraChance;
      }), Codec.INT.fieldOf("extra_count").forGetter((p_162182_) -> {
         return p_162182_.extraCount;
      })).apply(p_70733_, FrequencyWithExtraChanceDecoratorConfiguration::new);
   });
   public final int count;
   public final float extraChance;
   public final int extraCount;

   public FrequencyWithExtraChanceDecoratorConfiguration(int p_70729_, float p_70730_, int p_70731_) {
      this.count = p_70729_;
      this.extraChance = p_70730_;
      this.extraCount = p_70731_;
   }
}