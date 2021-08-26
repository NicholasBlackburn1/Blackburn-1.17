package net.minecraft.world.item.enchantment;

import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ThornsEnchantment extends Enchantment
{
    private static final float CHANCE_PER_LEVEL = 0.15F;

    public ThornsEnchantment(Enchantment.Rarity p_45196_, EquipmentSlot... p_45197_)
    {
        super(p_45196_, EnchantmentCategory.ARMOR_CHEST, p_45197_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 10 + 20 * (pEnchantmentLevel - 1);
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return super.getMinCost(pEnchantmentLevel) + 50;
    }

    public int getMaxLevel()
    {
        return 3;
    }

    public boolean canEnchant(ItemStack pStack)
    {
        return pStack.getItem() instanceof ArmorItem ? true : super.canEnchant(pStack);
    }

    public void doPostHurt(LivingEntity pUser, Entity pAttacker, int pLevel)
    {
        Random random = pUser.getRandom();
        Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.THORNS, pUser);

        if (shouldHit(pLevel, random))
        {
            if (pAttacker != null)
            {
                pAttacker.hurt(DamageSource.thorns(pUser), (float)getDamage(pLevel, random));
            }

            if (entry != null)
            {
                entry.getValue().hurtAndBreak(2, pUser, (p_45208_) ->
                {
                    p_45208_.broadcastBreakEvent(entry.getKey());
                });
            }
        }
    }

    public static boolean shouldHit(int pLevel, Random pRnd)
    {
        if (pLevel <= 0)
        {
            return false;
        }
        else
        {
            return pRnd.nextFloat() < 0.15F * (float)pLevel;
        }
    }

    public static int getDamage(int pLevel, Random pRnd)
    {
        return pLevel > 10 ? pLevel - 10 : 1 + pRnd.nextInt(4);
    }
}
