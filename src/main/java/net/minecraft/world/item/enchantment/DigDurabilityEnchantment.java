package net.minecraft.world.item.enchantment;

import java.util.Random;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class DigDurabilityEnchantment extends Enchantment
{
    protected DigDurabilityEnchantment(Enchantment.Rarity p_44648_, EquipmentSlot... p_44649_)
    {
        super(p_44648_, EnchantmentCategory.BREAKABLE, p_44649_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 5 + (pEnchantmentLevel - 1) * 8;
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
        return pStack.isDamageableItem() ? true : super.canEnchant(pStack);
    }

    public static boolean shouldIgnoreDurabilityDrop(ItemStack pStack, int pLevel, Random pRand)
    {
        if (pStack.getItem() instanceof ArmorItem && pRand.nextFloat() < 0.6F)
        {
            return false;
        }
        else
        {
            return pRand.nextInt(pLevel + 1) > 0;
        }
    }
}
