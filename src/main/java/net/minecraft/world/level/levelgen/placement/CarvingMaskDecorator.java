package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMaskDecorator extends FeatureDecorator<CarvingMaskDecoratorConfiguration>
{
    public CarvingMaskDecorator(Codec<CarvingMaskDecoratorConfiguration> p_70422_)
    {
        super(p_70422_);
    }

    public Stream<BlockPos> getPositions(DecorationContext pHelper, Random pRand, CarvingMaskDecoratorConfiguration pConfig, BlockPos pPos)
    {
        ChunkPos chunkpos = new ChunkPos(pPos);
        BitSet bitset = pHelper.getCarvingMask(chunkpos, pConfig.step);
        return IntStream.range(0, bitset.length()).filter(bitset::get).mapToObj((p_162074_) ->
        {
            int i = p_162074_ & 15;
            int j = p_162074_ >> 4 & 15;
            int k = p_162074_ >> 8;
            return new BlockPos(chunkpos.getMinBlockX() + i, k, chunkpos.getMinBlockZ() + j);
        });
    }
}
