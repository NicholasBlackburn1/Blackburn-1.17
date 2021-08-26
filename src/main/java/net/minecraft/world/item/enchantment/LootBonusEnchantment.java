package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class LootBonusEnchantment extends Enchantment
{
    protected LootBonusEnchantment(Enchantment.Rarity p_45087_, EnchantmentCategory p_45088_, EquipmentSlot... p_45089_)
    {
        super(p_45087_, p_45088_, p_45089_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 15 + (pEnchantmentLevel - 1) * 9;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return super.getMinCost(pEnchantmentLevel) + 50;
    }

    public int getMaxLevel()
    {
        return 3;
    }

    public boolean checkCompatibility(Enchantment pEnch)
    {
        return super.checkCompatibility(pEnch) && pEnch != Enchantments.SILK_TOUCH;
    }
}
