package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DaylightDetectorBlock extends BaseEntityBlock
{
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    public DaylightDetectorBlock(BlockBehaviour.Properties p_52382_)
    {
        super(p_52382_);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)).setValue(INVERTED, Boolean.valueOf(false)));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE;
    }

    public boolean useShapeForLightOcclusion(BlockState pState)
    {
        return true;
    }

    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return pBlockState.getValue(POWER);
    }

    private static void updateSignalStrength(BlockState pState, Level pLevel, BlockPos pPos)
    {
        int i = pLevel.getBrightness(LightLayer.SKY, pPos) - pLevel.getSkyDarken();
        float f = pLevel.getSunAngle(1.0F);
        boolean flag = pState.getValue(INVERTED);

        if (flag)
        {
            i = 15 - i;
        }
        else if (i > 0)
        {
            float f1 = f < (float)Math.PI ? 0.0F : ((float)Math.PI * 2F);
            f = f + (f1 - f) * 0.2F;
            i = Math.round((float)i * Mth.cos(f));
        }

        i = Mth.clamp(i, 0, 15);

        if (pState.getValue(POWER) != i)
        {
            pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(i)), 3);
        }
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (pPlayer.mayBuild())
        {
            if (pLevel.isClientSide)
            {
                return InteractionResult.SUCCESS;
            }
            else
            {
                BlockState blockstate = pState.cycle(INVERTED);
                pLevel.setBlock(pPos, blockstate, 4);
                updateSignalStrength(blockstate, pLevel, pPos);
                return InteractionResult.CONSUME;
            }
        }
        else
        {
            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        }
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    public boolean isSignalSource(BlockState pState)
    {
        return true;
    }

    public BlockEntity newBlockEntity(BlockPos p_153118_, BlockState p_153119_)
    {
        return new DaylightDetectorBlockEntity(p_153118_, p_153119_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153109_, BlockState p_153110_, BlockEntityType<T> p_153111_)
    {
        return !p_153109_.isClientSide && p_153109_.dimensionType().hasSkyLight() ? createTickerHelper(p_153111_, BlockEntityType.DAYLIGHT_DETECTOR, DaylightDetectorBlock::tickEntity) : null;
    }

    private static void tickEntity(Level p_153113_, BlockPos p_153114_, BlockState p_153115_, DaylightDetectorBlockEntity p_153116_)
    {
        if (p_153113_.getGameTime() % 20L == 0L)
        {
            updateSignalStrength(p_153115_, p_153113_, p_153114_);
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(POWER, INVERTED);
    }
}
