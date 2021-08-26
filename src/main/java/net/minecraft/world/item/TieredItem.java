package net.minecraft.world.item;

public class TieredItem extends Item
{
    private final Tier tier;

    public TieredItem(Tier p_43308_, Item.Properties p_43309_)
    {
        super(p_43309_.defaultDurability(p_43308_.getUses()));
        this.tier = p_43308_;
    }

    public Tier getTier()
    {
        return this.tier;
    }

    public int getEnchantmentValue()
    {
        return this.tier.getEnchantmentValue();
    }

    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair)
    {
        return this.tier.getRepairIngredient().test(pRepair) || super.isValidRepairItem(pToRepair, pRepair);
    }
}
