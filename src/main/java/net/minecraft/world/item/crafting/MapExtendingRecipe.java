package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapExtendingRecipe extends ShapedRecipe
{
    public MapExtendingRecipe(ResourceLocation p_43984_)
    {
        super(p_43984_, "", 3, 3, NonNullList.m_122783_(Ingredient.EMPTY, Ingredient.m_43929_(Items.PAPER), Ingredient.m_43929_(Items.PAPER), Ingredient.m_43929_(Items.PAPER), Ingredient.m_43929_(Items.PAPER), Ingredient.m_43929_(Items.FILLED_MAP), Ingredient.m_43929_(Items.PAPER), Ingredient.m_43929_(Items.PAPER), Ingredient.m_43929_(Items.PAPER), Ingredient.m_43929_(Items.PAPER)), new ItemStack(Items.MAP));
    }

    public boolean matches(CraftingContainer pInv, Level pLevel)
    {
        if (!super.matches(pInv, pLevel))
        {
            return false;
        }
        else
        {
            ItemStack itemstack = ItemStack.EMPTY;

            for (int i = 0; i < pInv.getContainerSize() && itemstack.isEmpty(); ++i)
            {
                ItemStack itemstack1 = pInv.getItem(i);

                if (itemstack1.is(Items.FILLED_MAP))
                {
                    itemstack = itemstack1;
                }
            }

            if (itemstack.isEmpty())
            {
                return false;
            }
            else
            {
                MapItemSavedData mapitemsaveddata = MapItem.getSavedData(itemstack, pLevel);

                if (mapitemsaveddata == null)
                {
                    return false;
                }
                else if (mapitemsaveddata.isExplorationMap())
                {
                    return false;
                }
                else
                {
                    return mapitemsaveddata.scale < 4;
                }
            }
        }
    }

    public ItemStack assemble(CraftingContainer pInv)
    {
        ItemStack itemstack = ItemStack.EMPTY;

        for (int i = 0; i < pInv.getContainerSize() && itemstack.isEmpty(); ++i)
        {
            ItemStack itemstack1 = pInv.getItem(i);

            if (itemstack1.is(Items.FILLED_MAP))
            {
                itemstack = itemstack1;
            }
        }

        itemstack = itemstack.copy();
        itemstack.setCount(1);
        itemstack.getOrCreateTag().putInt("map_scale_direction", 1);
        return itemstack;
    }

    public boolean isSpecial()
    {
        return true;
    }

    public RecipeSerializer<?> getSerializer()
    {
        return RecipeSerializer.MAP_EXTENDING;
    }
}
