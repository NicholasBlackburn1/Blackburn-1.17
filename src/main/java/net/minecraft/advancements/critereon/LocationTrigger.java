package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class LocationTrigger extends SimpleCriterionTrigger<LocationTrigger.TriggerInstance>
{
    final ResourceLocation id;

    public LocationTrigger(ResourceLocation p_53643_)
    {
        this.id = p_53643_;
    }

    public ResourceLocation getId()
    {
        return this.id;
    }

    public LocationTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        JsonObject jsonobject = GsonHelper.getAsJsonObject(pJson, "location", pJson);
        LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject);
        return new LocationTrigger.TriggerInstance(this.id, pEntityPredicate, locationpredicate);
    }

    public void trigger(ServerPlayer pPlayer)
    {
        this.trigger(pPlayer, (p_53649_) ->
        {
            return p_53649_.matches(pPlayer.getLevel(), pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final LocationPredicate location;

        public TriggerInstance(ResourceLocation p_53662_, EntityPredicate.Composite p_53663_, LocationPredicate p_53664_)
        {
            super(p_53662_, p_53663_);
            this.location = p_53664_;
        }

        public static LocationTrigger.TriggerInstance located(LocationPredicate pLocation)
        {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.ANY, pLocation);
        }

        public static LocationTrigger.TriggerInstance located(EntityPredicate pLocation)
        {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.wrap(pLocation), LocationPredicate.ANY);
        }

        public static LocationTrigger.TriggerInstance sleptInBed()
        {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY);
        }

        public static LocationTrigger.TriggerInstance raidWon()
        {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY);
        }

        public static LocationTrigger.TriggerInstance walkOnBlockWithEquipment(Block p_154323_, Item p_154324_)
        {
            return located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().m_151445_(p_154324_).build()).build()).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().m_146726_(p_154323_).build()).build()).build());
        }

        public boolean matches(ServerLevel pLevel, double pX, double p_53668_, double pY)
        {
            return this.location.matches(pLevel, pX, p_53668_, pY);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("location", this.location.serializeToJson());
            return jsonobject;
        }
    }
}
