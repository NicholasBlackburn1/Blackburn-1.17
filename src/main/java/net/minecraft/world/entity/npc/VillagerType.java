package net.minecraft.world.entity.npc;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class VillagerType {
   public static final VillagerType DESERT = register("desert");
   public static final VillagerType JUNGLE = register("jungle");
   public static final VillagerType PLAINS = register("plains");
   public static final VillagerType SAVANNA = register("savanna");
   public static final VillagerType SNOW = register("snow");
   public static final VillagerType SWAMP = register("swamp");
   public static final VillagerType TAIGA = register("taiga");
   private final String name;
   private static final Map<ResourceKey<Biome>, VillagerType> BY_BIOME = Util.make(Maps.newHashMap(), (p_35834_) -> {
      p_35834_.put(Biomes.BADLANDS, DESERT);
      p_35834_.put(Biomes.BADLANDS_PLATEAU, DESERT);
      p_35834_.put(Biomes.DESERT, DESERT);
      p_35834_.put(Biomes.DESERT_HILLS, DESERT);
      p_35834_.put(Biomes.DESERT_LAKES, DESERT);
      p_35834_.put(Biomes.ERODED_BADLANDS, DESERT);
      p_35834_.put(Biomes.MODIFIED_BADLANDS_PLATEAU, DESERT);
      p_35834_.put(Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, DESERT);
      p_35834_.put(Biomes.WOODED_BADLANDS_PLATEAU, DESERT);
      p_35834_.put(Biomes.BAMBOO_JUNGLE, JUNGLE);
      p_35834_.put(Biomes.BAMBOO_JUNGLE_HILLS, JUNGLE);
      p_35834_.put(Biomes.JUNGLE, JUNGLE);
      p_35834_.put(Biomes.JUNGLE_EDGE, JUNGLE);
      p_35834_.put(Biomes.JUNGLE_HILLS, JUNGLE);
      p_35834_.put(Biomes.MODIFIED_JUNGLE, JUNGLE);
      p_35834_.put(Biomes.MODIFIED_JUNGLE_EDGE, JUNGLE);
      p_35834_.put(Biomes.SAVANNA_PLATEAU, SAVANNA);
      p_35834_.put(Biomes.SAVANNA, SAVANNA);
      p_35834_.put(Biomes.SHATTERED_SAVANNA, SAVANNA);
      p_35834_.put(Biomes.SHATTERED_SAVANNA_PLATEAU, SAVANNA);
      p_35834_.put(Biomes.DEEP_FROZEN_OCEAN, SNOW);
      p_35834_.put(Biomes.FROZEN_OCEAN, SNOW);
      p_35834_.put(Biomes.FROZEN_RIVER, SNOW);
      p_35834_.put(Biomes.ICE_SPIKES, SNOW);
      p_35834_.put(Biomes.SNOWY_BEACH, SNOW);
      p_35834_.put(Biomes.SNOWY_MOUNTAINS, SNOW);
      p_35834_.put(Biomes.SNOWY_TAIGA, SNOW);
      p_35834_.put(Biomes.SNOWY_TAIGA_HILLS, SNOW);
      p_35834_.put(Biomes.SNOWY_TAIGA_MOUNTAINS, SNOW);
      p_35834_.put(Biomes.SNOWY_TUNDRA, SNOW);
      p_35834_.put(Biomes.SWAMP, SWAMP);
      p_35834_.put(Biomes.SWAMP_HILLS, SWAMP);
      p_35834_.put(Biomes.GIANT_SPRUCE_TAIGA, TAIGA);
      p_35834_.put(Biomes.GIANT_SPRUCE_TAIGA_HILLS, TAIGA);
      p_35834_.put(Biomes.GIANT_TREE_TAIGA, TAIGA);
      p_35834_.put(Biomes.GIANT_TREE_TAIGA_HILLS, TAIGA);
      p_35834_.put(Biomes.GRAVELLY_MOUNTAINS, TAIGA);
      p_35834_.put(Biomes.MODIFIED_GRAVELLY_MOUNTAINS, TAIGA);
      p_35834_.put(Biomes.MOUNTAIN_EDGE, TAIGA);
      p_35834_.put(Biomes.MOUNTAINS, TAIGA);
      p_35834_.put(Biomes.TAIGA, TAIGA);
      p_35834_.put(Biomes.TAIGA_HILLS, TAIGA);
      p_35834_.put(Biomes.TAIGA_MOUNTAINS, TAIGA);
      p_35834_.put(Biomes.WOODED_MOUNTAINS, TAIGA);
   });

   private VillagerType(String p_35830_) {
      this.name = p_35830_;
   }

   public String toString() {
      return this.name;
   }

   private static VillagerType register(String p_35832_) {
      return Registry.register(Registry.VILLAGER_TYPE, new ResourceLocation(p_35832_), new VillagerType(p_35832_));
   }

   public static VillagerType byBiome(Optional<ResourceKey<Biome>> p_35836_) {
      return p_35836_.flatMap((p_35838_) -> {
         return Optional.ofNullable(BY_BIOME.get(p_35838_));
      }).orElse(PLAINS);
   }
}