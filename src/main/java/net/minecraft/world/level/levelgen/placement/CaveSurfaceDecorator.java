package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Column;

public class CaveSurfaceDecorator extends FeatureDecorator<CaveDecoratorConfiguration> {
   public CaveSurfaceDecorator(Codec<CaveDecoratorConfiguration> p_162117_) {
      super(p_162117_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_162126_, Random p_162127_, CaveDecoratorConfiguration p_162128_, BlockPos p_162129_) {
      Optional<Column> optional = Column.scan(p_162126_.getLevel(), p_162129_, p_162128_.floorToCeilingSearchRange, BlockBehaviour.BlockStateBase::isAir, (p_162119_) -> {
         return p_162119_.getMaterial().isSolid();
      });
      if (!optional.isPresent()) {
         return Stream.of();
      } else {
         OptionalInt optionalint = p_162128_.surface == CaveSurface.CEILING ? optional.get().getCeiling() : optional.get().getFloor();
         return !optionalint.isPresent() ? Stream.of() : Stream.of(p_162129_.atY(optionalint.getAsInt() - p_162128_.surface.getY()));
      }
   }
}