package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceResultSlot extends Slot
{
    private final Player player;
    private int removeCount;

    public FurnaceResultSlot(Player p_39542_, Container p_39543_, int p_39544_, int p_39545_, int p_39546_)
    {
        super(p_39543_, p_39544_, p_39545_, p_39546_);
        this.player = p_39542_;
    }

    public boolean mayPlace(ItemStack pStack)
    {
        return false;
    }

    public ItemStack remove(int pAmount)
    {
        if (this.hasItem())
        {
            this.removeCount += Math.min(pAmount, this.getItem().getCount());
        }

        return super.remove(pAmount);
    }

    public void onTake(Player p_150563_, ItemStack p_150564_)
    {
        this.checkTakeAchievements(p_150564_);
        super.onTake(p_150563_, p_150564_);
    }

    protected void onQuickCraft(ItemStack pStack, int pAmount)
    {
        this.removeCount += pAmount;
        this.checkTakeAchievements(pStack);
    }

    protected void checkTakeAchievements(ItemStack pStack)
    {
        pStack.onCraftedBy(this.player.level, this.player, this.removeCount);

        if (this.player instanceof ServerPlayer && this.container instanceof AbstractFurnaceBlockEntity)
        {
            ((AbstractFurnaceBlockEntity)this.container).awardUsedRecipesAndPopExperience((ServerPlayer)this.player);
        }

        this.removeCount = 0;
    }
}
