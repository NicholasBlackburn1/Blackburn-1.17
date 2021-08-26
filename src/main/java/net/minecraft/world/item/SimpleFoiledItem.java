package net.minecraft.world.item;

public class SimpleFoiledItem extends Item
{
    public SimpleFoiledItem(Item.Properties p_43136_)
    {
        super(p_43136_);
    }

    public boolean isFoil(ItemStack pStack)
    {
        return true;
    }
}
