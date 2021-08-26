package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentChannelingEnchantment extends Enchantment
{
    public TridentChannelingEnchantment(Enchantment.Rarity p_45219_, EquipmentSlot... p_45220_)
    {
        super(p_45219_, EnchantmentCategory.TRIDENT, p_45220_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 25;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return 50;
    }

    public int getMaxLevel()
    {
        return 1;
    }
}
