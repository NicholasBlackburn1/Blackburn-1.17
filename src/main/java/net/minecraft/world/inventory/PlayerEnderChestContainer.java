package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

public class PlayerEnderChestContainer extends SimpleContainer
{
    @Nullable
    private EnderChestBlockEntity activeChest;

    public PlayerEnderChestContainer()
    {
        super(27);
    }

    public void setActiveChest(EnderChestBlockEntity pChestBlockEntity)
    {
        this.activeChest = pChestBlockEntity;
    }

    public boolean isActiveChest(EnderChestBlockEntity p_150634_)
    {
        return this.activeChest == p_150634_;
    }

    public void fromTag(ListTag p_40108_)
    {
        for (int i = 0; i < this.getContainerSize(); ++i)
        {
            this.setItem(i, ItemStack.EMPTY);
        }

        for (int k = 0; k < p_40108_.size(); ++k)
        {
            CompoundTag compoundtag = p_40108_.getCompound(k);
            int j = compoundtag.getByte("Slot") & 255;

            if (j >= 0 && j < this.getContainerSize())
            {
                this.setItem(j, ItemStack.of(compoundtag));
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
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte)i);
                itemstack.save(compoundtag);
                listtag.add(compoundtag);
            }
        }

        return listtag;
    }

    public boolean stillValid(Player pPlayer)
    {
        return this.activeChest != null && !this.activeChest.stillValid(pPlayer) ? false : super.stillValid(pPlayer);
    }

    public void startOpen(Player pPlayer)
    {
        if (this.activeChest != null)
        {
            this.activeChest.startOpen(pPlayer);
        }

        super.startOpen(pPlayer);
    }

    public void stopOpen(Player pPlayer)
    {
        if (this.activeChest != null)
        {
            this.activeChest.stopOpen(pPlayer);
        }

        super.stopOpen(pPlayer);
        this.activeChest = null;
    }
}
