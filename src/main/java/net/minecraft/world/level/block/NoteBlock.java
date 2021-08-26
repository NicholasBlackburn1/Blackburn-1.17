package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;

public class NoteBlock extends Block
{
    public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty NOTE = BlockStateProperties.NOTE;

    public NoteBlock(BlockBehaviour.Properties p_55016_)
    {
        super(p_55016_);
        this.registerDefaultState(this.stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, Integer.valueOf(0)).setValue(POWERED, Boolean.valueOf(false)));
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        return this.defaultBlockState().setValue(INSTRUMENT, NoteBlockInstrument.byState(pContext.getLevel().getBlockState(pContext.getClickedPos().below())));
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        return pFacing == Direction.DOWN ? pState.setValue(INSTRUMENT, NoteBlockInstrument.byState(pFacingState)) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving)
    {
        boolean flag = pLevel.hasNeighborSignal(pPos);

        if (flag != pState.getValue(POWERED))
        {
            if (flag)
            {
                this.playNote(pLevel, pPos);
            }

            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)), 3);
        }
    }

    private void playNote(Level pLevel, BlockPos pPos)
    {
        if (pLevel.getBlockState(pPos.above()).isAir())
        {
            pLevel.blockEvent(pPos, this, 0, 0);
        }
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (pLevel.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        else
        {
            pState = pState.cycle(NOTE);
            pLevel.setBlock(pPos, pState, 3);
            this.playNote(pLevel, pPos);
            pPlayer.awardStat(Stats.TUNE_NOTEBLOCK);
            return InteractionResult.CONSUME;
        }
    }

    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer)
    {
        if (!pLevel.isClientSide)
        {
            this.playNote(pLevel, pPos);
            pPlayer.awardStat(Stats.PLAY_NOTEBLOCK);
        }
    }

    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam)
    {
        int i = pState.getValue(NOTE);
        float f = (float)Math.pow(2.0D, (double)(i - 12) / 12.0D);
        pLevel.playSound((Player)null, pPos, pState.getValue(INSTRUMENT).getSoundEvent(), SoundSource.RECORDS, 3.0F, f);
        pLevel.addParticle(ParticleTypes.NOTE, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 1.2D, (double)pPos.getZ() + 0.5D, (double)i / 24.0D, 0.0D, 0.0D);
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(INSTRUMENT, POWERED, NOTE);
    }
}
