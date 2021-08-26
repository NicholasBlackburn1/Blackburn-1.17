package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class WaterWorkerEnchantment extends Enchantment
{
    public WaterWorkerEnchantment(Enchantment.Rarity p_45290_, EquipmentSlot... p_45291_)
    {
        super(p_45290_, EnchantmentCategory.ARMOR_HEAD, p_45291_);
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return 1;
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return this.getMinCost(pEnchantmentLevel) + 40;
    }

    public int getMaxLevel()
    {
        return 1;
    }
}
