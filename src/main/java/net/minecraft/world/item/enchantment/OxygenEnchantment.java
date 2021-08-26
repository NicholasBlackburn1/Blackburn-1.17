package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class OxygenEnchantment extends Enchantment
{
    public OxygenEnchantment(Enchantment.Rarity p_45117_, EquipmentSlot... p_45118_)
    {
        super(p_45117_, EnchantmentCategory.ARMOR_HEAD, p_45118_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 10 * pEnchantmentLevel;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return this.getMinCost(pEnchantmentLevel) + 30;
    }

    public int getMaxLevel()
    {
        return 3;
    }
}
