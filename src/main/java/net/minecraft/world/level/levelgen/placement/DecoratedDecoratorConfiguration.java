package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class DecoratedDecoratorConfiguration implements DecoratorConfiguration {
   public static final Codec<DecoratedDecoratorConfiguration> CODEC = RecordCodecBuilder.create((p_70579_) -> {
      return p_70579_.group(ConfiguredDecorator.CODEC.fieldOf("outer").forGetter(DecoratedDecoratorConfiguration::outer), ConfiguredDecorator.CODEC.fieldOf("inner").forGetter(DecoratedDecoratorConfiguration::inner)).apply(p_70579_, DecoratedDecoratorConfiguration::new);
   });
   private final ConfiguredDecorator<?> outer;
   private final ConfiguredDecorator<?> inner;

   public DecoratedDecoratorConfiguration(ConfiguredDecorator<?> p_70575_, ConfiguredDecorator<?> p_70576_) {
      this.outer = p_70575_;
      this.inner = p_70576_;
   }

   public ConfiguredDecorator<?> outer() {
      return this.outer;
   }

   public ConfiguredDecorator<?> inner() {
      return this.inner;
   }
}