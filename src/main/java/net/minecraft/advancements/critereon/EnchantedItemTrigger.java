package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("enchanted_item");

    public ResourceLocation getId()
    {
        return ID;
    }

    public EnchantedItemTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
        MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("levels"));
        return new EnchantedItemTrigger.TriggerInstance(pEntityPredicate, itempredicate, minmaxbounds$ints);
    }

    public void trigger(ServerPlayer pPlayer, ItemStack pItem, int pLevelsSpent)
    {
        this.trigger(pPlayer, (p_27675_) ->
        {
            return p_27675_.matches(pItem, pLevelsSpent);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final ItemPredicate item;
        private final MinMaxBounds.Ints levels;

        public TriggerInstance(EntityPredicate.Composite p_27688_, ItemPredicate p_27689_, MinMaxBounds.Ints p_27690_)
        {
            super(EnchantedItemTrigger.ID, p_27688_);
            this.item = p_27689_;
            this.levels = p_27690_;
        }

        public static EnchantedItemTrigger.TriggerInstance enchantedItem()
        {
            return new EnchantedItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ItemStack pItem, int pLevels)
        {
            if (!this.item.matches(pItem))
            {
                return false;
            }
            else
            {
                return this.levels.matches(pLevels);
            }
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("item", this.item.serializeToJson());
            jsonobject.add("levels", this.levels.serializeToJson());
            return jsonobject;
        }
    }
}
