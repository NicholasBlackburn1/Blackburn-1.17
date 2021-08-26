package net.minecraft.world.level.block;

import com.google.common.base.MoreObjects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireHookBlock extends Block
{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final int WIRE_DIST_MIN = 1;
    protected static final int WIRE_DIST_MAX = 42;
    private static final int RECHECK_PERIOD = 10;
    protected static final int AABB_OFFSET = 3;
    protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 0.0D, 10.0D, 11.0D, 10.0D, 16.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 10.0D, 6.0D);
    protected static final VoxelShape WEST_AABB = Block.box(10.0D, 0.0D, 5.0D, 16.0D, 10.0D, 11.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 5.0D, 6.0D, 10.0D, 11.0D);

    public TripWireHookBlock(BlockBehaviour.Properties p_57676_)
    {
        super(p_57676_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false)));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        switch ((Direction)pState.getValue(FACING))
        {
            case EAST:
            default:
                return EAST_AABB;

            case WEST:
                return WEST_AABB;

            case SOUTH:
                return SOUTH_AABB;

            case NORTH:
                return NORTH_AABB;
        }
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        Direction direction = pState.getValue(FACING);
        BlockPos blockpos = pPos.relative(direction.getOpposite());
        BlockState blockstate = pLevel.getBlockState(blockpos);
        return direction.getAxis().isHorizontal() && blockstate.isFaceSturdy(pLevel, blockpos, direction);
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        return pFacing.getOpposite() == pState.getValue(FACING) && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        BlockState blockstate = this.defaultBlockState().setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false));
        LevelReader levelreader = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        Direction[] adirection = pContext.getNearestLookingDirections();

        for (Direction direction : adirection)
        {
            if (direction.getAxis().isHorizontal())
            {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.setValue(FACING, direction1);

                if (blockstate.canSurvive(levelreader, blockpos))
                {
                    return blockstate;
                }
            }
        }

        return null;
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack)
    {
        this.calculateState(pLevel, pPos, pState, false, false, -1, (BlockState)null);
    }

    public void calculateState(Level pLevel, BlockPos pPos, BlockState pHookState, boolean pAttaching, boolean pShouldNotifyNeighbours, int pSearchRange, @Nullable BlockState pState)
    {
        Direction direction = pHookState.getValue(FACING);
        boolean flag = pHookState.getValue(ATTACHED);
        boolean flag1 = pHookState.getValue(POWERED);
        boolean flag2 = !pAttaching;
        boolean flag3 = false;
        int i = 0;
        BlockState[] ablockstate = new BlockState[42];

        for (int j = 1; j < 42; ++j)
        {
            BlockPos blockpos = pPos.relative(direction, j);
            BlockState blockstate = pLevel.getBlockState(blockpos);

            if (blockstate.is(Blocks.TRIPWIRE_HOOK))
            {
                if (blockstate.getValue(FACING) == direction.getOpposite())
                {
                    i = j;
                }

                break;
            }

            if (!blockstate.is(Blocks.TRIPWIRE) && j != pSearchRange)
            {
                ablockstate[j] = null;
                flag2 = false;
            }
            else
            {
                if (j == pSearchRange)
                {
                    blockstate = MoreObjects.firstNonNull(pState, blockstate);
                }

                boolean flag4 = !blockstate.getValue(TripWireBlock.DISARMED);
                boolean flag5 = blockstate.getValue(TripWireBlock.POWERED);
                flag3 |= flag4 && flag5;
                ablockstate[j] = blockstate;

                if (j == pSearchRange)
                {
                    pLevel.getBlockTicks().scheduleTick(pPos, this, 10);
                    flag2 &= flag4;
                }
            }
        }

        flag2 = flag2 & i > 1;
        flag3 = flag3 & flag2;
        BlockState blockstate1 = this.defaultBlockState().setValue(ATTACHED, Boolean.valueOf(flag2)).setValue(POWERED, Boolean.valueOf(flag3));

        if (i > 0)
        {
            BlockPos blockpos1 = pPos.relative(direction, i);
            Direction direction1 = direction.getOpposite();
            pLevel.setBlock(blockpos1, blockstate1.setValue(FACING, direction1), 3);
            this.notifyNeighbors(pLevel, blockpos1, direction1);
            this.playSound(pLevel, blockpos1, flag2, flag3, flag, flag1);
        }

        this.playSound(pLevel, pPos, flag2, flag3, flag, flag1);

        if (!pAttaching)
        {
            pLevel.setBlock(pPos, blockstate1.setValue(FACING, direction), 3);

            if (pShouldNotifyNeighbours)
            {
                this.notifyNeighbors(pLevel, pPos, direction);
            }
        }

        if (flag != flag2)
        {
            for (int k = 1; k < i; ++k)
            {
                BlockPos blockpos2 = pPos.relative(direction, k);
                BlockState blockstate2 = ablockstate[k];

                if (blockstate2 != null)
                {
                    pLevel.setBlock(blockpos2, blockstate2.setValue(ATTACHED, Boolean.valueOf(flag2)), 3);

                    if (!pLevel.getBlockState(blockpos2).isAir())
                    {
                    }
                }
            }
        }
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        this.calculateState(pLevel, pPos, pState, false, true, -1, (BlockState)null);
    }

    private void playSound(Level pLevel, BlockPos pPos, boolean pAttaching, boolean pActivated, boolean pDetaching, boolean pDeactivating)
    {
        if (pActivated && !pDeactivating)
        {
            pLevel.playSound((Player)null, pPos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4F, 0.6F);
            pLevel.gameEvent(GameEvent.BLOCK_PRESS, pPos);
        }
        else if (!pActivated && pDeactivating)
        {
            pLevel.playSound((Player)null, pPos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4F, 0.5F);
            pLevel.gameEvent(GameEvent.BLOCK_UNPRESS, pPos);
        }
        else if (pAttaching && !pDetaching)
        {
            pLevel.playSound((Player)null, pPos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4F, 0.7F);
            pLevel.gameEvent(GameEvent.BLOCK_ATTACH, pPos);
        }
        else if (!pAttaching && pDetaching)
        {
            pLevel.playSound((Player)null, pPos, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4F, 1.2F / (pLevel.random.nextFloat() * 0.2F + 0.9F));
            pLevel.gameEvent(GameEvent.BLOCK_DETACH, pPos);
        }
    }

    private void notifyNeighbors(Level pLevel, BlockPos pPos, Direction pSide)
    {
        pLevel.updateNeighborsAt(pPos, this);
        pLevel.updateNeighborsAt(pPos.relative(pSide.getOpposite()), this);
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pIsMoving && !pState.is(pNewState.getBlock()))
        {
            boolean flag = pState.getValue(ATTACHED);
            boolean flag1 = pState.getValue(POWERED);

            if (flag || flag1)
            {
                this.calculateState(pLevel, pPos, pState, true, false, -1, (BlockState)null);
            }

            if (flag1)
            {
                pLevel.updateNeighborsAt(pPos, this);
                pLevel.updateNeighborsAt(pPos.relative(pState.getValue(FACING).getOpposite()), this);
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
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
            return pBlockState.getValue(FACING) == pSide ? 15 : 0;
        }
    }

    public boolean isSignalSource(BlockState pState)
    {
        return true;
    }

    public BlockState rotate(BlockState pState, Rotation pRot)
    {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror)
    {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(FACING, POWERED, ATTACHED);
    }
}
