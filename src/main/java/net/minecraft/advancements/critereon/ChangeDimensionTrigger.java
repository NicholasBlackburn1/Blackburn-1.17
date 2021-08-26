package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("changed_dimension");

    public ResourceLocation getId()
    {
        return ID;
    }

    public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        ResourceKey<Level> resourcekey = pJson.has("from") ? ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(GsonHelper.getAsString(pJson, "from"))) : null;
        ResourceKey<Level> resourcekey1 = pJson.has("to") ? ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(GsonHelper.getAsString(pJson, "to"))) : null;
        return new ChangeDimensionTrigger.TriggerInstance(pEntityPredicate, resourcekey, resourcekey1);
    }

    public void trigger(ServerPlayer pPlayer, ResourceKey<Level> pFromLevel, ResourceKey<Level> pToLevel)
    {
        this.trigger(pPlayer, (p_19768_) ->
        {
            return p_19768_.matches(pFromLevel, pToLevel);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        @Nullable
        private final ResourceKey<Level> from;
        @Nullable
        private final ResourceKey<Level> to;

        public TriggerInstance(EntityPredicate.Composite p_19777_, @Nullable ResourceKey<Level> p_19778_, @Nullable ResourceKey<Level> p_19779_)
        {
            super(ChangeDimensionTrigger.ID, p_19777_);
            this.from = p_19778_;
            this.to = p_19779_;
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimension()
        {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, (ResourceKey<Level>)null, (ResourceKey<Level>)null);
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimension(ResourceKey<Level> p_147561_, ResourceKey<Level> p_147562_)
        {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, p_147561_, p_147562_);
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(ResourceKey<Level> pToLevel)
        {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, (ResourceKey<Level>)null, pToLevel);
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimensionFrom(ResourceKey<Level> p_147564_)
        {
            return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, p_147564_, (ResourceKey<Level>)null);
        }

        public boolean matches(ResourceKey<Level> pFromLevel, ResourceKey<Level> pToLevel)
        {
            if (this.from != null && this.from != pFromLevel)
            {
                return false;
            }
            else
            {
                return this.to == null || this.to == pToLevel;
            }
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);

            if (this.from != null)
            {
                jsonobject.addProperty("from", this.from.location().toString());
            }

            if (this.to != null)
            {
                jsonobject.addProperty("to", this.to.location().toString());
            }

            return jsonobject;
        }
    }
}
