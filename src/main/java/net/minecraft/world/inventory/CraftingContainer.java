package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

public class CraftingContainer implements Container, StackedContentsCompatible
{
    private final NonNullList<ItemStack> items;
    private final int width;
    private final int height;
    private final AbstractContainerMenu menu;

    public CraftingContainer(AbstractContainerMenu p_39325_, int p_39326_, int p_39327_)
    {
        this.items = NonNullList.withSize(p_39326_ * p_39327_, ItemStack.EMPTY);
        this.menu = p_39325_;
        this.width = p_39326_;
        this.height = p_39327_;
    }

    public int getContainerSize()
    {
        return this.items.size();
    }

    public boolean isEmpty()
    {
        for (ItemStack itemstack : this.items)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    public ItemStack getItem(int pIndex)
    {
        return pIndex >= this.getContainerSize() ? ItemStack.EMPTY : this.items.get(pIndex);
    }

    public ItemStack removeItemNoUpdate(int pIndex)
    {
        return ContainerHelper.takeItem(this.items, pIndex);
    }

    public ItemStack removeItem(int pIndex, int pCount)
    {
        ItemStack itemstack = ContainerHelper.removeItem(this.items, pIndex, pCount);

        if (!itemstack.isEmpty())
        {
            this.menu.slotsChanged(this);
        }

        return itemstack;
    }

    public void setItem(int pIndex, ItemStack pStack)
    {
        this.items.set(pIndex, pStack);
        this.menu.slotsChanged(this);
    }

    public void setChanged()
    {
    }

    public boolean stillValid(Player pPlayer)
    {
        return true;
    }

    public void clearContent()
    {
        this.items.clear();
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getWidth()
    {
        return this.width;
    }

    public void fillStackedContents(StackedContents pHelper)
    {
        for (ItemStack itemstack : this.items)
        {
            pHelper.accountSimpleStack(itemstack);
        }
    }
}
