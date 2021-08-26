package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class ShulkerBoxColoring extends CustomRecipe
{
    public ShulkerBoxColoring(ResourceLocation p_44312_)
    {
        super(p_44312_);
    }

    public boolean matches(CraftingContainer pInv, Level pLevel)
    {
        int i = 0;
        int j = 0;

        for (int k = 0; k < pInv.getContainerSize(); ++k)
        {
            ItemStack itemstack = pInv.getItem(k);

            if (!itemstack.isEmpty())
            {
                if (Block.byItem(itemstack.getItem()) instanceof ShulkerBoxBlock)
                {
                    ++i;
                }
                else
                {
                    if (!(itemstack.getItem() instanceof DyeItem))
                    {
                        return false;
                    }

                    ++j;
                }

                if (j > 1 || i > 1)
                {
                    return false;
                }
            }
        }

        return i == 1 && j == 1;
    }

    public ItemStack assemble(CraftingContainer pInv)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        DyeItem dyeitem = (DyeItem)Items.WHITE_DYE;

        for (int i = 0; i < pInv.getContainerSize(); ++i)
        {
            ItemStack itemstack1 = pInv.getItem(i);

            if (!itemstack1.isEmpty())
            {
                Item item = itemstack1.getItem();

                if (Block.byItem(item) instanceof ShulkerBoxBlock)
                {
                    itemstack = itemstack1;
                }
                else if (item instanceof DyeItem)
                {
                    dyeitem = (DyeItem)item;
                }
            }
        }

        ItemStack itemstack2 = ShulkerBoxBlock.getColoredItemStack(dyeitem.getDyeColor());

        if (itemstack.hasTag())
        {
            itemstack2.setTag(itemstack.getTag().copy());
        }

        return itemstack2;
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight)
    {
        return pWidth * pHeight >= 2;
    }

    public RecipeSerializer<?> getSerializer()
    {
        return RecipeSerializer.SHULKER_BOX_COLORING;
    }
}
