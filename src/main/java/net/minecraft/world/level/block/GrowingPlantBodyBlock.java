package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock
{
    protected GrowingPlantBodyBlock(BlockBehaviour.Properties p_53886_, Direction p_53887_, VoxelShape p_53888_, boolean p_53889_)
    {
        super(p_53886_, p_53887_, p_53888_, p_53889_);
    }

    protected BlockState updateHeadAfterConvertedFromBody(BlockState p_153326_, BlockState p_153327_)
    {
        return p_153327_;
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (pFacing == this.growthDirection.getOpposite() && !pState.canSurvive(pLevel, pCurrentPos))
        {
            pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
        }

        GrowingPlantHeadBlock growingplantheadblock = this.getHeadBlock();

        if (pFacing == this.growthDirection && !pFacingState.is(this) && !pFacingState.is(growingplantheadblock))
        {
            return this.updateHeadAfterConvertedFromBody(pState, growingplantheadblock.getStateForPlacement(pLevel));
        }
        else
        {
            if (this.scheduleFluidTicks)
            {
                pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
            }

            return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        }
    }

    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState)
    {
        return new ItemStack(this.getHeadBlock());
    }

    public boolean isValidBonemealTarget(BlockGetter pLevel, BlockPos pPos, BlockState pState, boolean pIsClient)
    {
        Optional<BlockPos> optional = this.getHeadPos(pLevel, pPos, pState.getBlock());
        return optional.isPresent() && this.getHeadBlock().canGrowInto(pLevel.getBlockState(optional.get().relative(this.growthDirection)));
    }

    public boolean isBonemealSuccess(Level pLevel, Random pRand, BlockPos pPos, BlockState pState)
    {
        return true;
    }

    public void performBonemeal(ServerLevel pLevel, Random pRand, BlockPos pPos, BlockState pState)
    {
        Optional<BlockPos> optional = this.getHeadPos(pLevel, pPos, pState.getBlock());

        if (optional.isPresent())
        {
            BlockState blockstate = pLevel.getBlockState(optional.get());
            ((GrowingPlantHeadBlock)blockstate.getBlock()).performBonemeal(pLevel, pRand, optional.get(), blockstate);
        }
    }

    private Optional<BlockPos> getHeadPos(BlockGetter p_153323_, BlockPos p_153324_, Block p_153325_)
    {
        return BlockUtil.getTopConnectedBlock(p_153323_, p_153324_, p_153325_, this.growthDirection, this.getHeadBlock());
    }

    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext)
    {
        boolean flag = super.canBeReplaced(pState, pUseContext);
        return flag && pUseContext.getItemInHand().is(this.getHeadBlock().asItem()) ? false : flag;
    }

    protected Block getBodyBlock()
    {
        return this;
    }
}
