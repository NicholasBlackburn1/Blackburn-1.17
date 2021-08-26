package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

    public ResourceLocation getId()
    {
        return ID;
    }

    public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        DamagePredicate damagepredicate = DamagePredicate.fromJson(pJson.get("damage"));
        EntityPredicate.Composite entitypredicate$composite = EntityPredicate.Composite.fromJson(pJson, "entity", pConditionsParser);
        return new PlayerHurtEntityTrigger.TriggerInstance(pEntityPredicate, damagepredicate, entitypredicate$composite);
    }

    public void trigger(ServerPlayer pPlayer, Entity pEntity, DamageSource pSource, float pAmountDealt, float pAmountTaken, boolean pBlocked)
    {
        LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
        this.trigger(pPlayer, (p_60126_) ->
        {
            return p_60126_.matches(pPlayer, lootcontext, pSource, pAmountDealt, pAmountTaken, pBlocked);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final DamagePredicate damage;
        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite p_60139_, DamagePredicate p_60140_, EntityPredicate.Composite p_60141_)
        {
            super(PlayerHurtEntityTrigger.ID, p_60139_);
            this.damage = p_60140_;
            this.entity = p_60141_;
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity()
        {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, DamagePredicate.ANY, EntityPredicate.Composite.ANY);
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate pBuilder)
        {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pBuilder, EntityPredicate.Composite.ANY);
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder pBuilder)
        {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pBuilder.build(), EntityPredicate.Composite.ANY);
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(EntityPredicate pBuilder)
        {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, DamagePredicate.ANY, EntityPredicate.Composite.wrap(pBuilder));
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate pBuilder, EntityPredicate p_156065_)
        {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pBuilder, EntityPredicate.Composite.wrap(p_156065_));
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder pBuilder, EntityPredicate p_156060_)
        {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pBuilder.build(), EntityPredicate.Composite.wrap(p_156060_));
        }

        public boolean matches(ServerPlayer pPlayer, LootContext pContext, DamageSource pDamage, float pDealt, float pTaken, boolean pBlocked)
        {
            if (!this.damage.matches(pPlayer, pDamage, pDealt, pTaken, pBlocked))
            {
                return false;
            }
            else
            {
                return this.entity.matches(pContext);
            }
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("damage", this.damage.serializeToJson());
            jsonobject.add("entity", this.entity.toJson(pConditions));
            return jsonobject;
        }
    }
}
