package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;

public class HeightmapDecorator extends FeatureDecorator<HeightmapConfiguration> {
   public HeightmapDecorator(Codec<HeightmapConfiguration> p_70747_) {
      super(p_70747_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_162193_, Random p_162194_, HeightmapConfiguration p_162195_, BlockPos p_162196_) {
      int i = p_162196_.getX();
      int j = p_162196_.getZ();
      int k = p_162193_.getHeight(p_162195_.heightmap, i, j);
      return k > p_162193_.getMinBuildHeight() ? Stream.of(new BlockPos(i, k, j)) : Stream.of();
   }
}