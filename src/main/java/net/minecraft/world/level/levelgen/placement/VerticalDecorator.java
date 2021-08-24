package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class VerticalDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
   public VerticalDecorator(Codec<DC> p_162323_) {
      super(p_162323_);
   }

   protected abstract int y(DecorationContext p_162324_, Random p_162325_, DC p_162326_, int p_162327_);

   public final Stream<BlockPos> getPositions(DecorationContext p_162329_, Random p_162330_, DC p_162331_, BlockPos p_162332_) {
      return Stream.of(new BlockPos(p_162332_.getX(), this.y(p_162329_, p_162330_, p_162331_, p_162332_.getY()), p_162332_.getZ()));
   }
}