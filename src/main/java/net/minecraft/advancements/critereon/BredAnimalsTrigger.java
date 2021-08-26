package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("bred_animals");

    public ResourceLocation getId()
    {
        return ID;
    }

    public BredAnimalsTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        EntityPredicate.Composite entitypredicate$composite = EntityPredicate.Composite.fromJson(pJson, "parent", pConditionsParser);
        EntityPredicate.Composite entitypredicate$composite1 = EntityPredicate.Composite.fromJson(pJson, "partner", pConditionsParser);
        EntityPredicate.Composite entitypredicate$composite2 = EntityPredicate.Composite.fromJson(pJson, "child", pConditionsParser);
        return new BredAnimalsTrigger.TriggerInstance(pEntityPredicate, entitypredicate$composite, entitypredicate$composite1, entitypredicate$composite2);
    }

    public void trigger(ServerPlayer p_147279_, Animal p_147280_, Animal p_147281_, @Nullable AgeableMob p_147282_)
    {
        LootContext lootcontext = EntityPredicate.createContext(p_147279_, p_147280_);
        LootContext lootcontext1 = EntityPredicate.createContext(p_147279_, p_147281_);
        LootContext lootcontext2 = p_147282_ != null ? EntityPredicate.createContext(p_147279_, p_147282_) : null;
        this.trigger(p_147279_, (p_18653_) ->
        {
            return p_18653_.matches(lootcontext, lootcontext1, lootcontext2);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final EntityPredicate.Composite parent;
        private final EntityPredicate.Composite partner;
        private final EntityPredicate.Composite child;

        public TriggerInstance(EntityPredicate.Composite p_18663_, EntityPredicate.Composite p_18664_, EntityPredicate.Composite p_18665_, EntityPredicate.Composite p_18666_)
        {
            super(BredAnimalsTrigger.ID, p_18663_);
            this.parent = p_18664_;
            this.partner = p_18665_;
            this.child = p_18666_;
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals()
        {
            return new BredAnimalsTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY);
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder pBuilder)
        {
            return new BredAnimalsTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pBuilder.build()));
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate pBuilder, EntityPredicate p_18671_, EntityPredicate p_18672_)
        {
            return new BredAnimalsTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(pBuilder), EntityPredicate.Composite.wrap(p_18671_), EntityPredicate.Composite.wrap(p_18672_));
        }

        public boolean matches(LootContext pParentContext, LootContext pPartnerContext, @Nullable LootContext pChildContext)
        {
            if (this.child == EntityPredicate.Composite.ANY || pChildContext != null && this.child.matches(pChildContext))
            {
                return this.parent.matches(pParentContext) && this.partner.matches(pPartnerContext) || this.parent.matches(pPartnerContext) && this.partner.matches(pParentContext);
            }
            else
            {
                return false;
            }
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("parent", this.parent.toJson(pConditions));
            jsonobject.add("partner", this.partner.toJson(pConditions));
            jsonobject.add("child", this.child.toJson(pConditions));
            return jsonobject;
        }
    }
}
