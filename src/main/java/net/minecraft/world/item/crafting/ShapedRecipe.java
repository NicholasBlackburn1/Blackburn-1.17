package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe
{
    final int width;
    final int height;
    final NonNullList<Ingredient> recipeItems;
    final ItemStack result;
    private final ResourceLocation id;
    final String group;

    public ShapedRecipe(ResourceLocation p_44153_, String p_44154_, int p_44155_, int p_44156_, NonNullList<Ingredient> p_44157_, ItemStack p_44158_)
    {
        this.id = p_44153_;
        this.group = p_44154_;
        this.width = p_44155_;
        this.height = p_44156_;
        this.recipeItems = p_44157_;
        this.result = p_44158_;
    }

    public ResourceLocation getId()
    {
        return this.id;
    }

    public RecipeSerializer<?> getSerializer()
    {
        return RecipeSerializer.SHAPED_RECIPE;
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
        return this.recipeItems;
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight)
    {
        return pWidth >= this.width && pHeight >= this.height;
    }

    public boolean matches(CraftingContainer pCraftingInventory, Level pWidth)
    {
        for (int i = 0; i <= pCraftingInventory.getWidth() - this.width; ++i)
        {
            for (int j = 0; j <= pCraftingInventory.getHeight() - this.height; ++j)
            {
                if (this.matches(pCraftingInventory, i, j, true))
                {
                    return true;
                }

                if (this.matches(pCraftingInventory, i, j, false))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matches(CraftingContainer pCraftingInventory, int pWidth, int pHeight, boolean p_44174_)
    {
        for (int i = 0; i < pCraftingInventory.getWidth(); ++i)
        {
            for (int j = 0; j < pCraftingInventory.getHeight(); ++j)
            {
                int k = i - pWidth;
                int l = j - pHeight;
                Ingredient ingredient = Ingredient.EMPTY;

                if (k >= 0 && l >= 0 && k < this.width && l < this.height)
                {
                    if (p_44174_)
                    {
                        ingredient = this.recipeItems.get(this.width - k - 1 + l * this.width);
                    }
                    else
                    {
                        ingredient = this.recipeItems.get(k + l * this.width);
                    }
                }

                if (!ingredient.test(pCraftingInventory.getItem(i + j * pCraftingInventory.getWidth())))
                {
                    return false;
                }
            }
        }

        return true;
    }

    public ItemStack assemble(CraftingContainer pInv)
    {
        return this.getResultItem().copy();
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    static NonNullList<Ingredient> m_44202_(String[] p_44203_, Map<String, Ingredient> p_44204_, int p_44205_, int p_44206_)
    {
        NonNullList<Ingredient> nonnulllist = NonNullList.withSize(p_44205_ * p_44206_, Ingredient.EMPTY);
        Set<String> set = Sets.newHashSet(p_44204_.keySet());
        set.remove(" ");

        for (int i = 0; i < p_44203_.length; ++i)
        {
            for (int j = 0; j < p_44203_[i].length(); ++j)
            {
                String s = p_44203_[i].substring(j, j + 1);
                Ingredient ingredient = p_44204_.get(s);

                if (ingredient == null)
                {
                    throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
                }

                set.remove(s);
                nonnulllist.set(j + p_44205_ * i, ingredient);
            }
        }

        if (!set.isEmpty())
        {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
        }
        else
        {
            return nonnulllist;
        }
    }

    @VisibleForTesting
    static String[] m_44186_(String... p_44187_)
    {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;

        for (int i1 = 0; i1 < p_44187_.length; ++i1)
        {
            String s = p_44187_[i1];
            i = Math.min(i, firstNonSpace(s));
            int j1 = lastNonSpace(s);
            j = Math.max(j, j1);

            if (j1 < 0)
            {
                if (k == i1)
                {
                    ++k;
                }

                ++l;
            }
            else
            {
                l = 0;
            }
        }

        if (p_44187_.length == l)
        {
            return new String[0];
        }
        else
        {
            String[] astring = new String[p_44187_.length - l - k];

            for (int k1 = 0; k1 < astring.length; ++k1)
            {
                astring[k1] = p_44187_[k1 + k].substring(i, j + 1);
            }

            return astring;
        }
    }

    public boolean isIncomplete()
    {
        NonNullList<Ingredient> nonnulllist = this.getIngredients();
        return nonnulllist.isEmpty() || nonnulllist.stream().filter((p_151277_) ->
        {
            return !p_151277_.isEmpty();
        }).anyMatch((p_151273_) ->
        {
            return p_151273_.getItems().length == 0;
        });
    }

    private static int firstNonSpace(String pStr)
    {
        int i;

        for (i = 0; i < pStr.length() && pStr.charAt(i) == ' '; ++i)
        {
        }

        return i;
    }

    private static int lastNonSpace(String pStr)
    {
        int i;

        for (i = pStr.length() - 1; i >= 0 && pStr.charAt(i) == ' '; --i)
        {
        }

        return i;
    }

    static String[] patternFromJson(JsonArray pJsonArr)
    {
        String[] astring = new String[pJsonArr.size()];

        if (astring.length > 3)
        {
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
        }
        else if (astring.length == 0)
        {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }
        else
        {
            for (int i = 0; i < astring.length; ++i)
            {
                String s = GsonHelper.convertToString(pJsonArr.get(i), "pattern[" + i + "]");

                if (s.length() > 3)
                {
                    throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
                }

                if (i > 0 && astring[0].length() != s.length())
                {
                    throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                }

                astring[i] = s;
            }

            return astring;
        }
    }

    static Map<String, Ingredient> keyFromJson(JsonObject pJson)
    {
        Map<String, Ingredient> map = Maps.newHashMap();

        for (Entry<String, JsonElement> entry : pJson.entrySet())
        {
            if (entry.getKey().length() != 1)
            {
                throw new JsonSyntaxException("Invalid key entry: '" + (String)entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }

            if (" ".equals(entry.getKey()))
            {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            map.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
        }

        map.put(" ", Ingredient.EMPTY);
        return map;
    }

    public static ItemStack itemStackFromJson(JsonObject p_151275_)
    {
        Item item = itemFromJson(p_151275_);

        if (p_151275_.has("data"))
        {
            throw new JsonParseException("Disallowed data tag found");
        }
        else
        {
            int i = GsonHelper.getAsInt(p_151275_, "count", 1);

            if (i < 1)
            {
                throw new JsonSyntaxException("Invalid output count: " + i);
            }
            else
            {
                return new ItemStack(item, i);
            }
        }
    }

    public static Item itemFromJson(JsonObject p_151279_)
    {
        String s = GsonHelper.getAsString(p_151279_, "item");
        Item item = Registry.ITEM.getOptional(new ResourceLocation(s)).orElseThrow(() ->
        {
            return new JsonSyntaxException("Unknown item '" + s + "'");
        });

        if (item == Items.AIR)
        {
            throw new JsonSyntaxException("Invalid item: " + s);
        }
        else
        {
            return item;
        }
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipe>
    {
        public ShapedRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson)
        {
            String s = GsonHelper.getAsString(pJson, "group", "");
            Map<String, Ingredient> map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(pJson, "key"));
            String[] astring = ShapedRecipe.m_44186_(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(pJson, "pattern")));
            int i = astring[0].length();
            int j = astring.length;
            NonNullList<Ingredient> nonnulllist = ShapedRecipe.m_44202_(astring, map, i, j);
            ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
            return new ShapedRecipe(pRecipeId, s, i, j, nonnulllist, itemstack);
        }

        public ShapedRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer)
        {
            int i = pBuffer.readVarInt();
            int j = pBuffer.readVarInt();
            String s = pBuffer.readUtf();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);

            for (int k = 0; k < nonnulllist.size(); ++k)
            {
                nonnulllist.set(k, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack itemstack = pBuffer.readItem();
            return new ShapedRecipe(pRecipeId, s, i, j, nonnulllist, itemstack);
        }

        public void toNetwork(FriendlyByteBuf pBuffer, ShapedRecipe pRecipe)
        {
            pBuffer.writeVarInt(pRecipe.width);
            pBuffer.writeVarInt(pRecipe.height);
            pBuffer.writeUtf(pRecipe.group);

            for (Ingredient ingredient : pRecipe.recipeItems)
            {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItem(pRecipe.result);
        }
    }
}
