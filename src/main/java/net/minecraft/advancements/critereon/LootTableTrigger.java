package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("player_generates_container_loot");

    public ResourceLocation getId()
    {
        return ID;
    }

    protected LootTableTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "loot_table"));
        return new LootTableTrigger.TriggerInstance(pEntityPredicate, resourcelocation);
    }

    public void trigger(ServerPlayer pPlayer, ResourceLocation pGeneratedLoot)
    {
        this.trigger(pPlayer, (p_54606_) ->
        {
            return p_54606_.matches(pGeneratedLoot);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final ResourceLocation lootTable;

        public TriggerInstance(EntityPredicate.Composite p_54614_, ResourceLocation p_54615_)
        {
            super(LootTableTrigger.ID, p_54614_);
            this.lootTable = p_54615_;
        }

        public static LootTableTrigger.TriggerInstance lootTableUsed(ResourceLocation pGeneratedLoot)
        {
            return new LootTableTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pGeneratedLoot);
        }

        public boolean matches(ResourceLocation pGeneratedLoot)
        {
            return this.lootTable.equals(pGeneratedLoot);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.addProperty("loot_table", this.lootTable.toString());
            return jsonobject;
        }
    }
}
