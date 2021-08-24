package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class IcebergPlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
   public IcebergPlacementDecorator(Codec<NoneDecoratorConfiguration> p_70760_) {
      super(p_70760_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_162243_, Random p_162244_, NoneDecoratorConfiguration p_162245_, BlockPos p_162246_) {
      int i = p_162244_.nextInt(8) + 4 + p_162246_.getX();
      int j = p_162244_.nextInt(8) + 4 + p_162246_.getZ();
      return Stream.of(new BlockPos(i, p_162246_.getY(), j));
   }
}