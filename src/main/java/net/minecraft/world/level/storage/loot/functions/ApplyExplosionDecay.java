package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyExplosionDecay extends LootItemConditionalFunction
{
    ApplyExplosionDecay(LootItemCondition[] p_80029_)
    {
        super(p_80029_);
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.EXPLOSION_DECAY;
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        Float f = pContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);

        if (f != null)
        {
            Random random = pContext.getRandom();
            float f1 = 1.0F / f;
            int i = pStack.getCount();
            int j = 0;

            for (int k = 0; k < i; ++k)
            {
                if (random.nextFloat() <= f1)
                {
                    ++j;
                }
            }

            pStack.setCount(j);
        }

        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> explosionDecay()
    {
        return simpleBuilder(ApplyExplosionDecay::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyExplosionDecay>
    {
        public ApplyExplosionDecay m_6821_(JsonObject p_80040_, JsonDeserializationContext p_80041_, LootItemCondition[] p_80042_)
        {
            return new ApplyExplosionDecay(p_80042_);
        }
    }
}
