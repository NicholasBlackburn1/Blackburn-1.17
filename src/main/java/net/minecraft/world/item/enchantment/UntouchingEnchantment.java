package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class UntouchingEnchantment extends Enchantment
{
    protected UntouchingEnchantment(Enchantment.Rarity p_45260_, EquipmentSlot... p_45261_)
    {
        super(p_45260_, EnchantmentCategory.DIGGER, p_45261_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 15;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return super.getMinCost(pEnchantmentLevel) + 50;
    }

    public int getMaxLevel()
    {
        return 1;
    }

    public boolean checkCompatibility(Enchantment pEnch)
    {
        return super.checkCompatibility(pEnch) && pEnch != Enchantments.BLOCK_FORTUNE;
    }
}
