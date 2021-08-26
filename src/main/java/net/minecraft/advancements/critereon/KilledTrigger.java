package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance>
{
    final ResourceLocation id;

    public KilledTrigger(ResourceLocation p_48102_)
    {
        this.id = p_48102_;
    }

    public ResourceLocation getId()
    {
        return this.id;
    }

    public KilledTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        return new KilledTrigger.TriggerInstance(this.id, pEntityPredicate, EntityPredicate.Composite.fromJson(pJson, "entity", pConditionsParser), DamageSourcePredicate.fromJson(pJson.get("killing_blow")));
    }

    public void trigger(ServerPlayer pPlayer, Entity pEntity, DamageSource pSource)
    {
        LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
        this.trigger(pPlayer, (p_48112_) ->
        {
            return p_48112_.matches(pPlayer, lootcontext, pSource);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final EntityPredicate.Composite entityPredicate;
        private final DamageSourcePredicate killingBlow;

        public TriggerInstance(ResourceLocation p_48126_, EntityPredicate.Composite p_48127_, EntityPredicate.Composite p_48128_, DamageSourcePredicate p_48129_)
        {
            super(p_48126_, p_48127_);
            this.entityPredicate = p_48128_;
            this.killingBlow = p_48129_;
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate pBuilder)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pBuilder), DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder pBuilder)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pBuilder.build()), DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity()
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate pBuilder, DamageSourcePredicate p_152115_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pBuilder), p_152115_);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder pBuilder, DamageSourcePredicate p_152107_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pBuilder.build()), p_152107_);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate pBuilder, DamageSourcePredicate.Builder p_152112_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pBuilder), p_152112_.build());
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder pBuilder, DamageSourcePredicate.Builder p_48138_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pBuilder.build()), p_48138_.build());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate p_152125_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(p_152125_), DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder p_152117_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(p_152117_.build()), DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer()
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate p_152130_, DamageSourcePredicate p_152131_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(p_152130_), p_152131_);
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder p_152122_, DamageSourcePredicate p_152123_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(p_152122_.build()), p_152123_);
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate p_152127_, DamageSourcePredicate.Builder p_152128_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(p_152127_), p_152128_.build());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder p_152119_, DamageSourcePredicate.Builder p_152120_)
        {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(p_152119_.build()), p_152120_.build());
        }

        public boolean matches(ServerPlayer pPlayer, LootContext pContext, DamageSource pSource)
        {
            return !this.killingBlow.matches(pPlayer, pSource) ? false : this.entityPredicate.matches(pContext);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("entity", this.entityPredicate.toJson(pConditions));
            jsonobject.add("killing_blow", this.killingBlow.serializeToJson());
            return jsonobject;
        }
    }
}
