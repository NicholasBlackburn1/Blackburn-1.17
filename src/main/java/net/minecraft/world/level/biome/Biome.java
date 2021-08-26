package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Biome
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create((p_47527_) ->
    {
        return p_47527_.group(Biome.ClimateSettings.CODEC.forGetter((p_151717_) -> {
            return p_151717_.climateSettings;
        }), Biome.BiomeCategory.CODEC.fieldOf("category").forGetter((p_151715_) -> {
            return p_151715_.biomeCategory;
        }), Codec.FLOAT.fieldOf("depth").forGetter((p_151713_) -> {
            return p_151713_.depth;
        }), Codec.FLOAT.fieldOf("scale").forGetter((p_151711_) -> {
            return p_151711_.scale;
        }), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter((p_151709_) -> {
            return p_151709_.specialEffects;
        }), BiomeGenerationSettings.CODEC.forGetter((p_151707_) -> {
            return p_151707_.generationSettings;
        }), MobSpawnSettings.CODEC.forGetter((p_151705_) -> {
            return p_151705_.mobSettings;
        })).apply(p_47527_, Biome::new);
    });
    public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create((p_47504_) ->
    {
        return p_47504_.group(Biome.ClimateSettings.CODEC.forGetter((p_151703_) -> {
            return p_151703_.climateSettings;
        }), Biome.BiomeCategory.CODEC.fieldOf("category").forGetter((p_151701_) -> {
            return p_151701_.biomeCategory;
        }), Codec.FLOAT.fieldOf("depth").forGetter((p_151699_) -> {
            return p_151699_.depth;
        }), Codec.FLOAT.fieldOf("scale").forGetter((p_151695_) -> {
            return p_151695_.scale;
        }), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter((p_151675_) -> {
            return p_151675_.specialEffects;
        })).apply(p_47504_, (p_151669_, p_151670_, p_151671_, p_151672_, p_151673_) -> {
            return new Biome(p_151669_, p_151670_, p_151671_, p_151672_, p_151673_, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY);
        });
    });
    public static final Codec<Supplier<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<Biome>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    private final Map < Integer, List < StructureFeature<? >>> structuresByStep = Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy((p_47525_) ->
    {
        return p_47525_.step().ordinal();
    }));
    private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), ImmutableList.of(0));
    static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(3456L), ImmutableList.of(-2, -1, 0));
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), ImmutableList.of(0));
    private static final int TEMPERATURE_CACHE_SIZE = 1024;
    private final Biome.ClimateSettings climateSettings;
    private final BiomeGenerationSettings generationSettings;
    private final MobSpawnSettings mobSettings;
    private final float depth;
    private final float scale;
    private final Biome.BiomeCategory biomeCategory;
    private final BiomeSpecialEffects specialEffects;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() ->
    {
        return Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(1024, 0.25F)
            {
                protected void rehash(int p_47580_)
                {
                }
            };
            long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
            return long2floatlinkedopenhashmap;
        });
    });

    Biome(Biome.ClimateSettings p_47447_, Biome.BiomeCategory p_47448_, float p_47449_, float p_47450_, BiomeSpecialEffects p_47451_, BiomeGenerationSettings p_47452_, MobSpawnSettings p_47453_)
    {
        this.climateSettings = p_47447_;
        this.generationSettings = p_47452_;
        this.mobSettings = p_47453_;
        this.biomeCategory = p_47448_;
        this.depth = p_47449_;
        this.scale = p_47450_;
        this.specialEffects = p_47451_;
    }

    public int getSkyColor()
    {
        return this.specialEffects.getSkyColor();
    }

    public MobSpawnSettings getMobSettings()
    {
        return this.mobSettings;
    }

    public Biome.Precipitation getPrecipitation()
    {
        return this.climateSettings.precipitation;
    }

    public boolean isHumid()
    {
        return this.getDownfall() > 0.85F;
    }

    private float getHeightAdjustedTemperature(BlockPos pPos)
    {
        float f = this.climateSettings.temperatureModifier.modifyTemperature(pPos, this.getBaseTemperature());

        if (pPos.getY() > 64)
        {
            float f1 = (float)(TEMPERATURE_NOISE.getValue((double)((float)pPos.getX() / 8.0F), (double)((float)pPos.getZ() / 8.0F), false) * 4.0D);
            return f - (f1 + (float)pPos.getY() - 64.0F) * 0.05F / 30.0F;
        }
        else
        {
            return f;
        }
    }

    public final float getTemperature(BlockPos pPos)
    {
        long i = pPos.asLong();
        Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = this.temperatureCache.get();
        float f = long2floatlinkedopenhashmap.get(i);

        if (!Float.isNaN(f))
        {
            return f;
        }
        else
        {
            float f1 = this.getHeightAdjustedTemperature(pPos);

            if (long2floatlinkedopenhashmap.size() == 1024)
            {
                long2floatlinkedopenhashmap.removeFirstFloat();
            }

            long2floatlinkedopenhashmap.put(i, f1);
            return f1;
        }
    }

    public boolean shouldFreeze(LevelReader pLevel, BlockPos pPos)
    {
        return this.shouldFreeze(pLevel, pPos, true);
    }

    public boolean shouldFreeze(LevelReader pLevel, BlockPos pPos, boolean p_47483_)
    {
        if (this.getTemperature(pPos) >= 0.15F)
        {
            return false;
        }
        else
        {
            if (pPos.getY() >= pLevel.getMinBuildHeight() && pPos.getY() < pLevel.getMaxBuildHeight() && pLevel.getBrightness(LightLayer.BLOCK, pPos) < 10)
            {
                BlockState blockstate = pLevel.getBlockState(pPos);
                FluidState fluidstate = pLevel.getFluidState(pPos);

                if (fluidstate.getType() == Fluids.WATER && blockstate.getBlock() instanceof LiquidBlock)
                {
                    if (!p_47483_)
                    {
                        return true;
                    }

                    boolean flag = pLevel.isWaterAt(pPos.west()) && pLevel.isWaterAt(pPos.east()) && pLevel.isWaterAt(pPos.north()) && pLevel.isWaterAt(pPos.south());

                    if (!flag)
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean isColdEnoughToSnow(BlockPos p_151697_)
    {
        return this.getTemperature(p_151697_) < 0.15F;
    }

    public boolean shouldSnow(LevelReader pLevel, BlockPos pPos)
    {
        if (!this.isColdEnoughToSnow(pPos))
        {
            return false;
        }
        else
        {
            if (pPos.getY() >= pLevel.getMinBuildHeight() && pPos.getY() < pLevel.getMaxBuildHeight() && pLevel.getBrightness(LightLayer.BLOCK, pPos) < 10)
            {
                BlockState blockstate = pLevel.getBlockState(pPos);

                if (blockstate.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(pLevel, pPos))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public BiomeGenerationSettings getGenerationSettings()
    {
        return this.generationSettings;
    }

    public void generate(StructureFeatureManager pStructureManager, ChunkGenerator pChunkGenerator, WorldGenRegion pLevelGenRegion, long pSeed, WorldgenRandom p_47489_, BlockPos pRand)
    {
        List < List < Supplier < ConfiguredFeature <? , ? >>> > list = this.generationSettings.features();
        Registry < ConfiguredFeature <? , ? >> registry = pLevelGenRegion.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
        Registry < StructureFeature<? >> registry1 = pLevelGenRegion.registryAccess().registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY);
        int i = GenerationStep.Decoration.values().length;

        for (int j = 0; j < i; ++j)
        {
            int k = 0;

            if (pStructureManager.shouldGenerateFeatures())
            {
                for (StructureFeature<?> structurefeature : this.structuresByStep.getOrDefault(j, Collections.emptyList()))
                {
                    p_47489_.setFeatureSeed(pSeed, k, j);
                    int l = SectionPos.blockToSectionCoord(pRand.getX());
                    int i1 = SectionPos.blockToSectionCoord(pRand.getZ());
                    int j1 = SectionPos.sectionToBlockCoord(l);
                    int k1 = SectionPos.sectionToBlockCoord(i1);
                    Supplier<String> supplier = () ->
                    {
                        return registry1.getResourceKey(structurefeature).map(Object::toString).orElseGet(structurefeature::toString);
                    };

                    try
                    {
                        int l1 = pLevelGenRegion.getMinBuildHeight() + 1;
                        int i2 = pLevelGenRegion.getMaxBuildHeight() - 1;
                        pLevelGenRegion.setCurrentlyGenerating(supplier);
                        pStructureManager.startsForFeature(SectionPos.of(pRand), structurefeature).forEach((p_151667_) ->
                        {
                            p_151667_.placeInChunk(pLevelGenRegion, pStructureManager, pChunkGenerator, p_47489_, new BoundingBox(j1, l1, k1, j1 + 15, i2, k1 + 15), new ChunkPos(l, i1));
                        });
                    }
                    catch (Exception exception)
                    {
                        CrashReport crashreport = CrashReport.forThrowable(exception, "Feature placement");
                        crashreport.addCategory("Feature").setDetail("Description", supplier::get);
                        throw new ReportedException(crashreport);
                    }

                    ++k;
                }
            }

            if (list.size() > j)
            {
                for (Supplier < ConfiguredFeature <? , ? >> supplier1 : list.get(j))
                {
                    ConfiguredFeature <? , ? > configuredfeature = supplier1.get();
                    Supplier<String> supplier2 = () ->
                    {
                        return registry.getResourceKey(configuredfeature).map(Object::toString).orElseGet(configuredfeature::toString);
                    };
                    p_47489_.setFeatureSeed(pSeed, k, j);

                    try
                    {
                        pLevelGenRegion.setCurrentlyGenerating(supplier2);
                        configuredfeature.place(pLevelGenRegion, pChunkGenerator, p_47489_, pRand);
                    }
                    catch (Exception exception1)
                    {
                        CrashReport crashreport1 = CrashReport.forThrowable(exception1, "Feature placement");
                        crashreport1.addCategory("Feature").setDetail("Description", supplier2::get);
                        throw new ReportedException(crashreport1);
                    }

                    ++k;
                }
            }
        }

        pLevelGenRegion.setCurrentlyGenerating((Supplier<String>)null);
    }

    public int getFogColor()
    {
        return this.specialEffects.getFogColor();
    }

    public int getGrassColor(double pPosX, double p_47466_)
    {
        int i = this.specialEffects.getGrassColorOverride().orElseGet(this::getGrassColorFromTexture);
        return this.specialEffects.getGrassColorModifier().modifyColor(pPosX, p_47466_, i);
    }

    private int getGrassColorFromTexture()
    {
        double d0 = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
        double d1 = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
        return GrassColor.get(d0, d1);
    }

    public int getFoliageColor()
    {
        return this.specialEffects.getFoliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
    }

    private int getFoliageColorFromTexture()
    {
        double d0 = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
        double d1 = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
        return FoliageColor.get(d0, d1);
    }

    public void buildSurfaceAt(Random p_151683_, ChunkAccess p_151684_, int p_151685_, int p_151686_, int p_151687_, double p_151688_, BlockState p_151689_, BlockState p_151690_, int p_151691_, int p_151692_, long p_151693_)
    {
        ConfiguredSurfaceBuilder<?> configuredsurfacebuilder = this.generationSettings.getSurfaceBuilder().get();
        configuredsurfacebuilder.initNoise(p_151693_);
        configuredsurfacebuilder.apply(p_151683_, p_151684_, this, p_151685_, p_151686_, p_151687_, p_151688_, p_151689_, p_151690_, p_151691_, p_151692_, p_151693_);
    }

    public final float getDepth()
    {
        return this.depth;
    }

    public final float getDownfall()
    {
        return this.climateSettings.downfall;
    }

    public final float getScale()
    {
        return this.scale;
    }

    public final float getBaseTemperature()
    {
        return this.climateSettings.temperature;
    }

    public BiomeSpecialEffects getSpecialEffects()
    {
        return this.specialEffects;
    }

    public final int getWaterColor()
    {
        return this.specialEffects.getWaterColor();
    }

    public final int getWaterFogColor()
    {
        return this.specialEffects.getWaterFogColor();
    }

    public Optional<AmbientParticleSettings> getAmbientParticle()
    {
        return this.specialEffects.getAmbientParticleSettings();
    }

    public Optional<SoundEvent> getAmbientLoop()
    {
        return this.specialEffects.getAmbientLoopSoundEvent();
    }

    public Optional<AmbientMoodSettings> getAmbientMood()
    {
        return this.specialEffects.getAmbientMoodSettings();
    }

    public Optional<AmbientAdditionsSettings> getAmbientAdditions()
    {
        return this.specialEffects.getAmbientAdditionsSettings();
    }

    public Optional<Music> getBackgroundMusic()
    {
        return this.specialEffects.getBackgroundMusic();
    }

    public final Biome.BiomeCategory getBiomeCategory()
    {
        return this.biomeCategory;
    }

    public String toString()
    {
        ResourceLocation resourcelocation = BuiltinRegistries.BIOME.getKey(this);
        return resourcelocation == null ? super.toString() : resourcelocation.toString();
    }

    public static class BiomeBuilder
    {
        @Nullable
        private Biome.Precipitation precipitation;
        @Nullable
        private Biome.BiomeCategory biomeCategory;
        @Nullable
        private Float depth;
        @Nullable
        private Float scale;
        @Nullable
        private Float temperature;
        private Biome.TemperatureModifier temperatureModifier = Biome.TemperatureModifier.NONE;
        @Nullable
        private Float downfall;
        @Nullable
        private BiomeSpecialEffects specialEffects;
        @Nullable
        private MobSpawnSettings mobSpawnSettings;
        @Nullable
        private BiomeGenerationSettings generationSettings;

        public Biome.BiomeBuilder precipitation(Biome.Precipitation pPrecipitation)
        {
            this.precipitation = pPrecipitation;
            return this;
        }

        public Biome.BiomeBuilder biomeCategory(Biome.BiomeCategory pBiomeCategory)
        {
            this.biomeCategory = pBiomeCategory;
            return this;
        }

        public Biome.BiomeBuilder depth(float pDepth)
        {
            this.depth = pDepth;
            return this;
        }

        public Biome.BiomeBuilder scale(float pScale)
        {
            this.scale = pScale;
            return this;
        }

        public Biome.BiomeBuilder temperature(float pTemperature)
        {
            this.temperature = pTemperature;
            return this;
        }

        public Biome.BiomeBuilder downfall(float pDownfall)
        {
            this.downfall = pDownfall;
            return this;
        }

        public Biome.BiomeBuilder specialEffects(BiomeSpecialEffects pEffects)
        {
            this.specialEffects = pEffects;
            return this;
        }

        public Biome.BiomeBuilder mobSpawnSettings(MobSpawnSettings pMobSpawnSettings)
        {
            this.mobSpawnSettings = pMobSpawnSettings;
            return this;
        }

        public Biome.BiomeBuilder generationSettings(BiomeGenerationSettings pGenerationSettings)
        {
            this.generationSettings = pGenerationSettings;
            return this;
        }

        public Biome.BiomeBuilder temperatureAdjustment(Biome.TemperatureModifier pTemperatureSettings)
        {
            this.temperatureModifier = pTemperatureSettings;
            return this;
        }

        public Biome build()
        {
            if (this.precipitation != null && this.biomeCategory != null && this.depth != null && this.scale != null && this.temperature != null && this.downfall != null && this.specialEffects != null && this.mobSpawnSettings != null && this.generationSettings != null)
            {
                return new Biome(new Biome.ClimateSettings(this.precipitation, this.temperature, this.temperatureModifier, this.downfall), this.biomeCategory, this.depth, this.scale, this.specialEffects, this.generationSettings, this.mobSpawnSettings);
            }
            else
            {
                throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
            }
        }

        public String toString()
        {
            return "BiomeBuilder{\nprecipitation=" + this.precipitation + ",\nbiomeCategory=" + this.biomeCategory + ",\ndepth=" + this.depth + ",\nscale=" + this.scale + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + this.temperatureModifier + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\nmobSpawnSettings=" + this.mobSpawnSettings + ",\ngenerationSettings=" + this.generationSettings + ",\n}";
        }
    }

    public static enum BiomeCategory implements StringRepresentable
    {
        NONE("none"),
        TAIGA("taiga"),
        EXTREME_HILLS("extreme_hills"),
        JUNGLE("jungle"),
        MESA("mesa"),
        PLAINS("plains"),
        SAVANNA("savanna"),
        ICY("icy"),
        THEEND("the_end"),
        BEACH("beach"),
        FOREST("forest"),
        OCEAN("ocean"),
        DESERT("desert"),
        RIVER("river"),
        SWAMP("swamp"),
        MUSHROOM("mushroom"),
        NETHER("nether"),
        UNDERGROUND("underground");

        public static final Codec<Biome.BiomeCategory> CODEC = StringRepresentable.fromEnum(Biome.BiomeCategory::values, Biome.BiomeCategory::byName);
        private static final Map<String, Biome.BiomeCategory> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Biome.BiomeCategory::getName, (p_47642_) -> {
            return p_47642_;
        }));
        private final String name;

        private BiomeCategory(String p_47639_)
        {
            this.name = p_47639_;
        }

        public String getName()
        {
            return this.name;
        }

        public static Biome.BiomeCategory byName(String pName)
        {
            return BY_NAME.get(pName);
        }

        public String getSerializedName()
        {
            return this.name;
        }
    }

    public static class ClimateParameters
    {
        public static final Codec<Biome.ClimateParameters> CODEC = RecordCodecBuilder.create((p_47665_) ->
        {
            return p_47665_.group(Codec.floatRange(-2.0F, 2.0F).fieldOf("temperature").forGetter((p_151730_) -> {
                return p_151730_.temperature;
            }), Codec.floatRange(-2.0F, 2.0F).fieldOf("humidity").forGetter((p_151728_) -> {
                return p_151728_.humidity;
            }), Codec.floatRange(-2.0F, 2.0F).fieldOf("altitude").forGetter((p_151726_) -> {
                return p_151726_.altitude;
            }), Codec.floatRange(-2.0F, 2.0F).fieldOf("weirdness").forGetter((p_151724_) -> {
                return p_151724_.weirdness;
            }), Codec.floatRange(0.0F, 1.0F).fieldOf("offset").forGetter((p_151722_) -> {
                return p_151722_.offset;
            })).apply(p_47665_, Biome.ClimateParameters::new);
        });
        private final float temperature;
        private final float humidity;
        private final float altitude;
        private final float weirdness;
        private final float offset;

        public ClimateParameters(float p_47657_, float p_47658_, float p_47659_, float p_47660_, float p_47661_)
        {
            this.temperature = p_47657_;
            this.humidity = p_47658_;
            this.altitude = p_47659_;
            this.weirdness = p_47660_;
            this.offset = p_47661_;
        }

        public String toString()
        {
            return "temp: " + this.temperature + ", hum: " + this.humidity + ", alt: " + this.altitude + ", weird: " + this.weirdness + ", offset: " + this.offset;
        }

        public boolean equals(Object p_47675_)
        {
            if (this == p_47675_)
            {
                return true;
            }
            else if (p_47675_ != null && this.getClass() == p_47675_.getClass())
            {
                Biome.ClimateParameters biome$climateparameters = (Biome.ClimateParameters)p_47675_;

                if (Float.compare(biome$climateparameters.temperature, this.temperature) != 0)
                {
                    return false;
                }
                else if (Float.compare(biome$climateparameters.humidity, this.humidity) != 0)
                {
                    return false;
                }
                else if (Float.compare(biome$climateparameters.altitude, this.altitude) != 0)
                {
                    return false;
                }
                else
                {
                    return Float.compare(biome$climateparameters.weirdness, this.weirdness) == 0;
                }
            }
            else
            {
                return false;
            }
        }

        public int hashCode()
        {
            int i = this.temperature != 0.0F ? Float.floatToIntBits(this.temperature) : 0;
            i = 31 * i + (this.humidity != 0.0F ? Float.floatToIntBits(this.humidity) : 0);
            i = 31 * i + (this.altitude != 0.0F ? Float.floatToIntBits(this.altitude) : 0);
            return 31 * i + (this.weirdness != 0.0F ? Float.floatToIntBits(this.weirdness) : 0);
        }

        public float fitness(Biome.ClimateParameters pAttributes)
        {
            return (this.temperature - pAttributes.temperature) * (this.temperature - pAttributes.temperature) + (this.humidity - pAttributes.humidity) * (this.humidity - pAttributes.humidity) + (this.altitude - pAttributes.altitude) * (this.altitude - pAttributes.altitude) + (this.weirdness - pAttributes.weirdness) * (this.weirdness - pAttributes.weirdness) + (this.offset - pAttributes.offset) * (this.offset - pAttributes.offset);
        }
    }

    static class ClimateSettings
    {
        public static final MapCodec<Biome.ClimateSettings> CODEC = RecordCodecBuilder.mapCodec((p_47699_) ->
        {
            return p_47699_.group(Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter((p_151739_) -> {
                return p_151739_.precipitation;
            }), Codec.FLOAT.fieldOf("temperature").forGetter((p_151737_) -> {
                return p_151737_.temperature;
            }), Biome.TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", Biome.TemperatureModifier.NONE).forGetter((p_151735_) -> {
                return p_151735_.temperatureModifier;
            }), Codec.FLOAT.fieldOf("downfall").forGetter((p_151733_) -> {
                return p_151733_.downfall;
            })).apply(p_47699_, Biome.ClimateSettings::new);
        });
        final Biome.Precipitation precipitation;
        final float temperature;
        final Biome.TemperatureModifier temperatureModifier;
        final float downfall;

        ClimateSettings(Biome.Precipitation p_47686_, float p_47687_, Biome.TemperatureModifier p_47688_, float p_47689_)
        {
            this.precipitation = p_47686_;
            this.temperature = p_47687_;
            this.temperatureModifier = p_47688_;
            this.downfall = p_47689_;
        }
    }

    public static enum Precipitation implements StringRepresentable
    {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        public static final Codec<Biome.Precipitation> CODEC = StringRepresentable.fromEnum(Biome.Precipitation::values, Biome.Precipitation::byName);
        private static final Map<String, Biome.Precipitation> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Biome.Precipitation::getName, (p_47728_) -> {
            return p_47728_;
        }));
        private final String name;

        private Precipitation(String p_47725_)
        {
            this.name = p_47725_;
        }

        public String getName()
        {
            return this.name;
        }

        public static Biome.Precipitation byName(String pName)
        {
            return BY_NAME.get(pName);
        }

        public String getSerializedName()
        {
            return this.name;
        }
    }

    public static enum TemperatureModifier implements StringRepresentable
    {
        NONE("none")
        {
            public float modifyTemperature(BlockPos pPos, float pTemperature)
            {
                return pTemperature;
            }
        },
        FROZEN("frozen")
        {
            public float modifyTemperature(BlockPos pPos, float pTemperature)
            {
                double d0 = Biome.FROZEN_TEMPERATURE_NOISE.getValue((double)pPos.getX() * 0.05D, (double)pPos.getZ() * 0.05D, false) * 7.0D;
                double d1 = Biome.BIOME_INFO_NOISE.getValue((double)pPos.getX() * 0.2D, (double)pPos.getZ() * 0.2D, false);
                double d2 = d0 + d1;

                if (d2 < 0.3D)
                {
                    double d3 = Biome.BIOME_INFO_NOISE.getValue((double)pPos.getX() * 0.09D, (double)pPos.getZ() * 0.09D, false);

                    if (d3 < 0.8D)
                    {
                        return 0.2F;
                    }
                }

                return pTemperature;
            }
        };

        private final String name;
        public static final Codec<Biome.TemperatureModifier> CODEC = StringRepresentable.fromEnum(Biome.TemperatureModifier::values, Biome.TemperatureModifier::byName);
        private static final Map<String, Biome.TemperatureModifier> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Biome.TemperatureModifier::getName, (p_47753_) -> {
            return p_47753_;
        }));

        public abstract float modifyTemperature(BlockPos pPos, float pTemperature);

        TemperatureModifier(String p_47745_)
        {
            this.name = p_47745_;
        }

        public String getName()
        {
            return this.name;
        }

        public String getSerializedName()
        {
            return this.name;
        }

        public static Biome.TemperatureModifier byName(String pName)
        {
            return BY_NAME.get(pName);
        }
    }
}
