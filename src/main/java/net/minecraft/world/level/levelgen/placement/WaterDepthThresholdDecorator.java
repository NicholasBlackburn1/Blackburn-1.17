package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public class WaterDepthThresholdDecorator extends FeatureDecorator<WaterDepthThresholdConfiguration> {
   public WaterDepthThresholdDecorator(Codec<WaterDepthThresholdConfiguration> p_162343_) {
      super(p_162343_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_162350_, Random p_162351_, WaterDepthThresholdConfiguration p_162352_, BlockPos p_162353_) {
      int i = p_162350_.getHeight(Heightmap.Types.OCEAN_FLOOR, p_162353_.getX(), p_162353_.getZ());
      int j = p_162350_.getHeight(Heightmap.Types.WORLD_SURFACE, p_162353_.getX(), p_162353_.getZ());
      return j - i > p_162352_.maxWaterDepth ? Stream.of() : Stream.of(p_162353_);
   }
}