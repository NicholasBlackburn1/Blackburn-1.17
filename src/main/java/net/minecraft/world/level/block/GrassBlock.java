package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractFlowerFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class GrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock
{
    public GrassBlock(BlockBehaviour.Properties p_53685_)
    {
        super(p_53685_);
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
        BlockPos blockpos = pPos.above();
        BlockState blockstate = Blocks.GRASS.defaultBlockState();
        label48:

        for (int i = 0; i < 128; ++i)
        {
            BlockPos blockpos1 = blockpos;

            for (int j = 0; j < i / 16; ++j)
            {
                blockpos1 = blockpos1.offset(pRand.nextInt(3) - 1, (pRand.nextInt(3) - 1) * pRand.nextInt(3) / 2, pRand.nextInt(3) - 1);

                if (!pLevel.getBlockState(blockpos1.below()).is(this) || pLevel.getBlockState(blockpos1).isCollisionShapeFullBlock(pLevel, blockpos1))
                {
                    continue label48;
                }
            }

            BlockState blockstate2 = pLevel.getBlockState(blockpos1);

            if (blockstate2.is(blockstate.getBlock()) && pRand.nextInt(10) == 0)
            {
                ((BonemealableBlock)blockstate.getBlock()).performBonemeal(pLevel, pRand, blockpos1, blockstate2);
            }

            if (blockstate2.isAir())
            {
                BlockState blockstate1;

                if (pRand.nextInt(8) == 0)
                {
                    List < ConfiguredFeature <? , ? >> list = pLevel.getBiome(blockpos1).getGenerationSettings().getFlowerFeatures();

                    if (list.isEmpty())
                    {
                        continue;
                    }

                    blockstate1 = getBlockState(pRand, blockpos1, list.get(0));
                }
                else
                {
                    blockstate1 = blockstate;
                }

                if (blockstate1.canSurvive(pLevel, blockpos1))
                {
                    pLevel.setBlock(blockpos1, blockstate1, 3);
                }
            }
        }
    }

    private static <U extends FeatureConfiguration> BlockState getBlockState(Random p_153318_, BlockPos p_153319_, ConfiguredFeature < U, ? > p_153320_)
    {
        AbstractFlowerFeature<U> abstractflowerfeature = (AbstractFlowerFeature)p_153320_.feature;
        return abstractflowerfeature.getRandomFlower(p_153318_, p_153319_, p_153320_.config());
    }
}
