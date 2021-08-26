package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HalfTransparentBlock extends Block
{
    protected HalfTransparentBlock(BlockBehaviour.Properties p_53970_)
    {
        super(p_53970_);
    }

    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide)
    {
        return pAdjacentBlockState.is(this) ? true : super.skipRendering(pState, pAdjacentBlockState, pSide);
    }
}
