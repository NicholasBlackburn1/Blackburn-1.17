package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MendingEnchantment extends Enchantment
{
    public MendingEnchantment(Enchantment.Rarity p_45098_, EquipmentSlot... p_45099_)
    {
        super(p_45098_, EnchantmentCategory.BREAKABLE, p_45099_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return pEnchantmentLevel * 25;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return this.getMinCost(pEnchantmentLevel) + 50;
    }

    public boolean isTreasureOnly()
    {
        return true;
    }

    public int getMaxLevel()
    {
        return 1;
    }
}
