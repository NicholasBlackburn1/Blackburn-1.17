package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SimpleContainer implements Container, StackedContentsCompatible
{
    private final int size;
    private final NonNullList<ItemStack> items;
    private List<ContainerListener> listeners;

    public SimpleContainer(int p_19150_)
    {
        this.size = p_19150_;
        this.items = NonNullList.withSize(p_19150_, ItemStack.EMPTY);
    }

    public SimpleContainer(ItemStack... p_19152_)
    {
        this.size = p_19152_.length;
        this.items = NonNullList.m_122783_(ItemStack.EMPTY, p_19152_);
    }

    public void addListener(ContainerListener pListener)
    {
        if (this.listeners == null)
        {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(pListener);
    }

    public void removeListener(ContainerListener pListener)
    {
        this.listeners.remove(pListener);
    }

    public ItemStack getItem(int pIndex)
    {
        return pIndex >= 0 && pIndex < this.items.size() ? this.items.get(pIndex) : ItemStack.EMPTY;
    }

    public List<ItemStack> removeAllItems()
    {
        List<ItemStack> list = this.items.stream().filter((p_19197_) ->
        {
            return !p_19197_.isEmpty();
        }).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    public ItemStack removeItem(int pIndex, int pCount)
    {
        ItemStack itemstack = ContainerHelper.removeItem(this.items, pIndex, pCount);

        if (!itemstack.isEmpty())
        {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack removeItemType(Item p_19171_, int p_19172_)
    {
        ItemStack itemstack = new ItemStack(p_19171_, 0);

        for (int i = this.size - 1; i >= 0; --i)
        {
            ItemStack itemstack1 = this.getItem(i);

            if (itemstack1.getItem().equals(p_19171_))
            {
                int j = p_19172_ - itemstack.getCount();
                ItemStack itemstack2 = itemstack1.split(j);
                itemstack.grow(itemstack2.getCount());

                if (itemstack.getCount() == p_19172_)
                {
                    break;
                }
            }
        }

        if (!itemstack.isEmpty())
        {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack addItem(ItemStack pStack)
    {
        ItemStack itemstack = pStack.copy();
        this.moveItemToOccupiedSlotsWithSameType(itemstack);

        if (itemstack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            this.moveItemToEmptySlots(itemstack);
            return itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
        }
    }

    public boolean canAddItem(ItemStack p_19184_)
    {
        boolean flag = false;

        for (ItemStack itemstack : this.items)
        {
            if (itemstack.isEmpty() || ItemStack.isSameItemSameTags(itemstack, p_19184_) && itemstack.getCount() < itemstack.getMaxStackSize())
            {
                flag = true;
                break;
            }
        }

        return flag;
    }

    public ItemStack removeItemNoUpdate(int pIndex)
    {
        ItemStack itemstack = this.items.get(pIndex);

        if (itemstack.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            this.items.set(pIndex, ItemStack.EMPTY);
            return itemstack;
        }
    }

    public void setItem(int pIndex, ItemStack pStack)
    {
        this.items.set(pIndex, pStack);

        if (!pStack.isEmpty() && pStack.getCount() > this.getMaxStackSize())
        {
            pStack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    public int getContainerSize()
    {
        return this.size;
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

    public void setChanged()
    {
        if (this.listeners != null)
        {
            for (ContainerListener containerlistener : this.listeners)
            {
                containerlistener.containerChanged(this);
            }
        }
    }

    public boolean stillValid(Player pPlayer)
    {
        return true;
    }

    public void clearContent()
    {
        this.items.clear();
        this.setChanged();
    }

    public void fillStackedContents(StackedContents pHelper)
    {
        for (ItemStack itemstack : this.items)
        {
            pHelper.accountStack(itemstack);
        }
    }

    public String toString()
    {
        return this.items.stream().filter((p_19194_) ->
        {
            return !p_19194_.isEmpty();
        }).collect(Collectors.toList()).toString();
    }

    private void moveItemToEmptySlots(ItemStack p_19190_)
    {
        for (int i = 0; i < this.size; ++i)
        {
            ItemStack itemstack = this.getItem(i);

            if (itemstack.isEmpty())
            {
                this.setItem(i, p_19190_.copy());
                p_19190_.setCount(0);
                return;
            }
        }
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack p_19192_)
    {
        for (int i = 0; i < this.size; ++i)
        {
            ItemStack itemstack = this.getItem(i);

            if (ItemStack.isSameItemSameTags(itemstack, p_19192_))
            {
                this.moveItemsBetweenStacks(p_19192_, itemstack);

                if (p_19192_.isEmpty())
                {
                    return;
                }
            }
        }
    }

    private void moveItemsBetweenStacks(ItemStack p_19186_, ItemStack p_19187_)
    {
        int i = Math.min(this.getMaxStackSize(), p_19187_.getMaxStackSize());
        int j = Math.min(p_19186_.getCount(), i - p_19187_.getCount());

        if (j > 0)
        {
            p_19187_.grow(j);
            p_19186_.shrink(j);
            this.setChanged();
        }
    }

    public void fromTag(ListTag p_19178_)
    {
        for (int i = 0; i < p_19178_.size(); ++i)
        {
            ItemStack itemstack = ItemStack.of(p_19178_.getCompound(i));

            if (!itemstack.isEmpty())
            {
                this.addItem(itemstack);
            }
        }
    }

    public ListTag createTag()
    {
        ListTag listtag = new ListTag();

        for (int i = 0; i < this.getContainerSize(); ++i)
        {
            ItemStack itemstack = this.getItem(i);

            if (!itemstack.isEmpty())
            {
                listtag.add(itemstack.save(new CompoundTag()));
            }
        }

        return listtag;
    }
}
