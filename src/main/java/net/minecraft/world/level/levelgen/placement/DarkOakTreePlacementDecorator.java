package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class DarkOakTreePlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
   public DarkOakTreePlacementDecorator(Codec<NoneDecoratorConfiguration> p_70532_) {
      super(p_70532_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_70543_, Random p_70544_, NoneDecoratorConfiguration p_70545_, BlockPos p_70546_) {
      return IntStream.range(0, 16).mapToObj((p_162165_) -> {
         int i = p_162165_ / 4;
         int j = p_162165_ % 4;
         int k = i * 4 + 1 + p_70544_.nextInt(3) + p_70546_.getX();
         int l = j * 4 + 1 + p_70544_.nextInt(3) + p_70546_.getZ();
         return new BlockPos(k, p_70546_.getY(), l);
      });
   }
}