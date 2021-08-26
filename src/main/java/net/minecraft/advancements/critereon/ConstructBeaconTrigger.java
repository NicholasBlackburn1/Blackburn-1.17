package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("construct_beacon");

    public ResourceLocation getId()
    {
        return ID;
    }

    public ConstructBeaconTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("level"));
        return new ConstructBeaconTrigger.TriggerInstance(pEntityPredicate, minmaxbounds$ints);
    }

    public void trigger(ServerPlayer p_148030_, int p_148031_)
    {
        this.trigger(p_148030_, (p_148028_) ->
        {
            return p_148028_.matches(p_148031_);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final MinMaxBounds.Ints level;

        public TriggerInstance(EntityPredicate.Composite p_22763_, MinMaxBounds.Ints p_22764_)
        {
            super(ConstructBeaconTrigger.ID, p_22763_);
            this.level = p_22764_;
        }

        public static ConstructBeaconTrigger.TriggerInstance constructedBeacon()
        {
            return new ConstructBeaconTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY);
        }

        public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints pLevel)
        {
            return new ConstructBeaconTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pLevel);
        }

        public boolean matches(int p_148033_)
        {
            return this.level.matches(p_148033_);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("level", this.level.serializeToJson());
            return jsonobject;
        }
    }
}
