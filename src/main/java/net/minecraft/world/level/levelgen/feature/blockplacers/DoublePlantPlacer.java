package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DoublePlantPlacer extends BlockPlacer
{
    public static final Codec<DoublePlantPlacer> CODEC;
    public static final DoublePlantPlacer INSTANCE = new DoublePlantPlacer();

    protected BlockPlacerType<?> type()
    {
        return BlockPlacerType.DOUBLE_PLANT_PLACER;
    }

    public void place(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Random pRandom)
    {
        DoublePlantBlock.placeAt(pLevel, pState, pPos, 2);
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return INSTANCE;
        });
    }
}
