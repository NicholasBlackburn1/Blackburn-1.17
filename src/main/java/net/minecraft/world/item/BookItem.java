package net.minecraft.world.item;

public class BookItem extends Item
{
    public BookItem(Item.Properties p_40643_)
    {
        super(p_40643_);
    }

    public boolean isEnchantable(ItemStack pStack)
    {
        return pStack.getCount() == 1;
    }

    public int getEnchantmentValue()
    {
        return 1;
    }
}
