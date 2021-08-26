package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("shot_crossbow");

    public ResourceLocation getId()
    {
        return ID;
    }

    public ShotCrossbowTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
        return new ShotCrossbowTrigger.TriggerInstance(pEntityPredicate, itempredicate);
    }

    public void trigger(ServerPlayer pShooter, ItemStack pStack)
    {
        this.trigger(pShooter, (p_65467_) ->
        {
            return p_65467_.matches(pStack);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite p_65479_, ItemPredicate p_65480_)
        {
            super(ShotCrossbowTrigger.ID, p_65479_);
            this.item = p_65480_;
        }

        public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemPredicate pItemProvider)
        {
            return new ShotCrossbowTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pItemProvider);
        }

        public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemLike pItemProvider)
        {
            return new ShotCrossbowTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ItemPredicate.Builder.item().m_151445_(pItemProvider).build());
        }

        public boolean matches(ItemStack pStack)
        {
            return this.item.matches(pStack);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("item", this.item.serializeToJson());
            return jsonobject;
        }
    }
}
