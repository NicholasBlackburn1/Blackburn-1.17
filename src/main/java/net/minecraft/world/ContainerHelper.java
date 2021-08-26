package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class ContainerHelper
{
    public static ItemStack removeItem(List<ItemStack> pStacks, int pIndex, int pAmount)
    {
        return pIndex >= 0 && pIndex < pStacks.size() && !pStacks.get(pIndex).isEmpty() && pAmount > 0 ? pStacks.get(pIndex).split(pAmount) : ItemStack.EMPTY;
    }

    public static ItemStack takeItem(List<ItemStack> pStacks, int pIndex)
    {
        return pIndex >= 0 && pIndex < pStacks.size() ? pStacks.set(pIndex, ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    public static CompoundTag saveAllItems(CompoundTag pTag, NonNullList<ItemStack> pList)
    {
        return saveAllItems(pTag, pList, true);
    }

    public static CompoundTag saveAllItems(CompoundTag pTag, NonNullList<ItemStack> pList, boolean p_18979_)
    {
        ListTag listtag = new ListTag();

        for (int i = 0; i < pList.size(); ++i)
        {
            ItemStack itemstack = pList.get(i);

            if (!itemstack.isEmpty())
            {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte)i);
                itemstack.save(compoundtag);
                listtag.add(compoundtag);
            }
        }

        if (!listtag.isEmpty() || p_18979_)
        {
            pTag.put("Items", listtag);
        }

        return pTag;
    }

    public static void loadAllItems(CompoundTag pTag, NonNullList<ItemStack> pList)
    {
        ListTag listtag = pTag.getList("Items", 10);

        for (int i = 0; i < listtag.size(); ++i)
        {
            CompoundTag compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;

            if (j >= 0 && j < pList.size())
            {
                pList.set(j, ItemStack.of(compoundtag));
            }
        }
    }

    public static int clearOrCountMatchingItems(Container p_18957_, Predicate<ItemStack> p_18958_, int p_18959_, boolean p_18960_)
    {
        int i = 0;

        for (int j = 0; j < p_18957_.getContainerSize(); ++j)
        {
            ItemStack itemstack = p_18957_.getItem(j);
            int k = clearOrCountMatchingItems(itemstack, p_18958_, p_18959_ - i, p_18960_);

            if (k > 0 && !p_18960_ && itemstack.isEmpty())
            {
                p_18957_.setItem(j, ItemStack.EMPTY);
            }

            i += k;
        }

        return i;
    }

    public static int clearOrCountMatchingItems(ItemStack p_18962_, Predicate<ItemStack> p_18963_, int p_18964_, boolean p_18965_)
    {
        if (!p_18962_.isEmpty() && p_18963_.test(p_18962_))
        {
            if (p_18965_)
            {
                return p_18962_.getCount();
            }
            else
            {
                int i = p_18964_ < 0 ? p_18962_.getCount() : Math.min(p_18964_, p_18962_.getCount());
                p_18962_.shrink(i);
                return i;
            }
        }
        else
        {
            return 0;
        }
    }
}
