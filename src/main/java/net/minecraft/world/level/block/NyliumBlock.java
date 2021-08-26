package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.NetherForestVegetationFeature;
import net.minecraft.world.level.levelgen.feature.TwistingVinesFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;

public class NyliumBlock extends Block implements BonemealableBlock
{
    protected NyliumBlock(BlockBehaviour.Properties p_55057_)
    {
        super(p_55057_);
    }

    private static boolean canBeNylium(BlockState pState, LevelReader pReader, BlockPos pPos)
    {
        BlockPos blockpos = pPos.above();
        BlockState blockstate = pReader.getBlockState(blockpos);
        int i = LayerLightEngine.getLightBlockInto(pReader, pState, pPos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(pReader, blockpos));
        return i < pReader.getMaxLightLevel();
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom)
    {
        if (!canBeNylium(pState, pLevel, pPos))
        {
            pLevel.setBlockAndUpdate(pPos, Blocks.NETHERRACK.defaultBlockState());
        }
    }

    public boolean isValidBonemealTarget(BlockGetter pLevel, BlockPos pPos, BlockState pState, boolean pIsClient)
    {
        return pLevel.getBlockState(pPos.above()).isAir();
    }

    public boolean isBonemealSuccess(Level pLevel, Random pRand, BlockPos pPos, BlockState pState)
    {
        return true;
    }

    public void performBonemeal(ServerLevel pLevel, Random pRand, BlockPos pPos, BlockState pState)
    {
        BlockState blockstate = pLevel.getBlockState(pPos);
        BlockPos blockpos = pPos.above();

        if (blockstate.is(Blocks.CRIMSON_NYLIUM))
        {
            NetherForestVegetationFeature.place(pLevel, pRand, blockpos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
        }
        else if (blockstate.is(Blocks.WARPED_NYLIUM))
        {
            NetherForestVegetationFeature.place(pLevel, pRand, blockpos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
            NetherForestVegetationFeature.place(pLevel, pRand, blockpos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);

            if (pRand.nextInt(8) == 0)
            {
                TwistingVinesFeature.place(pLevel, pRand, blockpos, 3, 1, 2);
            }
        }
    }
}
