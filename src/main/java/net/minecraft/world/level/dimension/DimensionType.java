package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DimensionType
{
    public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
    public static final int MIN_HEIGHT = 16;
    public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
    public static final int MAX_Y = (Y_SIZE >> 1) - 1;
    public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
    public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
    public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
    public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
    public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.<DimensionType>create((p_63914_) ->
    {
        return p_63914_.group(Codec.LONG.optionalFieldOf("fixed_time").xmap((p_156696_) -> {
            return p_156696_.map(OptionalLong::of).orElseGet(OptionalLong::empty);
        }, (p_156698_) -> {
            return p_156698_.isPresent() ? Optional.of(p_156698_.getAsLong()) : Optional.empty();
        }).forGetter((p_156731_) -> {
            return p_156731_.fixedTime;
        }), Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight), Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling), Codec.BOOL.fieldOf("ultrawarm").forGetter(DimensionType::ultraWarm), Codec.BOOL.fieldOf("natural").forGetter(DimensionType::natural), Codec.doubleRange((double)1.0E-5F, 3.0E7D).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale), Codec.BOOL.fieldOf("piglin_safe").forGetter(DimensionType::piglinSafe), Codec.BOOL.fieldOf("bed_works").forGetter(DimensionType::bedWorks), Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(DimensionType::respawnAnchorWorks), Codec.BOOL.fieldOf("has_raids").forGetter(DimensionType::hasRaids), Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y").forGetter(DimensionType::minY), Codec.intRange(16, Y_SIZE).fieldOf("height").forGetter(DimensionType::height), Codec.intRange(0, Y_SIZE).fieldOf("logical_height").forGetter(DimensionType::logicalHeight), ResourceLocation.CODEC.fieldOf("infiniburn").forGetter((p_156729_) -> {
            return p_156729_.infiniburn;
        }), ResourceLocation.CODEC.fieldOf("effects").orElse(OVERWORLD_EFFECTS).forGetter((p_156725_) -> {
            return p_156725_.effectsLocation;
        }), Codec.FLOAT.fieldOf("ambient_light").forGetter((p_156721_) -> {
            return p_156721_.ambientLight;
        })).apply(p_63914_, DimensionType::new);
    }).comapFlatMap(DimensionType::guardY, Function.identity());
    private static final int MOON_PHASES = 8;
    public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[] {1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
    public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
    protected static final DimensionType DEFAULT_OVERWORLD = create(OptionalLong.empty(), true, false, false, true, 1.0D, false, false, true, false, true, 0, 256, 256, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);
    protected static final DimensionType DEFAULT_NETHER = create(OptionalLong.of(18000L), false, true, true, false, 8.0D, false, true, false, true, false, 0, 256, 128, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_NETHER.getName(), NETHER_EFFECTS, 0.1F);
    protected static final DimensionType DEFAULT_END = create(OptionalLong.of(6000L), false, false, false, false, 1.0D, true, false, false, false, true, 0, 256, 256, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_END.getName(), END_EFFECTS, 0.0F);
    public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves"));
    protected static final DimensionType DEFAULT_OVERWORLD_CAVES = create(OptionalLong.empty(), true, true, false, true, 1.0D, false, false, true, false, true, 0, 256, 256, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);
    public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
    private final OptionalLong fixedTime;
    private final boolean hasSkylight;
    private final boolean hasCeiling;
    private final boolean ultraWarm;
    private final boolean natural;
    private final double coordinateScale;
    private final boolean createDragonFight;
    private final boolean piglinSafe;
    private final boolean bedWorks;
    private final boolean respawnAnchorWorks;
    private final boolean hasRaids;
    private final int minY;
    private final int height;
    private final int logicalHeight;
    private final BiomeZoomer biomeZoomer;
    private final ResourceLocation infiniburn;
    private final ResourceLocation effectsLocation;
    private final float ambientLight;
    private final transient float[] brightnessRamp;

    private static DataResult<DimensionType> guardY(DimensionType p_156719_)
    {
        if (p_156719_.height() < 16)
        {
            return DataResult.error("height has to be at least 16");
        }
        else if (p_156719_.minY() + p_156719_.height() > MAX_Y + 1)
        {
            return DataResult.error("min_y + height cannot be higher than: " + (MAX_Y + 1));
        }
        else if (p_156719_.logicalHeight() > p_156719_.height())
        {
            return DataResult.error("logical_height cannot be higher than height");
        }
        else if (p_156719_.height() % 16 != 0)
        {
            return DataResult.error("height has to be multiple of 16");
        }
        else
        {
            return p_156719_.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(p_156719_);
        }
    }

    private DimensionType(OptionalLong p_156656_, boolean p_156657_, boolean p_156658_, boolean p_156659_, boolean p_156660_, double p_156661_, boolean p_156662_, boolean p_156663_, boolean p_156664_, boolean p_156665_, int p_156666_, int p_156667_, int p_156668_, ResourceLocation p_156669_, ResourceLocation p_156670_, float p_156671_)
    {
        this(p_156656_, p_156657_, p_156658_, p_156659_, p_156660_, p_156661_, false, p_156662_, p_156663_, p_156664_, p_156665_, p_156666_, p_156667_, p_156668_, FuzzyOffsetBiomeZoomer.INSTANCE, p_156669_, p_156670_, p_156671_);
    }

    public static DimensionType create(OptionalLong p_156700_, boolean p_156701_, boolean p_156702_, boolean p_156703_, boolean p_156704_, double p_156705_, boolean p_156706_, boolean p_156707_, boolean p_156708_, boolean p_156709_, boolean p_156710_, int p_156711_, int p_156712_, int p_156713_, BiomeZoomer p_156714_, ResourceLocation p_156715_, ResourceLocation p_156716_, float p_156717_)
    {
        DimensionType dimensiontype = new DimensionType(p_156700_, p_156701_, p_156702_, p_156703_, p_156704_, p_156705_, p_156706_, p_156707_, p_156708_, p_156709_, p_156710_, p_156711_, p_156712_, p_156713_, p_156714_, p_156715_, p_156716_, p_156717_);
        guardY(dimensiontype).error().ifPresent((p_156692_) ->
        {
            throw new IllegalStateException(p_156692_.message());
        });
        return dimensiontype;
    }

    @Deprecated
    private DimensionType(OptionalLong p_156673_, boolean p_156674_, boolean p_156675_, boolean p_156676_, boolean p_156677_, double p_156678_, boolean p_156679_, boolean p_156680_, boolean p_156681_, boolean p_156682_, boolean p_156683_, int p_156684_, int p_156685_, int p_156686_, BiomeZoomer p_156687_, ResourceLocation p_156688_, ResourceLocation p_156689_, float p_156690_)
    {
        this.fixedTime = p_156673_;
        this.hasSkylight = p_156674_;
        this.hasCeiling = p_156675_;
        this.ultraWarm = p_156676_;
        this.natural = p_156677_;
        this.coordinateScale = p_156678_;
        this.createDragonFight = p_156679_;
        this.piglinSafe = p_156680_;
        this.bedWorks = p_156681_;
        this.respawnAnchorWorks = p_156682_;
        this.hasRaids = p_156683_;
        this.minY = p_156684_;
        this.height = p_156685_;
        this.logicalHeight = p_156686_;
        this.biomeZoomer = p_156687_;
        this.infiniburn = p_156688_;
        this.effectsLocation = p_156689_;
        this.ambientLight = p_156690_;
        this.brightnessRamp = fillBrightnessRamp(p_156690_);
    }

    private static float[] fillBrightnessRamp(float pLight)
    {
        float[] afloat = new float[16];

        for (int i = 0; i <= 15; ++i)
        {
            float f = (float)i / 15.0F;
            float f1 = f / (4.0F - 3.0F * f);
            afloat[i] = Mth.lerp(pLight, f1, 1.0F);
        }

        return afloat;
    }

    @Deprecated
    public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> pDynamic)
    {
        Optional<Number> optional = pDynamic.asNumber().result();

        if (optional.isPresent())
        {
            int i = optional.get().intValue();

            if (i == -1)
            {
                return DataResult.success(Level.NETHER);
            }

            if (i == 0)
            {
                return DataResult.success(Level.OVERWORLD);
            }

            if (i == 1)
            {
                return DataResult.success(Level.END);
            }
        }

        return Level.RESOURCE_KEY_CODEC.parse(pDynamic);
    }

    public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder pImpl)
    {
        WritableRegistry<DimensionType> writableregistry = pImpl.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        writableregistry.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
        writableregistry.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
        writableregistry.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
        writableregistry.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
        return pImpl;
    }

    private static ChunkGenerator defaultEndGenerator(Registry<Biome> pLookUpRegistryBiome, Registry<NoiseGeneratorSettings> pSettingsRegistry, long pSeed)
    {
        return new NoiseBasedChunkGenerator(new TheEndBiomeSource(pLookUpRegistryBiome, pSeed), pSeed, () ->
        {
            return pSettingsRegistry.getOrThrow(NoiseGeneratorSettings.END);
        });
    }

    private static ChunkGenerator defaultNetherGenerator(Registry<Biome> pLookUpRegistryBiome, Registry<NoiseGeneratorSettings> pLookUpRegistryDimensionType, long pSeed)
    {
        return new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(pLookUpRegistryBiome, pSeed), pSeed, () ->
        {
            return pLookUpRegistryDimensionType.getOrThrow(NoiseGeneratorSettings.NETHER);
        });
    }

    public static MappedRegistry<LevelStem> defaultDimensions(Registry<DimensionType> pLookUpRegistryDimensionType, Registry<Biome> pLookUpRegistryBiome, Registry<NoiseGeneratorSettings> pLookUpRegistryDimensionSettings, long pSeed)
    {
        MappedRegistry<LevelStem> mappedregistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        mappedregistry.register(LevelStem.NETHER, new LevelStem(() ->
        {
            return pLookUpRegistryDimensionType.getOrThrow(NETHER_LOCATION);
        }, defaultNetherGenerator(pLookUpRegistryBiome, pLookUpRegistryDimensionSettings, pSeed)), Lifecycle.stable());
        mappedregistry.register(LevelStem.END, new LevelStem(() ->
        {
            return pLookUpRegistryDimensionType.getOrThrow(END_LOCATION);
        }, defaultEndGenerator(pLookUpRegistryBiome, pLookUpRegistryDimensionSettings, pSeed)), Lifecycle.stable());
        return mappedregistry;
    }

    public static double getTeleportationScale(DimensionType pFirstType, DimensionType pSecondType)
    {
        double d0 = pFirstType.coordinateScale();
        double d1 = pSecondType.coordinateScale();
        return d0 / d1;
    }

    @Deprecated
    public String getFileSuffix()
    {
        return this.equalTo(DEFAULT_END) ? "_end" : "";
    }

    public static File getStorageFolder(ResourceKey<Level> pDimensionKey, File pLevelFolder)
    {
        if (pDimensionKey == Level.OVERWORLD)
        {
            return pLevelFolder;
        }
        else if (pDimensionKey == Level.END)
        {
            return new File(pLevelFolder, "DIM1");
        }
        else
        {
            return pDimensionKey == Level.NETHER ? new File(pLevelFolder, "DIM-1") : new File(pLevelFolder, "dimensions/" + pDimensionKey.location().getNamespace() + "/" + pDimensionKey.location().getPath());
        }
    }

    public boolean hasSkyLight()
    {
        return this.hasSkylight;
    }

    public boolean hasCeiling()
    {
        return this.hasCeiling;
    }

    public boolean ultraWarm()
    {
        return this.ultraWarm;
    }

    public boolean natural()
    {
        return this.natural;
    }

    public double coordinateScale()
    {
        return this.coordinateScale;
    }

    public boolean piglinSafe()
    {
        return this.piglinSafe;
    }

    public boolean bedWorks()
    {
        return this.bedWorks;
    }

    public boolean respawnAnchorWorks()
    {
        return this.respawnAnchorWorks;
    }

    public boolean hasRaids()
    {
        return this.hasRaids;
    }

    public int minY()
    {
        return this.minY;
    }

    public int height()
    {
        return this.height;
    }

    public int logicalHeight()
    {
        return this.logicalHeight;
    }

    public boolean createDragonFight()
    {
        return this.createDragonFight;
    }

    public BiomeZoomer getBiomeZoomer()
    {
        return this.biomeZoomer;
    }

    public boolean hasFixedTime()
    {
        return this.fixedTime.isPresent();
    }

    public float timeOfDay(long pDayTime)
    {
        double d0 = Mth.frac((double)this.fixedTime.orElse(pDayTime) / 24000.0D - 0.25D);
        double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
        return (float)(d0 * 2.0D + d1) / 3.0F;
    }

    public int moonPhase(long pDayTime)
    {
        return (int)(pDayTime / 24000L % 8L + 8L) % 8;
    }

    public float brightness(int pLight)
    {
        return this.brightnessRamp[pLight];
    }

    public Tag<Block> infiniburn()
    {
        Tag<Block> tag = BlockTags.getAllTags().getTag(this.infiniburn);
        return (Tag<Block>)(tag != null ? tag : BlockTags.INFINIBURN_OVERWORLD);
    }

    public ResourceLocation effectsLocation()
    {
        return this.effectsLocation;
    }

    public boolean equalTo(DimensionType pType)
    {
        if (this == pType)
        {
            return true;
        }
        else
        {
            return this.hasSkylight == pType.hasSkylight && this.hasCeiling == pType.hasCeiling && this.ultraWarm == pType.ultraWarm && this.natural == pType.natural && this.coordinateScale == pType.coordinateScale && this.createDragonFight == pType.createDragonFight && this.piglinSafe == pType.piglinSafe && this.bedWorks == pType.bedWorks && this.respawnAnchorWorks == pType.respawnAnchorWorks && this.hasRaids == pType.hasRaids && this.minY == pType.minY && this.height == pType.height && this.logicalHeight == pType.logicalHeight && Float.compare(pType.ambientLight, this.ambientLight) == 0 && this.fixedTime.equals(pType.fixedTime) && this.biomeZoomer.equals(pType.biomeZoomer) && this.infiniburn.equals(pType.infiniburn) && this.effectsLocation.equals(pType.effectsLocation);
        }
    }
}
