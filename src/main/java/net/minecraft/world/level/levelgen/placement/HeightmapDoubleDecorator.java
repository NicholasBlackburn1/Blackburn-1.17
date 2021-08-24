package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;

public class HeightmapDoubleDecorator extends FeatureDecorator<HeightmapConfiguration> {
   public HeightmapDoubleDecorator(Codec<HeightmapConfiguration> p_70751_) {
      super(p_70751_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_162233_, Random p_162234_, HeightmapConfiguration p_162235_, BlockPos p_162236_) {
      int i = p_162236_.getX();
      int j = p_162236_.getZ();
      int k = p_162233_.getHeight(p_162235_.heightmap, i, j);
      return k == p_162233_.getMinBuildHeight() ? Stream.of() : Stream.of(new BlockPos(i, p_162233_.getMinBuildHeight() + p_162234_.nextInt((k - p_162233_.getMinBuildHeight()) * 2), j));
   }
}