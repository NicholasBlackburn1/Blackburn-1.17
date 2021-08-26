package net.minecraft.data.worldgen;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures
{
    public static void addDefaultOverworldLandMesaStructures(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addStructureStart(StructureFeatures.MINESHAFT_MESA);
        pBuilder.addStructureStart(StructureFeatures.STRONGHOLD);
    }

    public static void addDefaultOverworldLandStructures(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addStructureStart(StructureFeatures.MINESHAFT);
        pBuilder.addStructureStart(StructureFeatures.STRONGHOLD);
    }

    public static void addDefaultOverworldOceanStructures(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addStructureStart(StructureFeatures.MINESHAFT);
        pBuilder.addStructureStart(StructureFeatures.SHIPWRECK);
    }

    public static void addDefaultCarvers(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
        pBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
    }

    public static void addOceanCarvers(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.OCEAN_CAVE);
        pBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
        pBuilder.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CANYON);
        pBuilder.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CAVE);
    }

    public static void addDefaultLakes(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
        pBuilder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
    }

    public static void addDesertLakes(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
    }

    public static void addDefaultMonsterRoom(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.MONSTER_ROOM);
    }

    public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder pBuilder)
    {
        addDefaultUndergroundVariety(pBuilder, false);
    }

    public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder pBuilder, boolean p_176856_)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIRT);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRAVEL);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRANITE);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIORITE);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_ANDESITE);

        if (!p_176856_)
        {
            pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.GLOW_LICHEN);
        }

        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_TUFF);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DEEPSLATE);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RARE_DRIPSTONE_CLUSTER_FEATURE);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RARE_SMALL_DRIPSTONE_FEATURE);
    }

    public static void addDripstone(BiomeGenerationSettings.Builder p_176864_)
    {
        p_176864_.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.LARGE_DRIPSTONE_FEATURE);
        p_176864_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.DRIPSTONE_CLUSTER_FEATURE);
        p_176864_.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SMALL_DRIPSTONE_FEATURE);
    }

    public static void addDefaultOres(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COAL);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_IRON);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_REDSTONE);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_LAPIS);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COPPER);
    }

    public static void addExtraGold(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD_EXTRA);
    }

    public static void addExtraEmeralds(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_EMERALD);
    }

    public static void addInfestedStone(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_INFESTED);
    }

    public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_SAND);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_GRAVEL);
    }

    public static void addSwampClayDisk(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
    }

    public static void addMossyStoneBlock(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.FOREST_ROCK);
    }

    public static void addFerns(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_LARGE_FERN);
    }

    public static void addBerryBushes(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_DECORATED);
    }

    public static void addSparseBerryBushes(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_SPARSE);
    }

    public static void addLightBambooVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_LIGHT);
    }

    public static void addBambooVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_VEGETATION);
    }

    public static void addTaigaTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TAIGA_VEGETATION);
    }

    public static void addWaterTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_WATER);
    }

    public static void addBirchTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_BIRCH);
    }

    public static void addOtherBirchTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_OTHER);
    }

    public static void addTallBirchTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_TALL);
    }

    public static void addSavannaTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SAVANNA);
    }

    public static void addShatteredSavannaTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SHATTERED_SAVANNA);
    }

    public static void addLushCavesVegetationFeatures(BiomeGenerationSettings.Builder p_176851_)
    {
        p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_CEILING_VEGETATION);
        p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CAVE_VINES);
        p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_CLAY);
        p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_VEGETATION);
        p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.ROOTED_AZALEA_TREES);
        p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPORE_BLOSSOM_FEATURE);
        p_176851_.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CLASSIC_VINES_CAVE_FEATURE);
    }

    public static void addLushCavesSpecialOres(BiomeGenerationSettings.Builder p_176853_)
    {
        p_176853_.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_CLAY);
    }

    public static void addMountainTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN);
    }

    public static void addMountainEdgeTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN_EDGE);
    }

    public static void addJungleTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE);
    }

    public static void addJungleEdgeTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE_EDGE);
    }

    public static void addBadlandsTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_BADLANDS);
    }

    public static void addSnowyTrees(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SNOWY);
    }

    public static void addJungleGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_JUNGLE);
    }

    public static void addSavannaGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS);
    }

    public static void addShatteredSavannaGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
    }

    public static void addSavannaExtraGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_SAVANNA);
    }

    public static void addBadlandGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_BADLANDS);
    }

    public static void addForestFlowers(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION);
    }

    public static void addForestGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_FOREST);
    }

    public static void addSwampVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SWAMP);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_SWAMP);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_WATERLILLY);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_SWAMP);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_SWAMP);
    }

    public static void addMushroomFieldVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.MUSHROOM_FIELD_VEGETATION);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PLAIN_VEGETATION);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_PLAIN_DECORATED);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
    }

    public static void addDesertVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_2);
    }

    public static void addGiantTaigaVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_GIANT);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_GIANT);
    }

    public static void addDefaultFlowers(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_DEFAULT);
    }

    public static void addWarmFlowers(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_WARM);
    }

    public static void addDefaultGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
    }

    public static void addTaigaGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA_2);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainGrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS_2);
    }

    public static void addDefaultMushrooms(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_NORMAL);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_NORMAL);
    }

    public static void addDefaultExtraVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
    }

    public static void addBadlandExtraVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_BADLANDS);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DECORATED);
    }

    public static void addJungleExtraVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_MELON);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.VINES);
    }

    public static void addDesertExtraVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_DESERT);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DESERT);
    }

    public static void addSwampExtraVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_SWAMP);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
    }

    public static void addDesertExtraDecoration(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.WELL);
    }

    public static void addFossilDecoration(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.FOSSIL);
    }

    public static void addColdOceanExtraVegetation(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_COLD);
    }

    public static void addDefaultSeagrass(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SIMPLE);
    }

    public static void addLukeWarmKelp(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_WARM);
    }

    public static void addDefaultSprings(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_WATER);
        pBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
    }

    public static void addIcebergs(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_PACKED);
        pBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_BLUE);
    }

    public static void addBlueIce(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.BLUE_ICE);
    }

    public static void addSurfaceFreezing(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.FREEZE_TOP_LAYER);
    }

    public static void addNetherDefaultOres(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GRAVEL_NETHER);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_BLACKSTONE);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_NETHER);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_NETHER);
        addAncientDebris(pBuilder);
    }

    public static void addAncientDebris(BiomeGenerationSettings.Builder pBuilder)
    {
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_LARGE);
        pBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_SMALL);
    }

    public static void addDefaultCrystalFormations(BiomeGenerationSettings.Builder p_176858_)
    {
        p_176858_.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.AMETHYST_GEODE);
    }

    public static void farmAnimals(MobSpawnSettings.Builder pBuilder)
    {
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 12, 4, 4));
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 4, 4));
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 4, 4));
    }

    public static void caveSpawns(MobSpawnSettings.Builder p_176860_)
    {
        p_176860_.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 8, 8));
        caveWaterSpawns(p_176860_);
    }

    public static void commonSpawns(MobSpawnSettings.Builder pBuilder)
    {
        caveSpawns(pBuilder);
        monsters(pBuilder, 95, 5, 100);
    }

    public static void caveWaterSpawns(MobSpawnSettings.Builder p_176862_)
    {
        p_176862_.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 4, 6));
        p_176862_.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 10, 4, 6));
    }

    public static void oceanSpawns(MobSpawnSettings.Builder pBuilder, int pSquidWeight, int pSquidMaxCount, int pCodWeight)
    {
        pBuilder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, pSquidWeight, 1, pSquidMaxCount));
        pBuilder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, pCodWeight, 3, 6));
        commonSpawns(pBuilder);
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
    }

    public static void warmOceanSpawns(MobSpawnSettings.Builder pBuilder, int pSquidWeight, int pSquidMinCount)
    {
        pBuilder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, pSquidWeight, pSquidMinCount, 4));
        pBuilder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
        pBuilder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
        commonSpawns(pBuilder);
    }

    public static void plainsSpawns(MobSpawnSettings.Builder pBuilder)
    {
        farmAnimals(pBuilder);
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 5, 2, 6));
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 3));
        commonSpawns(pBuilder);
    }

    public static void snowySpawns(MobSpawnSettings.Builder pBuilder)
    {
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 10, 2, 3));
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        caveSpawns(pBuilder);
        monsters(pBuilder, 95, 5, 20);
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 80, 4, 4));
    }

    public static void desertSpawns(MobSpawnSettings.Builder pBuilder)
    {
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        caveSpawns(pBuilder);
        monsters(pBuilder, 19, 1, 100);
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 80, 4, 4));
    }

    public static void monsters(MobSpawnSettings.Builder pBuilder, int pZombieWeight, int pZombieVillagerWeight, int pSkeletonWeight)
    {
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4));
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, pZombieWeight, 4, 4));
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, pZombieVillagerWeight, 1, 1));
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, pSkeletonWeight, 4, 4));
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 100, 4, 4));
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 4, 4));
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
    }

    public static void mooshroomSpawns(MobSpawnSettings.Builder pBuilder)
    {
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 8, 4, 8));
        caveSpawns(pBuilder);
    }

    public static void baseJungleSpawns(MobSpawnSettings.Builder pBuilder)
    {
        farmAnimals(pBuilder);
        pBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        commonSpawns(pBuilder);
    }

    public static void endSpawns(MobSpawnSettings.Builder pBuilder)
    {
        pBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
    }
}
