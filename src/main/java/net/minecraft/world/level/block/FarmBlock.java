package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FarmBlock extends Block
{
    public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
    public static final int MAX_MOISTURE = 7;

    protected FarmBlock(BlockBehaviour.Properties p_53247_)
    {
        super(p_53247_);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, Integer.valueOf(0)));
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        if (pFacing == Direction.UP && !pState.canSurvive(pLevel, pCurrentPos))
        {
            pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        BlockState blockstate = pLevel.getBlockState(pPos.above());
        return !blockstate.getMaterial().isSolid() || blockstate.getBlock() instanceof FenceGateBlock || blockstate.getBlock() instanceof MovingPistonBlock;
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        return !this.defaultBlockState().canSurvive(pContext.getLevel(), pContext.getClickedPos()) ? Blocks.DIRT.defaultBlockState() : super.getStateForPlacement(pContext);
    }

    public boolean useShapeForLightOcclusion(BlockState pState)
    {
        return true;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE;
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
        if (!pState.canSurvive(pLevel, pPos))
        {
            turnToDirt(pState, pLevel, pPos);
        }
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom)
    {
        int i = pState.getValue(MOISTURE);

        if (!isNearWater(pLevel, pPos) && !pLevel.isRainingAt(pPos.above()))
        {
            if (i > 0)
            {
                pLevel.setBlock(pPos, pState.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
            }
            else if (!isUnderCrops(pLevel, pPos))
            {
                turnToDirt(pState, pLevel, pPos);
            }
        }
        else if (i < 7)
        {
            pLevel.setBlock(pPos, pState.setValue(MOISTURE, Integer.valueOf(7)), 2);
        }
    }

    public void fallOn(Level p_153227_, BlockState p_153228_, BlockPos p_153229_, Entity p_153230_, float p_153231_)
    {
        if (!p_153227_.isClientSide && p_153227_.random.nextFloat() < p_153231_ - 0.5F && p_153230_ instanceof LivingEntity && (p_153230_ instanceof Player || p_153227_.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) && p_153230_.getBbWidth() * p_153230_.getBbWidth() * p_153230_.getBbHeight() > 0.512F)
        {
            turnToDirt(p_153228_, p_153227_, p_153229_);
        }

        super.fallOn(p_153227_, p_153228_, p_153229_, p_153230_, p_153231_);
    }

    public static void turnToDirt(BlockState pState, Level pLevel, BlockPos pPos)
    {
        pLevel.setBlockAndUpdate(pPos, pushEntitiesUp(pState, Blocks.DIRT.defaultBlockState(), pLevel, pPos));
    }

    private static boolean isUnderCrops(BlockGetter pLevel, BlockPos pPos)
    {
        Block block = pLevel.getBlockState(pPos.above()).getBlock();
        return block instanceof CropBlock || block instanceof StemBlock || block instanceof AttachedStemBlock;
    }

    private static boolean isNearWater(LevelReader pLevel, BlockPos pPos)
    {
        for (BlockPos blockpos : BlockPos.betweenClosed(pPos.offset(-4, 0, -4), pPos.offset(4, 1, 4)))
        {
            if (pLevel.getFluidState(blockpos).is(FluidTags.WATER))
            {
                return true;
            }
        }

        return false;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(MOISTURE);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType)
    {
        return false;
    }
}
