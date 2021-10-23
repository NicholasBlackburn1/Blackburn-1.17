package net.blackburn.client.triggers;

import com.google.gson.JsonObject;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.advancements.critereon.EnterBlockTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;

public class CodeDefinedTrigger extends SimpleCriterionTrigger<CodeDefinedTrigger.TriggerInstance>

{
    public static ResourceLocation ID;


    public CodeDefinedTrigger( ResourceLocation id){
        CodeDefinedTrigger.ID = id;
    }
 

    public ResourceLocation getId()
    {
        return ID;
    }

    public CodeDefinedTrigger.TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pEntityPredicate, DeserializationContext pConditionsParser)
    {
      
        return new CodeDefinedTrigger.TriggerInstance(pEntityPredicate);
    }

    public void trigger(ServerPlayer serverPlayer)
    {
        this.trigger(serverPlayer, (p_74436_-> true));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance
    {
        

        public TriggerInstance(EntityPredicate.Composite p_74448_)
        {
            super(ID,p_74448_);
        
        }
    }
}
