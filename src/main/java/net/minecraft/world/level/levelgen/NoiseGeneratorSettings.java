package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public final class NoiseGeneratorSettings
{
    public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create((p_64475_) ->
    {
        return p_64475_.group(StructureSettings.CODEC.fieldOf("structures").forGetter(NoiseGeneratorSettings::structureSettings), NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings), BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::getDefaultBlock), BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::getDefaultFluid), Codec.INT.fieldOf("bedrock_roof_position").forGetter(NoiseGeneratorSettings::getBedrockRoofPosition), Codec.INT.fieldOf("bedrock_floor_position").forGetter(NoiseGeneratorSettings::getBedrockFloorPosition), Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel), Codec.INT.fieldOf("min_surface_level").forGetter(NoiseGeneratorSettings::getMinSurfaceLevel), Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration), Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled), Codec.BOOL.fieldOf("noise_caves_enabled").forGetter(NoiseGeneratorSettings::isNoiseCavesEnabled), Codec.BOOL.fieldOf("deepslate_enabled").forGetter(NoiseGeneratorSettings::isDeepslateEnabled), Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::isOreVeinsEnabled), Codec.BOOL.fieldOf("noodle_caves_enabled").forGetter(NoiseGeneratorSettings::isOreVeinsEnabled)).apply(p_64475_, NoiseGeneratorSettings::new);
    });
    public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
    private final StructureSettings structureSettings;
    private final NoiseSettings noiseSettings;
    private final BlockState defaultBlock;
    private final BlockState defaultFluid;
    private final int bedrockRoofPosition;
    private final int bedrockFloorPosition;
    private final int seaLevel;
    private final int minSurfaceLevel;
    private final boolean disableMobGeneration;
    private final boolean aquifersEnabled;
    private final boolean noiseCavesEnabled;
    private final boolean deepslateEnabled;
    private final boolean oreVeinsEnabled;
    private final boolean noodleCavesEnabled;
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified"));
    public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether"));
    public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
    public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves"));
    public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands"));
    private static final NoiseGeneratorSettings BUILTIN_OVERWORLD = register(OVERWORLD, overworld(new StructureSettings(true), false));

    private NoiseGeneratorSettings(StructureSettings p_158539_, NoiseSettings p_158540_, BlockState p_158541_, BlockState p_158542_, int p_158543_, int p_158544_, int p_158545_, int p_158546_, boolean p_158547_, boolean p_158548_, boolean p_158549_, boolean p_158550_, boolean p_158551_, boolean p_158552_)
    {
        this.structureSettings = p_158539_;
        this.noiseSettings = p_158540_;
        this.defaultBlock = p_158541_;
        this.defaultFluid = p_158542_;
        this.bedrockRoofPosition = p_158543_;
        this.bedrockFloorPosition = p_158544_;
        this.seaLevel = p_158545_;
        this.minSurfaceLevel = p_158546_;
        this.disableMobGeneration = p_158547_;
        this.aquifersEnabled = p_158548_;
        this.noiseCavesEnabled = p_158549_;
        this.deepslateEnabled = p_158550_;
        this.oreVeinsEnabled = p_158551_;
        this.noodleCavesEnabled = p_158552_;
    }

    public StructureSettings structureSettings()
    {
        return this.structureSettings;
    }

    public NoiseSettings noiseSettings()
    {
        return this.noiseSettings;
    }

    public BlockState getDefaultBlock()
    {
        return this.defaultBlock;
    }

    public BlockState getDefaultFluid()
    {
        return this.defaultFluid;
    }

    public int getBedrockRoofPosition()
    {
        return this.bedrockRoofPosition;
    }

    public int getBedrockFloorPosition()
    {
        return this.bedrockFloorPosition;
    }

    public int seaLevel()
    {
        return this.seaLevel;
    }

    public int getMinSurfaceLevel()
    {
        return this.minSurfaceLevel;
    }

    @Deprecated
    protected boolean disableMobGeneration()
    {
        return this.disableMobGeneration;
    }

    protected boolean isAquifersEnabled()
    {
        return this.aquifersEnabled;
    }

    protected boolean isNoiseCavesEnabled()
    {
        return this.noiseCavesEnabled;
    }

    protected boolean isDeepslateEnabled()
    {
        return this.deepslateEnabled;
    }

    protected boolean isOreVeinsEnabled()
    {
        return this.oreVeinsEnabled;
    }

    protected boolean isNoodleCavesEnabled()
    {
        return this.noodleCavesEnabled;
    }

    public boolean stable(ResourceKey<NoiseGeneratorSettings> p_64477_)
    {
        return Objects.equals(this, BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(p_64477_));
    }

    private static NoiseGeneratorSettings register(ResourceKey<NoiseGeneratorSettings> pKey, NoiseGeneratorSettings pSettings)
    {
        BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, pKey.location(), pSettings);
        return pSettings;
    }

    public static NoiseGeneratorSettings bootstrap()
    {
        return BUILTIN_OVERWORLD;
    }

    private static NoiseGeneratorSettings endLikePreset(StructureSettings p_158558_, BlockState p_158559_, BlockState p_158560_, boolean p_158561_, boolean p_158562_)
    {
        return new NoiseGeneratorSettings(p_158558_, NoiseSettings.create(0, 128, new NoiseSamplingSettings(2.0D, 1.0D, 80.0D, 160.0D), new NoiseSlideSettings(-3000, 64, -46), new NoiseSlideSettings(-30, 7, 1), 2, 1, 0.0D, 0.0D, true, false, p_158562_, false), p_158559_, p_158560_, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0, p_158561_, false, false, false, false, false);
    }

    private static NoiseGeneratorSettings netherLikePreset(StructureSettings p_158554_, BlockState p_158555_, BlockState p_158556_)
    {
        Map < StructureFeature<?>, StructureFeatureConfiguration > map = Maps.newHashMap(StructureSettings.DEFAULTS);
        map.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
        return new NoiseGeneratorSettings(new StructureSettings(Optional.ofNullable(p_158554_.stronghold()), map), NoiseSettings.create(0, 128, new NoiseSamplingSettings(1.0D, 3.0D, 80.0D, 60.0D), new NoiseSlideSettings(120, 3, 0), new NoiseSlideSettings(320, 4, -1), 1, 2, 0.0D, 0.019921875D, false, false, false, false), p_158555_, p_158556_, 0, 0, 32, 0, false, false, false, false, false, false);
    }

    private static NoiseGeneratorSettings overworld(StructureSettings p_158564_, boolean p_158565_)
    {
        double d0 = 0.9999999814507745D;
        return new NoiseGeneratorSettings(p_158564_, NoiseSettings.create(0, 256, new NoiseSamplingSettings(0.9999999814507745D, 0.9999999814507745D, 80.0D, 160.0D), new NoiseSlideSettings(-10, 3, 0), new NoiseSlideSettings(15, 3, 0), 1, 2, 1.0D, -0.46875D, true, true, false, p_158565_), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), Integer.MIN_VALUE, 0, 63, 0, false, false, false, false, false, false);
    }

    static
    {
        register(AMPLIFIED, overworld(new StructureSettings(true), true));
        register(NETHER, netherLikePreset(new StructureSettings(false), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState()));
        register(END, endLikePreset(new StructureSettings(false), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), true, true));
        register(CAVES, netherLikePreset(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState()));
        register(FLOATING_ISLANDS, endLikePreset(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), false, false));
    }
}
