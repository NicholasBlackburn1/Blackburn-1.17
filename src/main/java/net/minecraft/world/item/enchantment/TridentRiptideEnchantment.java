package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentRiptideEnchantment extends Enchantment
{
    public TridentRiptideEnchantment(Enchantment.Rarity p_45250_, EquipmentSlot... p_45251_)
    {
        super(p_45250_, EnchantmentCategory.TRIDENT, p_45251_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 10 + pEnchantmentLevel * 7;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return 50;
    }

    public int getMaxLevel()
    {
        return 3;
    }

    public boolean checkCompatibility(Enchantment pEnch)
    {
        return super.checkCompatibility(pEnch) && pEnch != Enchantments.LOYALTY && pEnch != Enchantments.CHANNELING;
    }
}
