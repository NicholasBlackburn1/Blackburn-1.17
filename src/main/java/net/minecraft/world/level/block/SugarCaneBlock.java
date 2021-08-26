package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SugarCaneBlock extends Block
{
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    protected static final float AABB_OFFSET = 6.0F;
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    protected SugarCaneBlock(BlockBehaviour.Properties p_57168_)
    {
        super(p_57168_);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE;
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        if (!pState.canSurvive(pLevel, pPos))
        {
            pLevel.destroyBlock(pPos, true);
        }
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom)
    {
        if (pLevel.isEmptyBlock(pPos.above()))
        {
            int i;

            for (i = 1; pLevel.getBlockState(pPos.below(i)).is(this); ++i)
            {
            }

            if (i < 3)
            {
                int j = pState.getValue(AGE);

                if (j == 15)
                {
                    pLevel.setBlockAndUpdate(pPos.above(), this.defaultBlockState());
                    pLevel.setBlock(pPos, pState.setValue(AGE, Integer.valueOf(0)), 4);
                }
                else
                {
                    pLevel.setBlock(pPos, pState.setValue(AGE, Integer.valueOf(j + 1)), 4);
                }
            }
        }
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (!pState.canSurvive(pLevel, pCurrentPos))
        {
            pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        BlockState blockstate = pLevel.getBlockState(pPos.below());

        if (blockstate.is(this))
        {
            return true;
        }
        else
        {
            if (blockstate.is(BlockTags.DIRT) || blockstate.is(Blocks.SAND) || blockstate.is(Blocks.RED_SAND))
            {
                BlockPos blockpos = pPos.below();

                for (Direction direction : Direction.Plane.HORIZONTAL)
                {
                    BlockState blockstate1 = pLevel.getBlockState(blockpos.relative(direction));
                    FluidState fluidstate = pLevel.getFluidState(blockpos.relative(direction));

                    if (fluidstate.is(FluidTags.WATER) || blockstate1.is(Blocks.FROSTED_ICE))
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(AGE);
    }
}
