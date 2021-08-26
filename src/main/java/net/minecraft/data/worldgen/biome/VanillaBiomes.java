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

public class VanillaBiomes
{
    private static int calculateSkyColor(float pTemperature)
    {
        float f = pTemperature / 3.0F;
        f = Mth.clamp(f, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1.0F);
    }

    public static Biome giantTreeTaiga(float pDepth, float pScale, float pTemperature, boolean pIsSpruceVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));

        if (pIsSpruceVariant)
        {
            BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        }
        else
        {
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
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsSpruceVariant ? Features.TREES_GIANT_SPRUCE : Features.TREES_GIANT);
        BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addGiantTaigaVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSparseBerryBushes(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.TAIGA).depth(pDepth).scale(pScale).temperature(pTemperature).downfall(0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome birchForestBiome(float pDepth, float pScale, boolean pIsTallVariant)
    {
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

        if (pIsTallVariant)
        {
            BiomeDefaultFeatures.addTallBirchTrees(biomegenerationsettings$builder);
        }
        else
        {
            BiomeDefaultFeatures.addBirchTrees(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addForestGrass(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(pDepth).scale(pScale).temperature(0.6F).downfall(0.6F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.6F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome jungleBiome()
    {
        return jungleBiome(0.1F, 0.2F, 40, 2, 3);
    }

    public static Biome jungleEdgeBiome()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
        return baseJungleBiome(0.1F, 0.2F, 0.8F, false, true, false, mobspawnsettings$builder);
    }

    public static Biome modifiedJungleEdgeBiome()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
        return baseJungleBiome(0.2F, 0.4F, 0.8F, false, true, true, mobspawnsettings$builder);
    }

    public static Biome modifiedJungleBiome()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 10, 1, 1)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
        return baseJungleBiome(0.2F, 0.4F, 0.9F, false, false, true, mobspawnsettings$builder);
    }

    public static Biome jungleHillsBiome()
    {
        return jungleBiome(0.45F, 0.3F, 10, 1, 1);
    }

    public static Biome bambooJungleBiome()
    {
        return bambooJungleBiome(0.1F, 0.2F, 40, 2);
    }

    public static Biome bambooJungleHillsBiome()
    {
        return bambooJungleBiome(0.45F, 0.3F, 10, 1);
    }

    private static Biome jungleBiome(float pDepth, float pScale, int pParrotWeight, int pParrotMaxCount, int pOcelotMaxCount)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, pParrotWeight, 1, pParrotMaxCount)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, pOcelotMaxCount)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 1, 2));
        mobspawnsettings$builder.setPlayerCanSpawn();
        return baseJungleBiome(pDepth, pScale, 0.9F, false, false, false, mobspawnsettings$builder);
    }

    private static Biome bambooJungleBiome(float pDepth, float pScale, int pParrotWeight, int pParrotMaxCount)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, pParrotWeight, 1, pParrotMaxCount)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 80, 1, 2)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
        return baseJungleBiome(pDepth, pScale, 0.9F, true, false, false, mobspawnsettings$builder);
    }

    private static Biome baseJungleBiome(float pDepth, float pScale, float pDownfall, boolean pHasOnlyBambooVegetation, boolean pIsEdgeBiome, boolean pIsModified, MobSpawnSettings.Builder pMobSpawnBuilder)
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);

        if (!pIsEdgeBiome && !pIsModified)
        {
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

        if (pHasOnlyBambooVegetation)
        {
            BiomeDefaultFeatures.addBambooVegetation(biomegenerationsettings$builder);
        }
        else
        {
            if (!pIsEdgeBiome && !pIsModified)
            {
                BiomeDefaultFeatures.addLightBambooVegetation(biomegenerationsettings$builder);
            }

            if (pIsEdgeBiome)
            {
                BiomeDefaultFeatures.addJungleEdgeTrees(biomegenerationsettings$builder);
            }
            else
            {
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
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.JUNGLE).depth(pDepth).scale(pScale).temperature(0.95F).downfall(pDownfall).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.95F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(pMobSpawnBuilder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome mountainBiome(float pDepth, float pScale, ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> pSurfaceBuilder, boolean pIsEdgeBiome)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 5, 4, 6));
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 10, 4, 6));
        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pSurfaceBuilder);
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
        BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);

        if (pIsEdgeBiome)
        {
            BiomeDefaultFeatures.addMountainEdgeTrees(biomegenerationsettings$builder);
        }
        else
        {
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
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.EXTREME_HILLS).depth(pDepth).scale(pScale).temperature(0.2F).downfall(0.3F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.2F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome desertBiome(float pDepth, float pScale, boolean pHasVillageAndOutpost, boolean pHasDesertPyramid, boolean pHasFossils)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.desertSpawns(mobspawnsettings$builder);
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.DESERT);

        if (pHasVillageAndOutpost)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_DESERT);
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        if (pHasDesertPyramid)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.DESERT_PYRAMID);
        }

        if (pHasFossils)
        {
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
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.DESERT).depth(pDepth).scale(pScale).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome plainsBiome(boolean pIsSunflowerVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.plainsSpawns(mobspawnsettings$builder);

        if (!pIsSunflowerVariant)
        {
            mobspawnsettings$builder.setPlayerCanSpawn();
        }

        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);

        if (!pIsSunflowerVariant)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_PLAINS).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addPlainGrass(biomegenerationsettings$builder);

        if (pIsSunflowerVariant)
        {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUNFLOWER);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addPlainVegetation(biomegenerationsettings$builder);

        if (pIsSunflowerVariant)
        {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
        }

        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);

        if (pIsSunflowerVariant)
        {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        }
        else
        {
            BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.PLAINS).depth(0.125F).scale(0.05F).temperature(0.8F).downfall(0.4F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    private static Biome baseEndBiome(BiomeGenerationSettings.Builder pGenerationSettingsBuilder)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.endSpawns(mobspawnsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.THEEND).depth(0.1F).scale(0.2F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(10518688).skyColor(0).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(pGenerationSettingsBuilder.build()).build();
    }

    public static Biome endBarrensBiome()
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome theEndBiome()
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome endMidlandsBiome()
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END).addStructureStart(StructureFeatures.END_CITY);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome endHighlandsBiome()
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END).addStructureStart(StructureFeatures.END_CITY).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome smallEndIslandsBiome()
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.END).addFeature(GenerationStep.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED);
        return baseEndBiome(biomegenerationsettings$builder);
    }

    public static Biome mushroomFieldsBiome(float pDepth, float pScale)
    {
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
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.MUSHROOM).depth(pDepth).scale(pScale).temperature(0.9F).downfall(1.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.9F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    private static Biome baseSavannaBiome(float pDepth, float pScale, float pTemperature, boolean pIsHighland, boolean pIsShatteredSavanna, MobSpawnSettings.Builder pMobSpawnBuilder)
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pIsShatteredSavanna ? SurfaceBuilders.SHATTERED_SAVANNA : SurfaceBuilders.GRASS);

        if (!pIsHighland && !pIsShatteredSavanna)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_SAVANNA).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addStructureStart(pIsHighland ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);

        if (!pIsShatteredSavanna)
        {
            BiomeDefaultFeatures.addSavannaGrass(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);

        if (pIsShatteredSavanna)
        {
            BiomeDefaultFeatures.addShatteredSavannaTrees(biomegenerationsettings$builder);
            BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
            BiomeDefaultFeatures.addShatteredSavannaGrass(biomegenerationsettings$builder);
        }
        else
        {
            BiomeDefaultFeatures.addSavannaTrees(biomegenerationsettings$builder);
            BiomeDefaultFeatures.addWarmFlowers(biomegenerationsettings$builder);
            BiomeDefaultFeatures.addSavannaExtraGrass(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.SAVANNA).depth(pDepth).scale(pScale).temperature(pTemperature).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(pMobSpawnBuilder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome savannaBiome(float pDepth, float pScale, float pTemperature, boolean pIsHighland, boolean pIsShatteredSavanna)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = savannaMobs();
        return baseSavannaBiome(pDepth, pScale, pTemperature, pIsHighland, pIsShatteredSavanna, mobspawnsettings$builder);
    }

    private static MobSpawnSettings.Builder savannaMobs()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 2, 6)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 1));
        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        return mobspawnsettings$builder;
    }

    public static Biome savanaPlateauBiome()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = savannaMobs();
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 8, 4, 4));
        return baseSavannaBiome(1.5F, 0.025F, 1.0F, true, false, mobspawnsettings$builder);
    }

    private static Biome baseBadlandsBiome(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> pSurfaceBuilder, float pDepth, float pScale, boolean pIsHighland, boolean pHasOakTrees)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pSurfaceBuilder);
        BiomeDefaultFeatures.addDefaultOverworldLandMesaStructures(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addStructureStart(pIsHighland ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addExtraGold(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);

        if (pHasOakTrees)
        {
            BiomeDefaultFeatures.addBadlandsTrees(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addBadlandGrass(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addBadlandExtraVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.MESA).depth(pDepth).scale(pScale).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(2.0F)).foliageColorOverride(10387789).grassColorOverride(9470285).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome badlandsBiome(float pDepth, float pScale, boolean pIsHighland)
    {
        return baseBadlandsBiome(SurfaceBuilders.BADLANDS, pDepth, pScale, pIsHighland, false);
    }

    public static Biome woodedBadlandsPlateauBiome(float pDepth, float pScale)
    {
        return baseBadlandsBiome(SurfaceBuilders.WOODED_BADLANDS, pDepth, pScale, true, true);
    }

    public static Biome erodedBadlandsBiome()
    {
        return baseBadlandsBiome(SurfaceBuilders.ERODED_BADLANDS, 0.1F, 0.2F, true, false);
    }

    private static Biome baseOceanBiome(MobSpawnSettings.Builder pMobSpawnBuilder, int pWaterColor, int pWaterFogColor, boolean pIsDeepVariant, BiomeGenerationSettings.Builder pGenerationSettingsBuilder)
    {
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.OCEAN).depth(pIsDeepVariant ? -1.8F : -1.0F).scale(0.1F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(pWaterColor).waterFogColor(pWaterFogColor).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(pMobSpawnBuilder.build()).generationSettings(pGenerationSettingsBuilder.build()).build();
    }

    private static BiomeGenerationSettings.Builder baseOceanGeneration(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> pSurfaceBuilder, boolean pHasOceanMonument, boolean pIsWarmOcean, boolean pIsDeepVariant)
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pSurfaceBuilder);
        ConfiguredStructureFeature <? , ? > configuredstructurefeature = pIsWarmOcean ? StructureFeatures.OCEAN_RUIN_WARM : StructureFeatures.OCEAN_RUIN_COLD;

        if (pIsDeepVariant)
        {
            if (pHasOceanMonument)
            {
                biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
            }

            BiomeDefaultFeatures.addDefaultOverworldOceanStructures(biomegenerationsettings$builder);
            biomegenerationsettings$builder.addStructureStart(configuredstructurefeature);
        }
        else
        {
            biomegenerationsettings$builder.addStructureStart(configuredstructurefeature);

            if (pHasOceanMonument)
            {
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

    public static Biome coldOceanBiome(boolean pIsDeepVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 3, 4, 15);
        mobspawnsettings$builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5));
        boolean flag = !pIsDeepVariant;
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.GRASS, pIsDeepVariant, false, flag);
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsDeepVariant ? Features.SEAGRASS_DEEP_COLD : Features.SEAGRASS_COLD);
        BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return baseOceanBiome(mobspawnsettings$builder, 4020182, 329011, pIsDeepVariant, biomegenerationsettings$builder);
    }

    public static Biome oceanBiome(boolean pIsDeepVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 1, 4, 10);
        mobspawnsettings$builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.GRASS, pIsDeepVariant, false, true);
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsDeepVariant ? Features.SEAGRASS_DEEP : Features.SEAGRASS_NORMAL);
        BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return baseOceanBiome(mobspawnsettings$builder, 4159204, 329011, pIsDeepVariant, biomegenerationsettings$builder);
    }

    public static Biome lukeWarmOceanBiome(boolean pIsDeepVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();

        if (pIsDeepVariant)
        {
            BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 8, 4, 8);
        }
        else
        {
            BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 10, 2, 15);
        }

        mobspawnsettings$builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8)).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.OCEAN_SAND, pIsDeepVariant, true, false);
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsDeepVariant ? Features.SEAGRASS_DEEP_WARM : Features.SEAGRASS_WARM);

        if (pIsDeepVariant)
        {
            BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addLukeWarmKelp(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return baseOceanBiome(mobspawnsettings$builder, 4566514, 267827, pIsDeepVariant, biomegenerationsettings$builder);
    }

    public static Biome warmOceanBiome()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
        BiomeDefaultFeatures.warmOceanSpawns(mobspawnsettings$builder, 10, 4);
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.FULL_SAND, false, true, false).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARM_OCEAN_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_WARM).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEA_PICKLE);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return baseOceanBiome(mobspawnsettings$builder, 4445678, 270131, false, biomegenerationsettings$builder);
    }

    public static Biome deepWarmOceanBiome()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.warmOceanSpawns(mobspawnsettings$builder, 5, 1);
        mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = baseOceanGeneration(SurfaceBuilders.FULL_SAND, true, true, false).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
        BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return baseOceanBiome(mobspawnsettings$builder, 4445678, 270131, true, biomegenerationsettings$builder);
    }

    public static Biome frozenOceanBiome(boolean pIsDeepVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
        float f = pIsDeepVariant ? 0.5F : 0.0F;
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.FROZEN_OCEAN);
        biomegenerationsettings$builder.addStructureStart(StructureFeatures.OCEAN_RUIN_COLD);

        if (pIsDeepVariant)
        {
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
        return (new Biome.BiomeBuilder()).precipitation(pIsDeepVariant ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW).biomeCategory(Biome.BiomeCategory.OCEAN).depth(pIsDeepVariant ? -1.8F : -1.0F).scale(0.1F).temperature(f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    private static Biome baseForestBiome(float pDepth, float pScale, boolean pIsFlowerForestVariant, MobSpawnSettings.Builder pMobSpawnBuilder)
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);

        if (pIsFlowerForestVariant)
        {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
        }
        else
        {
            BiomeDefaultFeatures.addForestFlowers(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);

        if (pIsFlowerForestVariant)
        {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_TREES);
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_FOREST);
            BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
        }
        else
        {
            BiomeDefaultFeatures.addOtherBirchTrees(biomegenerationsettings$builder);
            BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
            BiomeDefaultFeatures.addForestGrass(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSprings(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(pDepth).scale(pScale).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(pMobSpawnBuilder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    private static MobSpawnSettings.Builder defaultSpawns()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        return mobspawnsettings$builder;
    }

    public static Biome forestBiome(float pDepth, float pScale)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = defaultSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4)).setPlayerCanSpawn();
        return baseForestBiome(pDepth, pScale, false, mobspawnsettings$builder);
    }

    public static Biome flowerForestBiome()
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = defaultSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        return baseForestBiome(0.1F, 0.4F, true, mobspawnsettings$builder);
    }

    public static Biome taigaBiome(float pDepth, float pScale, boolean pIsSnowyVariant, boolean pIsMountainVariant, boolean pHasVillageAndOutpost, boolean pHasIgloos)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));

        if (!pIsSnowyVariant && !pIsMountainVariant)
        {
            mobspawnsettings$builder.setPlayerCanSpawn();
        }

        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        float f = pIsSnowyVariant ? -0.5F : 0.25F;
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.GRASS);

        if (pHasVillageAndOutpost)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_TAIGA);
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        if (pHasIgloos)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.IGLOO);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addStructureStart(pIsMountainVariant ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
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

        if (pIsSnowyVariant)
        {
            BiomeDefaultFeatures.addBerryBushes(biomegenerationsettings$builder);
        }
        else
        {
            BiomeDefaultFeatures.addSparseBerryBushes(biomegenerationsettings$builder);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(pIsSnowyVariant ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.TAIGA).depth(pDepth).scale(pScale).temperature(f).downfall(pIsSnowyVariant ? 0.4F : 0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(pIsSnowyVariant ? 4020182 : 4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome darkForestBiome(float pDepth, float pScale, boolean pIsHillsVariant)
    {
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
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, pIsHillsVariant ? Features.DARK_FOREST_VEGETATION_RED : Features.DARK_FOREST_VEGETATION_BROWN);
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
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.FOREST).depth(pDepth).scale(pScale).temperature(0.7F).downfall(0.8F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.7F)).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome swampBiome(float pDepth, float pScale, boolean pIsHillsVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(mobspawnsettings$builder);
        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.SWAMP);

        if (!pIsHillsVariant)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.SWAMP_HUT);
        }

        biomegenerationsettings$builder.addStructureStart(StructureFeatures.MINESHAFT);
        biomegenerationsettings$builder.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
        BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);

        if (!pIsHillsVariant)
        {
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

        if (pIsHillsVariant)
        {
            BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings$builder);
        }
        else
        {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SWAMP);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.SWAMP).depth(pDepth).scale(pScale).temperature(0.8F).downfall(0.9F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(calculateSkyColor(0.8F)).foliageColorOverride(6975545).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome tundraBiome(float pDepth, float pScale, boolean pIsIceSpikesBiome, boolean pIsMountainVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).creatureGenerationProbability(0.07F);
        BiomeDefaultFeatures.snowySpawns(mobspawnsettings$builder);
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pIsIceSpikesBiome ? SurfaceBuilders.ICE_SPIKES : SurfaceBuilders.GRASS);

        if (!pIsIceSpikesBiome && !pIsMountainVariant)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.VILLAGE_SNOWY).addStructureStart(StructureFeatures.IGLOO);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);

        if (!pIsIceSpikesBiome && !pIsMountainVariant)
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        biomegenerationsettings$builder.addStructureStart(pIsMountainVariant ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultLakes(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomegenerationsettings$builder);

        if (pIsIceSpikesBiome)
        {
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
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.SNOW).biomeCategory(Biome.BiomeCategory.ICY).depth(pDepth).scale(pScale).temperature(0.0F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.0F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome riverBiome(float pDepth, float pScale, float pTemperature, int pWaterColor, boolean pIsSnowy)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5));
        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        mobspawnsettings$builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, pIsSnowy ? 1 : 100, 1, 1));
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

        if (!pIsSnowy)
        {
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_RIVER);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(pIsSnowy ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(Biome.BiomeCategory.RIVER).depth(pDepth).scale(pScale).temperature(pTemperature).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(pWaterColor).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome beachBiome(float pDepth, float pScale, float pTemperature, float pDownfall, int pWaterColor, boolean pIsColdBiome, boolean pIsStoneVariant)
    {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();

        if (!pIsStoneVariant && !pIsColdBiome)
        {
            mobspawnsettings$builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
        }

        BiomeDefaultFeatures.commonSpawns(mobspawnsettings$builder);
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(pIsStoneVariant ? SurfaceBuilders.STONE : SurfaceBuilders.DESERT);

        if (pIsStoneVariant)
        {
            BiomeDefaultFeatures.addDefaultOverworldLandStructures(biomegenerationsettings$builder);
        }
        else
        {
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.MINESHAFT);
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.BURIED_TREASURE);
            biomegenerationsettings$builder.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
        }

        biomegenerationsettings$builder.addStructureStart(pIsStoneVariant ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
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
        return (new Biome.BiomeBuilder()).precipitation(pIsColdBiome ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN).biomeCategory(pIsStoneVariant ? Biome.BiomeCategory.NONE : Biome.BiomeCategory.BEACH).depth(pDepth).scale(pScale).temperature(pTemperature).downfall(pDownfall).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(pWaterColor).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(pTemperature)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(mobspawnsettings$builder.build()).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome theVoidBiome()
    {
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.NOPE);
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.VOID_START_PLATFORM);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NONE).depth(0.1F).scale(0.2F).temperature(0.5F).downfall(0.5F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome netherWastesBiome()
    {
        MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 15, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.NETHER).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
        BiomeDefaultFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(3344392).skyColor(calculateSkyColor(2.0F)).ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome soulSandValleyBiome()
    {
        double d0 = 0.7D;
        double d1 = 0.15D;
        MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 20, 5, 5)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).addMobCharge(EntityType.SKELETON, 0.7D, 0.15D).addMobCharge(EntityType.GHAST, 0.7D, 0.15D).addMobCharge(EntityType.ENDERMAN, 0.7D, 0.15D).addMobCharge(EntityType.STRIDER, 0.7D, 0.15D).build();
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.SOUL_SAND_VALLEY).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.NETHER_FOSSIL).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addStructureStart(StructureFeatures.BASTION_REMNANT).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA).addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.BASALT_PILLAR).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_CRIMSON_ROOTS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_SOUL_SAND);
        BiomeDefaultFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(1787717).skyColor(calculateSkyColor(2.0F)).ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.00625F)).ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome basaltDeltasBiome()
    {
        MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 40, 1, 1)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.BASALT_DELTAS).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addStructureStart(StructureFeatures.NETHER_BRIDGE).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.DELTA).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA_DOUBLE).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.SMALL_BASALT_COLUMNS).addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.LARGE_BASALT_COLUMNS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BASALT_BLOBS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BLACKSTONE_BLOBS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_DELTA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED_DOUBLE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_DELTAS).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_DELTAS);
        BiomeDefaultFeatures.addAncientDebris(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(4341314).fogColor(6840176).skyColor(calculateSkyColor(2.0F)).ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F)).ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome crimsonForestBiome()
    {
        MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HOGLIN, 9, 3, 4)).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 5, 3, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).build();
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.CRIMSON_FOREST).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WEEPING_VINES).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FUNGI).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FOREST_VEGETATION);
        BiomeDefaultFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(3343107).skyColor(calculateSkyColor(2.0F)).ambientParticle(new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.025F)).ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome warpedForestBiome()
    {
        MobSpawnSettings mobspawnsettings = (new MobSpawnSettings.Builder()).addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4)).addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2)).addMobCharge(EntityType.ENDERMAN, 1.0D, 0.12D).build();
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(SurfaceBuilders.WARPED_FOREST).addStructureStart(StructureFeatures.NETHER_BRIDGE).addStructureStart(StructureFeatures.BASTION_REMNANT).addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER).addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA).addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FUNGI).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FOREST_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.NETHER_SPROUTS).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TWISTING_VINES);
        BiomeDefaultFeatures.addNetherDefaultOres(biomegenerationsettings$builder);
        return (new Biome.BiomeBuilder()).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NETHER).depth(0.1F).scale(0.2F).temperature(2.0F).downfall(0.0F).specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011).fogColor(1705242).skyColor(calculateSkyColor(2.0F)).ambientParticle(new AmbientParticleSettings(ParticleTypes.WARPED_SPORE, 0.01428F)).ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP).ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0D)).ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111D)).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST)).build()).mobSpawnSettings(mobspawnsettings).generationSettings(biomegenerationsettings$builder.build()).build();
    }

    public static Biome lushCaves()
    {
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

    public static Biome dripstoneCaves()
    {
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
