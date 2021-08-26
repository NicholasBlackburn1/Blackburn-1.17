package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class NetherTravelTrigger extends SimpleCriterionTrigger<NetherTravelTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("nether_travel");

    public ResourceLocation getId()
    {
        return ID;
    }

    public NetherTravelTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        LocationPredicate locationpredicate = LocationPredicate.fromJson(pJson.get("entered"));
        LocationPredicate locationpredicate1 = LocationPredicate.fromJson(pJson.get("exited"));
        DistancePredicate distancepredicate = DistancePredicate.fromJson(pJson.get("distance"));
        return new NetherTravelTrigger.TriggerInstance(pEntityPredicate, locationpredicate, locationpredicate1, distancepredicate);
    }

    public void trigger(ServerPlayer pPlayer, Vec3 pEnteredNetherPosition)
    {
        this.trigger(pPlayer, (p_58445_) ->
        {
            return p_58445_.matches(pPlayer.getLevel(), pEnteredNetherPosition, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final LocationPredicate entered;
        private final LocationPredicate exited;
        private final DistancePredicate distance;

        public TriggerInstance(EntityPredicate.Composite p_58459_, LocationPredicate p_58460_, LocationPredicate p_58461_, DistancePredicate p_58462_)
        {
            super(NetherTravelTrigger.ID, p_58459_);
            this.entered = p_58460_;
            this.exited = p_58461_;
            this.distance = p_58462_;
        }

        public static NetherTravelTrigger.TriggerInstance travelledThroughNether(DistancePredicate pDistance)
        {
            return new NetherTravelTrigger.TriggerInstance(EntityPredicate.Composite.ANY, LocationPredicate.ANY, LocationPredicate.ANY, pDistance);
        }

        public boolean matches(ServerLevel pLevel, Vec3 pEnteredNetherPosition, double pX, double p_58467_, double pY)
        {
            if (!this.entered.matches(pLevel, pEnteredNetherPosition.x, pEnteredNetherPosition.y, pEnteredNetherPosition.z))
            {
                return false;
            }
            else if (!this.exited.matches(pLevel, pX, p_58467_, pY))
            {
                return false;
            }
            else
            {
                return this.distance.matches(pEnteredNetherPosition.x, pEnteredNetherPosition.y, pEnteredNetherPosition.z, pX, p_58467_, pY);
            }
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("entered", this.entered.serializeToJson());
            jsonobject.add("exited", this.exited.serializeToJson());
            jsonobject.add("distance", this.distance.serializeToJson());
            return jsonobject;
        }
    }
}
