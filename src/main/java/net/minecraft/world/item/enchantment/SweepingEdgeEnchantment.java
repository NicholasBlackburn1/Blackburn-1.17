package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class SweepingEdgeEnchantment extends Enchantment
{
    public SweepingEdgeEnchantment(Enchantment.Rarity p_45186_, EquipmentSlot... p_45187_)
    {
        super(p_45186_, EnchantmentCategory.WEAPON, p_45187_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 5 + (pEnchantmentLevel - 1) * 9;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return this.getMinCost(pEnchantmentLevel) + 15;
    }

    public int getMaxLevel()
    {
        return 3;
    }

    public static float getSweepingDamageRatio(int pLevel)
    {
        return 1.0F - 1.0F / (float)(pLevel + 1);
    }
}
