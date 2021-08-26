package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class UpgradeRecipe implements Recipe<Container>
{
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;
    private final ResourceLocation id;

    public UpgradeRecipe(ResourceLocation p_44523_, Ingredient p_44524_, Ingredient p_44525_, ItemStack p_44526_)
    {
        this.id = p_44523_;
        this.base = p_44524_;
        this.addition = p_44525_;
        this.result = p_44526_;
    }

    public boolean matches(Container pInv, Level pLevel)
    {
        return this.base.test(pInv.getItem(0)) && this.addition.test(pInv.getItem(1));
    }

    public ItemStack assemble(Container pInv)
    {
        ItemStack itemstack = this.result.copy();
        CompoundTag compoundtag = pInv.getItem(0).getTag();

        if (compoundtag != null)
        {
            itemstack.setTag(compoundtag.copy());
        }

        return itemstack;
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight)
    {
        return pWidth * pHeight >= 2;
    }

    public ItemStack getResultItem()
    {
        return this.result;
    }

    public boolean isAdditionIngredient(ItemStack pAddition)
    {
        return this.addition.test(pAddition);
    }

    public ItemStack getToastSymbol()
    {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    public ResourceLocation getId()
    {
        return this.id;
    }

    public RecipeSerializer<?> getSerializer()
    {
        return RecipeSerializer.SMITHING;
    }

    public RecipeType<?> getType()
    {
        return RecipeType.SMITHING;
    }

    public boolean isIncomplete()
    {
        return Stream.of(this.base, this.addition).anyMatch((p_151284_) ->
        {
            return p_151284_.getItems().length == 0;
        });
    }

    public static class Serializer implements RecipeSerializer<UpgradeRecipe>
    {
        public UpgradeRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson)
        {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "base"));
            Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "addition"));
            ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pJson, "result"));
            return new UpgradeRecipe(pRecipeId, ingredient, ingredient1, itemstack);
        }

        public UpgradeRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer)
        {
            Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
            Ingredient ingredient1 = Ingredient.fromNetwork(pBuffer);
            ItemStack itemstack = pBuffer.readItem();
            return new UpgradeRecipe(pRecipeId, ingredient, ingredient1, itemstack);
        }

        public void toNetwork(FriendlyByteBuf pBuffer, UpgradeRecipe pRecipe)
        {
            pRecipe.base.toNetwork(pBuffer);
            pRecipe.addition.toNetwork(pBuffer);
            pBuffer.writeItem(pRecipe.result);
        }
    }
}
