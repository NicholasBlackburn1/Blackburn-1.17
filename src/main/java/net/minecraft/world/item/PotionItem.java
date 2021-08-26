package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class PotionItem extends Item
{
    private static final int DRINK_DURATION = 32;

    public PotionItem(Item.Properties p_42979_)
    {
        super(p_42979_);
    }

    public ItemStack getDefaultInstance()
    {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
    }

    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving)
    {
        Player player = pEntityLiving instanceof Player ? (Player)pEntityLiving : null;

        if (player instanceof ServerPlayer)
        {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, pStack);
        }

        if (!pLevel.isClientSide)
        {
            for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(pStack))
            {
                if (mobeffectinstance.getEffect().isInstantenous())
                {
                    mobeffectinstance.getEffect().applyInstantenousEffect(player, player, pEntityLiving, mobeffectinstance.getAmplifier(), 1.0D);
                }
                else
                {
                    pEntityLiving.addEffect(new MobEffectInstance(mobeffectinstance));
                }
            }
        }

        if (player != null)
        {
            player.awardStat(Stats.ITEM_USED.get(this));

            if (!player.getAbilities().instabuild)
            {
                pStack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild)
        {
            if (pStack.isEmpty())
            {
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            if (player != null)
            {
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        pLevel.gameEvent(pEntityLiving, GameEvent.DRINKING_FINISH, pEntityLiving.eyeBlockPosition());
        return pStack;
    }

    public int getUseDuration(ItemStack pStack)
    {
        return 32;
    }

    public UseAnim getUseAnimation(ItemStack pStack)
    {
        return UseAnim.DRINK;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand)
    {
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
    }

    public String getDescriptionId(ItemStack pStack)
    {
        return PotionUtils.getPotion(pStack).getName(this.getDescriptionId() + ".effect.");
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        PotionUtils.addPotionTooltip(pStack, pTooltip, 1.0F);
    }

    public boolean isFoil(ItemStack pStack)
    {
        return super.isFoil(pStack) || !PotionUtils.getMobEffects(pStack).isEmpty();
    }

    public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems)
    {
        if (this.allowdedIn(pGroup))
        {
            for (Potion potion : Registry.POTION)
            {
                if (potion != Potions.EMPTY)
                {
                    pItems.add(PotionUtils.setPotion(new ItemStack(this), potion));
                }
            }
        }
    }
}
