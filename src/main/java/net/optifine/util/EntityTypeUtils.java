package net.optifine.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class EntityTypeUtils
{
    public static EntityType getEntityType(ResourceLocation loc)
    {
        return !Registry.ENTITY_TYPE.containsKey(loc) ? null : Registry.ENTITY_TYPE.get(loc);
    }
}
