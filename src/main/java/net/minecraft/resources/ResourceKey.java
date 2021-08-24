package net.minecraft.resources;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class ResourceKey<T> {
   private static final Map<String, ResourceKey<?>> VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());
   private final ResourceLocation registryName;
   private final ResourceLocation location;

   public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> p_135786_, ResourceLocation p_135787_) {
      return create(p_135786_.location, p_135787_);
   }

   public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation p_135789_) {
      return create(Registry.ROOT_REGISTRY_NAME, p_135789_);
   }

   private static <T> ResourceKey<T> create(ResourceLocation p_135791_, ResourceLocation p_135792_) {
      String s = (p_135791_ + ":" + p_135792_).intern();
      return (ResourceKey<T>)VALUES.computeIfAbsent(s, (p_135796_) -> {
         return new ResourceKey(p_135791_, p_135792_);
      });
   }

   private ResourceKey(ResourceLocation p_135780_, ResourceLocation p_135781_) {
      this.registryName = p_135780_;
      this.location = p_135781_;
   }

   public String toString() {
      return "ResourceKey[" + this.registryName + " / " + this.location + "]";
   }

   public boolean isFor(ResourceKey<? extends Registry<?>> p_135784_) {
      return this.registryName.equals(p_135784_.location());
   }

   public ResourceLocation location() {
      return this.location;
   }

   public static <T> Function<ResourceLocation, ResourceKey<T>> elementKey(ResourceKey<? extends Registry<T>> p_135798_) {
      return (p_135801_) -> {
         return create(p_135798_, p_135801_);
      };
   }
}
