package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowPiercingEnchantment extends Enchantment
{
    public ArrowPiercingEnchantment(Enchantment.Rarity p_44602_, EquipmentSlot... p_44603_)
    {
        super(p_44602_, EnchantmentCategory.CROSSBOW, p_44603_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 1 + (pEnchantmentLevel - 1) * 10;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return 50;
    }

    public int getMaxLevel()
    {
        return 4;
    }

    public boolean checkCompatibility(Enchantment pEnch)
    {
        return super.checkCompatibility(pEnch) && pEnch != Enchantments.MULTISHOT;
    }
}
