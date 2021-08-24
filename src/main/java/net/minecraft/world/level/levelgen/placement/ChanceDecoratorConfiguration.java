package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ChanceDecoratorConfiguration implements DecoratorConfiguration {
   public static final Codec<ChanceDecoratorConfiguration> CODEC = Codec.INT.fieldOf("chance").xmap(ChanceDecoratorConfiguration::new, (p_70470_) -> {
      return p_70470_.chance;
   }).codec();
   public final int chance;

   public ChanceDecoratorConfiguration(int p_70468_) {
      this.chance = p_70468_;
   }
}