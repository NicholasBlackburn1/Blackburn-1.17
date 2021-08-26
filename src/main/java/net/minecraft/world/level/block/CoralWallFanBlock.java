package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class CoralWallFanBlock extends BaseCoralWallFanBlock
{
    private final Block deadBlock;

    protected CoralWallFanBlock(Block p_52202_, BlockBehaviour.Properties p_52203_)
    {
        super(p_52203_);
        this.deadBlock = p_52202_;
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        this.tryScheduleDieTick(pState, pLevel, pPos);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        if (!scanForWater(pState, pLevel, pPos))
        {
            pLevel.setBlock(pPos, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, pState.getValue(FACING)), 2);
        }
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (pFacing.getOpposite() == pState.getValue(FACING) && !pState.canSurvive(pLevel, pCurrentPos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        else
        {
            if (pState.getValue(WATERLOGGED))
            {
                pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
            }

            this.tryScheduleDieTick(pState, pLevel, pCurrentPos);
            return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        }
    }
}
