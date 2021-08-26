package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WritableBookItem extends Item
{
    public WritableBookItem(Item.Properties p_43445_)
    {
        super(p_43445_);
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);

        if (blockstate.is(Blocks.LECTERN))
        {
            return LecternBlock.tryPlaceBook(pContext.getPlayer(), level, blockpos, blockstate, pContext.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        }
        else
        {
            return InteractionResult.PASS;
        }
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.openItemGui(itemstack, pHand);
        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    public static boolean makeSureTagIsValid(@Nullable CompoundTag pNbt)
    {
        if (pNbt == null)
        {
            return false;
        }
        else if (!pNbt.contains("pages", 9))
        {
            return false;
        }
        else
        {
            ListTag listtag = pNbt.getList("pages", 8);

            for (int i = 0; i < listtag.size(); ++i)
            {
                String s = listtag.getString(i);

                if (s.length() > 32767)
                {
                    return false;
                }
            }

            return true;
        }
    }
}
