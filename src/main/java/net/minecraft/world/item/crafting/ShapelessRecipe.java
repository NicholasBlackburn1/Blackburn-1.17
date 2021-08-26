package net.minecraft.world.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe
{
    private final ResourceLocation id;
    final String group;
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;

    public ShapelessRecipe(ResourceLocation p_44246_, String p_44247_, ItemStack p_44248_, NonNullList<Ingredient> p_44249_)
    {
        this.id = p_44246_;
        this.group = p_44247_;
        this.result = p_44248_;
        this.ingredients = p_44249_;
    }

    public ResourceLocation getId()
    {
        return this.id;
    }

    public RecipeSerializer<?> getSerializer()
    {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    public String getGroup()
    {
        return this.group;
    }

    public ItemStack getResultItem()
    {
        return this.result;
    }

    public NonNullList<Ingredient> getIngredients()
    {
        return this.ingredients;
    }

    public boolean matches(CraftingContainer pInv, Level pLevel)
    {
        StackedContents stackedcontents = new StackedContents();
        int i = 0;

        for (int j = 0; j < pInv.getContainerSize(); ++j)
        {
            ItemStack itemstack = pInv.getItem(j);

            if (!itemstack.isEmpty())
            {
                ++i;
                stackedcontents.accountStack(itemstack, 1);
            }
        }

        return i == this.ingredients.size() && stackedcontents.canCraft(this, (IntList)null);
    }

    public ItemStack assemble(CraftingContainer pInv)
    {
        return this.result.copy();
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight)
    {
        return pWidth * pHeight >= this.ingredients.size();
    }

    public static class Serializer implements RecipeSerializer<ShapelessRecipe>
    {
        public ShapelessRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson)
        {
            String s = GsonHelper.getAsString(pJson, "group", "");
            NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(pJson, "ingredients"));

            if (nonnulllist.isEmpty())
            {
                throw new JsonParseException("No ingredients for shapeless recipe");
            }
            else if (nonnulllist.size() > 9)
            {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            }
            else
            {
                ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
                return new ShapelessRecipe(pRecipeId, s, itemstack, nonnulllist);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray pIngredientArray)
        {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < pIngredientArray.size(); ++i)
            {
                Ingredient ingredient = Ingredient.fromJson(pIngredientArray.get(i));

                if (!ingredient.isEmpty())
                {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        public ShapelessRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer)
        {
            String s = pBuffer.readUtf();
            int i = pBuffer.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

            for (int j = 0; j < nonnulllist.size(); ++j)
            {
                nonnulllist.set(j, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack itemstack = pBuffer.readItem();
            return new ShapelessRecipe(pRecipeId, s, itemstack, nonnulllist);
        }

        public void toNetwork(FriendlyByteBuf pBuffer, ShapelessRecipe pRecipe)
        {
            pBuffer.writeUtf(pRecipe.group);
            pBuffer.writeVarInt(pRecipe.ingredients.size());

            for (Ingredient ingredient : pRecipe.ingredients)
            {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItem(pRecipe.result);
        }
    }
}
