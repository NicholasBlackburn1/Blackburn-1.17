package net.minecraft.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T>
{
    private final int defaultCookingTime;
    private final SimpleCookingSerializer.CookieBaker<T> factory;

    public SimpleCookingSerializer(SimpleCookingSerializer.CookieBaker<T> p_44330_, int p_44331_)
    {
        this.defaultCookingTime = p_44331_;
        this.factory = p_44330_;
    }

    public T fromJson(ResourceLocation pRecipeId, JsonObject pJson)
    {
        String s = GsonHelper.getAsString(pJson, "group", "");
        JsonElement jsonelement = (JsonElement)(GsonHelper.isArrayNode(pJson, "ingredient") ? GsonHelper.getAsJsonArray(pJson, "ingredient") : GsonHelper.getAsJsonObject(pJson, "ingredient"));
        Ingredient ingredient = Ingredient.fromJson(jsonelement);
        String s1 = GsonHelper.getAsString(pJson, "result");
        ResourceLocation resourcelocation = new ResourceLocation(s1);
        ItemStack itemstack = new ItemStack(Registry.ITEM.getOptional(resourcelocation).orElseThrow(() ->
        {
            return new IllegalStateException("Item: " + s1 + " does not exist");
        }));
        float f = GsonHelper.getAsFloat(pJson, "experience", 0.0F);
        int i = GsonHelper.getAsInt(pJson, "cookingtime", this.defaultCookingTime);
        return this.factory.create(pRecipeId, s, ingredient, itemstack, f, i);
    }

    public T fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer)
    {
        String s = pBuffer.readUtf();
        Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
        ItemStack itemstack = pBuffer.readItem();
        float f = pBuffer.readFloat();
        int i = pBuffer.readVarInt();
        return this.factory.create(pRecipeId, s, ingredient, itemstack, f, i);
    }

    public void toNetwork(FriendlyByteBuf pBuffer, T pRecipe)
    {
        pBuffer.writeUtf(pRecipe.group);
        pRecipe.ingredient.toNetwork(pBuffer);
        pBuffer.writeItem(pRecipe.result);
        pBuffer.writeFloat(pRecipe.experience);
        pBuffer.writeVarInt(pRecipe.cookingTime);
    }

    interface CookieBaker<T extends AbstractCookingRecipe>
    {
        T create(ResourceLocation p_44353_, String p_44354_, Ingredient p_44355_, ItemStack p_44356_, float p_44357_, int p_44358_);
    }
}
