package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentLoyaltyEnchantment extends Enchantment
{
    public TridentLoyaltyEnchantment(Enchantment.Rarity p_45240_, EquipmentSlot... p_45241_)
    {
        super(p_45240_, EnchantmentCategory.TRIDENT, p_45241_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 5 + pEnchantmentLevel * 7;
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
