package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class CreativeInventoryListener implements ContainerListener
{
    private final Minecraft minecraft;

    public CreativeInventoryListener(Minecraft p_98492_)
    {
        this.minecraft = p_98492_;
    }

    public void slotChanged(AbstractContainerMenu pContainerToSend, int pSlotInd, ItemStack pStack)
    {
        this.minecraft.gameMode.handleCreativeModeItemAdd(pStack, pSlotInd);
    }

    public void dataChanged(AbstractContainerMenu p_169732_, int p_169733_, int p_169734_)
    {
    }
}
