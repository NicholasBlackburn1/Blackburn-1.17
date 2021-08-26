package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class QuickChargeEnchantment extends Enchantment
{
    public QuickChargeEnchantment(Enchantment.Rarity p_45167_, EquipmentSlot... p_45168_)
    {
        super(p_45167_, EnchantmentCategory.CROSSBOW, p_45168_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 12 + (pEnchantmentLevel - 1) * 20;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return 50;
    }

    public int getMaxLevel()
    {
        return 3;
    }
}
