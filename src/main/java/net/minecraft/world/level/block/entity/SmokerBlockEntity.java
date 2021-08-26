package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class SmokerBlockEntity extends AbstractFurnaceBlockEntity
{
    public SmokerBlockEntity(BlockPos p_155749_, BlockState p_155750_)
    {
        super(BlockEntityType.SMOKER, p_155749_, p_155750_, RecipeType.SMOKING);
    }

    protected Component getDefaultName()
    {
        return new TranslatableComponent("container.smoker");
    }

    protected int getBurnDuration(ItemStack pFuel)
    {
        return super.getBurnDuration(pFuel) / 2;
    }

    protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer)
    {
        return new SmokerMenu(pId, pPlayer, this, this.dataAccess);
    }
}
