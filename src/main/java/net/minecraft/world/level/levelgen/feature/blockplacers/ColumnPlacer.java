package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ColumnPlacer extends BlockPlacer
{
    public static final Codec<ColumnPlacer> CODEC = RecordCodecBuilder.create((p_160711_) ->
    {
        return p_160711_.group(IntProvider.NON_NEGATIVE_CODEC.fieldOf("size").forGetter((p_160713_) -> {
            return p_160713_.size;
        })).apply(p_160711_, ColumnPlacer::new);
    });
    private final IntProvider size;

    public ColumnPlacer(IntProvider p_160709_)
    {
        this.size = p_160709_;
    }

    protected BlockPlacerType<?> type()
    {
        return BlockPlacerType.COLUMN_PLACER;
    }

    public void place(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Random pRandom)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();
        int i = this.size.sample(pRandom);

        for (int j = 0; j < i; ++j)
        {
            pLevel.setBlock(blockpos$mutableblockpos, pState, 2);
            blockpos$mutableblockpos.move(Direction.UP);
        }
    }
}
