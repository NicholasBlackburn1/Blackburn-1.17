package net.minecraft.resources;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class ResourceKey<T>
{
    private static final Map < String, ResourceKey<? >> VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());
    private final ResourceLocation registryName;
    private final ResourceLocation location;

    public static <T> ResourceKey<T> create(ResourceKey <? extends Registry<T >> pParent, ResourceLocation pLocation)
    {
        return create(pParent.location, pLocation);
    }

    public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation pLocation)
    {
        return create(Registry.ROOT_REGISTRY_NAME, pLocation);
    }

    private static <T> ResourceKey<T> create(ResourceLocation pParent, ResourceLocation pLocation)
    {
        String s = (pParent + ":" + pLocation).intern();
        return (ResourceKey<T>)VALUES.computeIfAbsent(s, (p_135796_) ->
        {
            return new ResourceKey(pParent, pLocation);
        });
    }

    private ResourceKey(ResourceLocation p_135780_, ResourceLocation p_135781_)
    {
        this.registryName = p_135780_;
        this.location = p_135781_;
    }

    public String toString()
    {
        return "ResourceKey[" + this.registryName + " / " + this.location + "]";
    }

    public boolean isFor(ResourceKey <? extends Registry<? >> pKey)
    {
        return this.registryName.equals(pKey.location());
    }

    public ResourceLocation location()
    {
        return this.location;
    }

    public static <T> Function<ResourceLocation, ResourceKey<T>> elementKey(ResourceKey <? extends Registry<T >> pParent)
    {
        return (p_135801_) ->
        {
            return create(pParent, p_135801_);
        };
    }
}
