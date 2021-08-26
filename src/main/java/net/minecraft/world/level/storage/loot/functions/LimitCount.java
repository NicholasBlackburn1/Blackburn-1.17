package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction
{
    final IntRange limiter;

    LimitCount(LootItemCondition[] p_165213_, IntRange p_165214_)
    {
        super(p_165213_);
        this.limiter = p_165214_;
    }

    public LootItemFunctionType getType()
    {
        return LootItemFunctions.LIMIT_COUNT;
    }

    public Set < LootContextParam<? >> getReferencedContextParams()
    {
        return this.limiter.getReferencedContextParams();
    }

    public ItemStack run(ItemStack pStack, LootContext pContext)
    {
        int i = this.limiter.clamp(pContext, pStack.getCount());
        pStack.setCount(i);
        return pStack;
    }

    public static LootItemConditionalFunction.Builder<?> limitCount(IntRange p_165216_)
    {
        return simpleBuilder((p_165219_) ->
        {
            return new LimitCount(p_165219_, p_165216_);
        });
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LimitCount>
    {
        public void serialize(JsonObject p_80660_, LimitCount p_80661_, JsonSerializationContext p_80662_)
        {
            super.serialize(p_80660_, p_80661_, p_80662_);
            p_80660_.add("limit", p_80662_.serialize(p_80661_.limiter));
        }

        public LimitCount m_6821_(JsonObject p_80656_, JsonDeserializationContext p_80657_, LootItemCondition[] p_80658_)
        {
            IntRange intrange = GsonHelper.getAsObject(p_80656_, "limit", p_80657_, IntRange.class);
            return new LimitCount(p_80658_, intrange);
        }
    }
}
