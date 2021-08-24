package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class RepeatingDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
   public RepeatingDecorator(Codec<DC> p_162290_) {
      super(p_162290_);
   }

   protected abstract int count(Random p_162299_, DC p_162300_, BlockPos p_162301_);

   public Stream<BlockPos> getPositions(DecorationContext p_162292_, Random p_162293_, DC p_162294_, BlockPos p_162295_) {
      return IntStream.range(0, this.count(p_162293_, p_162294_, p_162295_)).mapToObj((p_162298_) -> {
         return p_162295_;
      });
   }
}