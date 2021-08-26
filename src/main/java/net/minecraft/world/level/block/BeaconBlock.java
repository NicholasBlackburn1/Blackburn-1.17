package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BeaconBlock extends BaseEntityBlock implements BeaconBeamBlock
{
    public BeaconBlock(BlockBehaviour.Properties p_49421_)
    {
        super(p_49421_);
    }

    public DyeColor getColor()
    {
        return DyeColor.WHITE;
    }

    public BlockEntity newBlockEntity(BlockPos p_152164_, BlockState p_152165_)
    {
        return new BeaconBlockEntity(p_152164_, p_152165_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152160_, BlockState p_152161_, BlockEntityType<T> p_152162_)
    {
        return createTickerHelper(p_152162_, BlockEntityType.BEACON, BeaconBlockEntity::tick);
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (pLevel.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        else
        {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);

            if (blockentity instanceof BeaconBlockEntity)
            {
                pPlayer.openMenu((BeaconBlockEntity)blockentity);
                pPlayer.awardStat(Stats.INTERACT_WITH_BEACON);
            }

            return InteractionResult.CONSUME;
        }
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack)
    {
        if (pStack.hasCustomHoverName())
        {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);

            if (blockentity instanceof BeaconBlockEntity)
            {
                ((BeaconBlockEntity)blockentity).setCustomName(pStack.getHoverName());
            }
        }
    }
}
