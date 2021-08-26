package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EnterBlockTrigger extends SimpleCriterionTrigger<EnterBlockTrigger.TriggerInstance>
{
    static final ResourceLocation ID = new ResourceLocation("enter_block");

    public ResourceLocation getId()
    {
        return ID;
    }

    public EnterBlockTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
        Block block = deserializeBlock(pJson);
        StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(pJson.get("state"));

        if (block != null)
        {
            statepropertiespredicate.checkState(block.getStateDefinition(), (p_31274_) ->
            {
                throw new JsonSyntaxException("Block " + block + " has no property " + p_31274_);
            });
        }

        return new EnterBlockTrigger.TriggerInstance(pEntityPredicate, block, statepropertiespredicate);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject pJsonObject)
    {
        if (pJsonObject.has("block"))
        {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJsonObject, "block"));
            return Registry.BLOCK.getOptional(resourcelocation).orElseThrow(() ->
            {
                return new JsonSyntaxException("Unknown block type '" + resourcelocation + "'");
            });
        }
        else
        {
            return null;
        }
    }

    public void trigger(ServerPlayer pPlayer, BlockState pState)
    {
        this.trigger(pPlayer, (p_31277_) ->
        {
            return p_31277_.matches(pState);
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        private final Block block;
        private final StatePropertiesPredicate state;

        public TriggerInstance(EntityPredicate.Composite p_31294_, @Nullable Block p_31295_, StatePropertiesPredicate p_31296_)
        {
            super(EnterBlockTrigger.ID, p_31294_);
            this.block = p_31295_;
            this.state = p_31296_;
        }

        public static EnterBlockTrigger.TriggerInstance entersBlock(Block pBlock)
        {
            return new EnterBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, pBlock, StatePropertiesPredicate.ANY);
        }

        public JsonObject serializeToJson(SerializationContext pConditions)
        {
            JsonObject jsonobject = super.serializeToJson(pConditions);

            if (this.block != null)
            {
                jsonobject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
            }

            jsonobject.add("state", this.state.serializeToJson());
            return jsonobject;
        }

        public boolean matches(BlockState pState)
        {
            if (this.block != null && !pState.is(this.block))
            {
                return false;
            }
            else
            {
                return this.state.matches(pState);
            }
        }
    }
}
