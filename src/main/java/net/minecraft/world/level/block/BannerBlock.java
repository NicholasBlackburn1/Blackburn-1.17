package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BannerBlock extends AbstractBannerBlock
{
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final Map<DyeColor, Block> BY_COLOR = Maps.newHashMap();
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public BannerBlock(DyeColor p_49012_, BlockBehaviour.Properties p_49013_)
    {
        super(p_49012_, p_49013_);
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, Integer.valueOf(0)));
        BY_COLOR.put(p_49012_, this);
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        return pLevel.getBlockState(pPos.below()).getMaterial().isSolid();
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE;
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        return this.defaultBlockState().setValue(ROTATION, Integer.valueOf(Mth.floor((double)((180.0F + pContext.getRotation()) * 16.0F / 360.0F) + 0.5D) & 15));
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public BlockState rotate(BlockState pState, Rotation pRot)
    {
        return pState.setValue(ROTATION, Integer.valueOf(pRot.rotate(pState.getValue(ROTATION), 16)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror)
    {
        return pState.setValue(ROTATION, Integer.valueOf(pMirror.mirror(pState.getValue(ROTATION), 16)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(ROTATION);
    }

    public static Block byColor(DyeColor pColor)
    {
        return BY_COLOR.getOrDefault(pColor, Blocks.WHITE_BANNER);
    }
}
