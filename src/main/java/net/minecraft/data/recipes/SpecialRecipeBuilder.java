package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

public class SpecialRecipeBuilder
{
    final SimpleRecipeSerializer<?> serializer;

    public SpecialRecipeBuilder(SimpleRecipeSerializer<?> p_126356_)
    {
        this.serializer = p_126356_;
    }

    public static SpecialRecipeBuilder special(SimpleRecipeSerializer<?> p_126358_)
    {
        return new SpecialRecipeBuilder(p_126358_);
    }

    public void save(Consumer<FinishedRecipe> pConsumer, final String pId)
    {
        pConsumer.accept(new FinishedRecipe()
        {
            public void serializeRecipeData(JsonObject pJson)
            {
            }
            public RecipeSerializer<?> getType()
            {
                return SpecialRecipeBuilder.this.serializer;
            }
            public ResourceLocation getId()
            {
                return new ResourceLocation(pId);
            }
            @Nullable
            public JsonObject serializeAdvancement()
            {
                return null;
            }
            public ResourceLocation getAdvancementId()
            {
                return new ResourceLocation("");
            }
        });
    }
}
