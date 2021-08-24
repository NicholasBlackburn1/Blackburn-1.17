package net.minecraft.data.worldgen;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures {
   public static void addDefaultOverworldLandMesaStructures(BiomeGenerationSettings.Builder p_126733_) {
      p_126733_.addStructureStart(StructureFeatures.MINESHAFT_MESA);
      p_126733_.addStructureStart(StructureFeatures.STRONGHOLD);
   }

   public static void addDefaultOverworldLandStructures(BiomeGenerationSettings.Builder p_126778_) {
      p_126778_.addStructureStart(StructureFeatures.MINESHAFT);
      p_126778_.addStructureStart(StructureFeatures.STRONGHOLD);
   }

   public static void addDefaultOverworldOceanStructures(BiomeGenerationSettings.Builder p_126787_) {
      p_126787_.addStructureStart(StructureFeatures.MINESHAFT);
      p_126787_.addStructureStart(StructureFeatures.SHIPWRECK);
   }

   public static void addDefaultCarvers(BiomeGenerationSettings.Builder p_126791_) {
      p_126791_.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
      p_126791_.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
   }

   public static void addOceanCarvers(BiomeGenerationSettings.Builder p_126795_) {
      p_126795_.addCarver(GenerationStep.Carving.AIR, Carvers.OCEAN_CAVE);
      p_126795_.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
      p_126795_.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CANYON);
      p_126795_.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CAVE);
   }

   public static void addDefaultLakes(BiomeGenerationSettings.Builder p_126799_) {
      p_126799_.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
      p_126799_.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
   }

   public static void addDesertLakes(BiomeGenerationSettings.Builder p_126803_) {
      p_126803_.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
   }

   public static void addDefaultMonsterRoom(BiomeGenerationSettings.Builder p_126807_) {
      p_126807_.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.MONSTER_ROOM);
   }

   public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder p_126811_) {
      addDefaultUndergroundVariety(p_126811_, false);
   }

   public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder p_176855_, boolean p_176856_) {
      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIRT);
      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRAVEL);
      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRANITE);
      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIORITE);
      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_ANDESITE);
      if (!p_176856_) {
         p_176855_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.GLOW_LICHEN);
      }

      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_TUFF);
      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DEEPSLATE);
      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RARE_DRIPSTONE_CLUSTER_FEATURE);
      p_176855_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RARE_SMALL_DRIPSTONE_FEATURE);
   }

   public static void addDripstone(BiomeGenerationSettings.Builder p_176864_) {
      p_176864_.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.LARGE_DRIPSTONE_FEATURE);
      p_176864_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.DRIPSTONE_CLUSTER_FEATURE);
      p_176864_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SMALL_DRIPSTONE_FEATURE);
   }

   public static void addDefaultOres(BiomeGenerationSettings.Builder p_126815_) {
      p_126815_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COAL);
      p_126815_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_IRON);
      p_126815_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD);
      p_126815_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_REDSTONE);
      p_126815_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND);
      p_126815_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_LAPIS);
      p_126815_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COPPER);
   }

   public static void addExtraGold(BiomeGenerationSettings.Builder p_126817_) {
      p_126817_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD_EXTRA);
   }

   public static void addExtraEmeralds(BiomeGenerationSettings.Builder p_126819_) {
      p_126819_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_EMERALD);
   }

   public static void addInfestedStone(BiomeGenerationSettings.Builder p_126821_) {
      p_126821_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_INFESTED);
   }

   public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder p_126823_) {
      p_126823_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_SAND);
      p_126823_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
      p_126823_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_GRAVEL);
   }

   public static void addSwampClayDisk(BiomeGenerationSettings.Builder p_126825_) {
      p_126825_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
   }

   public static void addMossyStoneBlock(BiomeGenerationSettings.Builder p_126827_) {
      p_126827_.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.FOREST_ROCK);
   }

   public static void addFerns(BiomeGenerationSettings.Builder p_126829_) {
      p_126829_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_LARGE_FERN);
   }

   public static void addBerryBushes(BiomeGenerationSettings.Builder p_126831_) {
      p_126831_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_DECORATED);
   }

   public static void addSparseBerryBushes(BiomeGenerationSettings.Builder p_126833_) {
      p_126833_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_SPARSE);
   }

   public static void addLightBambooVegetation(BiomeGenerationSettings.Builder p_126835_) {
      p_126835_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_LIGHT);
   }

   public static void addBambooVegetation(BiomeGenerationSettings.Builder p_126837_) {
      p_126837_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO);
      p_126837_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_VEGETATION);
   }

   public static void addTaigaTrees(BiomeGenerationSettings.Builder p_126839_) {
      p_126839_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TAIGA_VEGETATION);
   }

   public static void addWaterTrees(BiomeGenerationSettings.Builder p_126841_) {
      p_126841_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_WATER);
   }

   public static void addBirchTrees(BiomeGenerationSettings.Builder p_126843_) {
      p_126843_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_BIRCH);
   }

   public static void addOtherBirchTrees(BiomeGenerationSettings.Builder p_126845_) {
      p_126845_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_OTHER);
   }

   public static void addTallBirchTrees(BiomeGenerationSettings.Builder p_126847_) {
      p_126847_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_TALL);
   }

   public static void addSavannaTrees(BiomeGenerationSettings.Builder p_126681_) {
      p_126681_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SAVANNA);
   }

   public static void addShatteredSavannaTrees(BiomeGenerationSettings.Builder p_126683_) {
      p_126683_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SHATTERED_SAVANNA);
   }

   public static void addLushCavesVegetationFeatures(BiomeGenerationSettings.Builder p_176851_) {
      p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_CEILING_VEGETATION);
      p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CAVE_VINES);
      p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_CLAY);
      p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_VEGETATION);
      p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.ROOTED_AZALEA_TREES);
      p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPORE_BLOSSOM_FEATURE);
      p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CLASSIC_VINES_CAVE_FEATURE);
   }

   public static void addLushCavesSpecialOres(BiomeGenerationSettings.Builder p_176853_) {
      p_176853_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_CLAY);
   }

   public static void addMountainTrees(BiomeGenerationSettings.Builder p_126685_) {
      p_126685_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN);
   }

   public static void addMountainEdgeTrees(BiomeGenerationSettings.Builder p_126687_) {
      p_126687_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN_EDGE);
   }

   public static void addJungleTrees(BiomeGenerationSettings.Builder p_126689_) {
      p_126689_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE);
   }

   public static void addJungleEdgeTrees(BiomeGenerationSettings.Builder p_126691_) {
      p_126691_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE_EDGE);
   }

   public static void addBadlandsTrees(BiomeGenerationSettings.Builder p_126693_) {
      p_126693_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_BADLANDS);
   }

   public static void addSnowyTrees(BiomeGenerationSettings.Builder p_126695_) {
      p_126695_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SNOWY);
   }

   public static void addJungleGrass(BiomeGenerationSettings.Builder p_126697_) {
      p_126697_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_JUNGLE);
   }

   public static void addSavannaGrass(BiomeGenerationSettings.Builder p_126699_) {
      p_126699_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS);
   }

   public static void addShatteredSavannaGrass(BiomeGenerationSettings.Builder p_126701_) {
      p_126701_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
   }

   public static void addSavannaExtraGrass(BiomeGenerationSettings.Builder p_126703_) {
      p_126703_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_SAVANNA);
   }

   public static void addBadlandGrass(BiomeGenerationSettings.Builder p_126705_) {
      p_126705_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
      p_126705_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_BADLANDS);
   }

   public static void addForestFlowers(BiomeGenerationSettings.Builder p_126707_) {
      p_126707_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION);
   }

   public static void addForestGrass(BiomeGenerationSettings.Builder p_126709_) {
      p_126709_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_FOREST);
   }

   public static void addSwampVegetation(BiomeGenerationSettings.Builder p_126711_) {
      p_126711_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SWAMP);
      p_126711_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_SWAMP);
      p_126711_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
      p_126711_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
      p_126711_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_WATERLILLY);
      p_126711_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_SWAMP);
      p_126711_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_SWAMP);
   }

   public static void addMushroomFieldVegetation(BiomeGenerationSettings.Builder p_126713_) {
      p_126713_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.MUSHROOM_FIELD_VEGETATION);
      p_126713_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
      p_126713_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
   }

   public static void addPlainVegetation(BiomeGenerationSettings.Builder p_126715_) {
      p_126715_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PLAIN_VEGETATION);
      p_126715_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_PLAIN_DECORATED);
      p_126715_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
   }

   public static void addDesertVegetation(BiomeGenerationSettings.Builder p_126717_) {
      p_126717_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_2);
   }

   public static void addGiantTaigaVegetation(BiomeGenerationSettings.Builder p_126719_) {
      p_126719_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA);
      p_126719_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
      p_126719_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_GIANT);
      p_126719_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_GIANT);
   }

   public static void addDefaultFlowers(BiomeGenerationSettings.Builder p_126721_) {
      p_126721_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_DEFAULT);
   }

   public static void addWarmFlowers(BiomeGenerationSettings.Builder p_126723_) {
      p_126723_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_WARM);
   }

   public static void addDefaultGrass(BiomeGenerationSettings.Builder p_126725_) {
      p_126725_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
   }

   public static void addTaigaGrass(BiomeGenerationSettings.Builder p_126727_) {
      p_126727_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA_2);
      p_126727_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
      p_126727_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
   }

   public static void addPlainGrass(BiomeGenerationSettings.Builder p_126729_) {
      p_126729_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS_2);
   }

   public static void addDefaultMushrooms(BiomeGenerationSettings.Builder p_126731_) {
      p_126731_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_NORMAL);
      p_126731_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_NORMAL);
   }

   public static void addDefaultExtraVegetation(BiomeGenerationSettings.Builder p_126746_) {
      p_126746_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
      p_126746_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
   }

   public static void addBadlandExtraVegetation(BiomeGenerationSettings.Builder p_126748_) {
      p_126748_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_BADLANDS);
      p_126748_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
      p_126748_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DECORATED);
   }

   public static void addJungleExtraVegetation(BiomeGenerationSettings.Builder p_126750_) {
      p_126750_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_MELON);
      p_126750_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.VINES);
   }

   public static void addDesertExtraVegetation(BiomeGenerationSettings.Builder p_126752_) {
      p_126752_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_DESERT);
      p_126752_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
      p_126752_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DESERT);
   }

   public static void addSwampExtraVegetation(BiomeGenerationSettings.Builder p_126754_) {
      p_126754_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_SWAMP);
      p_126754_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
   }

   public static void addDesertExtraDecoration(BiomeGenerationSettings.Builder p_126756_) {
      p_126756_.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.WELL);
   }

   public static void addFossilDecoration(BiomeGenerationSettings.Builder p_126758_) {
      p_126758_.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.FOSSIL);
   }

   public static void addColdOceanExtraVegetation(BiomeGenerationSettings.Builder p_126760_) {
      p_126760_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_COLD);
   }

   public static void addDefaultSeagrass(BiomeGenerationSettings.Builder p_126762_) {
      p_126762_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SIMPLE);
   }

   public static void addLukeWarmKelp(BiomeGenerationSettings.Builder p_126764_) {
      p_126764_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_WARM);
   }

   public static void addDefaultSprings(BiomeGenerationSettings.Builder p_126766_) {
      p_126766_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_WATER);
      p_126766_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
   }

   public static void addIcebergs(BiomeGenerationSettings.Builder p_126768_) {
      p_126768_.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_PACKED);
      p_126768_.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_BLUE);
   }

   public static void addBlueIce(BiomeGenerationSettings.Builder p_126770_) {
      p_126770_.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.BLUE_ICE);
   }

   public static void addSurfaceFreezing(BiomeGenerationSettings.Builder p_126772_) {
      p_126772_.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.FREEZE_TOP_LAYER);
   }

   public static void addNetherDefaultOres(BiomeGenerationSettings.Builder p_126774_) {
      p_126774_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GRAVEL_NETHER);
      p_126774_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_BLACKSTONE);
      p_126774_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_NETHER);
      p_126774_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_NETHER);
      addAncientDebris(p_126774_);
   }

   public static void addAncientDebris(BiomeGenerationSettings.Builder p_126776_) {
      p_126776_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_LARGE);
      p_126776_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_SMALL);
   }

   public static void addDefaultCrystalFormations(BiomeGenerationSettings.Builder p_176858_) {
      p_176858_.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.AMETHYST_GEODE);
   }

   public static void farmAnimals(MobSpawnSettings.Builder p_126735_) {
      p_126735_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 12, 4, 4));
      p_126735_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 4, 4));
      p_126735_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
      p_126735_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 4, 4));
   }

   public static void caveSpawns(MobSpawnSettings.Builder p_176860_) {
      p_176860_.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 8, 8));
      caveWaterSpawns(p_176860_);
   }

   public static void commonSpawns(MobSpawnSettings.Builder p_126789_) {
      caveSpawns(p_126789_);
      monsters(p_126789_, 95, 5, 100);
   }

   public static void caveWaterSpawns(MobSpawnSettings.Builder p_176862_) {
      p_176862_.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 4, 6));
      p_176862_.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 10, 4, 6));
   }

   public static void oceanSpawns(MobSpawnSettings.Builder p_126741_, int p_126742_, int p_126743_, int p_126744_) {
      p_126741_.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, p_126742_, 1, p_126743_));
      p_126741_.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, p_126744_, 3, 6));
      commonSpawns(p_126741_);
      p_126741_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
   }

   public static void warmOceanSpawns(MobSpawnSettings.Builder p_126737_, int p_126738_, int p_126739_) {
      p_126737_.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, p_126738_, p_126739_, 4));
      p_126737_.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
      p_126737_.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
      commonSpawns(p_126737_);
   }

   public static void plainsSpawns(MobSpawnSettings.Builder p_126793_) {
      farmAnimals(p_126793_);
      p_126793_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 5, 2, 6));
      p_126793_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 3));
      commonSpawns(p_126793_);
   }

   public static void snowySpawns(MobSpawnSettings.Builder p_126797_) {
      p_126797_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 10, 2, 3));
      p_126797_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
      caveSpawns(p_126797_);
      monsters(p_126797_, 95, 5, 20);
      p_126797_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 80, 4, 4));
   }

   public static void desertSpawns(MobSpawnSettings.Builder p_126801_) {
      p_126801_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
      caveSpawns(p_126801_);
      monsters(p_126801_, 19, 1, 100);
      p_126801_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 80, 4, 4));
   }

   public static void monsters(MobSpawnSettings.Builder p_126782_, int p_126783_, int p_126784_, int p_126785_) {
      p_126782_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4));
      p_126782_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, p_126783_, 4, 4));
      p_126782_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, p_126784_, 1, 1));
      p_126782_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, p_126785_, 4, 4));
      p_126782_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 100, 4, 4));
      p_126782_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 4, 4));
      p_126782_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
      p_126782_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
   }

   public static void mooshroomSpawns(MobSpawnSettings.Builder p_126805_) {
      p_126805_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 8, 4, 8));
      caveSpawns(p_126805_);
   }

   public static void baseJungleSpawns(MobSpawnSettings.Builder p_126809_) {
      farmAnimals(p_126809_);
      p_126809_.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
      commonSpawns(p_126809_);
   }

   public static void endSpawns(MobSpawnSettings.Builder p_126813_) {
      p_126813_.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
   }
}