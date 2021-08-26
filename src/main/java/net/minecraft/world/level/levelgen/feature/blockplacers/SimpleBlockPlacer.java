package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockPlacer extends BlockPlacer
{
    public static final Codec<SimpleBlockPlacer> CODEC;
    public static final SimpleBlockPlacer INSTANCE = new SimpleBlockPlacer();

    protected BlockPlacerType<?> type()
    {
        return BlockPlacerType.SIMPLE_BLOCK_PLACER;
    }

    public void place(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Random pRandom)
    {
        pLevel.setBlock(pPos, pState, 2);
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return INSTANCE;
        });
    }
}
