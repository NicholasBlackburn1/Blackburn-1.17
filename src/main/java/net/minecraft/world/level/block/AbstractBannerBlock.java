package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBannerBlock extends BaseEntityBlock
{
    private final DyeColor color;

    protected AbstractBannerBlock(DyeColor p_48659_, BlockBehaviour.Properties p_48660_)
    {
        super(p_48660_);
        this.color = p_48659_;
    }

    public boolean isPossibleToRespawnInThis()
    {
        return true;
    }

    public BlockEntity newBlockEntity(BlockPos p_151892_, BlockState p_151893_)
    {
        return new BannerBlockEntity(p_151892_, p_151893_, this.color);
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack)
    {
        if (pStack.hasCustomHoverName())
        {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);

            if (blockentity instanceof BannerBlockEntity)
            {
                ((BannerBlockEntity)blockentity).setCustomName(pStack.getHoverName());
            }
        }
    }

    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        return blockentity instanceof BannerBlockEntity ? ((BannerBlockEntity)blockentity).getItem() : super.getCloneItemStack(pLevel, pPos, pState);
    }

    public DyeColor getColor()
    {
        return this.color;
    }
}
