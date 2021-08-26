package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NetherrackBlock extends Block implements BonemealableBlock
{
    public NetherrackBlock(BlockBehaviour.Properties p_54995_)
    {
        super(p_54995_);
    }

    public boolean isValidBonemealTarget(BlockGetter pLevel, BlockPos pPos, BlockState pState, boolean pIsClient)
    {
        if (!pLevel.getBlockState(pPos.above()).propagatesSkylightDown(pLevel, pPos))
        {
            return false;
        }
        else
        {
            for (BlockPos blockpos : BlockPos.betweenClosed(pPos.offset(-1, -1, -1), pPos.offset(1, 1, 1)))
            {
                if (pLevel.getBlockState(blockpos).is(BlockTags.NYLIUM))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isBonemealSuccess(Level pLevel, Random pRand, BlockPos pPos, BlockState pState)
    {
        return true;
    }

    public void performBonemeal(ServerLevel pLevel, Random pRand, BlockPos pPos, BlockState pState)
    {
        boolean flag = false;
        boolean flag1 = false;

        for (BlockPos blockpos : BlockPos.betweenClosed(pPos.offset(-1, -1, -1), pPos.offset(1, 1, 1)))
        {
            BlockState blockstate = pLevel.getBlockState(blockpos);

            if (blockstate.is(Blocks.WARPED_NYLIUM))
            {
                flag1 = true;
            }

            if (blockstate.is(Blocks.CRIMSON_NYLIUM))
            {
                flag = true;
            }

            if (flag1 && flag)
            {
                break;
            }
        }

        if (flag1 && flag)
        {
            pLevel.setBlock(pPos, pRand.nextBoolean() ? Blocks.WARPED_NYLIUM.defaultBlockState() : Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
        }
        else if (flag1)
        {
            pLevel.setBlock(pPos, Blocks.WARPED_NYLIUM.defaultBlockState(), 3);
        }
        else if (flag)
        {
            pLevel.setBlock(pPos, Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
        }
    }
}
