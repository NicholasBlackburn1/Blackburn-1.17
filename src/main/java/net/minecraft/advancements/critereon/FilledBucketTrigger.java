package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FilledBucketTrigger extends SimpleCriterionTrigger<FilledBucketTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("filled_bucket");

    public ResourceLocation getId()
    {
        return ID;
    }

    public FilledBucketTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
        return new FilledBucketTrigger.TriggerInstance(pEntityPredicate, itempredicate);
    }

    public void trigger(ServerPlayer pPlayer, ItemStack pStack)
    {
        this.trigger(pPlayer, (p_38777_) ->
        {
            return p_38777_.matches(pStack);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite p_38789_, ItemPredicate p_38790_)
        {
            super(FilledBucketTrigger.ID, p_38789_);
            this.item = p_38790_;
        }

        public static FilledBucketTrigger.TriggerInstance filledBucket(ItemPredicate pItemCondition)
        {
            return new FilledBucketTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pItemCondition);
        }

        public boolean matches(ItemStack pStack)
        {
            return this.item.matches(pStack);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("item", this.item.serializeToJson());
            return jsonobject;
        }
    }
}
