package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.Material;

public class HugeFungusFeature extends Feature<HugeFungusConfiguration>
{
    private static final float HUGE_PROBABILITY = 0.06F;

    public HugeFungusFeature(Codec<HugeFungusConfiguration> p_65922_)
    {
        super(p_65922_);
    }

    public boolean place(FeaturePlaceContext<HugeFungusConfiguration> p_159878_)
    {
        WorldGenLevel worldgenlevel = p_159878_.level();
        BlockPos blockpos = p_159878_.origin();
        Random random = p_159878_.random();
        ChunkGenerator chunkgenerator = p_159878_.chunkGenerator();
        HugeFungusConfiguration hugefungusconfiguration = p_159878_.config();
        Block block = hugefungusconfiguration.validBaseState.getBlock();
        BlockPos blockpos1 = null;
        BlockState blockstate = worldgenlevel.getBlockState(blockpos.below());

        if (blockstate.is(block))
        {
            blockpos1 = blockpos;
        }

        if (blockpos1 == null)
        {
            return false;
        }
        else
        {
            int i = Mth.nextInt(random, 4, 13);

            if (random.nextInt(12) == 0)
            {
                i *= 2;
            }

            if (!hugefungusconfiguration.planted)
            {
                int j = chunkgenerator.getGenDepth();

                if (blockpos1.getY() + i + 1 >= j)
                {
                    return false;
                }
            }

            boolean flag = !hugefungusconfiguration.planted && random.nextFloat() < 0.06F;
            worldgenlevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 4);
            this.placeStem(worldgenlevel, random, hugefungusconfiguration, blockpos1, i, flag);
            this.placeHat(worldgenlevel, random, hugefungusconfiguration, blockpos1, i, flag);
            return true;
        }
    }

    private static boolean isReplaceable(LevelAccessor pLevel, BlockPos pPos, boolean p_65926_)
    {
        return pLevel.isStateAtPosition(pPos, (p_65966_) ->
        {
            Material material = p_65966_.getMaterial();
            return p_65966_.getMaterial().isReplaceable() || p_65926_ && material == Material.PLANT;
        });
    }

    private void placeStem(LevelAccessor pLevel, Random pRand, HugeFungusConfiguration pConfig, BlockPos pPos, int p_65940_, boolean p_65941_)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        BlockState blockstate = pConfig.stemState;
        int i = p_65941_ ? 1 : 0;

        for (int j = -i; j <= i; ++j)
        {
            for (int k = -i; k <= i; ++k)
            {
                boolean flag = p_65941_ && Mth.abs(j) == i && Mth.abs(k) == i;

                for (int l = 0; l < p_65940_; ++l)
                {
                    blockpos$mutableblockpos.setWithOffset(pPos, j, l, k);

                    if (isReplaceable(pLevel, blockpos$mutableblockpos, true))
                    {
                        if (pConfig.planted)
                        {
                            if (!pLevel.getBlockState(blockpos$mutableblockpos.below()).isAir())
                            {
                                pLevel.destroyBlock(blockpos$mutableblockpos, true);
                            }

                            pLevel.setBlock(blockpos$mutableblockpos, blockstate, 3);
                        }
                        else if (flag)
                        {
                            if (pRand.nextFloat() < 0.1F)
                            {
                                this.setBlock(pLevel, blockpos$mutableblockpos, blockstate);
                            }
                        }
                        else
                        {
                            this.setBlock(pLevel, blockpos$mutableblockpos, blockstate);
                        }
                    }
                }
            }
        }
    }

    private void placeHat(LevelAccessor pLevel, Random pRand, HugeFungusConfiguration pConfig, BlockPos pPos, int p_65972_, boolean p_65973_)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        boolean flag = pConfig.hatState.is(Blocks.NETHER_WART_BLOCK);
        int i = Math.min(pRand.nextInt(1 + p_65972_ / 3) + 5, p_65972_);
        int j = p_65972_ - i;

        for (int k = j; k <= p_65972_; ++k)
        {
            int l = k < p_65972_ - pRand.nextInt(3) ? 2 : 1;

            if (i > 8 && k < j + 4)
            {
                l = 3;
            }

            if (p_65973_)
            {
                ++l;
            }

            for (int i1 = -l; i1 <= l; ++i1)
            {
                for (int j1 = -l; j1 <= l; ++j1)
                {
                    boolean flag1 = i1 == -l || i1 == l;
                    boolean flag2 = j1 == -l || j1 == l;
                    boolean flag3 = !flag1 && !flag2 && k != p_65972_;
                    boolean flag4 = flag1 && flag2;
                    boolean flag5 = k < j + 3;
                    blockpos$mutableblockpos.setWithOffset(pPos, i1, k, j1);

                    if (isReplaceable(pLevel, blockpos$mutableblockpos, false))
                    {
                        if (pConfig.planted && !pLevel.getBlockState(blockpos$mutableblockpos.below()).isAir())
                        {
                            pLevel.destroyBlock(blockpos$mutableblockpos, true);
                        }

                        if (flag5)
                        {
                            if (!flag3)
                            {
                                this.placeHatDropBlock(pLevel, pRand, blockpos$mutableblockpos, pConfig.hatState, flag);
                            }
                        }
                        else if (flag3)
                        {
                            this.placeHatBlock(pLevel, pRand, pConfig, blockpos$mutableblockpos, 0.1F, 0.2F, flag ? 0.1F : 0.0F);
                        }
                        else if (flag4)
                        {
                            this.placeHatBlock(pLevel, pRand, pConfig, blockpos$mutableblockpos, 0.01F, 0.7F, flag ? 0.083F : 0.0F);
                        }
                        else
                        {
                            this.placeHatBlock(pLevel, pRand, pConfig, blockpos$mutableblockpos, 5.0E-4F, 0.98F, flag ? 0.07F : 0.0F);
                        }
                    }
                }
            }
        }
    }

    private void placeHatBlock(LevelAccessor pLevel, Random pRand, HugeFungusConfiguration pConfig, BlockPos.MutableBlockPos pPos, float p_65932_, float p_65933_, float p_65934_)
    {
        if (pRand.nextFloat() < p_65932_)
        {
            this.setBlock(pLevel, pPos, pConfig.decorState);
        }
        else if (pRand.nextFloat() < p_65933_)
        {
            this.setBlock(pLevel, pPos, pConfig.hatState);

            if (pRand.nextFloat() < p_65934_)
            {
                tryPlaceWeepingVines(pPos, pLevel, pRand);
            }
        }
    }

    private void placeHatDropBlock(LevelAccessor pLevel, Random pRand, BlockPos pPos, BlockState pState, boolean pIsNetherWart)
    {
        if (pLevel.getBlockState(pPos.below()).is(pState.getBlock()))
        {
            this.setBlock(pLevel, pPos, pState);
        }
        else if ((double)pRand.nextFloat() < 0.15D)
        {
            this.setBlock(pLevel, pPos, pState);

            if (pIsNetherWart && pRand.nextInt(11) == 0)
            {
                tryPlaceWeepingVines(pPos, pLevel, pRand);
            }
        }
    }

    private static void tryPlaceWeepingVines(BlockPos pPos, LevelAccessor pLevel, Random pRand)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable().move(Direction.DOWN);

        if (pLevel.isEmptyBlock(blockpos$mutableblockpos))
        {
            int i = Mth.nextInt(pRand, 1, 5);

            if (pRand.nextInt(7) == 0)
            {
                i *= 2;
            }

            int j = 23;
            int k = 25;
            WeepingVinesFeature.placeWeepingVinesColumn(pLevel, pRand, blockpos$mutableblockpos, i, 23, 25);
        }
    }
}
