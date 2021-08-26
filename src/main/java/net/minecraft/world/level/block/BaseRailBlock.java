package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseRailBlock extends Block implements SimpleWaterloggedBlock
{
    protected static final VoxelShape FLAT_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final boolean isStraight;

    public static boolean isRail(Level pLevel, BlockPos pPos)
    {
        return isRail(pLevel.getBlockState(pPos));
    }

    public static boolean isRail(BlockState pLevel)
    {
        return pLevel.is(BlockTags.RAILS) && pLevel.getBlock() instanceof BaseRailBlock;
    }

    protected BaseRailBlock(boolean p_49360_, BlockBehaviour.Properties p_49361_)
    {
        super(p_49361_);
        this.isStraight = p_49360_;
    }

    public boolean isStraight()
    {
        return this.isStraight;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        RailShape railshape = pState.is(this) ? pState.getValue(this.getShapeProperty()) : null;
        return railshape != null && railshape.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        return canSupportRigidBlock(pLevel, pPos.below());
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        if (!pOldState.is(pState.getBlock()))
        {
            this.updateState(pState, pLevel, pPos, pIsMoving);
        }
    }

    protected BlockState updateState(BlockState pState, Level pLevel, BlockPos pPos, boolean pBlock)
    {
        pState = this.updateDir(pLevel, pPos, pState, true);

        if (this.isStraight)
        {
            pState.neighborChanged(pLevel, pPos, this, pPos, pBlock);
        }

        return pState;
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving)
    {
        if (!pLevel.isClientSide && pLevel.getBlockState(pPos).is(this))
        {
            RailShape railshape = pState.getValue(this.getShapeProperty());

            if (shouldBeRemoved(pPos, pLevel, railshape))
            {
                dropResources(pState, pLevel, pPos);
                pLevel.removeBlock(pPos, pIsMoving);
            }
            else
            {
                this.updateState(pState, pLevel, pPos, pBlock);
            }
        }
    }

    private static boolean shouldBeRemoved(BlockPos pPos, Level pLevel, RailShape pRailShape)
    {
        if (!canSupportRigidBlock(pLevel, pPos.below()))
        {
            return true;
        }
        else
        {
            switch (pRailShape)
            {
                case ASCENDING_EAST:
                    return !canSupportRigidBlock(pLevel, pPos.east());

                case ASCENDING_WEST:
                    return !canSupportRigidBlock(pLevel, pPos.west());

                case ASCENDING_NORTH:
                    return !canSupportRigidBlock(pLevel, pPos.north());

                case ASCENDING_SOUTH:
                    return !canSupportRigidBlock(pLevel, pPos.south());

                default:
                    return false;
            }
        }
    }

    protected void updateState(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock)
    {
    }

    protected BlockState updateDir(Level pLevel, BlockPos pPos, BlockState pState, boolean pPlacing)
    {
        if (pLevel.isClientSide)
        {
            return pState;
        }
        else
        {
            RailShape railshape = pState.getValue(this.getShapeProperty());
            return (new RailState(pLevel, pPos, pState)).place(pLevel.hasNeighborSignal(pPos), pPlacing, railshape).getState();
        }
    }

    public PushReaction getPistonPushReaction(BlockState pState)
    {
        return PushReaction.NORMAL;
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pIsMoving)
        {
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);

            if (pState.getValue(this.getShapeProperty()).isAscending())
            {
                pLevel.updateNeighborsAt(pPos.above(), this);
            }

            if (this.isStraight)
            {
                pLevel.updateNeighborsAt(pPos, this);
                pLevel.updateNeighborsAt(pPos.below(), this);
            }
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        BlockState blockstate = super.defaultBlockState();
        Direction direction = pContext.getHorizontalDirection();
        boolean flag1 = direction == Direction.EAST || direction == Direction.WEST;
        return blockstate.setValue(this.getShapeProperty(), flag1 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(flag));
    }

    public abstract Property<RailShape> getShapeProperty();

    public BlockState updateShape(BlockState p_152151_, Direction p_152152_, BlockState p_152153_, LevelAccessor p_152154_, BlockPos p_152155_, BlockPos p_152156_)
    {
        if (p_152151_.getValue(WATERLOGGED))
        {
            p_152154_.getLiquidTicks().scheduleTick(p_152155_, Fluids.WATER, Fluids.WATER.getTickDelay(p_152154_));
        }

        return super.updateShape(p_152151_, p_152152_, p_152153_, p_152154_, p_152155_, p_152156_);
    }

    public FluidState getFluidState(BlockState p_152158_)
    {
        return p_152158_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_152158_);
    }
}
