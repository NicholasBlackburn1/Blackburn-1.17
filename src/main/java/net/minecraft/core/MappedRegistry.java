package net.minecraft.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.RegistryDataPackCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T>
{
    protected static final Logger LOGGER = LogManager.getLogger();
    private final ObjectList<T> byId = new ObjectArrayList<>(256);
    private final Object2IntMap<T> toId = new Object2IntOpenCustomHashMap<>(Util.identityStrategy());
    private final BiMap<ResourceLocation, T> storage;
    private final BiMap<ResourceKey<T>, T> keyStorage;
    private final Map<T, Lifecycle> lifecycles;
    private Lifecycle elementsLifecycle;
    protected Object[] randomCache;
    private int nextId;

    public MappedRegistry(ResourceKey <? extends Registry<T >> p_122681_, Lifecycle p_122682_)
    {
        super(p_122681_, p_122682_);
        this.toId.defaultReturnValue(-1);
        this.storage = HashBiMap.create();
        this.keyStorage = HashBiMap.create();
        this.lifecycles = Maps.newIdentityHashMap();
        this.elementsLifecycle = p_122682_;
    }

    public static <T> MapCodec<MappedRegistry.RegistryEntry<T>> withNameAndId(ResourceKey <? extends Registry<T >> pRegistryKey, MapCodec<T> pMapCodec)
    {
        return RecordCodecBuilder.mapCodec((p_122733_) ->
        {
            return p_122733_.group(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(pRegistryKey), ResourceKey::location).fieldOf("name").forGetter((p_175394_) -> {
                return p_175394_.key;
            }), Codec.INT.fieldOf("id").forGetter((p_175390_) -> {
                return p_175390_.id;
            }), pMapCodec.forGetter((p_175383_) -> {
                return p_175383_.value;
            })).apply(p_122733_, MappedRegistry.RegistryEntry::new);
        });
    }

    public <V extends T> V registerMapping(int pId, ResourceKey<T> pName, V pInstance, Lifecycle pLifecycle)
    {
        return this.registerMapping(pId, pName, pInstance, pLifecycle, true);
    }

    private <V extends T> V registerMapping(int pId, ResourceKey<T> pName, V pInstance, Lifecycle pLifecycle, boolean p_122695_)
    {
        Validate.notNull(pName);
        Validate.notNull((T)pInstance);
        this.byId.size(Math.max(this.byId.size(), pId + 1));
        this.byId.set(pId, pInstance);
        this.toId.put((T)pInstance, pId);
        this.randomCache = null;

        if (p_122695_ && this.keyStorage.containsKey(pName))
        {
            LOGGER.debug("Adding duplicate key '{}' to registry", (Object)pName);
        }

        if (this.storage.containsValue(pInstance))
        {
            LOGGER.error("Adding duplicate value '{}' to registry", pInstance);
        }

        this.storage.put(pName.location(), (T)pInstance);
        this.keyStorage.put(pName, (T)pInstance);
        this.lifecycles.put((T)pInstance, pLifecycle);
        this.elementsLifecycle = this.elementsLifecycle.add(pLifecycle);

        if (this.nextId <= pId)
        {
            this.nextId = pId + 1;
        }

        return pInstance;
    }

    public <V extends T> V register(ResourceKey<T> pName, V pInstance, Lifecycle pLifecycle)
    {
        return this.registerMapping(this.nextId, pName, pInstance, pLifecycle);
    }

    public <V extends T> V registerOrOverride(OptionalInt pIndex, ResourceKey<T> pRegistryKey, V pValue, Lifecycle pLifecycle)
    {
        Validate.notNull(pRegistryKey);
        Validate.notNull((T)pValue);
        T t = this.keyStorage.get(pRegistryKey);
        int i;

        if (t == null)
        {
            i = pIndex.isPresent() ? pIndex.getAsInt() : this.nextId;
        }
        else
        {
            i = this.toId.getInt(t);

            if (pIndex.isPresent() && pIndex.getAsInt() != i)
            {
                throw new IllegalStateException("ID mismatch");
            }

            this.toId.removeInt(t);
            this.lifecycles.remove(t);
        }

        return this.registerMapping(i, pRegistryKey, pValue, pLifecycle, false);
    }

    @Nullable
    public ResourceLocation getKey(T pValue)
    {
        return this.storage.inverse().get(pValue);
    }

    public Optional<ResourceKey<T>> getResourceKey(T pValue)
    {
        return Optional.ofNullable(this.keyStorage.inverse().get(pValue));
    }

    public int getId(@Nullable T pValue)
    {
        return this.toId.getInt(pValue);
    }

    @Nullable
    public T get(@Nullable ResourceKey<T> pKey)
    {
        return this.keyStorage.get(pKey);
    }

    @Nullable
    public T byId(int pValue)
    {
        return (T)(pValue >= 0 && pValue < this.byId.size() ? this.byId.get(pValue) : null);
    }

    public Lifecycle lifecycle(T pObject)
    {
        return this.lifecycles.get(pObject);
    }

    public Lifecycle elementsLifecycle()
    {
        return this.elementsLifecycle;
    }

    public Iterator<T> iterator()
    {
        return Iterators.filter(this.byId.iterator(), Objects::nonNull);
    }

    @Nullable
    public T get(@Nullable ResourceLocation pKey)
    {
        return this.storage.get(pKey);
    }

    public Set<ResourceLocation> keySet()
    {
        return Collections.unmodifiableSet(this.storage.keySet());
    }

    public Set<Entry<ResourceKey<T>, T>> entrySet()
    {
        return Collections.unmodifiableMap(this.keyStorage).entrySet();
    }

    public boolean isEmpty()
    {
        return this.storage.isEmpty();
    }

    @Nullable
    public T getRandom(Random p_122712_)
    {
        if (this.randomCache == null)
        {
            Collection<?> collection = this.storage.values();

            if (collection.isEmpty())
            {
                return (T)null;
            }

            this.randomCache = collection.toArray(new Object[collection.size()]);
        }

        return Util.m_137545_((T[])this.randomCache, p_122712_);
    }

    public boolean containsKey(ResourceLocation pName)
    {
        return this.storage.containsKey(pName);
    }

    public boolean containsKey(ResourceKey<T> pName)
    {
        return this.keyStorage.containsKey(pName);
    }

    public static <T> Codec<MappedRegistry<T>> networkCodec(ResourceKey <? extends Registry<T >> pRegistryKey, Lifecycle pLifecycle, Codec<T> pCodec)
    {
        return withNameAndId(pRegistryKey, pCodec.fieldOf("element")).codec().listOf().xmap((p_122722_) ->
        {
            MappedRegistry<T> mappedregistry = new MappedRegistry<>(pRegistryKey, pLifecycle);

            for (MappedRegistry.RegistryEntry<T> registryentry : p_122722_)
            {
                mappedregistry.registerMapping(registryentry.id, registryentry.key, registryentry.value, pLifecycle);
            }

            return mappedregistry;
        }, (p_122744_) ->
        {
            Builder<MappedRegistry.RegistryEntry<T>> builder = ImmutableList.builder();

            for (T t : p_122744_)
            {
                builder.add(new MappedRegistry.RegistryEntry<>(p_122744_.getResourceKey(t).get(), p_122744_.getId(t), t));
            }

            return builder.build();
        });
    }

    public static <T> Codec<MappedRegistry<T>> dataPackCodec(ResourceKey <? extends Registry<T >> pRegistryKey, Lifecycle pLifecycle, Codec<T> pMapCodec)
    {
        return RegistryDataPackCodec.create(pRegistryKey, pLifecycle, pMapCodec);
    }

    public static <T> Codec<MappedRegistry<T>> directCodec(ResourceKey <? extends Registry<T >> pRegistryKey, Lifecycle pLifecycle, Codec<T> pMapCodec)
    {
        return Codec.unboundedMap(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(pRegistryKey), ResourceKey::location), pMapCodec).xmap((p_122726_) ->
        {
            MappedRegistry<T> mappedregistry = new MappedRegistry<>(pRegistryKey, pLifecycle);
            p_122726_.forEach((p_175387_, p_175388_) -> {
                mappedregistry.register(p_175387_, p_175388_, pLifecycle);
            });
            return mappedregistry;
        }, (p_122699_) ->
        {
            return ImmutableMap.copyOf(p_122699_.keyStorage);
        });
    }

    public static class RegistryEntry<T>
    {
        public final ResourceKey<T> key;
        public final int id;
        public final T value;

        public RegistryEntry(ResourceKey<T> p_122770_, int p_122771_, T p_122772_)
        {
            this.key = p_122770_;
            this.id = p_122771_;
            this.value = p_122772_;
        }
    }
}
