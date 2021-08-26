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

public class CoralFanBlock extends BaseCoralFanBlock
{
    private final Block deadBlock;

    protected CoralFanBlock(Block p_52151_, BlockBehaviour.Properties p_52152_)
    {
        super(p_52152_);
        this.deadBlock = p_52151_;
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        this.tryScheduleDieTick(pState, pLevel, pPos);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        if (!scanForWater(pState, pLevel, pPos))
        {
            pLevel.setBlock(pPos, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)), 2);
        }
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        else
        {
            this.tryScheduleDieTick(pState, pLevel, pCurrentPos);

            if (pState.getValue(WATERLOGGED))
            {
                pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
            }

            return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        }
    }
}
