package net.minecraft.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class NameTagItem extends Item
{
    public NameTagItem(Item.Properties p_42952_)
    {
        super(p_42952_);
    }

    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pTarget, InteractionHand pHand)
    {
        if (pStack.hasCustomHoverName() && !(pTarget instanceof Player))
        {
            if (!pPlayer.level.isClientSide && pTarget.isAlive())
            {
                pTarget.setCustomName(pStack.getHoverName());

                if (pTarget instanceof Mob)
                {
                    ((Mob)pTarget).setPersistenceRequired();
                }

                pStack.shrink(1);
            }

            return InteractionResult.sidedSuccess(pPlayer.level.isClientSide);
        }
        else
        {
            return InteractionResult.PASS;
        }
    }
}
