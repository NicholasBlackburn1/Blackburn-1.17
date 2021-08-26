package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");

    public ResourceLocation getId()
    {
        return ID;
    }

    public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        DamagePredicate damagepredicate = DamagePredicate.fromJson(pJson.get("damage"));
        return new EntityHurtPlayerTrigger.TriggerInstance(pEntityPredicate, damagepredicate);
    }

    public void trigger(ServerPlayer pPlayer, DamageSource pSource, float pAmountDealt, float pAmountTaken, boolean pWasBlocked)
    {
        this.trigger(pPlayer, (p_35186_) ->
        {
            return p_35186_.matches(pPlayer, pSource, pAmountDealt, pAmountTaken, pWasBlocked);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final DamagePredicate damage;

        public TriggerInstance(EntityPredicate.Composite p_35198_, DamagePredicate p_35199_)
        {
            super(EntityHurtPlayerTrigger.ID, p_35198_);
            this.damage = p_35199_;
        }

        public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer()
        {
            return new EntityHurtPlayerTrigger.TriggerInstance(EntityPredicate.Composite.ANY, DamagePredicate.ANY);
        }

        public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate pDamageConditionBuilder)
        {
            return new EntityHurtPlayerTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pDamageConditionBuilder);
        }

        public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate.Builder pDamageConditionBuilder)
        {
            return new EntityHurtPlayerTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pDamageConditionBuilder.build());
        }

        public boolean matches(ServerPlayer pPlayer, DamageSource pSource, float pAmountDealt, float pAmountTaken, boolean pWasBlocked)
        {
            return this.damage.matches(pPlayer, pSource, pAmountDealt, pAmountTaken, pWasBlocked);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("damage", this.damage.serializeToJson());
            return jsonobject;
        }
    }
}
