package net.optifine.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class PotionUtils
{
    public static MobEffect getPotion(ResourceLocation loc)
    {
        return !Registry.MOB_EFFECT.containsKey(loc) ? null : Registry.MOB_EFFECT.get(loc);
    }
}
