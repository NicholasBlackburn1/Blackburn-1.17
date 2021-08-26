package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity & ItemSteerable> extends Item
{
    private final EntityType<T> canInteractWith;
    private final int consumeItemDamage;

    public FoodOnAStickItem(Item.Properties p_41307_, EntityType<T> p_41308_, int p_41309_)
    {
        super(p_41307_);
        this.canInteractWith = p_41308_;
        this.consumeItemDamage = p_41309_;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (pLevel.isClientSide)
        {
            return InteractionResultHolder.pass(itemstack);
        }
        else
        {
            Entity entity = pPlayer.getVehicle();

            if (pPlayer.isPassenger() && entity instanceof ItemSteerable && entity.getType() == this.canInteractWith)
            {
                ItemSteerable itemsteerable = (ItemSteerable)entity;

                if (itemsteerable.boost())
                {
                    itemstack.hurtAndBreak(this.consumeItemDamage, pPlayer, (p_41312_) ->
                    {
                        p_41312_.broadcastBreakEvent(pHand);
                    });

                    if (itemstack.isEmpty())
                    {
                        ItemStack itemstack1 = new ItemStack(Items.FISHING_ROD);
                        itemstack1.setTag(itemstack.getTag());
                        return InteractionResultHolder.success(itemstack1);
                    }

                    return InteractionResultHolder.success(itemstack);
                }
            }

            pPlayer.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.pass(itemstack);
        }
    }
}
