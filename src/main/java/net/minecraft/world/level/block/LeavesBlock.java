package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeavesBlock extends Block
{
    public static final int DECAY_DISTANCE = 7;
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    private static final int TICK_DELAY = 1;

    public LeavesBlock(BlockBehaviour.Properties p_54422_)
    {
        super(p_54422_);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(PERSISTENT, Boolean.valueOf(false)));
    }

    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos)
    {
        return Shapes.empty();
    }

    public boolean isRandomlyTicking(BlockState pState)
    {
        return pState.getValue(DISTANCE) == 7 && !pState.getValue(PERSISTENT);
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom)
    {
        if (!pState.getValue(PERSISTENT) && pState.getValue(DISTANCE) == 7)
        {
            dropResources(pState, pLevel, pPos);
            pLevel.removeBlock(pPos, false);
        }
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        pLevel.setBlock(pPos, updateDistance(pState, pLevel, pPos), 3);
    }

    public int getLightBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return 1;
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        int i = getDistanceAt(pFacingState) + 1;

        if (i != 1 || pState.getValue(DISTANCE) != i)
        {
            pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
        }

        return pState;
    }

    private static BlockState updateDistance(BlockState pState, LevelAccessor pLevel, BlockPos pPos)
    {
        int i = 7;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values())
        {
            blockpos$mutableblockpos.setWithOffset(pPos, direction);
            i = Math.min(i, getDistanceAt(pLevel.getBlockState(blockpos$mutableblockpos)) + 1);

            if (i == 1)
            {
                break;
            }
        }

        return pState.setValue(DISTANCE, Integer.valueOf(i));
    }

    private static int getDistanceAt(BlockState pNeighbor)
    {
        if (pNeighbor.is(BlockTags.LOGS))
        {
            return 0;
        }
        else
        {
            return pNeighbor.getBlock() instanceof LeavesBlock ? pNeighbor.getValue(DISTANCE) : 7;
        }
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        if (pLevel.isRainingAt(pPos.above()))
        {
            if (pRand.nextInt(15) == 1)
            {
                BlockPos blockpos = pPos.below();
                BlockState blockstate = pLevel.getBlockState(blockpos);

                if (!blockstate.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP))
                {
                    double d0 = (double)pPos.getX() + pRand.nextDouble();
                    double d1 = (double)pPos.getY() - 0.05D;
                    double d2 = (double)pPos.getZ() + pRand.nextDouble();
                    pLevel.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(DISTANCE, PERSISTENT);
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        return updateDistance(this.defaultBlockState().setValue(PERSISTENT, Boolean.valueOf(true)), pContext.getLevel(), pContext.getClickedPos());
    }
}
