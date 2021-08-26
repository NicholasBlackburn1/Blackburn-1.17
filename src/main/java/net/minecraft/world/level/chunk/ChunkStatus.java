package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ChunkStatus
{
    private static final EnumSet<Heightmap.Types> PRE_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
    private static final EnumSet<Heightmap.Types> POST_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
    private static final ChunkStatus.LoadingTask PASSTHROUGH_LOAD_TASK = (p_62461_, p_62462_, p_62463_, p_62464_, p_62465_, p_62466_) ->
    {
        if (p_62466_ instanceof ProtoChunk && !p_62466_.getStatus().isOrAfter(p_62461_))
        {
            ((ProtoChunk)p_62466_).setStatus(p_62461_);
        }

        return CompletableFuture.completedFuture(Either.left(p_62466_));
    };
    public static final ChunkStatus EMPTY = registerSimple("empty", (ChunkStatus)null, -1, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156307_, p_156308_, p_156309_, p_156310_, p_156311_) ->
    {
    });
    public static final ChunkStatus STRUCTURE_STARTS = register("structure_starts", EMPTY, 0, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156285_, p_156286_, p_156287_, p_156288_, p_156289_, p_156290_, p_156291_, p_156292_, p_156293_) ->
    {
        if (!p_156293_.getStatus().isOrAfter(p_156285_))
        {
            if (p_156287_.getServer().getWorldData().worldGenSettings().generateFeatures())
            {
                p_156288_.createStructures(p_156287_.registryAccess(), p_156287_.structureFeatureManager(), p_156293_, p_156289_, p_156287_.getSeed());
            }

            if (p_156293_ instanceof ProtoChunk)
            {
                ((ProtoChunk)p_156293_).setStatus(p_156285_);
            }
        }

        return CompletableFuture.completedFuture(Either.left(p_156293_));
    });
    public static final ChunkStatus STRUCTURE_REFERENCES = registerSimple("structure_references", STRUCTURE_STARTS, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156301_, p_156302_, p_156303_, p_156304_, p_156305_) ->
    {
        WorldGenRegion worldgenregion = new WorldGenRegion(p_156302_, p_156304_, p_156301_, -1);
        p_156303_.createReferences(worldgenregion, p_156302_.structureFeatureManager().forWorldGenRegion(worldgenregion), p_156305_);
    });
    public static final ChunkStatus BIOMES = registerSimple("biomes", STRUCTURE_REFERENCES, 0, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156295_, p_156296_, p_156297_, p_156298_, p_156299_) ->
    {
        p_156297_.createBiomes(p_156296_.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), p_156299_);
    });
    public static final ChunkStatus NOISE = register("noise", BIOMES, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156269_, p_156270_, p_156271_, p_156272_, p_156273_, p_156274_, p_156275_, p_156276_, p_156277_) ->
    {
        if (!p_156277_.getStatus().isOrAfter(p_156269_))
        {
            WorldGenRegion worldgenregion = new WorldGenRegion(p_156271_, p_156276_, p_156269_, 0);
            return p_156272_.fillFromNoise(p_156270_, p_156271_.structureFeatureManager().forWorldGenRegion(worldgenregion), p_156277_).thenApply((p_156235_) ->
            {
                if (p_156235_ instanceof ProtoChunk)
                {
                    ((ProtoChunk)p_156235_).setStatus(p_156269_);
                }

                return Either.left(p_156235_);
            });
        }
        else {
            return CompletableFuture.completedFuture(Either.left(p_156277_));
        }
    });
    public static final ChunkStatus SURFACE = registerSimple("surface", NOISE, 0, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156279_, p_156280_, p_156281_, p_156282_, p_156283_) ->
    {
        p_156281_.buildSurfaceAndBedrock(new WorldGenRegion(p_156280_, p_156282_, p_156279_, 0), p_156283_);
    });
    public static final ChunkStatus CARVERS = registerSimple("carvers", SURFACE, 0, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156263_, p_156264_, p_156265_, p_156266_, p_156267_) ->
    {
        p_156265_.applyCarvers(p_156264_.getSeed(), p_156264_.getBiomeManager(), p_156267_, GenerationStep.Carving.AIR);
    });
    public static final ChunkStatus LIQUID_CARVERS = registerSimple("liquid_carvers", CARVERS, 0, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156247_, p_156248_, p_156249_, p_156250_, p_156251_) ->
    {
        p_156249_.applyCarvers(p_156248_.getSeed(), p_156248_.getBiomeManager(), p_156251_, GenerationStep.Carving.LIQUID);
    });
    public static final ChunkStatus FEATURES = register("features", LIQUID_CARVERS, 8, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156253_, p_156254_, p_156255_, p_156256_, p_156257_, p_156258_, p_156259_, p_156260_, p_156261_) ->
    {
        ProtoChunk protochunk = (ProtoChunk)p_156261_;
        protochunk.setLightEngine(p_156258_);

        if (!p_156261_.getStatus().isOrAfter(p_156253_))
        {
            Heightmap.primeHeightmaps(p_156261_, EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE));
            WorldGenRegion worldgenregion = new WorldGenRegion(p_156255_, p_156260_, p_156253_, 1);
            p_156256_.applyBiomeDecoration(worldgenregion, p_156255_.structureFeatureManager().forWorldGenRegion(worldgenregion));
            protochunk.setStatus(p_156253_);
        }

        return CompletableFuture.completedFuture(Either.left(p_156261_));
    });
    public static final ChunkStatus LIGHT = register("light", FEATURES, 1, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156237_, p_156238_, p_156239_, p_156240_, p_156241_, p_156242_, p_156243_, p_156244_, p_156245_) ->
    {
        return lightChunk(p_156237_, p_156242_, p_156245_);
    }, (p_156227_, p_156228_, p_156229_, p_156230_, p_156231_, p_156232_) ->
    {
        return lightChunk(p_156227_, p_156230_, p_156232_);
    });
    public static final ChunkStatus SPAWN = registerSimple("spawn", LIGHT, 0, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156221_, p_156222_, p_156223_, p_156224_, p_156225_) ->
    {
        p_156223_.spawnOriginalMobs(new WorldGenRegion(p_156222_, p_156224_, p_156221_, -1));
    });
    public static final ChunkStatus HEIGHTMAPS = registerSimple("heightmaps", SPAWN, 0, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156188_, p_156189_, p_156190_, p_156191_, p_156192_) ->
    {
    });
    public static final ChunkStatus FULL = register("full", HEIGHTMAPS, 0, POST_FEATURES, ChunkStatus.ChunkType.LEVELCHUNK, (p_156201_, p_156202_, p_156203_, p_156204_, p_156205_, p_156206_, p_156207_, p_156208_, p_156209_) ->
    {
        return p_156207_.apply(p_156209_);
    }, (p_156194_, p_156195_, p_156196_, p_156197_, p_156198_, p_156199_) ->
    {
        return p_156198_.apply(p_156199_);
    });
    private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(FULL, FEATURES, LIQUID_CARVERS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS);
    private static final IntList RANGE_BY_STATUS = Util.make(new IntArrayList(getStatusList().size()), (p_156211_) ->
    {
        int i = 0;

        for (int j = getStatusList().size() - 1; j >= 0; --j)
        {
            while (i + 1 < STATUS_BY_RANGE.size() && j <= STATUS_BY_RANGE.get(i + 1).getIndex())
            {
                ++i;
            }

            p_156211_.add(0, i);
        }
    });
    private final String name;
    private final int index;
    private final ChunkStatus parent;
    private final ChunkStatus.GenerationTask generationTask;
    private final ChunkStatus.LoadingTask loadingTask;
    private final int range;
    private final ChunkStatus.ChunkType chunkType;
    private final EnumSet<Heightmap.Types> heightmapsAfter;

    private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> lightChunk(ChunkStatus pStatus, ThreadedLevelLightEngine pLightManager, ChunkAccess pChunk)
    {
        boolean flag = isLighted(pStatus, pChunk);

        if (!pChunk.getStatus().isOrAfter(pStatus))
        {
            ((ProtoChunk)pChunk).setStatus(pStatus);
        }

        return pLightManager.lightChunk(pChunk, flag).thenApply(Either::left);
    }

    private static ChunkStatus registerSimple(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, EnumSet<Heightmap.Types> pHeightmaps, ChunkStatus.ChunkType pType, ChunkStatus.SimpleGenerationTask pGenerationWorker)
    {
        return register(pKey, pParent, pTaskRange, pHeightmaps, pType, pGenerationWorker);
    }

    private static ChunkStatus register(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, EnumSet<Heightmap.Types> pHeightmaps, ChunkStatus.ChunkType pType, ChunkStatus.GenerationTask pGenerationWorker)
    {
        return register(pKey, pParent, pTaskRange, pHeightmaps, pType, pGenerationWorker, PASSTHROUGH_LOAD_TASK);
    }

    private static ChunkStatus register(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, EnumSet<Heightmap.Types> pHeightmaps, ChunkStatus.ChunkType pType, ChunkStatus.GenerationTask pGenerationWorker, ChunkStatus.LoadingTask p_62413_)
    {
        return Registry.register(Registry.CHUNK_STATUS, pKey, new ChunkStatus(pKey, pParent, pTaskRange, pHeightmaps, pType, pGenerationWorker, p_62413_));
    }

    public static List<ChunkStatus> getStatusList()
    {
        List<ChunkStatus> list = Lists.newArrayList();
        ChunkStatus chunkstatus;

        for (chunkstatus = FULL; chunkstatus.getParent() != chunkstatus; chunkstatus = chunkstatus.getParent())
        {
            list.add(chunkstatus);
        }

        list.add(chunkstatus);
        Collections.reverse(list);
        return list;
    }

    private static boolean isLighted(ChunkStatus pStatus, ChunkAccess pChunk)
    {
        return pChunk.getStatus().isOrAfter(pStatus) && pChunk.isLightCorrect();
    }

    public static ChunkStatus getStatusAroundFullChunk(int p_156186_)
    {
        if (p_156186_ >= STATUS_BY_RANGE.size())
        {
            return EMPTY;
        }
        else
        {
            return p_156186_ < 0 ? FULL : STATUS_BY_RANGE.get(p_156186_);
        }
    }

    public static int maxDistance()
    {
        return STATUS_BY_RANGE.size();
    }

    public static int getDistance(ChunkStatus pStatus)
    {
        return RANGE_BY_STATUS.getInt(pStatus.getIndex());
    }

    ChunkStatus(String p_62342_, @Nullable ChunkStatus p_62343_, int p_62344_, EnumSet<Heightmap.Types> p_62345_, ChunkStatus.ChunkType p_62346_, ChunkStatus.GenerationTask p_62347_, ChunkStatus.LoadingTask p_62348_)
    {
        this.name = p_62342_;
        this.parent = p_62343_ == null ? this : p_62343_;
        this.generationTask = p_62347_;
        this.loadingTask = p_62348_;
        this.range = p_62344_;
        this.chunkType = p_62346_;
        this.heightmapsAfter = p_62345_;
        this.index = p_62343_ == null ? 0 : p_62343_.getIndex() + 1;
    }

    public int getIndex()
    {
        return this.index;
    }

    public String getName()
    {
        return this.name;
    }

    public ChunkStatus getParent()
    {
        return this.parent;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> generate(Executor p_156213_, ServerLevel p_156214_, ChunkGenerator p_156215_, StructureManager p_156216_, ThreadedLevelLightEngine p_156217_, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> p_156218_, List<ChunkAccess> p_156219_)
    {
        return this.generationTask.doWork(this, p_156213_, p_156214_, p_156215_, p_156216_, p_156217_, p_156218_, p_156219_, p_156219_.get(p_156219_.size() / 2));
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> load(ServerLevel pLevel, StructureManager pTemplateManager, ThreadedLevelLightEngine pLightManager, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pLoadingFunction, ChunkAccess pLoadingChunk)
    {
        return this.loadingTask.doWork(this, pLevel, pTemplateManager, pLightManager, pLoadingFunction, pLoadingChunk);
    }

    public int getRange()
    {
        return this.range;
    }

    public ChunkStatus.ChunkType getChunkType()
    {
        return this.chunkType;
    }

    public static ChunkStatus byName(String pLocation)
    {
        return Registry.CHUNK_STATUS.get(ResourceLocation.tryParse(pLocation));
    }

    public EnumSet<Heightmap.Types> heightmapsAfter()
    {
        return this.heightmapsAfter;
    }

    public boolean isOrAfter(ChunkStatus pStatus)
    {
        return this.getIndex() >= pStatus.getIndex();
    }

    public String toString()
    {
        return Registry.CHUNK_STATUS.getKey(this).toString();
    }

    public static enum ChunkType
    {
        PROTOCHUNK,
        LEVELCHUNK;
    }

    interface GenerationTask
    {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus p_156313_, Executor p_156314_, ServerLevel p_156315_, ChunkGenerator p_156316_, StructureManager p_156317_, ThreadedLevelLightEngine p_156318_, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> p_156319_, List<ChunkAccess> p_156320_, ChunkAccess p_156321_);
    }

    interface LoadingTask
    {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus p_62531_, ServerLevel p_62532_, StructureManager p_62533_, ThreadedLevelLightEngine p_62534_, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> p_62535_, ChunkAccess p_62536_);
    }

    interface SimpleGenerationTask extends ChunkStatus.GenerationTask
    {
    default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus p_156329_, Executor p_156330_, ServerLevel p_156331_, ChunkGenerator p_156332_, StructureManager p_156333_, ThreadedLevelLightEngine p_156334_, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> p_156335_, List<ChunkAccess> p_156336_, ChunkAccess p_156337_)
        {
            if (!p_156337_.getStatus().isOrAfter(p_156329_))
            {
                this.doWork(p_156329_, p_156331_, p_156332_, p_156336_, p_156337_);

                if (p_156337_ instanceof ProtoChunk)
                {
                    ((ProtoChunk)p_156337_).setStatus(p_156329_);
                }
            }

            return CompletableFuture.completedFuture(Either.left(p_156337_));
        }

        void doWork(ChunkStatus p_156323_, ServerLevel p_156324_, ChunkGenerator p_156325_, List<ChunkAccess> p_156326_, ChunkAccess p_156327_);
    }
}
