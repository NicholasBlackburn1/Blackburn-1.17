package net.minecraft.advancements.critereon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeserializationContext
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceLocation id;
    private final PredicateManager predicateManager;
    private final Gson predicateGson = Deserializers.createConditionSerializer().create();

    public DeserializationContext(ResourceLocation p_25871_, PredicateManager p_25872_)
    {
        this.id = p_25871_;
        this.predicateManager = p_25872_;
    }

    public final LootItemCondition[] deserializeConditions(JsonArray pJson, String p_25876_, LootContextParamSet pParameterSet)
    {
        LootItemCondition[] alootitemcondition = this.predicateGson.fromJson(pJson, LootItemCondition[].class);
        ValidationContext validationcontext = new ValidationContext(pParameterSet, this.predicateManager::get, (p_25883_) ->
        {
            return null;
        });

        for (LootItemCondition lootitemcondition : alootitemcondition)
        {
            lootitemcondition.validate(validationcontext);
            validationcontext.getProblems().forEach((p_25880_, p_25881_) ->
            {
                LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", p_25876_, p_25880_, p_25881_);
            });
        }

        return alootitemcondition;
    }

    public ResourceLocation getAdvancementId()
    {
        return this.id;
    }
}
