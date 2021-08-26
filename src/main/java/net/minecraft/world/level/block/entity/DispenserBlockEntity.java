package net.minecraft.world.level.block.entity;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DispenserBlockEntity extends RandomizableContainerBlockEntity
{
    private static final Random RANDOM = new Random();
    public static final int CONTAINER_SIZE = 9;
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    protected DispenserBlockEntity(BlockEntityType<?> p_155489_, BlockPos p_155490_, BlockState p_155491_)
    {
        super(p_155489_, p_155490_, p_155491_);
    }

    public DispenserBlockEntity(BlockPos p_155493_, BlockState p_155494_)
    {
        this(BlockEntityType.DISPENSER, p_155493_, p_155494_);
    }

    public int getContainerSize()
    {
        return 9;
    }

    public int getRandomSlot()
    {
        this.unpackLootTable((Player)null);
        int i = -1;
        int j = 1;

        for (int k = 0; k < this.items.size(); ++k)
        {
            if (!this.items.get(k).isEmpty() && RANDOM.nextInt(j++) == 0)
            {
                i = k;
            }
        }

        return i;
    }

    public int addItem(ItemStack pStack)
    {
        for (int i = 0; i < this.items.size(); ++i)
        {
            if (this.items.get(i).isEmpty())
            {
                this.setItem(i, pStack);
                return i;
            }
        }

        return -1;
    }

    protected Component getDefaultName()
    {
        return new TranslatableComponent("container.dispenser");
    }

    public void load(CompoundTag p_155496_)
    {
        super.load(p_155496_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        if (!this.tryLoadLootTable(p_155496_))
        {
            ContainerHelper.loadAllItems(p_155496_, this.items);
        }
    }

    public CompoundTag save(CompoundTag pCompound)
    {
        super.save(pCompound);

        if (!this.trySaveLootTable(pCompound))
        {
            ContainerHelper.saveAllItems(pCompound, this.items);
        }

        return pCompound;
    }

    protected NonNullList<ItemStack> getItems()
    {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> pItems)
    {
        this.items = pItems;
    }

    protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer)
    {
        return new DispenserMenu(pId, pPlayer, this);
    }
}
