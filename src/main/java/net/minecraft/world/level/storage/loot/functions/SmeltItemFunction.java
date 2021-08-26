package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SmeltItemFunction extends LootItemConditionalFunction
{
    private static final Logger LOGGER = LogManager.getLogger();

    SmeltItemFunction(LootItemCondition[] p_81263_)
    {
        super(p_81263_);
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.FURNACE_SMELT;
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        if (pStack.isEmpty())
        {
            return pStack;
        }
        else
        {
            Optional<SmeltingRecipe> optional = pContext.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(pStack), pContext.getLevel());

            if (optional.isPresent())
            {
                ItemStack itemstack = optional.get().getResultItem();

                if (!itemstack.isEmpty())
                {
                    ItemStack itemstack1 = itemstack.copy();
                    itemstack1.setCount(pStack.getCount());
                    return itemstack1;
                }
            }

            LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", (Object)pStack);
            return pStack;
        }
    }

    public static LootItemConditionalFunction.Builder<?> smelted()
    {
        return simpleBuilder(SmeltItemFunction::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SmeltItemFunction>
    {
        public SmeltItemFunction m_6821_(JsonObject p_81274_, JsonDeserializationContext p_81275_, LootItemCondition[] p_81276_)
        {
            return new SmeltItemFunction(p_81276_);
        }
    }
}
