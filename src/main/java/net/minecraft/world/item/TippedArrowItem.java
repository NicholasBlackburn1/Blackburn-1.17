package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class TippedArrowItem extends ArrowItem
{
    public TippedArrowItem(Item.Properties p_43354_)
    {
        super(p_43354_);
    }

    public ItemStack getDefaultInstance()
    {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.POISON);
    }

    public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems)
    {
        if (this.allowdedIn(pGroup))
        {
            for (Potion potion : Registry.POTION)
            {
                if (!potion.getEffects().isEmpty())
                {
                    pItems.add(PotionUtils.setPotion(new ItemStack(this), potion));
                }
            }
        }
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        PotionUtils.addPotionTooltip(pStack, pTooltip, 0.125F);
    }

    public String getDescriptionId(ItemStack pStack)
    {
        return PotionUtils.getPotion(pStack).getName(this.getDescriptionId() + ".effect.");
    }
}
