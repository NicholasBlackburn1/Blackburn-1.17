package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RepeaterBlock extends DiodeBlock
{
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

    protected RepeaterBlock(BlockBehaviour.Properties p_55801_)
    {
        super(p_55801_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DELAY, Integer.valueOf(1)).setValue(LOCKED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)));
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (!pPlayer.getAbilities().mayBuild)
        {
            return InteractionResult.PASS;
        }
        else
        {
            pLevel.setBlock(pPos, pState.cycle(DELAY), 3);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }

    protected int getDelay(BlockState pState)
    {
        return pState.getValue(DELAY) * 2;
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        BlockState blockstate = super.getStateForPlacement(pContext);
        return blockstate.setValue(LOCKED, Boolean.valueOf(this.isLocked(pContext.getLevel(), pContext.getClickedPos(), blockstate)));
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        return !pLevel.isClientSide() && pFacing.getAxis() != pState.getValue(FACING).getAxis() ? pState.setValue(LOCKED, Boolean.valueOf(this.isLocked(pLevel, pCurrentPos, pState))) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public boolean isLocked(LevelReader pLevel, BlockPos pPos, BlockState pState)
    {
        return this.getAlternateSignal(pLevel, pPos, pState) > 0;
    }

    protected boolean isAlternateInput(BlockState pState)
    {
        return isDiode(pState);
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        if (pState.getValue(POWERED))
        {
            Direction direction = pState.getValue(FACING);
            double d0 = (double)pPos.getX() + 0.5D + (pRand.nextDouble() - 0.5D) * 0.2D;
            double d1 = (double)pPos.getY() + 0.4D + (pRand.nextDouble() - 0.5D) * 0.2D;
            double d2 = (double)pPos.getZ() + 0.5D + (pRand.nextDouble() - 0.5D) * 0.2D;
            float f = -5.0F;

            if (pRand.nextBoolean())
            {
                f = (float)(pState.getValue(DELAY) * 2 - 1);
            }

            f = f / 16.0F;
            double d3 = (double)(f * (float)direction.getStepX());
            double d4 = (double)(f * (float)direction.getStepZ());
            pLevel.addParticle(DustParticleOptions.REDSTONE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(FACING, DELAY, LOCKED, POWERED);
    }
}
