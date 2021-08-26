package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

public class DetectorRailBlock extends BaseRailBlock
{
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final int PRESSED_CHECK_PERIOD = 20;

    public DetectorRailBlock(BlockBehaviour.Properties p_52431_)
    {
        super(true, p_52431_);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)).setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public boolean isSignalSource(BlockState pState)
    {
        return true;
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity)
    {
        if (!pLevel.isClientSide)
        {
            if (!pState.getValue(POWERED))
            {
                this.checkPressed(pLevel, pPos, pState);
            }
        }
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        if (pState.getValue(POWERED))
        {
            this.checkPressed(pLevel, pPos, pState);
        }
    }

    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return pBlockState.getValue(POWERED) ? 15 : 0;
    }

    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        if (!pBlockState.getValue(POWERED))
        {
            return 0;
        }
        else
        {
            return pSide == Direction.UP ? 15 : 0;
        }
    }

    private void checkPressed(Level pLevel, BlockPos pPos, BlockState pState)
    {
        if (this.canSurvive(pState, pLevel, pPos))
        {
            boolean flag = pState.getValue(POWERED);
            boolean flag1 = false;
            List<AbstractMinecart> list = this.getInteractingMinecartOfType(pLevel, pPos, AbstractMinecart.class, (p_153125_) ->
            {
                return true;
            });

            if (!list.isEmpty())
            {
                flag1 = true;
            }

            if (flag1 && !flag)
            {
                BlockState blockstate = pState.setValue(POWERED, Boolean.valueOf(true));
                pLevel.setBlock(pPos, blockstate, 3);
                this.updatePowerToConnected(pLevel, pPos, blockstate, true);
                pLevel.updateNeighborsAt(pPos, this);
                pLevel.updateNeighborsAt(pPos.below(), this);
                pLevel.setBlocksDirty(pPos, pState, blockstate);
            }

            if (!flag1 && flag)
            {
                BlockState blockstate1 = pState.setValue(POWERED, Boolean.valueOf(false));
                pLevel.setBlock(pPos, blockstate1, 3);
                this.updatePowerToConnected(pLevel, pPos, blockstate1, false);
                pLevel.updateNeighborsAt(pPos, this);
                pLevel.updateNeighborsAt(pPos.below(), this);
                pLevel.setBlocksDirty(pPos, pState, blockstate1);
            }

            if (flag1)
            {
                pLevel.getBlockTicks().scheduleTick(pPos, this, 20);
            }

            pLevel.updateNeighbourForOutputSignal(pPos, this);
        }
    }

    protected void updatePowerToConnected(Level pLevel, BlockPos pPos, BlockState pState, boolean pPowered)
    {
        RailState railstate = new RailState(pLevel, pPos, pState);

        for (BlockPos blockpos : railstate.getConnections())
        {
            BlockState blockstate = pLevel.getBlockState(blockpos);
            blockstate.neighborChanged(pLevel, blockpos, blockstate.getBlock(), pPos, false);
        }
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
        if (!pOldState.is(pState.getBlock()))
        {
            BlockState blockstate = this.updateState(pState, pLevel, pPos, pIsMoving);
            this.checkPressed(pLevel, pPos, blockstate);
        }
    }

    public Property<RailShape> getShapeProperty()
    {
        return SHAPE;
    }

    public boolean hasAnalogOutputSignal(BlockState pState)
    {
        return true;
    }

    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos)
    {
        if (pBlockState.getValue(POWERED))
        {
            List<MinecartCommandBlock> list = this.getInteractingMinecartOfType(pLevel, pPos, MinecartCommandBlock.class, (p_153123_) ->
            {
                return true;
            });

            if (!list.isEmpty())
            {
                return list.get(0).getCommandBlock().getSuccessCount();
            }

            List<AbstractMinecart> list1 = this.getInteractingMinecartOfType(pLevel, pPos, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);

            if (!list1.isEmpty())
            {
                return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)list1.get(0));
            }
        }

        return 0;
    }

    private <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level pLevel, BlockPos pPos, Class<T> pCartType, Predicate<Entity> pFilter)
    {
        return pLevel.getEntitiesOfClass(pCartType, this.getSearchBB(pPos), pFilter);
    }

    private AABB getSearchBB(BlockPos pPos)
    {
        double d0 = 0.2D;
        return new AABB((double)pPos.getX() + 0.2D, (double)pPos.getY(), (double)pPos.getZ() + 0.2D, (double)(pPos.getX() + 1) - 0.2D, (double)(pPos.getY() + 1) - 0.2D, (double)(pPos.getZ() + 1) - 0.2D);
    }

    public BlockState rotate(BlockState pState, Rotation pRot)
    {
        switch (pRot)
        {
            case CLOCKWISE_180:
                switch ((RailShape)pState.getValue(SHAPE))
                {
                    case ASCENDING_EAST:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);

                    case ASCENDING_WEST:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);

                    case ASCENDING_NORTH:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);

                    case ASCENDING_SOUTH:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);

                    case SOUTH_EAST:
                        return pState.setValue(SHAPE, RailShape.NORTH_WEST);

                    case SOUTH_WEST:
                        return pState.setValue(SHAPE, RailShape.NORTH_EAST);

                    case NORTH_WEST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_EAST);

                    case NORTH_EAST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
                }

            case COUNTERCLOCKWISE_90:
                switch ((RailShape)pState.getValue(SHAPE))
                {
                    case ASCENDING_EAST:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);

                    case ASCENDING_WEST:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);

                    case ASCENDING_NORTH:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);

                    case ASCENDING_SOUTH:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);

                    case SOUTH_EAST:
                        return pState.setValue(SHAPE, RailShape.NORTH_EAST);

                    case SOUTH_WEST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_EAST);

                    case NORTH_WEST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_WEST);

                    case NORTH_EAST:
                        return pState.setValue(SHAPE, RailShape.NORTH_WEST);

                    case NORTH_SOUTH:
                        return pState.setValue(SHAPE, RailShape.EAST_WEST);

                    case EAST_WEST:
                        return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
                }

            case CLOCKWISE_90:
                switch ((RailShape)pState.getValue(SHAPE))
                {
                    case ASCENDING_EAST:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);

                    case ASCENDING_WEST:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);

                    case ASCENDING_NORTH:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);

                    case ASCENDING_SOUTH:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);

                    case SOUTH_EAST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_WEST);

                    case SOUTH_WEST:
                        return pState.setValue(SHAPE, RailShape.NORTH_WEST);

                    case NORTH_WEST:
                        return pState.setValue(SHAPE, RailShape.NORTH_EAST);

                    case NORTH_EAST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_EAST);

                    case NORTH_SOUTH:
                        return pState.setValue(SHAPE, RailShape.EAST_WEST);

                    case EAST_WEST:
                        return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
                }

            default:
                return pState;
        }
    }

    public BlockState mirror(BlockState pState, Mirror pMirror)
    {
        RailShape railshape = pState.getValue(SHAPE);

        switch (pMirror)
        {
            case LEFT_RIGHT:
                switch (railshape)
                {
                    case ASCENDING_NORTH:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);

                    case ASCENDING_SOUTH:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);

                    case SOUTH_EAST:
                        return pState.setValue(SHAPE, RailShape.NORTH_EAST);

                    case SOUTH_WEST:
                        return pState.setValue(SHAPE, RailShape.NORTH_WEST);

                    case NORTH_WEST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_WEST);

                    case NORTH_EAST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_EAST);

                    default:
                        return super.mirror(pState, pMirror);
                }

            case FRONT_BACK:
                switch (railshape)
                {
                    case ASCENDING_EAST:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);

                    case ASCENDING_WEST:
                        return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);

                    case ASCENDING_NORTH:
                    case ASCENDING_SOUTH:
                    default:
                        break;

                    case SOUTH_EAST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_WEST);

                    case SOUTH_WEST:
                        return pState.setValue(SHAPE, RailShape.SOUTH_EAST);

                    case NORTH_WEST:
                        return pState.setValue(SHAPE, RailShape.NORTH_EAST);

                    case NORTH_EAST:
                        return pState.setValue(SHAPE, RailShape.NORTH_WEST);
                }
        }

        return super.mirror(pState, pMirror);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(SHAPE, POWERED, WATERLOGGED);
    }
}
