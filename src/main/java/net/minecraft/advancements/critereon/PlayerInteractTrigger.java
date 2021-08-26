package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

    public ResourceLocation getId()
    {
        return ID;
    }

    protected PlayerInteractTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        ItemPredicate itempredicate = ItemPredicate.fromJson(pJson.get("item"));
        EntityPredicate.Composite entitypredicate$composite = EntityPredicate.Composite.fromJson(pJson, "entity", pConditionsParser);
        return new PlayerInteractTrigger.TriggerInstance(pEntityPredicate, itempredicate, entitypredicate$composite);
    }

    public void trigger(ServerPlayer pPlayer, ItemStack pStack, Entity pEntity)
    {
        LootContext lootcontext = EntityPredicate.createContext(pPlayer, pEntity);
        this.trigger(pPlayer, (p_61501_) ->
        {
            return p_61501_.matches(pStack, lootcontext);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final ItemPredicate item;
        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite p_61514_, ItemPredicate p_61515_, EntityPredicate.Composite p_61516_)
        {
            super(PlayerInteractTrigger.ID, p_61514_);
            this.item = p_61515_;
            this.entity = p_61516_;
        }

        public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(EntityPredicate.Composite pPlayer, ItemPredicate.Builder pStack, EntityPredicate.Composite pEntity)
        {
            return new PlayerInteractTrigger.TriggerInstance(pPlayer, pStack.build(), pEntity);
        }

        public boolean matches(ItemStack pStack, LootContext pContext)
        {
            return !this.item.matches(pStack) ? false : this.entity.matches(pContext);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);
            jsonobject.add("item", this.item.serializeToJson());
            jsonobject.add("entity", this.entity.toJson(pConditions));
            return jsonobject;
        }
    }
}
