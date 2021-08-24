package net.minecraft.data.worldgen.biome;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class VanillaBiomes {
   private static int calculateSkyColor(float p_127333_) {
      float f = p_127333_ / 3.0F;
      f = Mth.clamp(f, -1.0F, 1.0F);
      return Mth.hsvToRgb(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1.0F);
   }

   public static Biome giantTreeTaiga(float p_127352_, float p_127353_, float p_127354_, boolean p_127355_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      if (p_127355_) {
         BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      } else {
         BiomeDefaultFeatures.caveSpawns(mobspawnsettings$builder);
         BiomeDefaultFeatures.monsters(mobspawnsettings$builder, 100, 25, 100);
      }

      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GIANT_TREE_TAIGA);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addMossyStoneBlock(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addFerns(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, p_127355_ ? Features.TREES_GIANT_SPRUCE : Features.TREES_GIANT);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addGiantTaigaVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSparseBerryBushes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.TAIGA).depth(p_127352_).scale(p_127353_).temperature(p_127354_).downfall(0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(p_127354_)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome birchForestBiome(float p_127394_, float p_127395_, boolean p_127396_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (p_127396_) {
         BiomeDefaultFeatures.addTallBirchTrees(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addBirchTrees(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addForestGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(p_127394_).scale(p_127395_).temperature(0.6F).downfall(0.6F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.6F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome jungleBiome() {
      return jungleBiome(0.1F, 0.2F, 40, 2, 3);
   }

   public static Biome jungleEdgeBiome() {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
      return baseJungleBiome(0.1F, 0.2F, 0.8F, false, true, false, mobspawnsettings$builder);
   }

   public static Biome modifiedJungleEdgeBiome() {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
      return baseJungleBiome(0.2F, 0.4F, 0.8F, false, true, true, mobspawnsettings$builder);
   }

   public static Biome modifiedJungleBiome() {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 10, 1, 1)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
      return baseJungleBiome(0.2F, 0.4F, 0.9F, false, false, true, mobspawnsettings$builder);
   }

   public static Biome jungleHillsBiome() {
      return jungleBiome(0.45F, 0.3F, 10, 1, 1);
   }

   public static Biome bambooJungleBiome() {
      return bambooJungleBiome(0.1F, 0.2F, 40, 2);
   }

   public static Biome bambooJungleHillsBiome() {
      return bambooJungleBiome(0.45F, 0.3F, 10, 1);
   }

   private static Biome jungleBiome(float p_127383_, float p_127384_, int p_127385_, int p_127386_, int p_127387_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, p_127385_, 1, p_127386_)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, p_127387_)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 1, 2));
      mobspawnsettings$builder.setPlayerCanSpawn();
      return baseJungleBiome(p_127383_, p_127384_, 0.9F, false, false, false, mobspawnsettings$builder);
   }

   private static Biome bambooJungleBiome(float p_127378_, float p_127379_, int p_127380_, int p_127381_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, p_127380_, 1, p_127381_)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 80, 1, 2)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
      return baseJungleBiome(p_127378_, p_127379_, 0.9F, true, false, false, mobspawnsettings$builder);
   }

   private static Biome baseJungleBiome(float p_127370_, float p_127371_, float p_127372_, boolean p_127373_, boolean p_127374_, boolean p_127375_, MobSpawnSettings.Builder p_127376_) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      if (!p_127374_ && !p_127375_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.JUNGLE_TEMPLE);
      }

      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_JUNGLE);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (p_127373_) {
         BiomeDefaultFeatures.addBambooVegetation(biomegenerationsettings$builder);
      } else {
         if (!p_127374_ && !p_127375_) {
            BiomeDefaultFeatures.addLightBambooVegetation(biomegenerationsettings$builder);
         }

         if (p_127374_) {
            BiomeDefaultFeatures.addJungleEdgeTrees(biomegenerationsettings$builder);
         } else {
            BiomeDefaultFeatures.addJungleTrees(biomegenerationsettings$builder);
         }
      }

      BiomeDefaultFeatures.addWarmFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addJungleGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addJungleExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.JUNGLE).depth(p_127370_).scale(p_127371_).temperature(0.95F).downfall(p_127372_).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.95F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(p_127376_.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome mountainBiome(float p_127389_, float p_127390_, ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> p_127391_, boolean p_127392_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 5, 4, 6));
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 10, 4, 6));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(p_127391_);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (p_127392_) {
         BiomeDefaultFeatures.addMountainEdgeTrees(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addMountainTrees(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraEmeralds(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addInfestedStone(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.EXTREME_HILLS).depth(p_127389_).scale(p_127390_).temperature(0.2F).downfall(0.3F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.2F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome desertBiome(float p_127408_, float p_127409_, boolean p_127410_, boolean p_127411_, boolean p_127412_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.desertSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.DESERT);
      if (p_127410_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_DESERT);
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      if (p_127411_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.DESERT_PYRAMID);
      }

      if (p_127412_) {
         BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_DESERT);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDesertLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDesertVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDesertExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDesertExtraDecoration(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.DESERT).depth(p_127408_).scale(p_127409_).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome plainsBiome(boolean p_127440_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.plainsSpawns(mobspawnsettings$builder);
      if (!p_127440_) {
         mobspawnsettings$builder.setPlayerCanSpawn();
      }

      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      if (!p_127440_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_PLAINS).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
      if (p_127440_) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUNFLOWER);
      }

      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings$builder);
      if (p_127440_) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      if (p_127440_) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
      } else {
         BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.PLAINS).depth(0.125F).scale(0.05F).temperature(0.8F).downfall(0.4F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static Biome baseEndBiome(BiomeGenerationSettings.Builder p_127421_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.endSpawns(mobspawnsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.THEEND).depth(0.1F).scale(0.2F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(10518688).skyColor(0).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(p_127421_.build()).build();
   }

   public static Biome endBarrensBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome theEndBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome endMidlandsBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END).addStructureStart(StructureFeatures.END_CITY);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome endHighlandsBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END).addStructureStart(StructureFeatures.END_CITY).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome smallEndIslandsBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END).addFeature(GenerationStep.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED);
      return baseEndBiome(biomegenerationsettings$builder);
   }

   public static Biome mushroomFieldsBiome(float p_127335_, float p_127336_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.mooshroomSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.MYCELIUM);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addMushroomFieldVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.MUSHROOM).depth(p_127335_).scale(p_127336_).temperature(0.9F).downfall(1.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.9F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static Biome baseSavannaBiome(float p_127363_, float p_127364_, float p_127365_, boolean p_127366_, boolean p_127367_, MobSpawnSettings.Builder p_127368_) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(p_127367_ ? SurfaceBuilders.SHATTERED_SAVANNA : SurfaceBuilders.GRASS);
      if (!p_127366_ && !p_127367_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_SAVANNA).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(p_127366_ ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      if (!p_127367_) {
         BiomeDefaultFeatures.addSavannaGrass(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (p_127367_) {
         BiomeDefaultFeatures.addShatteredSavannaTrees(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addShatteredSavannaGrass(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addSavannaTrees(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addWarmFlowers(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addSavannaExtraGrass(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.SAVANNA).depth(p_127363_).scale(p_127364_).temperature(p_127365_).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(p_127365_)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(p_127368_.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome savannaBiome(float p_127357_, float p_127358_, float p_127359_, boolean p_127360_, boolean p_127361_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = savannaMobs();
      return baseSavannaBiome(p_127357_, p_127358_, p_127359_, p_127360_, p_127361_, mobspawnsettings$builder);
   }

   private static MobSpawnSettings.Builder savannaMobs() {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 2, 6)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 1));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      return mobspawnsettings$builder;
   }

   public static Biome savanaPlateauBiome() {
      MobSpawnSettings.Builder mobspawnsettings$builder = savannaMobs();
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 8, 4, 4));
      return baseSavannaBiome(1.5F, 0.025F, 1.0F, true, false, mobspawnsettings$builder);
   }

   private static Biome baseBadlandsBiome(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> p_127429_, float p_127430_, float p_127431_, boolean p_127432_, boolean p_127433_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(p_127429_);
      BiomeDefaultFeatures.addDefaultOverworldLandMesaStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(p_127432_ ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addExtraGold(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (p_127433_) {
         BiomeDefaultFeatures.addBadlandsTrees(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addBadlandGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addBadlandExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.MESA).depth(p_127430_).scale(p_127431_).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).foliageColorOverride(10387789).grassColorOverride(9470285).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome badlandsBiome(float p_127446_, float p_127447_, boolean p_127448_) {
      return baseBadlandsBiome(SurfaceBuilders.BADLANDS, p_127446_, p_127447_, p_127448_, false);
   }

   public static Biome woodedBadlandsPlateauBiome(float p_127443_, float p_127444_) {
      return baseBadlandsBiome(SurfaceBuilders.WOODED_BADLANDS, p_127443_, p_127444_, true, true);
   }

   public static Biome erodedBadlandsBiome() {
      return baseBadlandsBiome(SurfaceBuilders.ERODED_BADLANDS, 0.1F, 0.2F, true, false);
   }

   private static Biome baseOceanBiome(MobSpawnSettings.Builder p_127423_, int p_127424_, int p_127425_, boolean p_127426_, BiomeGenerationSettings.Builder p_127427_) {
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.OCEAN).depth(p_127426_ ? -1.8F : -1.0F).scale(0.1F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(p_127424_).waterFogColor(p_127425_).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(p_127423_.build()).generationSettings(p_127427_.build()).build();
   }

   private static BiomeGenerationSettings.Builder baseOceanGeneration(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> p_127435_, boolean p_127436_, boolean p_127437_, boolean p_127438_) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(p_127435_);
      ConfiguredStructureFeature<?, ?> configuredstructurefeature = p_127437_ ? StructureFeatures.OCEAN_RUIN_WARM : StructureFeatures.OCEAN_RUIN_COLD;
      if (p_127438_) {
         if (p_127436_) {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
         }

         BiomeDefaultFeatures.addDefaultOverworldOceanStructures(biomegenerationsettings$builder);
         biomegenerationsettings$builder.addStructureStart(configuredstructurefeature);
      } else {
         biomegenerationsettings$builder.addStructureStart(configuredstructurefeature);
         if (p_127436_) {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
         }

         BiomeDefaultFeatures.addDefaultOverworldOceanStructures(biomegenerationsettings$builder);
      }

      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
      BiomeDefaultFeatures.addOceanCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder, true);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      return biomegenerationsettings$builder;
   }

   public static Biome coldOceanBiome(boolean p_127450_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 3, 4, 15);
      mobspawnsettings$builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5));
      boolean flag = !p_127450_;
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.GRASS, p_127450_, false, flag);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, p_127450_ ? Features.SEAGRASS_DEEP_COLD : Features.SEAGRASS_COLD);
      BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawnsettings$builder, 4020182, 329011, p_127450_, biomegenerationsettings$builder);
   }

   public static Biome oceanBiome(boolean p_127460_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 1, 4, 10);
      mobspawnsettings$builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.GRASS, p_127460_, false, true);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, p_127460_ ? Features.SEAGRASS_DEEP : Features.SEAGRASS_NORMAL);
      BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawnsettings$builder, 4159204, 329011, p_127460_, biomegenerationsettings$builder);
   }

   public static Biome lukeWarmOceanBiome(boolean p_127467_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      if (p_127467_) {
         BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 8, 4, 8);
      } else {
         BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 10, 2, 15);
      }

      mobspawnsettings$builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8)).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.OCEAN_SAND, p_127467_, true, false);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, p_127467_ ? Features.SEAGRASS_DEEP_WARM : Features.SEAGRASS_WARM);
      if (p_127467_) {
         BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addLukeWarmKelp(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawnsettings$builder, 4566514, 267827, p_127467_, biomegenerationsettings$builder);
   }

   public static Biome warmOceanBiome() {
      MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
      BiomeDefaultFeatures.warmOceanSpawns(mobspawnsettings$builder, 10, 4);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.FULL_SAND, false, true, false).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARM_OCEAN_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_WARM).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEA_PICKLE);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawnsettings$builder, 4445678, 270131, false, biomegenerationsettings$builder);
   }

   public static Biome deepWarmOceanBiome() {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.warmOceanSpawns(mobspawnsettings$builder, 5, 1);
      mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.FULL_SAND, true, true, false).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
      BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return baseOceanBiome(mobspawnsettings$builder, 4445678, 270131, true, biomegenerationsettings$builder);
   }

   public static Biome frozenOceanBiome(boolean p_127470_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
      float f = p_127470_ ? 0.5F : 0.0F;
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.FROZEN_OCEAN);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_RUIN_COLD);
      if (p_127470_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
      }

      BiomeDefaultFeatures.addDefaultOverworldOceanStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
      BiomeDefaultFeatures.addOceanCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addIcebergs(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addBlueIce(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder, true);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(p_127470_ ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW).biomeCategory(Biome.BiomeCategory.OCEAN).depth(p_127470_ ? -1.8F : -1.0F).scale(0.1F).temperature(f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static Biome baseForestBiome(float p_127398_, float p_127399_, boolean p_127400_, MobSpawnSettings.Builder p_127401_) {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      if (p_127400_) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
      } else {
         BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      if (p_127400_) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_TREES);
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_FOREST);
         BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addOtherBirchTrees(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
         BiomeDefaultFeatures.addForestGrass(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(p_127398_).scale(p_127399_).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(p_127401_.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   private static MobSpawnSettings.Builder defaultSpawns() {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      return mobspawnsettings$builder;
   }

   public static Biome forestBiome(float p_127453_, float p_127454_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = defaultSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4)).setPlayerCanSpawn();
      return baseForestBiome(p_127453_, p_127454_, false, mobspawnsettings$builder);
   }

   public static Biome flowerForestBiome() {
      MobSpawnSettings.Builder mobspawnsettings$builder = defaultSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      return baseForestBiome(0.1F, 0.4F, true, mobspawnsettings$builder);
   }

   public static Biome taigaBiome(float p_127414_, float p_127415_, boolean p_127416_, boolean p_127417_, boolean p_127418_, boolean p_127419_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
      if (!p_127416_ && !p_127417_) {
         mobspawnsettings$builder.setPlayerCanSpawn();
      }

      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      float f = p_127416_ ? -0.5F : 0.25F;
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      if (p_127418_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_TAIGA);
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      if (p_127419_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.IGLOO);
      }

      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(p_127417_ ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addFerns(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addTaigaTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addTaigaGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      if (p_127416_) {
         BiomeDefaultFeatures.addBerryBushes(biomegenerationsettings$builder);
      } else {
         BiomeDefaultFeatures.addSparseBerryBushes(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(p_127416_ ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.TAIGA).depth(p_127414_).scale(p_127415_).temperature(f).downfall(p_127416_ ? 0.4F : 0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(p_127416_ ? 4020182 : 4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome darkForestBiome(float p_127456_, float p_127457_, boolean p_127458_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.WOODLAND_MANSION);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, p_127458_ ? Features.DARK_FOREST_VEGETATION_RED : Features.DARK_FOREST_VEGETATION_BROWN);
      BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addForestGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(p_127456_).scale(p_127457_).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome swampBiome(float p_127463_, float p_127464_, boolean p_127465_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.SWAMP);
      if (!p_127465_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.SWAMP_HUT);
      }

      biomegenerationsettings$builder.addStructureStart(StructureFeatures.MINESHAFT);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      if (!p_127465_) {
         BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings$builder);
      }

      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSwampClayDisk(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSwampVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSwampExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      if (p_127465_) {
         BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings$builder);
      } else {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SWAMP);
      }

      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.SWAMP).depth(p_127463_).scale(p_127464_).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(6975545).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome tundraBiome(float p_127403_, float p_127404_, boolean p_127405_, boolean p_127406_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).creatureGenerationProbability(0.07F);
      BiomeDefaultFeatures.snowySpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(p_127405_ ? SurfaceBuilders.ICE_SPIKES : SurfaceBuilders.GRASS);
      if (!p_127405_ && !p_127406_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_SNOWY).addStructureStart(StructureFeatures.IGLOO);
      }

      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      if (!p_127405_ && !p_127406_) {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
      }

      biomegenerationsettings$builder.addStructureStart(p_127406_ ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      if (p_127405_) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_SPIKE);
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_PATCH);
      }

      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSnowyTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.SNOW).biomeCategory(Biome.BiomeCategory.ICY).depth(p_127403_).scale(p_127404_).temperature(0.0F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.0F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome riverBiome(float p_127346_, float p_127347_, float p_127348_, int p_127349_, boolean p_127350_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5));
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, p_127350_ ? 1 : 100, 1, 1));
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.MINESHAFT);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      if (!p_127350_) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_RIVER);
      }

      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(p_127350_ ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.RIVER).depth(p_127346_).scale(p_127347_).temperature(p_127348_).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(p_127349_).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(p_127348_)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome beachBiome(float p_127338_, float p_127339_, float p_127340_, float p_127341_, int p_127342_, boolean p_127343_, boolean p_127344_) {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      if (!p_127344_ && !p_127343_) {
         mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
      }

      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(p_127344_ ? SurfaceBuilders.STONE : SurfaceBuilders.DESERT);
      if (p_127344_) {
         BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      } else {
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.MINESHAFT);
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.BURIED_TREASURE);
         biomegenerationsettings$builder.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
      }

      biomegenerationsettings$builder.addStructureStart(p_127344_ ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(p_127343_ ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(p_127344_ ? Biome.BiomeCategory.NONE : Biome.BiomeCategory.BEACH).depth(p_127338_).scale(p_127339_).temperature(p_127340_).downfall(p_127341_).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(p_127342_).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(p_127340_)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome theVoidBiome() {
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.NOPE);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.VOID_START_PLATFORM);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NONE).depth(0.1F).scale(0.2F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome netherWastesBiome() {
      MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 15, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.NETHER).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
      BiomeDefaultFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(3344392).skyColor(calculateSkyColor(2.0F)).ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome soulSandValleyBiome() {
      double d0 = 0.7D;
      double d1 = 0.15D;
      MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 20, 5, 5)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).addMobCharge(EntityType.SKELETON, 0.7D, 0.15D).addMobCharge(EntityType.GHAST, 0.7D, 0.15D).addMobCharge(EntityType.ENDERMAN, 0.7D, 0.15D).addMobCharge(EntityType.STRIDER, 0.7D, 0.15D).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.SOUL_SAND_VALLEY).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.NETHER_FOSSIL).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addStructureStart(StructureFeatures.BASTION_REMNANT).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA).addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.BASALT_PILLAR).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_CRIMSON_ROOTS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_SOUL_SAND);
      BiomeDefaultFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(1787717).skyColor(calculateSkyColor(2.0F)).ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.00625F)).ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome basaltDeltasBiome() {
      MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 40, 1, 1)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.BASALT_DELTAS).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addStructureStart(StructureFeatures.NETHER_BRIDGE).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.DELTA).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA_DOUBLE).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.SMALL_BASALT_COLUMNS).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.LARGE_BASALT_COLUMNS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BASALT_BLOBS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BLACKSTONE_BLOBS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_DELTA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED_DOUBLE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_DELTAS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_DELTAS);
      BiomeDefaultFeatures.addAncientDebris(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(4341314).fogColor(6840176).skyColor(calculateSkyColor(2.0F)).ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F)).ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome crimsonForestBiome() {
      MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HOGLIN, 9, 3, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 5, 3, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.CRIMSON_FOREST).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WEEPING_VINES).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FUNGI).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FOREST_VEGETATION);
      BiomeDefaultFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(3343107).skyColor(calculateSkyColor(2.0F)).ambientParticle(new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.025F)).ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome warpedForestBiome() {
      MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).addMobCharge(EntityType.ENDERMAN, 1.0D, 0.12D).build();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.WARPED_FOREST).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FUNGI).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FOREST_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.NETHER_SPROUTS).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TWISTING_VINES);
      BiomeDefaultFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(1705242).skyColor(calculateSkyColor(2.0F)).ambientParticle(new AmbientParticleSettings(ParticleTypes.WARPED_SPORE, 0.01428F)).ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome lushCaves() {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addLushCavesSpecialOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addLushCavesVegetationFeatures(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.UNDERGROUND).depth(0.1F).scale(0.2F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }

   public static Biome dripstoneCaves() {
      MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
      BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
      BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
      biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
      BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
      BiomeDefaultFeatures.addDripstone(biomegenerationsettings$builder);
      return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.UNDERGROUND).depth(0.125F).scale(0.05F).temperature(0.8F).downfall(0.4F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
   }
}