package net.optifine.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemUtils
{
    public static Item getItem(ResourceLocation loc)
    {
        return !Registry.ITEM.containsKey(loc) ? null : Registry.ITEM.get(loc);
    }

    public static int getId(Item item)
    {
        return Registry.ITEM.getId(item);
    }
}
