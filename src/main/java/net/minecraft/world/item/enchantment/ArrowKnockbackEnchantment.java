package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowKnockbackEnchantment extends Enchantment
{
    public ArrowKnockbackEnchantment(Enchantment.Rarity p_44594_, EquipmentSlot... p_44595_)
    {
        super(p_44594_, EnchantmentCategory.BOW, p_44595_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 12 + (pEnchantmentLevel - 1) * 20;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return this.getMinCost(pEnchantmentLevel) + 25;
    }

    public int getMaxLevel()
    {
        return 2;
    }
}
