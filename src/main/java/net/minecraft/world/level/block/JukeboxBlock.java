package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock extends BaseEntityBlock
{
    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

    protected JukeboxBlock(BlockBehaviour.Properties p_54257_)
    {
        super(p_54257_);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)));
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack)
    {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        CompoundTag compoundtag = pStack.getOrCreateTag();

        if (compoundtag.contains("BlockEntityTag"))
        {
            CompoundTag compoundtag1 = compoundtag.getCompound("BlockEntityTag");

            if (compoundtag1.contains("RecordItem"))
            {
                pLevel.setBlock(pPos, pState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
            }
        }
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (pState.getValue(HAS_RECORD))
        {
            this.dropRecording(pLevel, pPos);
            pState = pState.setValue(HAS_RECORD, Boolean.valueOf(false));
            pLevel.setBlock(pPos, pState, 2);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        else
        {
            return InteractionResult.PASS;
        }
    }

    public void setRecord(LevelAccessor pLevel, BlockPos pPos, BlockState pState, ItemStack pRecordStack)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof JukeboxBlockEntity)
        {
            ((JukeboxBlockEntity)blockentity).setRecord(pRecordStack.copy());
            pLevel.setBlock(pPos, pState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
        }
    }

    private void dropRecording(Level pLevel, BlockPos pPos)
    {
        if (!pLevel.isClientSide)
        {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);

            if (blockentity instanceof JukeboxBlockEntity)
            {
                JukeboxBlockEntity jukeboxblockentity = (JukeboxBlockEntity)blockentity;
                ItemStack itemstack = jukeboxblockentity.getRecord();

                if (!itemstack.isEmpty())
                {
                    pLevel.levelEvent(1010, pPos, 0);
                    jukeboxblockentity.clearContent();
                    float f = 0.7F;
                    double d0 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.15F;
                    double d1 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
                    double d2 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.15F;
                    ItemStack itemstack1 = itemstack.copy();
                    ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, itemstack1);
                    itementity.setDefaultPickUpDelay();
                    pLevel.addFreshEntity(itementity);
                }
            }
        }
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (!pState.is(pNewState.getBlock()))
        {
            this.dropRecording(pLevel, pPos);
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    public BlockEntity newBlockEntity(BlockPos p_153451_, BlockState p_153452_)
    {
        return new JukeboxBlockEntity(p_153451_, p_153452_);
    }

    public boolean hasAnalogOutputSignal(BlockState pState)
    {
        return true;
    }

    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof JukeboxBlockEntity)
        {
            Item item = ((JukeboxBlockEntity)blockentity).getRecord().getItem();

            if (item instanceof RecordItem)
            {
                return ((RecordItem)item).getAnalogOutput();
            }
        }

        return 0;
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.m_61104_(HAS_RECORD);
    }
}
