package net.minecraft.world.item;

import java.util.stream.Stream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemUtils
{
    public static InteractionResultHolder<ItemStack> startUsingInstantly(Level p_150960_, Player p_150961_, InteractionHand p_150962_)
    {
        p_150961_.startUsingItem(p_150962_);
        return InteractionResultHolder.consume(p_150961_.getItemInHand(p_150962_));
    }

    public static ItemStack createFilledResult(ItemStack pEmpty, Player pPlayer, ItemStack pFilled, boolean p_41821_)
    {
        boolean flag = pPlayer.getAbilities().instabuild;

        if (p_41821_ && flag)
        {
            if (!pPlayer.getInventory().contains(pFilled))
            {
                pPlayer.getInventory().add(pFilled);
            }

            return pEmpty;
        }
        else
        {
            if (!flag)
            {
                pEmpty.shrink(1);
            }

            if (pEmpty.isEmpty())
            {
                return pFilled;
            }
            else
            {
                if (!pPlayer.getInventory().add(pFilled))
                {
                    pPlayer.drop(pFilled, false);
                }

                return pEmpty;
            }
        }
    }

    public static ItemStack createFilledResult(ItemStack pEmpty, Player pPlayer, ItemStack pFilled)
    {
        return createFilledResult(pEmpty, pPlayer, pFilled, true);
    }

    public static void onContainerDestroyed(ItemEntity p_150953_, Stream<ItemStack> p_150954_)
    {
        Level level = p_150953_.level;

        if (!level.isClientSide)
        {
            p_150954_.forEach((p_160857_2_) ->
            {
                level.addFreshEntity(new ItemEntity(level, p_150953_.getX(), p_150953_.getY(), p_150953_.getZ(), p_160857_2_));
            });
        }
    }
}
