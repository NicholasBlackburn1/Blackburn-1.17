package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class NopePlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
   public NopePlacementDecorator(Codec<NoneDecoratorConfiguration> p_70824_) {
      super(p_70824_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_162275_, Random p_162276_, NoneDecoratorConfiguration p_162277_, BlockPos p_162278_) {
      return Stream.of(p_162278_);
   }
}