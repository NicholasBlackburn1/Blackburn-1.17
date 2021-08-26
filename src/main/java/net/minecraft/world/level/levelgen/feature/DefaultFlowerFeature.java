package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class DefaultFlowerFeature extends AbstractFlowerFeature<RandomPatchConfiguration>
{
    public DefaultFlowerFeature(Codec<RandomPatchConfiguration> p_65517_)
    {
        super(p_65517_);
    }

    public boolean isValid(LevelAccessor pLevel, BlockPos pPos, RandomPatchConfiguration pConfig)
    {
        return !pConfig.blacklist.contains(pLevel.getBlockState(pPos));
    }

    public int getCount(RandomPatchConfiguration pConfig)
    {
        return pConfig.tries;
    }

    public BlockPos getPos(Random pRand, BlockPos pPos, RandomPatchConfiguration pConfig)
    {
        return pPos.offset(pRand.nextInt(pConfig.xspread) - pRand.nextInt(pConfig.xspread), pRand.nextInt(pConfig.yspread) - pRand.nextInt(pConfig.yspread), pRand.nextInt(pConfig.zspread) - pRand.nextInt(pConfig.zspread));
    }

    public BlockState getRandomFlower(Random pRand, BlockPos pPos, RandomPatchConfiguration pConfgi)
    {
        return pConfgi.stateProvider.getState(pRand, pPos);
    }
}
