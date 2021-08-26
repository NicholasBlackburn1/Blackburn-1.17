package net.minecraft.world.item.crafting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FireworkRocketRecipe extends CustomRecipe
{
    private static final Ingredient PAPER_INGREDIENT = Ingredient.m_43929_(Items.PAPER);
    private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.m_43929_(Items.GUNPOWDER);
    private static final Ingredient STAR_INGREDIENT = Ingredient.m_43929_(Items.FIREWORK_STAR);

    public FireworkRocketRecipe(ResourceLocation p_43842_)
    {
        super(p_43842_);
    }

    public boolean matches(CraftingContainer pInv, Level pLevel)
    {
        boolean flag = false;
        int i = 0;

        for (int j = 0; j < pInv.getContainerSize(); ++j)
        {
            ItemStack itemstack = pInv.getItem(j);

            if (!itemstack.isEmpty())
            {
                if (PAPER_INGREDIENT.test(itemstack))
                {
                    if (flag)
                    {
                        return false;
                    }

                    flag = true;
                }
                else if (GUNPOWDER_INGREDIENT.test(itemstack))
                {
                    ++i;

                    if (i > 3)
                    {
                        return false;
                    }
                }
                else if (!STAR_INGREDIENT.test(itemstack))
                {
                    return false;
                }
            }
        }

        return flag && i >= 1;
    }

    public ItemStack assemble(CraftingContainer pInv)
    {
        ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, 3);
        CompoundTag compoundtag = itemstack.getOrCreateTagElement("Fireworks");
        ListTag listtag = new ListTag();
        int i = 0;

        for (int j = 0; j < pInv.getContainerSize(); ++j)
        {
            ItemStack itemstack1 = pInv.getItem(j);

            if (!itemstack1.isEmpty())
            {
                if (GUNPOWDER_INGREDIENT.test(itemstack1))
                {
                    ++i;
                }
                else if (STAR_INGREDIENT.test(itemstack1))
                {
                    CompoundTag compoundtag1 = itemstack1.getTagElement("Explosion");

                    if (compoundtag1 != null)
                    {
                        listtag.add(compoundtag1);
                    }
                }
            }
        }

        compoundtag.putByte("Flight", (byte)i);

        if (!listtag.isEmpty())
        {
            compoundtag.put("Explosions", listtag);
        }

        return itemstack;
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight)
    {
        return pWidth * pHeight >= 2;
    }

    public ItemStack getResultItem()
    {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    public RecipeSerializer<?> getSerializer()
    {
        return RecipeSerializer.FIREWORK_ROCKET;
    }
}
