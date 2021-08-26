package net.minecraft.world;

import java.util.Set;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface Container extends Clearable
{
    int LARGE_MAX_STACK_SIZE = 64;

    int getContainerSize();

    boolean isEmpty();

    ItemStack getItem(int pIndex);

    ItemStack removeItem(int pIndex, int pCount);

    ItemStack removeItemNoUpdate(int pIndex);

    void setItem(int pIndex, ItemStack pStack);

default int getMaxStackSize()
    {
        return 64;
    }

    void setChanged();

    boolean stillValid(Player pPlayer);

default void startOpen(Player pPlayer)
    {
    }

default void stopOpen(Player pPlayer)
    {
    }

default boolean canPlaceItem(int pIndex, ItemStack pStack)
    {
        return true;
    }

default int countItem(Item pItem)
    {
        int i = 0;

        for (int j = 0; j < this.getContainerSize(); ++j)
        {
            ItemStack itemstack = this.getItem(j);

            if (itemstack.getItem().equals(pItem))
            {
                i += itemstack.getCount();
            }
        }

        return i;
    }

default boolean hasAnyOf(Set<Item> pSet)
    {
        for (int i = 0; i < this.getContainerSize(); ++i)
        {
            ItemStack itemstack = this.getItem(i);

            if (pSet.contains(itemstack.getItem()) && itemstack.getCount() > 0)
            {
                return true;
            }
        }

        return false;
    }
}
