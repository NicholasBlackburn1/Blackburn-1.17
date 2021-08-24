package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMaskDecorator extends FeatureDecorator<CarvingMaskDecoratorConfiguration> {
   public CarvingMaskDecorator(Codec<CarvingMaskDecoratorConfiguration> p_70422_) {
      super(p_70422_);
   }

   public Stream<BlockPos> getPositions(DecorationContext p_70432_, Random p_70433_, CarvingMaskDecoratorConfiguration p_70434_, BlockPos p_70435_) {
      ChunkPos chunkpos = new ChunkPos(p_70435_);
      BitSet bitset = p_70432_.getCarvingMask(chunkpos, p_70434_.step);
      return IntStream.range(0, bitset.length()).filter(bitset::get).mapToObj((p_162074_) -> {
         int i = p_162074_ & 15;
         int j = p_162074_ >> 4 & 15;
         int k = p_162074_ >> 8;
         return new BlockPos(chunkpos.getMinBlockX() + i, k, chunkpos.getMinBlockZ() + j);
      });
   }
}