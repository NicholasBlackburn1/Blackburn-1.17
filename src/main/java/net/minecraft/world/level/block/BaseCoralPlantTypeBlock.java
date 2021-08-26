package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseCoralPlantTypeBlock extends Block implements SimpleWaterloggedBlock
{
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);

    protected BaseCoralPlantTypeBlock(BlockBehaviour.Properties p_49161_)
    {
        super(p_49161_);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(true)));
    }

    protected void tryScheduleDieTick(BlockState pState, LevelAccessor pLevel, BlockPos pPos)
    {
        if (!scanForWater(pState, pLevel, pPos))
        {
            pLevel.getBlockTicks().scheduleTick(pPos, this, 60 + pLevel.getRandom().nextInt(40));
        }
    }

    protected static boolean scanForWater(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        if (pState.getValue(WATERLOGGED))
        {
            return true;
        }
        else
        {
            for (Direction direction : Direction.values())
            {
                if (pLevel.getFluidState(pPos.relative(direction)).is(FluidTags.WATER))
                {
                    return true;
                }
            }

            return false;
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return AABB;
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (pState.getValue(WATERLOGGED))
        {
            pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return pFacing == Direction.DOWN && !this.canSurvive(pState, pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        BlockPos blockpos = pPos.below();
        return pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, Direction.UP);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(WATERLOGGED);
    }

    public FluidState getFluidState(BlockState pState)
    {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }
}
