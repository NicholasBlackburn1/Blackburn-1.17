package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.Decoratable;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ConfiguredDecorator<DC extends DecoratorConfiguration> implements Decoratable<ConfiguredDecorator<?>> {
   public static final Codec<ConfiguredDecorator<?>> CODEC = Registry.DECORATOR.dispatch("type", (p_70488_) -> {
      return p_70488_.decorator;
   }, FeatureDecorator::configuredCodec);
   private final FeatureDecorator<DC> decorator;
   private final DC config;

   public ConfiguredDecorator(FeatureDecorator<DC> p_70476_, DC p_70477_) {
      this.decorator = p_70476_;
      this.config = p_70477_;
   }

   public Stream<BlockPos> getPositions(DecorationContext p_70481_, Random p_70482_, BlockPos p_70483_) {
      return this.decorator.getPositions(p_70481_, p_70482_, this.config, p_70483_);
   }

   public String toString() {
      return String.format("[%s %s]", Registry.DECORATOR.getKey(this.decorator), this.config);
   }

   public ConfiguredDecorator<?> decorated(ConfiguredDecorator<?> p_70486_) {
      return new ConfiguredDecorator<>(FeatureDecorator.DECORATED, new DecoratedDecoratorConfiguration(p_70486_, this));
   }

   public DC config() {
      return this.config;
   }
}