package net.minecraft.world.inventory;

import net.minecraft.world.item.ItemStack;

public interface ContainerListener
{
    void slotChanged(AbstractContainerMenu pContainerToSend, int pSlotInd, ItemStack pStack);

    void dataChanged(AbstractContainerMenu p_150524_, int p_150525_, int p_150526_);
}
