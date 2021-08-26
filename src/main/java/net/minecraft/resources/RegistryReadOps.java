package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistryReadOps<T> extends DelegatingOps<T>
{
    static final Logger LOGGER = LogManager.getLogger();
    private static final String JSON = ".json";
    private final RegistryReadOps.ResourceAccess resources;
    private final RegistryAccess registryAccess;
    private final Map < ResourceKey <? extends Registry<? >> , RegistryReadOps.ReadCache<? >> readCache;
    private final RegistryReadOps<JsonElement> jsonOps;

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> p_179867_, ResourceManager p_179868_, RegistryAccess p_179869_)
    {
        return createAndLoad(p_179867_, RegistryReadOps.ResourceAccess.forResourceManager(p_179868_), p_179869_);
    }

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> p_179871_, RegistryReadOps.ResourceAccess p_179872_, RegistryAccess p_179873_)
    {
        RegistryReadOps<T> registryreadops = new RegistryReadOps<>(p_179871_, p_179872_, p_179873_, Maps.newIdentityHashMap());
        RegistryAccess.load(p_179873_, registryreadops);
        return registryreadops;
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> p_179883_, ResourceManager p_179884_, RegistryAccess p_179885_)
    {
        return create(p_179883_, RegistryReadOps.ResourceAccess.forResourceManager(p_179884_), p_179885_);
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> p_179887_, RegistryReadOps.ResourceAccess p_179888_, RegistryAccess p_179889_)
    {
        return new RegistryReadOps<>(p_179887_, p_179888_, p_179889_, Maps.newIdentityHashMap());
    }

    private RegistryReadOps(DynamicOps<T> p_179862_, RegistryReadOps.ResourceAccess p_179863_, RegistryAccess p_179864_, IdentityHashMap < ResourceKey <? extends Registry<? >> , RegistryReadOps.ReadCache<? >> p_179865_)
    {
        super(p_179862_);
        this.resources = p_179863_;
        this.registryAccess = p_179864_;
        this.readCache = p_179865_;
        this.jsonOps = p_179862_ == JsonOps.INSTANCE ? (RegistryReadOps<JsonElement>)this : new RegistryReadOps<>(JsonOps.INSTANCE, p_179863_, p_179864_, p_179865_);
    }

    protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T pInput, ResourceKey <? extends Registry<E >> pRegistryKey, Codec<E> pMapCodec, boolean pAllowInlineDefinitions)
    {
        Optional<WritableRegistry<E>> optional = this.registryAccess.ownedRegistry(pRegistryKey);

        if (!optional.isPresent())
        {
            return DataResult.error("Unknown registry: " + pRegistryKey);
        }
        else
        {
            WritableRegistry<E> writableregistry = optional.get();
            DataResult<Pair<ResourceLocation, T>> dataresult = ResourceLocation.CODEC.decode(this.delegate, pInput);

            if (!dataresult.result().isPresent())
            {
                return !pAllowInlineDefinitions ? DataResult.error("Inline definitions not allowed here") : pMapCodec.decode(this, pInput).map((p_135647_) ->
                {
                    return p_135647_.mapFirst((p_179881_) -> {
                        return () -> {
                            return p_179881_;
                        };
                    });
                });
            }
            else
            {
                Pair<ResourceLocation, T> pair = dataresult.result().get();
                ResourceLocation resourcelocation = pair.getFirst();
                return this.readAndRegisterElement(pRegistryKey, writableregistry, pMapCodec, resourcelocation).map((p_135650_) ->
                {
                    return Pair.of(p_135650_, pair.getSecond());
                });
            }
        }
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> pSimpleRegistry, ResourceKey <? extends Registry<E >> pRegistryKey, Codec<E> pMapCodec)
    {
        Collection<ResourceLocation> collection = this.resources.listResources(pRegistryKey);
        DataResult<MappedRegistry<E>> dataresult = DataResult.success(pSimpleRegistry, Lifecycle.stable());
        String s = pRegistryKey.location().getPath() + "/";

        for (ResourceLocation resourcelocation : collection)
        {
            String s1 = resourcelocation.getPath();

            if (!s1.endsWith(".json"))
            {
                LOGGER.warn("Skipping resource {} since it is not a json file", (Object)resourcelocation);
            }
            else if (!s1.startsWith(s))
            {
                LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", (Object)resourcelocation);
            }
            else
            {
                String s2 = s1.substring(s.length(), s1.length() - ".json".length());
                ResourceLocation resourcelocation1 = new ResourceLocation(resourcelocation.getNamespace(), s2);
                dataresult = dataresult.flatMap((p_135688_) ->
                {
                    return this.readAndRegisterElement(pRegistryKey, p_135688_, pMapCodec, resourcelocation1).map((p_179876_) -> {
                        return p_135688_;
                    });
                });
            }
        }

        return dataresult.setPartial(pSimpleRegistry);
    }

    private <E> DataResult<Supplier<E>> readAndRegisterElement(ResourceKey <? extends Registry<E >> pRegistryKey, final WritableRegistry<E> pMutableRegistry, Codec<E> pMapCodec, ResourceLocation pId)
    {
        final ResourceKey<E> resourcekey = ResourceKey.create(pRegistryKey, pId);
        RegistryReadOps.ReadCache<E> readcache = this.readCache(pRegistryKey);
        DataResult<Supplier<E>> dataresult = readcache.values.get(resourcekey);

        if (dataresult != null)
        {
            return dataresult;
        }
        else
        {
            Supplier<E> supplier = Suppliers.memoize(() ->
            {
                E e = pMutableRegistry.get(resourcekey);

                if (e == null)
                {
                    throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + resourcekey);
                }
                else {
                    return e;
                }
            });
            readcache.values.put(resourcekey, DataResult.success(supplier));
            Optional<DataResult<Pair<E, OptionalInt>>> optional = this.resources.parseElement(this.jsonOps, pRegistryKey, resourcekey, pMapCodec);
            DataResult<Supplier<E>> dataresult1;

            if (!optional.isPresent())
            {
                dataresult1 = DataResult.success(new Supplier<E>()
                {
                    public E get()
                    {
                        return pMutableRegistry.get(resourcekey);
                    }
                    public String toString()
                    {
                        return resourcekey.toString();
                    }
                }, Lifecycle.stable());
            }
            else
            {
                DataResult<Pair<E, OptionalInt>> dataresult2 = optional.get();
                Optional<Pair<E, OptionalInt>> optional1 = dataresult2.result();

                if (optional1.isPresent())
                {
                    Pair<E, OptionalInt> pair = optional1.get();
                    pMutableRegistry.registerOrOverride(pair.getSecond(), resourcekey, pair.getFirst(), dataresult2.lifecycle());
                }

                dataresult1 = dataresult2.map((p_135674_) ->
                {
                    return () -> {
                        return pMutableRegistry.get(resourcekey);
                    };
                });
            }

            readcache.values.put(resourcekey, dataresult1);
            return dataresult1;
        }
    }

    private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey <? extends Registry<E >> pKey)
    {
        return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(pKey, (p_135707_) ->
        {
            return new RegistryReadOps.ReadCache<E>();
        });
    }

    protected <E> DataResult<Registry<E>> registry(ResourceKey <? extends Registry<E >> pRegistryKey)
    {
        return this.registryAccess.ownedRegistry(pRegistryKey).map((p_135667_) ->
        {
            return DataResult.<Registry<E>>success(p_135667_, p_135667_.elementsLifecycle());
        }).orElseGet(() ->
        {
            return DataResult.error("Unknown registry: " + pRegistryKey);
        });
    }

    static final class ReadCache<E>
    {
        final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.newIdentityHashMap();
    }

    public interface ResourceAccess
    {
        Collection<ResourceLocation> listResources(ResourceKey <? extends Registry<? >> pRegistryKey);

        <E> Optional<DataResult<Pair<E, OptionalInt>>> parseElement(DynamicOps<JsonElement> p_179892_, ResourceKey <? extends Registry<E >> p_179893_, ResourceKey<E> p_179894_, Decoder<E> p_179895_);

        static RegistryReadOps.ResourceAccess forResourceManager(final ResourceManager pManager)
        {
            return new RegistryReadOps.ResourceAccess()
            {
                public Collection<ResourceLocation> listResources(ResourceKey <? extends Registry<? >> pRegistryKey)
                {
                    return pManager.listResources(pRegistryKey.location().getPath(), (p_135732_) ->
                    {
                        return p_135732_.endsWith(".json");
                    });
                }
                public <E> Optional<DataResult<Pair<E, OptionalInt>>> parseElement(DynamicOps<JsonElement> p_179897_, ResourceKey <? extends Registry<E >> p_179898_, ResourceKey<E> p_179899_, Decoder<E> p_179900_)
                {
                    ResourceLocation resourcelocation = p_179899_.location();
                    ResourceLocation resourcelocation1 = new ResourceLocation(resourcelocation.getNamespace(), p_179898_.location().getPath() + "/" + resourcelocation.getPath() + ".json");

                    if (!pManager.hasResource(resourcelocation1))
                    {
                        return Optional.empty();
                    }
                    else
                    {
                        try
                        {
                            Resource resource = pManager.getResource(resourcelocation1);
                            Optional optional;

                            try
                            {
                                Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);

                                try
                                {
                                    JsonParser jsonparser = new JsonParser();
                                    JsonElement jsonelement = jsonparser.parse(reader);
                                    optional = Optional.of(p_179900_.parse(p_179897_, jsonelement).map((p_135730_) ->
                                    {
                                        return Pair.of(p_135730_, OptionalInt.empty());
                                    }));
                                }
                                catch (Throwable throwable2)
                                {
                                    try
                                    {
                                        reader.close();
                                    }
                                    catch (Throwable throwable1)
                                    {
                                        throwable2.addSuppressed(throwable1);
                                    }

                                    throw throwable2;
                                }

                                reader.close();
                            }
                            catch (Throwable throwable3)
                            {
                                if (resource != null)
                                {
                                    try
                                    {
                                        resource.close();
                                    }
                                    catch (Throwable throwable)
                                    {
                                        throwable3.addSuppressed(throwable);
                                    }
                                }

                                throw throwable3;
                            }

                            if (resource != null)
                            {
                                resource.close();
                            }

                            return optional;
                        }
                        catch (JsonIOException | JsonSyntaxException | IOException ioexception)
                        {
                            return Optional.of(DataResult.error("Failed to parse " + resourcelocation1 + " file: " + ioexception.getMessage()));
                        }
                    }
                }
                public String toString()
                {
                    return "ResourceAccess[" + pManager + "]";
                }
            };
        }

        public static final class MemoryMap implements RegistryReadOps.ResourceAccess
        {
            private final Map < ResourceKey<?>, JsonElement > data = Maps.newIdentityHashMap();
            private final Object2IntMap < ResourceKey<? >> ids = new Object2IntOpenCustomHashMap<>(Util.identityStrategy());
            private final Map < ResourceKey<?>, Lifecycle > lifecycles = Maps.newIdentityHashMap();

            public <E> void add(RegistryAccess.RegistryHolder pDynamicRegistries, ResourceKey<E> pKey, Encoder<E> pEncoder, int pId, E pInstance, Lifecycle pLifecycle)
            {
                DataResult<JsonElement> dataresult = pEncoder.encodeStart(RegistryWriteOps.create(JsonOps.INSTANCE, pDynamicRegistries), pInstance);
                Optional<PartialResult<JsonElement>> optional = dataresult.error();

                if (optional.isPresent())
                {
                    RegistryReadOps.LOGGER.error("Error adding element: {}", (Object)optional.get().message());
                }
                else
                {
                    this.data.put(pKey, dataresult.result().get());
                    this.ids.put(pKey, pId);
                    this.lifecycles.put(pKey, pLifecycle);
                }
            }

            public Collection<ResourceLocation> listResources(ResourceKey <? extends Registry<? >> pRegistryKey)
            {
                return this.data.keySet().stream().filter((p_135762_) ->
                {
                    return p_135762_.isFor(pRegistryKey);
                }).map((p_135759_) ->
                {
                    return new ResourceLocation(p_135759_.location().getNamespace(), pRegistryKey.location().getPath() + "/" + p_135759_.location().getPath() + ".json");
                }).collect(Collectors.toList());
            }

            public <E> Optional<DataResult<Pair<E, OptionalInt>>> parseElement(DynamicOps<JsonElement> p_179902_, ResourceKey <? extends Registry<E >> p_179903_, ResourceKey<E> p_179904_, Decoder<E> p_179905_)
            {
                JsonElement jsonelement = this.data.get(p_179904_);
                return jsonelement == null ? Optional.of(DataResult.error("Unknown element: " + p_179904_)) : Optional.of(p_179905_.parse(p_179902_, jsonelement).setLifecycle(this.lifecycles.get(p_179904_)).map((p_135756_) ->
                {
                    return Pair.of(p_135756_, OptionalInt.of(this.ids.getInt(p_179904_)));
                }));
            }
        }
    }
}
