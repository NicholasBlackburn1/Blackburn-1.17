package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowFireEnchantment extends Enchantment
{
    public ArrowFireEnchantment(Enchantment.Rarity p_44576_, EquipmentSlot... p_44577_)
    {
        super(p_44576_, EnchantmentCategory.BOW, p_44577_);
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
}
