package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V702 extends Schema
{
    public V702(int p_18007_, Schema p_18008_)
    {
        super(p_18007_, p_18008_);
    }

    protected static void registerMob(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName)
    {
        pSchema.register(pMap, pName, () ->
        {
            return V100.equipment(pSchema);
        });
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema p_18016_)
    {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(p_18016_);
        registerMob(p_18016_, map, "ZombieVillager");
        registerMob(p_18016_, map, "Husk");
        return map;
    }
}
