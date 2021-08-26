package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureFeature<C extends FeatureConfiguration>
{
    public static final BiMap < String, StructureFeature<? >> STRUCTURES_REGISTRY = HashBiMap.create();
    private static final Map < StructureFeature<?>, GenerationStep.Decoration > STEP = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = register("Pillager_Outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register("Mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register("Mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register("Jungle_Pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register("Desert_Pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register("Igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = register("Ruined_Portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register("Shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final SwamplandHutFeature SWAMP_HUT = register("Swamp_Hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register("Stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS);
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register("Monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register("Ocean_Ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = register("Fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register("EndCity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = register("Buried_Treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
    public static final StructureFeature<JigsawConfiguration> VILLAGE = register("Village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final StructureFeature<RangeDecoratorConfiguration> NETHER_FOSSIL = register("Nether_Fossil", new NetherFossilFeature(RangeDecoratorConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
    public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = register("Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    public static final List < StructureFeature<? >> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
    private static final ResourceLocation JIGSAW_RENAME = new ResourceLocation("jigsaw");
    private static final Map<ResourceLocation, ResourceLocation> RENAMES = ImmutableMap.<ResourceLocation, ResourceLocation>builder().put(new ResourceLocation("nvi"), JIGSAW_RENAME).put(new ResourceLocation("pcp"), JIGSAW_RENAME).put(new ResourceLocation("bastionremnant"), JIGSAW_RENAME).put(new ResourceLocation("runtime"), JIGSAW_RENAME).build();
    public static final int MAX_STRUCTURE_RANGE = 8;
    private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;

    private static < F extends StructureFeature<? >> F register(String pName, F pStructure, GenerationStep.Decoration pDecorationStage)
    {
        STRUCTURES_REGISTRY.put(pName.toLowerCase(Locale.ROOT), pStructure);
        STEP.put(pStructure, pDecorationStage);
        return Registry.register(Registry.STRUCTURE_FEATURE, pName.toLowerCase(Locale.ROOT), pStructure);
    }

    public StructureFeature(Codec<C> p_67039_)
    {
        this.configuredStructureCodec = p_67039_.fieldOf("config").xmap((p_67094_) ->
        {
            return new ConfiguredStructureFeature<>(this, p_67094_);
        }, (p_67064_) ->
        {
            return p_67064_.config;
        }).codec();
    }

    public GenerationStep.Decoration step()
    {
        return STEP.get(this);
    }

    public static void bootstrap()
    {
    }

    @Nullable
    public static StructureStart<?> loadStaticStart(ServerLevel p_160448_, CompoundTag p_160449_, long p_160450_)
    {
        String s = p_160449_.getString("id");

        if ("INVALID".equals(s))
        {
            return StructureStart.INVALID_START;
        }
        else
        {
            StructureFeature<?> structurefeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(s.toLowerCase(Locale.ROOT)));

            if (structurefeature == null)
            {
                LOGGER.error("Unknown feature id: {}", (Object)s);
                return null;
            }
            else
            {
                ChunkPos chunkpos = new ChunkPos(p_160449_.getInt("ChunkX"), p_160449_.getInt("ChunkZ"));
                int i = p_160449_.getInt("references");
                ListTag listtag = p_160449_.getList("Children", 10);

                try
                {
                    StructureStart<?> structurestart = structurefeature.createStart(chunkpos, i, p_160450_);

                    for (int j = 0; j < listtag.size(); ++j)
                    {
                        CompoundTag compoundtag = listtag.getCompound(j);
                        String s1 = compoundtag.getString("id").toLowerCase(Locale.ROOT);
                        ResourceLocation resourcelocation = new ResourceLocation(s1);
                        ResourceLocation resourcelocation1 = RENAMES.getOrDefault(resourcelocation, resourcelocation);
                        StructurePieceType structurepiecetype = Registry.STRUCTURE_PIECE.get(resourcelocation1);

                        if (structurepiecetype == null)
                        {
                            LOGGER.error("Unknown structure piece id: {}", (Object)resourcelocation1);
                        }
                        else
                        {
                            try
                            {
                                StructurePiece structurepiece = structurepiecetype.load(p_160448_, compoundtag);
                                structurestart.addPiece(structurepiece);
                            }
                            catch (Exception exception)
                            {
                                LOGGER.error("Exception loading structure piece with id {}", resourcelocation1, exception);
                            }
                        }
                    }

                    return structurestart;
                }
                catch (Exception exception1)
                {
                    LOGGER.error("Failed Start with id {}", s, exception1);
                    return null;
                }
            }
        }
    }

    public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec()
    {
        return this.configuredStructureCodec;
    }

    public ConfiguredStructureFeature < C, ? extends StructureFeature<C >> configured(C p_67066_)
    {
        return new ConfiguredStructureFeature<>(this, p_67066_);
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(LevelReader p_67047_, StructureFeatureManager p_67048_, BlockPos p_67049_, int p_67050_, boolean p_67051_, long p_67052_, StructureFeatureConfiguration p_67053_)
    {
        int i = p_67053_.spacing();
        int j = SectionPos.blockToSectionCoord(p_67049_.getX());
        int k = SectionPos.blockToSectionCoord(p_67049_.getZ());
        int l = 0;

        for (WorldgenRandom worldgenrandom = new WorldgenRandom(); l <= p_67050_; ++l)
        {
            for (int i1 = -l; i1 <= l; ++i1)
            {
                boolean flag = i1 == -l || i1 == l;

                for (int j1 = -l; j1 <= l; ++j1)
                {
                    boolean flag1 = j1 == -l || j1 == l;

                    if (flag || flag1)
                    {
                        int k1 = j + i * i1;
                        int l1 = k + i * j1;
                        ChunkPos chunkpos = this.getPotentialFeatureChunk(p_67053_, p_67052_, worldgenrandom, k1, l1);
                        boolean flag2 = p_67047_.getBiomeManager().getPrimaryBiomeAtChunk(chunkpos).getGenerationSettings().isValidStart(this);

                        if (flag2)
                        {
                            ChunkAccess chunkaccess = p_67047_.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart<?> structurestart = p_67048_.getStartForFeature(SectionPos.bottomOf(chunkaccess), this, chunkaccess);

                            if (structurestart != null && structurestart.isValid())
                            {
                                if (p_67051_ && structurestart.canBeReferenced())
                                {
                                    structurestart.addReference();
                                    return structurestart.getLocatePos();
                                }

                                if (!p_67051_)
                                {
                                    return structurestart.getLocatePos();
                                }
                            }
                        }

                        if (l == 0)
                        {
                            break;
                        }
                    }
                }

                if (l == 0)
                {
                    break;
                }
            }
        }

        return null;
    }

    protected boolean linearSeparation()
    {
        return true;
    }

    public final ChunkPos getPotentialFeatureChunk(StructureFeatureConfiguration pSeparationSettings, long pSeed, WorldgenRandom p_67070_, int pRand, int pX)
    {
        int i = pSeparationSettings.spacing();
        int j = pSeparationSettings.separation();
        int k = Math.floorDiv(pRand, i);
        int l = Math.floorDiv(pX, i);
        p_67070_.setLargeFeatureWithSalt(pSeed, k, l, pSeparationSettings.salt());
        int i1;
        int j1;

        if (this.linearSeparation())
        {
            i1 = p_67070_.nextInt(i - j);
            j1 = p_67070_.nextInt(i - j);
        }
        else
        {
            i1 = (p_67070_.nextInt(i - j) + p_67070_.nextInt(i - j)) / 2;
            j1 = (p_67070_.nextInt(i - j) + p_67070_.nextInt(i - j)) / 2;
        }

        return new ChunkPos(k * i + i1, l * i + j1);
    }

    protected boolean isFeatureChunk(ChunkGenerator p_160455_, BiomeSource p_160456_, long p_160457_, WorldgenRandom p_160458_, ChunkPos p_160459_, Biome p_160460_, ChunkPos p_160461_, C p_160462_, LevelHeightAccessor p_160463_)
    {
        return true;
    }

    private StructureStart<C> createStart(ChunkPos p_160452_, int p_160453_, long p_160454_)
    {
        return this.getStartFactory().create(this, p_160452_, p_160453_, p_160454_);
    }

    public StructureStart<?> generate(RegistryAccess p_160465_, ChunkGenerator p_160466_, BiomeSource p_160467_, StructureManager p_160468_, long p_160469_, ChunkPos p_160470_, Biome p_160471_, int p_160472_, WorldgenRandom p_160473_, StructureFeatureConfiguration p_160474_, C p_160475_, LevelHeightAccessor p_160476_)
    {
        ChunkPos chunkpos = this.getPotentialFeatureChunk(p_160474_, p_160469_, p_160473_, p_160470_.x, p_160470_.z);

        if (p_160470_.x == chunkpos.x && p_160470_.z == chunkpos.z && this.isFeatureChunk(p_160466_, p_160467_, p_160469_, p_160473_, p_160470_, p_160471_, chunkpos, p_160475_, p_160476_))
        {
            StructureStart<C> structurestart = this.createStart(p_160470_, p_160472_, p_160469_);
            structurestart.generatePieces(p_160465_, p_160466_, p_160468_, p_160470_, p_160471_, p_160475_, p_160476_);

            if (structurestart.isValid())
            {
                return structurestart;
            }
        }

        return StructureStart.INVALID_START;
    }

    public abstract StructureFeature.StructureStartFactory<C> getStartFactory();

    public String getFeatureName()
    {
        return STRUCTURES_REGISTRY.inverse().get(this);
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies()
    {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialAnimals()
    {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialUndergroundWaterAnimals()
    {
        return MobSpawnSettings.EMPTY_MOB_LIST;
    }

    public interface StructureStartFactory<C extends FeatureConfiguration>
    {
        StructureStart<C> create(StructureFeature<C> p_160479_, ChunkPos p_160480_, int p_160481_, long p_160482_);
    }
}
