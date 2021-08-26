package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MultiShotEnchantment extends Enchantment
{
    public MultiShotEnchantment(Enchantment.Rarity p_45107_, EquipmentSlot... p_45108_)
    {
        super(p_45107_, EnchantmentCategory.CROSSBOW, p_45108_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 20;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return 50;
    }

    public int getMaxLevel()
    {
        return 1;
    }

    public boolean checkCompatibility(Enchantment pEnch)
    {
        return super.checkCompatibility(pEnch) && pEnch != Enchantments.PIERCING;
    }
}
