package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SeaPickleBlock extends BushBlock implements BonemealableBlock, SimpleWaterloggedBlock
{
    public static final int MAX_PICKLES = 4;
    public static final IntegerProperty PICKLES = BlockStateProperties.PICKLES;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape ONE_AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);
    protected static final VoxelShape TWO_AABB = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 6.0D, 13.0D);
    protected static final VoxelShape THREE_AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D);
    protected static final VoxelShape FOUR_AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 7.0D, 14.0D);

    protected SeaPickleBlock(BlockBehaviour.Properties p_56082_)
    {
        super(p_56082_);
        this.registerDefaultState(this.stateDefinition.any().setValue(PICKLES, Integer.valueOf(1)).setValue(WATERLOGGED, Boolean.valueOf(true)));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());

        if (blockstate.is(this))
        {
            return blockstate.setValue(PICKLES, Integer.valueOf(Math.min(4, blockstate.getValue(PICKLES) + 1)));
        }
        else
        {
            FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            return super.getStateForPlacement(pContext).setValue(WATERLOGGED, Boolean.valueOf(flag));
        }
    }

    public static boolean isDead(BlockState pState)
    {
        return !pState.getValue(WATERLOGGED);
    }

    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return !pState.getCollisionShape(pLevel, pPos).getFaceShape(Direction.UP).isEmpty() || pState.isFaceSturdy(pLevel, pPos, Direction.UP);
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        BlockPos blockpos = pPos.below();
        return this.mayPlaceOn(pLevel.getBlockState(blockpos), pLevel, blockpos);
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (!pState.canSurvive(pLevel, pCurrentPos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        else
        {
            if (pState.getValue(WATERLOGGED))
            {
                pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
            }

            return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        }
    }

    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext)
    {
        return !pUseContext.isSecondaryUseActive() && pUseContext.getItemInHand().is(this.asItem()) && pState.getValue(PICKLES) < 4 ? true : super.canBeReplaced(pState, pUseContext);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        switch (pState.getValue(PICKLES))
        {
            case 1:
            default:
                return ONE_AABB;

            case 2:
                return TWO_AABB;

            case 3:
                return THREE_AABB;

            case 4:
                return FOUR_AABB;
        }
    }

    public FluidState getFluidState(BlockState pState)
    {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(PICKLES, WATERLOGGED);
    }

    public boolean isValidBonemealTarget(BlockGetter pLevel, BlockPos pPos, BlockState pState, boolean pIsClient)
    {
        return true;
    }

    public boolean isBonemealSuccess(Level pLevel, Random pRand, BlockPos pPos, BlockState pState)
    {
        return true;
    }

    public void performBonemeal(ServerLevel pLevel, Random pRand, BlockPos pPos, BlockState pState)
    {
        if (!isDead(pState) && pLevel.getBlockState(pPos.below()).is(BlockTags.CORAL_BLOCKS))
        {
            int i = 5;
            int j = 1;
            int k = 2;
            int l = 0;
            int i1 = pPos.getX() - 2;
            int j1 = 0;

            for (int k1 = 0; k1 < 5; ++k1)
            {
                for (int l1 = 0; l1 < j; ++l1)
                {
                    int i2 = 2 + pPos.getY() - 1;

                    for (int j2 = i2 - 2; j2 < i2; ++j2)
                    {
                        BlockPos blockpos = new BlockPos(i1 + k1, j2, pPos.getZ() - j1 + l1);

                        if (blockpos != pPos && pRand.nextInt(6) == 0 && pLevel.getBlockState(blockpos).is(Blocks.WATER))
                        {
                            BlockState blockstate = pLevel.getBlockState(blockpos.below());

                            if (blockstate.is(BlockTags.CORAL_BLOCKS))
                            {
                                pLevel.setBlock(blockpos, Blocks.SEA_PICKLE.defaultBlockState().setValue(PICKLES, Integer.valueOf(pRand.nextInt(4) + 1)), 3);
                            }
                        }
                    }
                }

                if (l < 2)
                {
                    j += 2;
                    ++j1;
                }
                else
                {
                    j -= 2;
                    --j1;
                }

                ++l;
            }

            pLevel.setBlock(pPos, pState.setValue(PICKLES, Integer.valueOf(4)), 2);
        }
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType)
    {
        return false;
    }
}
