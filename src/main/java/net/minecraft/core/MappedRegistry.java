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

public class MappedRegistry<T> extends WritableRegistry<T> {
   protected static final Logger LOGGER = LogManager.getLogger();
   private final ObjectList<T> byId = new ObjectArrayList<>(256);
   private final Object2IntMap<T> toId = new Object2IntOpenCustomHashMap<>(Util.identityStrategy());
   private final BiMap<ResourceLocation, T> storage;
   private final BiMap<ResourceKey<T>, T> keyStorage;
   private final Map<T, Lifecycle> lifecycles;
   private Lifecycle elementsLifecycle;
   protected Object[] randomCache;
   private int nextId;

   public MappedRegistry(ResourceKey<? extends Registry<T>> p_122681_, Lifecycle p_122682_) {
      super(p_122681_, p_122682_);
      this.toId.defaultReturnValue(-1);
      this.storage = HashBiMap.create();
      this.keyStorage = HashBiMap.create();
      this.lifecycles = Maps.newIdentityHashMap();
      this.elementsLifecycle = p_122682_;
   }

   public static <T> MapCodec<MappedRegistry.RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> p_122728_, MapCodec<T> p_122729_) {
      return RecordCodecBuilder.mapCodec((p_122733_) -> {
         return p_122733_.group(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(p_122728_), ResourceKey::location).fieldOf("name").forGetter((p_175394_) -> {
            return p_175394_.key;
         }), Codec.INT.fieldOf("id").forGetter((p_175390_) -> {
            return p_175390_.id;
         }), p_122729_.forGetter((p_175383_) -> {
            return p_175383_.value;
         })).apply(p_122733_, MappedRegistry.RegistryEntry::new);
      });
   }

   public <V extends T> V registerMapping(int p_122686_, ResourceKey<T> p_122687_, V p_122688_, Lifecycle p_122689_) {
      return this.registerMapping(p_122686_, p_122687_, p_122688_, p_122689_, true);
   }

   private <V extends T> V registerMapping(int p_122691_, ResourceKey<T> p_122692_, V p_122693_, Lifecycle p_122694_, boolean p_122695_) {
      Validate.notNull(p_122692_);
      Validate.notNull((T)p_122693_);
      this.byId.size(Math.max(this.byId.size(), p_122691_ + 1));
      this.byId.set(p_122691_, p_122693_);
      this.toId.put((T)p_122693_, p_122691_);
      this.randomCache = null;
      if (p_122695_ && this.keyStorage.containsKey(p_122692_)) {
         LOGGER.debug("Adding duplicate key '{}' to registry", (Object)p_122692_);
      }

      if (this.storage.containsValue(p_122693_)) {
         LOGGER.error("Adding duplicate value '{}' to registry", p_122693_);
      }

      this.storage.put(p_122692_.location(), (T)p_122693_);
      this.keyStorage.put(p_122692_, (T)p_122693_);
      this.lifecycles.put((T)p_122693_, p_122694_);
      this.elementsLifecycle = this.elementsLifecycle.add(p_122694_);
      if (this.nextId <= p_122691_) {
         this.nextId = p_122691_ + 1;
      }

      return p_122693_;
   }

   public <V extends T> V register(ResourceKey<T> p_122735_, V p_122736_, Lifecycle p_122737_) {
      return this.registerMapping(this.nextId, p_122735_, p_122736_, p_122737_);
   }

   public <V extends T> V registerOrOverride(OptionalInt p_122708_, ResourceKey<T> p_122709_, V p_122710_, Lifecycle p_122711_) {
      Validate.notNull(p_122709_);
      Validate.notNull((T)p_122710_);
      T t = this.keyStorage.get(p_122709_);
      int i;
      if (t == null) {
         i = p_122708_.isPresent() ? p_122708_.getAsInt() : this.nextId;
      } else {
         i = this.toId.getInt(t);
         if (p_122708_.isPresent() && p_122708_.getAsInt() != i) {
            throw new IllegalStateException("ID mismatch");
         }

         this.toId.removeInt(t);
         this.lifecycles.remove(t);
      }

      return this.registerMapping(i, p_122709_, p_122710_, p_122711_, false);
   }

   @Nullable
   public ResourceLocation getKey(T p_122746_) {
      return this.storage.inverse().get(p_122746_);
   }

   public Optional<ResourceKey<T>> getResourceKey(T p_122755_) {
      return Optional.ofNullable(this.keyStorage.inverse().get(p_122755_));
   }

   public int getId(@Nullable T p_122706_) {
      return this.toId.getInt(p_122706_);
   }

   @Nullable
   public T get(@Nullable ResourceKey<T> p_122714_) {
      return this.keyStorage.get(p_122714_);
   }

   @Nullable
   public T byId(int p_122684_) {
      return (T)(p_122684_ >= 0 && p_122684_ < this.byId.size() ? this.byId.get(p_122684_) : null);
   }

   public Lifecycle lifecycle(T p_122764_) {
      return this.lifecycles.get(p_122764_);
   }

   public Lifecycle elementsLifecycle() {
      return this.elementsLifecycle;
   }

   public Iterator<T> iterator() {
      return Iterators.filter(this.byId.iterator(), Objects::nonNull);
   }

   @Nullable
   public T get(@Nullable ResourceLocation p_122739_) {
      return this.storage.get(p_122739_);
   }

   public Set<ResourceLocation> keySet() {
      return Collections.unmodifiableSet(this.storage.keySet());
   }

   public Set<Entry<ResourceKey<T>, T>> entrySet() {
      return Collections.unmodifiableMap(this.keyStorage).entrySet();
   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   @Nullable
   public T getRandom(Random p_122712_) {
      if (this.randomCache == null) {
         Collection<?> collection = this.storage.values();
         if (collection.isEmpty()) {
            return (T)null;
         }

         this.randomCache = collection.toArray(new Object[collection.size()]);
      }

      return Util.getRandom((T[])this.randomCache, p_122712_);
   }

   public boolean containsKey(ResourceLocation p_122761_) {
      return this.storage.containsKey(p_122761_);
   }

   public boolean containsKey(ResourceKey<T> p_175392_) {
      return this.keyStorage.containsKey(p_175392_);
   }

   public static <T> Codec<MappedRegistry<T>> networkCodec(ResourceKey<? extends Registry<T>> p_122716_, Lifecycle p_122717_, Codec<T> p_122718_) {
      return withNameAndId(p_122716_, p_122718_.fieldOf("element")).codec().listOf().xmap((p_122722_) -> {
         MappedRegistry<T> mappedregistry = new MappedRegistry<>(p_122716_, p_122717_);

         for(MappedRegistry.RegistryEntry<T> registryentry : p_122722_) {
            mappedregistry.registerMapping(registryentry.id, registryentry.key, registryentry.value, p_122717_);
         }

         return mappedregistry;
      }, (p_122744_) -> {
         Builder<MappedRegistry.RegistryEntry<T>> builder = ImmutableList.builder();

         for(T t : p_122744_) {
            builder.add(new MappedRegistry.RegistryEntry<>(p_122744_.getResourceKey(t).get(), p_122744_.getId(t), t));
         }

         return builder.build();
      });
   }

   public static <T> Codec<MappedRegistry<T>> dataPackCodec(ResourceKey<? extends Registry<T>> p_122748_, Lifecycle p_122749_, Codec<T> p_122750_) {
      return RegistryDataPackCodec.create(p_122748_, p_122749_, p_122750_);
   }

   public static <T> Codec<MappedRegistry<T>> directCodec(ResourceKey<? extends Registry<T>> p_122757_, Lifecycle p_122758_, Codec<T> p_122759_) {
      return Codec.unboundedMap(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(p_122757_), ResourceKey::location), p_122759_).xmap((p_122726_) -> {
         MappedRegistry<T> mappedregistry = new MappedRegistry<>(p_122757_, p_122758_);
         p_122726_.forEach((p_175387_, p_175388_) -> {
            mappedregistry.register(p_175387_, p_175388_, p_122758_);
         });
         return mappedregistry;
      }, (p_122699_) -> {
         return ImmutableMap.copyOf(p_122699_.keyStorage);
      });
   }

   public static class RegistryEntry<T> {
      public final ResourceKey<T> key;
      public final int id;
      public final T value;

      public RegistryEntry(ResourceKey<T> p_122770_, int p_122771_, T p_122772_) {
         this.key = p_122770_;
         this.id = p_122771_;
         this.value = p_122772_;
      }
   }
}