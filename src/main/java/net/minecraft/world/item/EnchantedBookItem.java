package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

public class EnchantedBookItem extends Item
{
    public static final String TAG_STORED_ENCHANTMENTS = "StoredEnchantments";

    public EnchantedBookItem(Item.Properties p_41149_)
    {
        super(p_41149_);
    }

    public boolean isFoil(ItemStack pStack)
    {
        return true;
    }

    public boolean isEnchantable(ItemStack pStack)
    {
        return false;
    }

    public static ListTag getEnchantments(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTag();
        return compoundtag != null ? compoundtag.getList("StoredEnchantments", 10) : new ListTag();
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        ItemStack.appendEnchantmentNames(pTooltip, getEnchantments(pStack));
    }

    public static void addEnchantment(ItemStack pStack, EnchantmentInstance pEnchantData)
    {
        ListTag listtag = getEnchantments(pStack);
        boolean flag = true;
        ResourceLocation resourcelocation = EnchantmentHelper.m_182432_(pEnchantData.enchantment);

        for (int i = 0; i < listtag.size(); ++i)
        {
            CompoundTag compoundtag = listtag.getCompound(i);
            ResourceLocation resourcelocation1 = EnchantmentHelper.m_182446_(compoundtag);

            if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation))
            {
                if (EnchantmentHelper.m_182438_(compoundtag) < pEnchantData.level)
                {
                    EnchantmentHelper.m_182440_(compoundtag, pEnchantData.level);
                }

                flag = false;
                break;
            }
        }

        if (flag)
        {
            listtag.add(EnchantmentHelper.m_182443_(resourcelocation, pEnchantData.level));
        }

        pStack.getOrCreateTag().put("StoredEnchantments", listtag);
    }

    public static ItemStack createForEnchantment(EnchantmentInstance pEnchantData)
    {
        ItemStack itemstack = new ItemStack(Items.ENCHANTED_BOOK);
        addEnchantment(itemstack, pEnchantData);
        return itemstack;
    }

    public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems)
    {
        if (pGroup == CreativeModeTab.TAB_SEARCH)
        {
            for (Enchantment enchantment : Registry.ENCHANTMENT)
            {
                if (enchantment.category != null)
                {
                    for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i)
                    {
                        pItems.add(createForEnchantment(new EnchantmentInstance(enchantment, i)));
                    }
                }
            }
        }
        else if (pGroup.getEnchantmentCategories().length != 0)
        {
            for (Enchantment enchantment1 : Registry.ENCHANTMENT)
            {
                if (pGroup.hasEnchantmentCategory(enchantment1.category))
                {
                    pItems.add(createForEnchantment(new EnchantmentInstance(enchantment1, enchantment1.getMaxLevel())));
                }
            }
        }
    }
}
