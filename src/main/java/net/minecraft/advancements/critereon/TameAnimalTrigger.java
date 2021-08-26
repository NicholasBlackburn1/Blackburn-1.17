package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("tame_animal");

    public ResourceLocation getId()
    {
        return ID;
    }

    public TameAnimalTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        EntityPredicate.Composite entitypredicate$composite = EntityPredicate.Composite.fromJson(pJson, "entity", pConditionsParser);
        return new TameAnimalTrigger.TriggerInstance(pEntityPredicate, entitypredicate$composite);
    }

    public void trigger(ServerPlayer pPlayer, Animal pEntity)
    {
        LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
        this.trigger(pPlayer, (p_68838_) ->
        {
            return p_68838_.matches(lootcontext);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite p_68846_, EntityPredicate.Composite p_68847_)
        {
            super(TameAnimalTrigger.ID, p_68846_);
            this.entity = p_68847_;
        }

        public static TameAnimalTrigger.TriggerInstance tamedAnimal()
        {
            return new TameAnimalTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY);
        }

        public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate pEntityCondition)
        {
            return new TameAnimalTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pEntityCondition));
        }

        public boolean matches(LootContext pContext)
        {
            return this.entity.matches(pContext);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("entity", this.entity.toJson(pConditions));
            return jsonobject;
        }
    }
}
