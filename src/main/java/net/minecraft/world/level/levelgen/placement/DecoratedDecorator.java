package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class DecoratedDecorator extends FeatureDecorator<DecoratedDecoratorConfiguration>
{
    public DecoratedDecorator(Codec<DecoratedDecoratorConfiguration> p_70554_)
    {
        super(p_70554_);
    }

    public Stream<BlockPos> getPositions(DecorationContext pHelper, Random pRand, DecoratedDecoratorConfiguration pConfig, BlockPos pPos)
    {
        return pConfig.outer().getPositions(pHelper, pRand, pPos).flatMap((p_70559_) ->
        {
            return pConfig.inner().getPositions(pHelper, pRand, p_70559_);
        });
    }
}
