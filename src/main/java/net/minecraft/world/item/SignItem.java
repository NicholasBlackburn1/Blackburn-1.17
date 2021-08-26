package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SignItem extends StandingAndWallBlockItem
{
    public SignItem(Item.Properties p_43126_, Block p_43127_, Block p_43128_)
    {
        super(p_43127_, p_43128_, p_43126_);
    }

    protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState)
    {
        boolean flag = super.updateCustomBlockEntityTag(pPos, pLevel, pPlayer, pStack, pState);

        if (!pLevel.isClientSide && !flag && pPlayer != null)
        {
            pPlayer.openTextEdit((SignBlockEntity)pLevel.getBlockEntity(pPos));
        }

        return flag;
    }
}
