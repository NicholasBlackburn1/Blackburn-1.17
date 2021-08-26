package net.minecraft.world.item;

public class EnchantedGoldenAppleItem extends Item
{
    public EnchantedGoldenAppleItem(Item.Properties p_41170_)
    {
        super(p_41170_);
    }

    public boolean isFoil(ItemStack pStack)
    {
        return true;
    }
}
