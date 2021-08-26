package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Criterion
{
    private final CriterionTriggerInstance trigger;

    public Criterion(CriterionTriggerInstance p_11415_)
    {
        this.trigger = p_11415_;
    }

    public Criterion()
    {
        this.trigger = null;
    }

    public void serializeToNetwork(FriendlyByteBuf pBuf)
    {
    }

    public static Criterion criterionFromJson(JsonObject pJson, DeserializationContext pConditionParser)
    {
        ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pJson, "trigger"));
        CriterionTrigger<?> criteriontrigger = CriteriaTriggers.getCriterion(resourcelocation);

        if (criteriontrigger == null)
        {
            throw new JsonSyntaxException("Invalid criterion trigger: " + resourcelocation);
        }
        else
        {
            CriterionTriggerInstance criteriontriggerinstance = criteriontrigger.createInstance(GsonHelper.getAsJsonObject(pJson, "conditions", new JsonObject()), pConditionParser);
            return new Criterion(criteriontriggerinstance);
        }
    }

    public static Criterion criterionFromNetwork(FriendlyByteBuf pBuffer)
    {
        return new Criterion();
    }

    public static Map<String, Criterion> criteriaFromJson(JsonObject pJson, DeserializationContext pConditionParser)
    {
        Map<String, Criterion> map = Maps.newHashMap();

        for (Entry<String, JsonElement> entry : pJson.entrySet())
        {
            map.put(entry.getKey(), criterionFromJson(GsonHelper.convertToJsonObject(entry.getValue(), "criterion"), pConditionParser));
        }

        return map;
    }

    public static Map<String, Criterion> criteriaFromNetwork(FriendlyByteBuf pBus)
    {
        return pBus.readMap(FriendlyByteBuf::readUtf, Criterion::criterionFromNetwork);
    }

    public static void serializeToNetwork(Map<String, Criterion> pCriteria, FriendlyByteBuf pBuf)
    {
        pBuf.writeMap(pCriteria, FriendlyByteBuf::writeUtf, (p_145258_, p_145259_) ->
        {
            p_145259_.serializeToNetwork(p_145258_);
        });
    }

    @Nullable
    public CriterionTriggerInstance getTrigger()
    {
        return this.trigger;
    }

    public JsonElement serializeToJson()
    {
        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("trigger", this.trigger.getCriterion().toString());
        JsonObject jsonobject1 = this.trigger.serializeToJson(SerializationContext.INSTANCE);

        if (jsonobject1.size() != 0)
        {
            jsonobject.add("conditions", jsonobject1);
        }

        return jsonobject;
    }
}
