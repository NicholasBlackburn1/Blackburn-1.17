package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemCombinerMenu extends AbstractContainerMenu
{
    public static final int INPUT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    protected final ResultContainer resultSlots = new ResultContainer();
    protected final Container inputSlots = new SimpleContainer(2)
    {
        public void setChanged()
        {
            super.setChanged();
            ItemCombinerMenu.this.slotsChanged(this);
        }
    };
    protected final ContainerLevelAccess access;
    protected final Player player;

    protected abstract boolean mayPickup(Player p_39798_, boolean p_39799_);

    protected abstract void onTake(Player p_150601_, ItemStack p_150602_);

    protected abstract boolean isValidBlock(BlockState p_39788_);

    public ItemCombinerMenu(@Nullable MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_)
    {
        super(p_39773_, p_39774_);
        this.access = p_39776_;
        this.player = p_39775_.player;
        this.addSlot(new Slot(this.inputSlots, 0, 27, 47));
        this.addSlot(new Slot(this.inputSlots, 1, 76, 47));
        this.addSlot(new Slot(this.resultSlots, 2, 134, 47)
        {
            public boolean mayPlace(ItemStack pStack)
            {
                return false;
            }
            public boolean mayPickup(Player pPlayer)
            {
                return ItemCombinerMenu.this.mayPickup(pPlayer, this.hasItem());
            }
            public void onTake(Player p_150604_, ItemStack p_150605_)
            {
                ItemCombinerMenu.this.onTake(p_150604_, p_150605_);
            }
        });

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlot(new Slot(p_39775_, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k)
        {
            this.addSlot(new Slot(p_39775_, k, 8 + k * 18, 142));
        }
    }

    public abstract void createResult();

    public void slotsChanged(Container pInventory)
    {
        super.slotsChanged(pInventory);

        if (pInventory == this.inputSlots)
        {
            this.createResult();
        }
    }

    public void removed(Player pPlayer)
    {
        super.removed(pPlayer);
        this.access.execute((p_39796_, p_39797_) ->
        {
            this.clearContainer(pPlayer, this.inputSlots);
        });
    }

    public boolean stillValid(Player pPlayer)
    {
        return this.access.evaluate((p_39785_, p_39786_) ->
        {
            return !this.isValidBlock(p_39785_.getBlockState(p_39786_)) ? false : pPlayer.distanceToSqr((double)p_39786_.getX() + 0.5D, (double)p_39786_.getY() + 0.5D, (double)p_39786_.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    protected boolean shouldQuickMoveToAdditionalSlot(ItemStack p_39787_)
    {
        return false;
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);

        if (slot != null && slot.hasItem())
        {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (pIndex == 2)
            {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            }
            else if (pIndex != 0 && pIndex != 1)
            {
                if (pIndex >= 3 && pIndex < 39)
                {
                    int i = this.shouldQuickMoveToAdditionalSlot(itemstack) ? 1 : 0;

                    if (!this.moveItemStackTo(itemstack1, i, 2, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if (!this.moveItemStackTo(itemstack1, 3, 39, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
        }

        return itemstack;
    }
}
