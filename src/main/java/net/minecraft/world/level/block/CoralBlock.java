package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class CoralBlock extends Block
{
    private final Block deadBlock;

    public CoralBlock(Block p_52130_, BlockBehaviour.Properties p_52131_)
    {
        super(p_52131_);
        this.deadBlock = p_52130_;
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        if (!this.scanForWater(pLevel, pPos))
        {
            pLevel.setBlock(pPos, this.deadBlock.defaultBlockState(), 2);
        }
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (!this.scanForWater(pLevel, pCurrentPos))
        {
            pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 60 + pLevel.getRandom().nextInt(40));
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    protected boolean scanForWater(BlockGetter pReader, BlockPos pPos)
    {
        for (Direction direction : Direction.values())
        {
            FluidState fluidstate = pReader.getFluidState(pPos.relative(direction));

            if (fluidstate.is(FluidTags.WATER))
            {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        if (!this.scanForWater(pContext.getLevel(), pContext.getClickedPos()))
        {
            pContext.getLevel().getBlockTicks().scheduleTick(pContext.getClickedPos(), this, 60 + pContext.getLevel().getRandom().nextInt(40));
        }

        return this.defaultBlockState();
    }
}
