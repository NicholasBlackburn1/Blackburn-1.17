package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePressurePlateBlock extends Block
{
    protected static final VoxelShape PRESSED_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 0.5D, 15.0D);
    protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
    protected static final AABB TOUCH_AABB = new AABB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);

    protected BasePressurePlateBlock(BlockBehaviour.Properties p_49290_)
    {
        super(p_49290_);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return this.getSignalForState(pState) > 0 ? PRESSED_AABB : AABB;
    }

    protected int getPressedTime()
    {
        return 20;
    }

    public boolean isPossibleToRespawnInThis()
    {
        return true;
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        BlockPos blockpos = pPos.below();
        return canSupportRigidBlock(pLevel, blockpos) || canSupportCenter(pLevel, blockpos, Direction.UP);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        int i = this.getSignalForState(pState);

        if (i > 0)
        {
            this.checkPressed((Entity)null, pLevel, pPos, pState, i);
        }
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity)
    {
        if (!pLevel.isClientSide)
        {
            int i = this.getSignalForState(pState);

            if (i == 0)
            {
                this.checkPressed(pEntity, pLevel, pPos, pState, i);
            }
        }
    }

    protected void checkPressed(@Nullable Entity p_152144_, Level p_152145_, BlockPos p_152146_, BlockState p_152147_, int p_152148_)
    {
        int i = this.getSignalStrength(p_152145_, p_152146_);
        boolean flag = p_152148_ > 0;
        boolean flag1 = i > 0;

        if (p_152148_ != i)
        {
            BlockState blockstate = this.setSignalForState(p_152147_, i);
            p_152145_.setBlock(p_152146_, blockstate, 2);
            this.updateNeighbours(p_152145_, p_152146_);
            p_152145_.setBlocksDirty(p_152146_, p_152147_, blockstate);
        }

        if (!flag1 && flag)
        {
            this.playOffSound(p_152145_, p_152146_);
            p_152145_.gameEvent(p_152144_, GameEvent.BLOCK_UNPRESS, p_152146_);
        }
        else if (flag1 && !flag)
        {
            this.playOnSound(p_152145_, p_152146_);
            p_152145_.gameEvent(p_152144_, GameEvent.BLOCK_PRESS, p_152146_);
        }

        if (flag1)
        {
            p_152145_.getBlockTicks().scheduleTick(new BlockPos(p_152146_), this, this.getPressedTime());
        }
    }

    protected abstract void playOnSound(LevelAccessor pLevel, BlockPos pPos);

    protected abstract void playOffSound(LevelAccessor pLevel, BlockPos pPos);

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pIsMoving && !pState.is(pNewState.getBlock()))
        {
            if (this.getSignalForState(pState) > 0)
            {
                this.updateNeighbours(pLevel, pPos);
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    protected void updateNeighbours(Level pLevel, BlockPos pPos)
    {
        pLevel.updateNeighborsAt(pPos, this);
        pLevel.updateNeighborsAt(pPos.below(), this);
    }

    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return this.getSignalForState(pBlockState);
    }

    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return pSide == Direction.UP ? this.getSignalForState(pBlockState) : 0;
    }

    public boolean isSignalSource(BlockState pState)
    {
        return true;
    }

    public PushReaction getPistonPushReaction(BlockState pState)
    {
        return PushReaction.DESTROY;
    }

    protected abstract int getSignalStrength(Level pLevel, BlockPos pPos);

    protected abstract int getSignalForState(BlockState pState);

    protected abstract BlockState setSignalForState(BlockState pState, int pStrength);
}
