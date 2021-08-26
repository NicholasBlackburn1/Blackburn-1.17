package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class KnockbackEnchantment extends Enchantment
{
    protected KnockbackEnchantment(Enchantment.Rarity p_45079_, EquipmentSlot... p_45080_)
    {
        super(p_45079_, EnchantmentCategory.WEAPON, p_45080_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 5 + 20 * (pEnchantmentLevel - 1);
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return super.getMinCost(pEnchantmentLevel) + 50;
    }

    public int getMaxLevel()
    {
        return 2;
    }
}
