package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Features;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Snooper;
import net.minecraft.world.SnooperPopulator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements SnooperPopulator, CommandSource, AutoCloseable
{
    static final Logger LOGGER = LogManager.getLogger();
    private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8F;
    private static final int TICK_STATS_SPAN = 100;
    public static final int MS_PER_TICK = 50;
    private static final int SNOOPER_UPDATE_INTERVAL = 6000;
    private static final int OVERLOADED_THRESHOLD = 2000;
    private static final int OVERLOADED_WARNING_INTERVAL = 15000;
    public static final String LEVEL_STORAGE_PROTOCOL = "level";
    public static final String LEVEL_STORAGE_SCHEMA = "level://";
    private static final long STATUS_EXPIRE_TIME_NS = 5000000000L;
    private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
    public static final String MAP_RESOURCE_FILE = "resources.zip";
    public static final File USERID_CACHE_FILE = new File("usercache.json");
    public static final int START_CHUNK_RADIUS = 11;
    private static final int START_TICKING_CHUNK_COUNT = 441;
    private static final int AUTOSAVE_INTERVAL = 6000;
    private static final int MAX_TICK_LATENCY = 3;
    public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
    public static final LevelSettings DEMO_SETTINGS = new LevelSettings("Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), DataPackConfig.DEFAULT);
    private static final long DELAYED_TASKS_TICK_EXTENSION = 50L;
    protected final LevelStorageSource.LevelStorageAccess storageSource;
    protected final PlayerDataStorage playerDataStorage;
    private final Snooper snooper = new Snooper("server", this, Util.getMillis());
    private final List<Runnable> tickables = Lists.newArrayList();
    private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    private ProfilerFiller profiler = this.metricsRecorder.getProfiler();
    private Consumer<ProfileResults> onMetricsRecordingStopped = (p_177903_) ->
    {
        this.stopRecordingMetrics();
    };
    private Consumer<Path> onMetricsRecordingFinished = (p_177954_) ->
    {
    };
    private boolean willStartRecordingMetrics;
    @Nullable
    private MinecraftServer.TimeProfiler debugCommandProfiler;
    private boolean debugCommandProfilerDelayStart;
    private final ServerConnectionListener connection;
    private final ChunkProgressListenerFactory progressListenerFactory;
    private final ServerStatus status = new ServerStatus();
    private final Random random = new Random();
    private final DataFixer fixerUpper;
    private String localIp;
    private int port = -1;
    protected final RegistryAccess.RegistryHolder registryHolder;
    private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.newLinkedHashMap();
    private PlayerList playerList;
    private volatile boolean running = true;
    private boolean stopped;
    private int tickCount;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean pvp;
    private boolean allowFlight;
    @Nullable
    private String motd;
    private int playerIdleTimeout;
    public final long[] tickTimes = new long[100];
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private String singleplayerName;
    private boolean isDemo;
    private String resourcePack = "";
    private String resourcePackHash = "";
    private volatile boolean isReady;
    private long lastOverloadWarning;
    private final MinecraftSessionService sessionService;
    @Nullable
    private final GameProfileRepository profileRepository;
    @Nullable
    private final GameProfileCache profileCache;
    private long lastServerStatus;
    private final Thread serverThread;
    private long nextTickTime = Util.getMillis();
    private long delayedTasksMaxNextTickTime;
    private boolean mayHaveDelayedTasks;
    private final PackRepository packRepository;
    private final ServerScoreboard scoreboard = new ServerScoreboard(this);
    @Nullable
    private CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents = new CustomBossEvents();
    private final ServerFunctionManager functionManager;
    private final FrameTimer frameTimer = new FrameTimer();
    private boolean enforceWhitelist;
    private float averageTickTime;
    private final Executor executor;
    @Nullable
    private String serverId;
    private ServerResources resources;
    private final StructureManager structureManager;
    protected final WorldData worldData;

    public static <S extends MinecraftServer> S spin(Function<Thread, S> pThreadFunction)
    {
        AtomicReference<S> atomicreference = new AtomicReference<>();
        Thread thread = new Thread(() ->
        {
            atomicreference.get().runServer();
        }, "Server thread");
        thread.setUncaughtExceptionHandler((p_177909_, p_177910_) ->
        {
            LOGGER.error(p_177910_);
        });
        S s = pThreadFunction.apply(thread);
        atomicreference.set(s);
        thread.start();
        return s;
    }

    public MinecraftServer(Thread p_129769_, RegistryAccess.RegistryHolder p_129770_, LevelStorageSource.LevelStorageAccess p_129771_, WorldData p_129772_, PackRepository p_129773_, Proxy p_129774_, DataFixer p_129775_, ServerResources p_129776_, @Nullable MinecraftSessionService p_129777_, @Nullable GameProfileRepository p_129778_, @Nullable GameProfileCache p_129779_, ChunkProgressListenerFactory p_129780_)
    {
        super("Server");
        this.registryHolder = p_129770_;
        this.worldData = p_129772_;
        this.proxy = p_129774_;
        this.packRepository = p_129773_;
        this.resources = p_129776_;
        this.sessionService = p_129777_;
        this.profileRepository = p_129778_;
        this.profileCache = p_129779_;

        if (p_129779_ != null)
        {
            p_129779_.setExecutor(this);
        }

        this.connection = new ServerConnectionListener(this);
        this.progressListenerFactory = p_129780_;
        this.storageSource = p_129771_;
        this.playerDataStorage = p_129771_.createPlayerStorage();
        this.fixerUpper = p_129775_;
        this.functionManager = new ServerFunctionManager(this, p_129776_.getFunctionLibrary());
        this.structureManager = new StructureManager(p_129776_.getResourceManager(), p_129771_, p_129775_);
        this.serverThread = p_129769_;
        this.executor = Util.backgroundExecutor();
    }

    private void readScoreboard(DimensionDataStorage p_129842_)
    {
        p_129842_.computeIfAbsent(this.getScoreboard()::createData, this.getScoreboard()::createData, "scoreboard");
    }

    protected abstract boolean initServer() throws IOException;

    public static void convertFromRegionFormatIfNeeded(LevelStorageSource.LevelStorageAccess p_129846_)
    {
        if (p_129846_.requiresConversion())
        {
            LOGGER.info("Converting map!");
            p_129846_.convertLevel(new ProgressListener()
            {
                private long timeStamp = Util.getMillis();
                public void progressStartNoAbort(Component pComponent)
                {
                }
                public void progressStart(Component pComponent)
                {
                }
                public void progressStagePercentage(int pProgress)
                {
                    if (Util.getMillis() - this.timeStamp >= 1000L)
                    {
                        this.timeStamp = Util.getMillis();
                        MinecraftServer.LOGGER.info("Converting... {}%", (int)pProgress);
                    }
                }
                public void stop()
                {
                }
                public void progressStage(Component pComponent)
                {
                }
            });
        }
    }

    protected void loadLevel()
    {
        this.detectBundledResources();
        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().isPresent());
        ChunkProgressListener chunkprogresslistener = this.progressListenerFactory.create(11);
        this.createLevels(chunkprogresslistener);
        this.forceDifficulty();
        this.prepareLevels(chunkprogresslistener);
    }

    protected void forceDifficulty()
    {
    }

    protected void createLevels(ChunkProgressListener p_129816_)
    {
        ServerLevelData serverleveldata = this.worldData.overworldData();
        WorldGenSettings worldgensettings = this.worldData.worldGenSettings();
        boolean flag = worldgensettings.isDebug();
        long i = worldgensettings.seed();
        long j = BiomeManager.obfuscateSeed(i);
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(serverleveldata));
        MappedRegistry<LevelStem> mappedregistry = worldgensettings.dimensions();
        LevelStem levelstem = mappedregistry.get(LevelStem.OVERWORLD);
        ChunkGenerator chunkgenerator;
        DimensionType dimensiontype;

        if (levelstem == null)
        {
            dimensiontype = this.registryHolder.<DimensionType>registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getOrThrow(DimensionType.OVERWORLD_LOCATION);
            chunkgenerator = WorldGenSettings.makeDefaultOverworld(this.registryHolder.registryOrThrow(Registry.BIOME_REGISTRY), this.registryHolder.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY), (new Random()).nextLong());
        }
        else
        {
            dimensiontype = levelstem.type();
            chunkgenerator = levelstem.generator();
        }

        ServerLevel serverlevel = new ServerLevel(this, this.executor, this.storageSource, serverleveldata, Level.OVERWORLD, dimensiontype, p_129816_, chunkgenerator, flag, j, list, true);
        this.levels.put(Level.OVERWORLD, serverlevel);
        DimensionDataStorage dimensiondatastorage = serverlevel.getDataStorage();
        this.readScoreboard(dimensiondatastorage);
        this.commandStorage = new CommandStorage(dimensiondatastorage);
        WorldBorder worldborder = serverlevel.getWorldBorder();
        worldborder.applySettings(serverleveldata.getWorldBorder());

        if (!serverleveldata.isInitialized())
        {
            try
            {
                setInitialSpawn(serverlevel, serverleveldata, worldgensettings.generateBonusChest(), flag);
                serverleveldata.setInitialized(true);

                if (flag)
                {
                    this.setupDebugLevel(this.worldData);
                }
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable1, "Exception initializing level");

                try
                {
                    serverlevel.fillReportDetails(crashreport);
                }
                catch (Throwable throwable)
                {
                }

                throw new ReportedException(crashreport);
            }

            serverleveldata.setInitialized(true);
        }

        this.getPlayerList().setLevel(serverlevel);

        if (this.worldData.getCustomBossEvents() != null)
        {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
        }

        for (Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedregistry.entrySet())
        {
            ResourceKey<LevelStem> resourcekey = entry.getKey();

            if (resourcekey != LevelStem.OVERWORLD)
            {
                ResourceKey<Level> resourcekey1 = ResourceKey.create(Registry.DIMENSION_REGISTRY, resourcekey.location());
                DimensionType dimensiontype1 = entry.getValue().type();
                ChunkGenerator chunkgenerator1 = entry.getValue().generator();
                DerivedLevelData derivedleveldata = new DerivedLevelData(this.worldData, serverleveldata);
                ServerLevel serverlevel1 = new ServerLevel(this, this.executor, this.storageSource, derivedleveldata, resourcekey1, dimensiontype1, p_129816_, chunkgenerator1, flag, j, ImmutableList.of(), false);
                worldborder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverlevel1.getWorldBorder()));
                this.levels.put(resourcekey1, serverlevel1);
            }
        }
    }

    private static void setInitialSpawn(ServerLevel p_177897_, ServerLevelData p_177898_, boolean p_177899_, boolean p_177900_)
    {
        if (p_177900_)
        {
            p_177898_.setSpawn(BlockPos.ZERO.above(80), 0.0F);
        }
        else
        {
            ChunkGenerator chunkgenerator = p_177897_.getChunkSource().getGenerator();
            BiomeSource biomesource = chunkgenerator.getBiomeSource();
            Random random = new Random(p_177897_.getSeed());
            BlockPos blockpos = biomesource.findBiomeHorizontal(0, p_177897_.getSeaLevel(), 0, 256, (p_177905_) ->
            {
                return p_177905_.getMobSettings().playerSpawnFriendly();
            }, random);
            ChunkPos chunkpos = blockpos == null ? new ChunkPos(0, 0) : new ChunkPos(blockpos);

            if (blockpos == null)
            {
                LOGGER.warn("Unable to find spawn biome");
            }

            boolean flag = false;

            for (Block block : BlockTags.VALID_SPAWN.getValues())
            {
                if (biomesource.getSurfaceBlocks().contains(block.defaultBlockState()))
                {
                    flag = true;
                    break;
                }
            }

            int j1 = chunkgenerator.getSpawnHeight(p_177897_);

            if (j1 < p_177897_.getMinBuildHeight())
            {
                BlockPos blockpos2 = chunkpos.getWorldPosition();
                j1 = p_177897_.getHeight(Heightmap.Types.WORLD_SURFACE, blockpos2.getX() + 8, blockpos2.getZ() + 8);
            }

            p_177898_.setSpawn(chunkpos.getWorldPosition().offset(8, j1, 8), 0.0F);
            int k1 = 0;
            int i = 0;
            int j = 0;
            int k = -1;
            int l = 32;

            for (int i1 = 0; i1 < 1024; ++i1)
            {
                if (k1 > -16 && k1 <= 16 && i > -16 && i <= 16)
                {
                    BlockPos blockpos1 = PlayerRespawnLogic.getSpawnPosInChunk(p_177897_, new ChunkPos(chunkpos.x + k1, chunkpos.z + i), flag);

                    if (blockpos1 != null)
                    {
                        p_177898_.setSpawn(blockpos1, 0.0F);
                        break;
                    }
                }

                if (k1 == i || k1 < 0 && k1 == -i || k1 > 0 && k1 == 1 - i)
                {
                    int l1 = j;
                    j = -k;
                    k = l1;
                }

                k1 += j;
                i += k;
            }

            if (p_177899_)
            {
                ConfiguredFeature <? , ? > configuredfeature = Features.BONUS_CHEST;
                configuredfeature.place(p_177897_, chunkgenerator, p_177897_.random, new BlockPos(p_177898_.getXSpawn(), p_177898_.getYSpawn(), p_177898_.getZSpawn()));
            }
        }
    }

    private void setupDebugLevel(WorldData p_129848_)
    {
        p_129848_.setDifficulty(Difficulty.PEACEFUL);
        p_129848_.setDifficultyLocked(true);
        ServerLevelData serverleveldata = p_129848_.overworldData();
        serverleveldata.setRaining(false);
        serverleveldata.setThundering(false);
        serverleveldata.setClearWeatherTime(1000000000);
        serverleveldata.setDayTime(6000L);
        serverleveldata.setGameType(GameType.SPECTATOR);
    }

    private void prepareLevels(ChunkProgressListener p_129941_)
    {
        ServerLevel serverlevel = this.overworld();
        LOGGER.info("Preparing start region for dimension {}", (Object)serverlevel.dimension().location());
        BlockPos blockpos = serverlevel.getSharedSpawnPos();
        p_129941_.updateSpawnPos(new ChunkPos(blockpos));
        ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
        serverchunkcache.getLightEngine().setTaskPerBatch(500);
        this.nextTickTime = Util.getMillis();
        serverchunkcache.addRegionTicket(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);

        while (serverchunkcache.getTickingGenerated() != 441)
        {
            this.nextTickTime = Util.getMillis() + 10L;
            this.waitUntilNextTick();
        }

        this.nextTickTime = Util.getMillis() + 10L;
        this.waitUntilNextTick();

        for (ServerLevel serverlevel1 : this.levels.values())
        {
            ForcedChunksSavedData forcedchunkssaveddata = serverlevel1.getDataStorage().get(ForcedChunksSavedData::load, "chunks");

            if (forcedchunkssaveddata != null)
            {
                LongIterator longiterator = forcedchunkssaveddata.getChunks().iterator();

                while (longiterator.hasNext())
                {
                    long i = longiterator.nextLong();
                    ChunkPos chunkpos = new ChunkPos(i);
                    serverlevel1.getChunkSource().updateChunkForced(chunkpos, true);
                }
            }
        }

        this.nextTickTime = Util.getMillis() + 10L;
        this.waitUntilNextTick();
        p_129941_.stop();
        serverchunkcache.getLightEngine().setTaskPerBatch(5);
        this.updateMobSpawningFlags();
    }

    protected void detectBundledResources()
    {
        File file1 = this.storageSource.getLevelPath(LevelResource.MAP_RESOURCE_FILE).toFile();

        if (file1.isFile())
        {
            String s = this.storageSource.getLevelId();

            try
            {
                this.setResourcePack("level://" + URLEncoder.encode(s, StandardCharsets.UTF_8.toString()) + "/resources.zip", "");
            }
            catch (UnsupportedEncodingException unsupportedencodingexception)
            {
                LOGGER.warn("Something went wrong url encoding {}", (Object)s);
            }
        }
    }

    public GameType getDefaultGameType()
    {
        return this.worldData.getGameType();
    }

    public boolean isHardcore()
    {
        return this.worldData.isHardcore();
    }

    public abstract int getOperatorUserPermissionLevel();

    public abstract int getFunctionCompilationLevel();

    public abstract boolean shouldRconBroadcast();

    public boolean saveAllChunks(boolean pSuppressLog, boolean pFlush, boolean pForced)
    {
        boolean flag = false;

        for (ServerLevel serverlevel : this.getAllLevels())
        {
            if (!pSuppressLog)
            {
                LOGGER.info("Saving chunks for level '{}'/{}", serverlevel, serverlevel.dimension().location());
            }

            serverlevel.save((ProgressListener)null, pFlush, serverlevel.noSave && !pForced);
            flag = true;
        }

        ServerLevel serverlevel2 = this.overworld();
        ServerLevelData serverleveldata = this.worldData.overworldData();
        serverleveldata.setWorldBorder(serverlevel2.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
        this.storageSource.saveDataTag(this.registryHolder, this.worldData, this.getPlayerList().getSingleplayerData());

        if (pFlush)
        {
            for (ServerLevel serverlevel1 : this.getAllLevels())
            {
                LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", (Object)serverlevel1.getChunkSource().chunkMap.m_182285_());
            }

            LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
        }

        return flag;
    }

    public void close()
    {
        this.stopServer();
    }

    public void stopServer()
    {
        LOGGER.info("Stopping server");

        if (this.getConnection() != null)
        {
            this.getConnection().stop();
        }

        if (this.playerList != null)
        {
            LOGGER.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll();
        }

        LOGGER.info("Saving worlds");

        for (ServerLevel serverlevel : this.getAllLevels())
        {
            if (serverlevel != null)
            {
                serverlevel.noSave = false;
            }
        }

        this.saveAllChunks(false, true, false);

        for (ServerLevel serverlevel1 : this.getAllLevels())
        {
            if (serverlevel1 != null)
            {
                try
                {
                    serverlevel1.close();
                }
                catch (IOException ioexception1)
                {
                    LOGGER.error("Exception closing the level", (Throwable)ioexception1);
                }
            }
        }

        if (this.snooper.isStarted())
        {
            this.snooper.interrupt();
        }

        this.resources.close();

        try
        {
            this.storageSource.close();
        }
        catch (IOException ioexception)
        {
            LOGGER.error("Failed to unlock level {}", this.storageSource.getLevelId(), ioexception);
        }
    }

    public String getLocalIp()
    {
        return this.localIp;
    }

    public void setLocalIp(String pHost)
    {
        this.localIp = pHost;
    }

    public boolean isRunning()
    {
        return this.running;
    }

    public void halt(boolean pWaitForServer)
    {
        this.running = false;

        if (pWaitForServer)
        {
            try
            {
                this.serverThread.join();
            }
            catch (InterruptedException interruptedexception)
            {
                LOGGER.error("Error while shutting down", (Throwable)interruptedexception);
            }
        }
    }

    protected void runServer()
    {
        try
        {
            if (this.initServer())
            {
                this.nextTickTime = Util.getMillis();
                this.status.setDescription(new TextComponent(this.motd));
                this.status.setVersion(new ServerStatus.Version(SharedConstants.getCurrentVersion().getName(), SharedConstants.getCurrentVersion().getProtocolVersion()));
                this.updateStatusIcon(this.status);

                while (this.running)
                {
                    long i = Util.getMillis() - this.nextTickTime;

                    if (i > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L)
                    {
                        long j = i / 50L;
                        LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", i, j);
                        this.nextTickTime += j * 50L;
                        this.lastOverloadWarning = this.nextTickTime;
                    }

                    if (this.debugCommandProfilerDelayStart)
                    {
                        this.debugCommandProfilerDelayStart = false;
                        this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
                    }

                    this.nextTickTime += 50L;
                    this.startMetricsRecordingTick();
                    this.profiler.push("tick");
                    this.tickServer(this::haveTime);
                    this.profiler.popPush("nextTickWait");
                    this.mayHaveDelayedTasks = true;
                    this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                    this.waitUntilNextTick();
                    this.profiler.pop();
                    this.endMetricsRecordingTick();
                    this.isReady = true;
                }
            }
            else
            {
                this.onServerCrash((CrashReport)null);
            }
        }
        catch (Throwable throwable1)
        {
            LOGGER.error("Encountered an unexpected exception", throwable1);
            CrashReport crashreport;

            if (throwable1 instanceof ReportedException)
            {
                crashreport = ((ReportedException)throwable1).getReport();
            }
            else
            {
                crashreport = new CrashReport("Exception in server tick loop", throwable1);
            }

            this.fillSystemReport(crashreport.getSystemReport());
            File file1 = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

            if (crashreport.saveToFile(file1))
            {
                LOGGER.error("This crash report has been saved to: {}", (Object)file1.getAbsolutePath());
            }
            else
            {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.onServerCrash(crashreport);
        }
        finally
        {
            try
            {
                this.stopped = true;
                this.stopServer();
            }
            catch (Throwable throwable)
            {
                LOGGER.error("Exception stopping the server", throwable);
            }
            finally
            {
                this.onServerExit();
            }
        }
    }

    private boolean haveTime()
    {
        return this.runningTask() || Util.getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
    }

    protected void waitUntilNextTick()
    {
        this.runAllTasks();
        this.managedBlock(() ->
        {
            return !this.haveTime();
        });
    }

    protected TickTask wrapRunnable(Runnable pRunnable)
    {
        return new TickTask(this.tickCount, pRunnable);
    }

    protected boolean shouldRun(TickTask pRunnable)
    {
        return pRunnable.getTick() + 3 < this.tickCount || this.haveTime();
    }

    public boolean pollTask()
    {
        boolean flag = this.pollTaskInternal();
        this.mayHaveDelayedTasks = flag;
        return flag;
    }

    private boolean pollTaskInternal()
    {
        if (super.pollTask())
        {
            return true;
        }
        else
        {
            if (this.haveTime())
            {
                for (ServerLevel serverlevel : this.getAllLevels())
                {
                    if (serverlevel.getChunkSource().pollTask())
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public void doRunTask(TickTask pTask)
    {
        this.getProfiler().incrementCounter("runTask");
        super.doRunTask(pTask);
    }

    private void updateStatusIcon(ServerStatus pResponse)
    {
        Optional<File> optional = Optional.of(this.getFile("server-icon.png")).filter(File::isFile);

        if (!optional.isPresent())
        {
            optional = this.storageSource.m_182514_().map(Path::toFile).filter(File::isFile);
        }

        optional.ifPresent((p_182663_) ->
        {
            try {
                BufferedImage bufferedimage = ImageIO.read(p_182663_);
                Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                ImageIO.write(bufferedimage, "PNG", bytearrayoutputstream);
                byte[] abyte = Base64.getEncoder().encode(bytearrayoutputstream.toByteArray());
                pResponse.setFavicon("data:image/png;base64," + new String(abyte, StandardCharsets.UTF_8));
            }
            catch (Exception exception)
            {
                LOGGER.error("Couldn't load server icon", (Throwable)exception);
            }
        });
    }

    public Optional<Path> m_182649_()
    {
        return this.storageSource.m_182514_();
    }

    public File getServerDirectory()
    {
        return new File(".");
    }

    protected void onServerCrash(CrashReport pReport)
    {
    }

    public void onServerExit()
    {
    }

    public void tickServer(BooleanSupplier pHasTimeLeft)
    {
        long i = Util.getNanos();
        ++this.tickCount;
        this.tickChildren(pHasTimeLeft);

        if (i - this.lastServerStatus >= 5000000000L)
        {
            this.lastServerStatus = i;
            this.status.setPlayers(new ServerStatus.Players(this.getMaxPlayers(), this.getPlayerCount()));
            GameProfile[] agameprofile = new GameProfile[Math.min(this.getPlayerCount(), 12)];
            int j = Mth.nextInt(this.random, 0, this.getPlayerCount() - agameprofile.length);

            for (int k = 0; k < agameprofile.length; ++k)
            {
                agameprofile[k] = this.playerList.getPlayers().get(j + k).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(agameprofile));
            this.status.getPlayers().m_134924_(agameprofile);
        }

        if (this.tickCount % 6000 == 0)
        {
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.playerList.saveAll();
            this.saveAllChunks(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }

        this.profiler.push("snooper");

        if (!this.snooper.isStarted() && this.tickCount > 100)
        {
            this.snooper.start();
        }

        if (this.tickCount % 6000 == 0)
        {
            this.snooper.prepare();
        }

        this.profiler.pop();
        this.profiler.push("tallying");
        long l = this.tickTimes[this.tickCount % 100] = Util.getNanos() - i;
        this.averageTickTime = this.averageTickTime * 0.8F + (float)l / 1000000.0F * 0.19999999F;
        long i1 = Util.getNanos();
        this.frameTimer.logFrameDuration(i1 - i);
        this.profiler.pop();
    }

    public void tickChildren(BooleanSupplier pHasTimeLeft)
    {
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");

        for (ServerLevel serverlevel : this.getAllLevels())
        {
            this.profiler.push(() ->
            {
                return serverlevel + " " + serverlevel.dimension().location();
            });

            if (this.tickCount % 20 == 0)
            {
                this.profiler.push("timeSync");
                this.playerList.broadcastAll(new ClientboundSetTimePacket(serverlevel.getGameTime(), serverlevel.getDayTime(), serverlevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), serverlevel.dimension());
                this.profiler.pop();
            }

            this.profiler.push("tick");

            try
            {
                serverlevel.tick(pHasTimeLeft);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception ticking world");
                serverlevel.fillReportDetails(crashreport);
                throw new ReportedException(crashreport);
            }

            this.profiler.pop();
            this.profiler.pop();
        }

        this.profiler.popPush("connection");
        this.getConnection().tick();
        this.profiler.popPush("players");
        this.playerList.tick();

        if (SharedConstants.IS_RUNNING_IN_IDE)
        {
            GameTestTicker.SINGLETON.tick();
        }

        this.profiler.popPush("server gui refresh");

        for (int i = 0; i < this.tickables.size(); ++i)
        {
            this.tickables.get(i).run();
        }

        this.profiler.pop();
    }

    public boolean isNetherEnabled()
    {
        return true;
    }

    public void addTickable(Runnable pTickable)
    {
        this.tickables.add(pTickable);
    }

    protected void setId(String pServerId)
    {
        this.serverId = pServerId;
    }

    public boolean isShutdown()
    {
        return !this.serverThread.isAlive();
    }

    public File getFile(String pFileName)
    {
        return new File(this.getServerDirectory(), pFileName);
    }

    public final ServerLevel overworld()
    {
        return this.levels.get(Level.OVERWORLD);
    }

    @Nullable
    public ServerLevel getLevel(ResourceKey<Level> pDimension)
    {
        return this.levels.get(pDimension);
    }

    public Set<ResourceKey<Level>> levelKeys()
    {
        return this.levels.keySet();
    }

    public Iterable<ServerLevel> getAllLevels()
    {
        return this.levels.values();
    }

    public String getServerVersion()
    {
        return SharedConstants.getCurrentVersion().getName();
    }

    public int getPlayerCount()
    {
        return this.playerList.getPlayerCount();
    }

    public int getMaxPlayers()
    {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayerNames()
    {
        return this.playerList.getPlayerNamesArray();
    }

    @DontObfuscate
    public String getServerModName()
    {
        return "vanilla";
    }

    public SystemReport fillSystemReport(SystemReport p_177936_)
    {
        if (this.playerList != null)
        {
            p_177936_.setDetail("Player Count", () ->
            {
                return this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers();
            });
        }

        p_177936_.setDetail("Data Packs", () ->
        {
            StringBuilder stringbuilder = new StringBuilder();

            for (Pack pack : this.packRepository.getSelectedPacks())
            {
                if (stringbuilder.length() > 0)
                {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(pack.getId());

                if (!pack.getCompatibility().isCompatible())
                {
                    stringbuilder.append(" (incompatible)");
                }
            }

            return stringbuilder.toString();
        });

        if (this.serverId != null)
        {
            p_177936_.setDetail("Server Id", () ->
            {
                return this.serverId;
            });
        }

        return this.fillServerSystemReport(p_177936_);
    }

    public abstract SystemReport fillServerSystemReport(SystemReport p_177901_);

    public abstract Optional<String> getModdedStatus();

    public void sendMessage(Component pComponent, UUID pSenderUUID)
    {
        LOGGER.info(pComponent.getString());
    }

    public KeyPair getKeyPair()
    {
        return this.keyPair;
    }

    public int getPort()
    {
        return this.port;
    }

    public void setPort(int pPort)
    {
        this.port = pPort;
    }

    public String getSingleplayerName()
    {
        return this.singleplayerName;
    }

    public void setSingleplayerName(String pOwner)
    {
        this.singleplayerName = pOwner;
    }

    public boolean isSingleplayer()
    {
        return this.singleplayerName != null;
    }

    protected void initializeKeyPair()
    {
        LOGGER.info("Generating keypair");

        try
        {
            this.keyPair = Crypt.generateKeyPair();
        }
        catch (CryptException cryptexception)
        {
            throw new IllegalStateException("Failed to generate key pair", cryptexception);
        }
    }

    public void setDifficulty(Difficulty pDifficulty, boolean p_129829_)
    {
        if (p_129829_ || !this.worldData.isDifficultyLocked())
        {
            this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : pDifficulty);
            this.updateMobSpawningFlags();
            this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
        }
    }

    public int getScaledTrackingDistance(int p_129935_)
    {
        return p_129935_;
    }

    private void updateMobSpawningFlags()
    {
        for (ServerLevel serverlevel : this.getAllLevels())
        {
            serverlevel.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        }
    }

    public void setDifficultyLocked(boolean pLocked)
    {
        this.worldData.setDifficultyLocked(pLocked);
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
    }

    private void sendDifficultyUpdate(ServerPlayer pPlayer)
    {
        LevelData leveldata = pPlayer.getLevel().getLevelData();
        pPlayer.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
    }

    public boolean isSpawningMonsters()
    {
        return this.worldData.getDifficulty() != Difficulty.PEACEFUL;
    }

    public boolean isDemo()
    {
        return this.isDemo;
    }

    public void setDemo(boolean pDemo)
    {
        this.isDemo = pDemo;
    }

    public String getResourcePack()
    {
        return this.resourcePack;
    }

    public String getResourcePackHash()
    {
        return this.resourcePackHash;
    }

    public void setResourcePack(String pUrl, String pHash)
    {
        this.resourcePack = pUrl;
        this.resourcePackHash = pHash;
    }

    public void populateSnooper(Snooper pSnooper)
    {
        pSnooper.setDynamicData("whitelist_enabled", false);
        pSnooper.setDynamicData("whitelist_count", 0);

        if (this.playerList != null)
        {
            pSnooper.setDynamicData("players_current", this.getPlayerCount());
            pSnooper.setDynamicData("players_max", this.getMaxPlayers());
            pSnooper.setDynamicData("players_seen", this.playerDataStorage.getSeenPlayers().length);
        }

        pSnooper.setDynamicData("uses_auth", this.onlineMode);
        pSnooper.setDynamicData("gui_state", this.hasGui() ? "enabled" : "disabled");
        pSnooper.setDynamicData("run_time", (Util.getMillis() - pSnooper.getStartupTime()) / 60L * 1000L);
        pSnooper.setDynamicData("avg_tick_ms", (int)(Mth.m_14078_(this.tickTimes) * 1.0E-6D));
        int i = 0;

        for (ServerLevel serverlevel : this.getAllLevels())
        {
            if (serverlevel != null)
            {
                pSnooper.setDynamicData("world[" + i + "][dimension]", serverlevel.dimension().location());
                pSnooper.setDynamicData("world[" + i + "][mode]", this.worldData.getGameType());
                pSnooper.setDynamicData("world[" + i + "][difficulty]", serverlevel.getDifficulty());
                pSnooper.setDynamicData("world[" + i + "][hardcore]", this.worldData.isHardcore());
                pSnooper.setDynamicData("world[" + i + "][height]", serverlevel.getMaxBuildHeight());
                pSnooper.setDynamicData("world[" + i + "][chunks_loaded]", serverlevel.getChunkSource().getLoadedChunksCount());
                ++i;
            }
        }

        pSnooper.setDynamicData("worlds", i);
    }

    public void populateSnooperInitial(Snooper p_177938_)
    {
        p_177938_.setFixedData("singleplayer", this.isSingleplayer());
        p_177938_.setFixedData("server_brand", this.getServerModName());
        p_177938_.setFixedData("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
        p_177938_.setFixedData("dedicated", this.isDedicatedServer());
    }

    public boolean isSnooperEnabled()
    {
        return true;
    }

    public abstract boolean isDedicatedServer();

    public abstract int getRateLimitPacketsPerSecond();

    public boolean usesAuthentication()
    {
        return this.onlineMode;
    }

    public void setUsesAuthentication(boolean pOnline)
    {
        this.onlineMode = pOnline;
    }

    public boolean getPreventProxyConnections()
    {
        return this.preventProxyConnections;
    }

    public void setPreventProxyConnections(boolean p_129994_)
    {
        this.preventProxyConnections = p_129994_;
    }

    public boolean isSpawningAnimals()
    {
        return true;
    }

    public boolean areNpcsEnabled()
    {
        return true;
    }

    public abstract boolean isEpollEnabled();

    public boolean isPvpAllowed()
    {
        return this.pvp;
    }

    public void setPvpAllowed(boolean pAllowPvp)
    {
        this.pvp = pAllowPvp;
    }

    public boolean isFlightAllowed()
    {
        return this.allowFlight;
    }

    public void setFlightAllowed(boolean pAllow)
    {
        this.allowFlight = pAllow;
    }

    public abstract boolean isCommandBlockEnabled();

    public String getMotd()
    {
        return this.motd;
    }

    public void setMotd(String pMotd)
    {
        this.motd = pMotd;
    }

    public boolean isStopped()
    {
        return this.stopped;
    }

    public PlayerList getPlayerList()
    {
        return this.playerList;
    }

    public void setPlayerList(PlayerList pList)
    {
        this.playerList = pList;
    }

    public abstract boolean isPublished();

    public void setDefaultGameType(GameType pGameMode)
    {
        this.worldData.setGameType(pGameMode);
    }

    @Nullable
    public ServerConnectionListener getConnection()
    {
        return this.connection;
    }

    public boolean isReady()
    {
        return this.isReady;
    }

    public boolean hasGui()
    {
        return false;
    }

    public boolean publishServer(@Nullable GameType pGameMode, boolean pCheats, int pPort)
    {
        return false;
    }

    public int getTickCount()
    {
        return this.tickCount;
    }

    public Snooper getSnooper()
    {
        return this.snooper;
    }

    public int getSpawnProtectionRadius()
    {
        return 16;
    }

    public boolean isUnderSpawnProtection(ServerLevel pLevel, BlockPos pPos, Player pPlayer)
    {
        return false;
    }

    public boolean repliesToStatus()
    {
        return true;
    }

    public Proxy getProxy()
    {
        return this.proxy;
    }

    public int getPlayerIdleTimeout()
    {
        return this.playerIdleTimeout;
    }

    public void setPlayerIdleTimeout(int pIdleTimeout)
    {
        this.playerIdleTimeout = pIdleTimeout;
    }

    public MinecraftSessionService getSessionService()
    {
        return this.sessionService;
    }

    public GameProfileRepository getProfileRepository()
    {
        return this.profileRepository;
    }

    public GameProfileCache getProfileCache()
    {
        return this.profileCache;
    }

    public ServerStatus getStatus()
    {
        return this.status;
    }

    public void invalidateStatus()
    {
        this.lastServerStatus = 0L;
    }

    public int getAbsoluteMaxWorldSize()
    {
        return 29999984;
    }

    public boolean scheduleExecutables()
    {
        return super.scheduleExecutables() && !this.isStopped();
    }

    public Thread getRunningThread()
    {
        return this.serverThread;
    }

    public int getCompressionThreshold()
    {
        return 256;
    }

    public long getNextTickTime()
    {
        return this.nextTickTime;
    }

    public DataFixer getFixerUpper()
    {
        return this.fixerUpper;
    }

    public int getSpawnRadius(@Nullable ServerLevel pLevel)
    {
        return pLevel != null ? pLevel.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
    }

    public ServerAdvancementManager getAdvancements()
    {
        return this.resources.getAdvancements();
    }

    public ServerFunctionManager getFunctions()
    {
        return this.functionManager;
    }

    public CompletableFuture<Void> reloadResources(Collection<String> p_129862_)
    {
        CompletableFuture<Void> completablefuture = CompletableFuture.supplyAsync(() ->
        {
            return p_129862_.stream().map(this.packRepository::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList());
        }, this).thenCompose((p_177907_) ->
        {
            return ServerResources.loadResources(p_177907_, this.registryHolder, this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED, this.getFunctionCompilationLevel(), this.executor, this);
        }).thenAcceptAsync((p_177917_) ->
        {
            this.resources.close();
            this.resources = p_177917_;
            this.packRepository.setSelected(p_129862_);
            this.worldData.setDataPackConfig(getSelectedPacks(this.packRepository));
            p_177917_.updateGlobals();
            this.getPlayerList().saveAll();
            this.getPlayerList().reloadResources();
            this.functionManager.replaceLibrary(this.resources.getFunctionLibrary());
            this.structureManager.onResourceManagerReload(this.resources.getResourceManager());
        }, this);

        if (this.isSameThread())
        {
            this.managedBlock(completablefuture::isDone);
        }

        return completablefuture;
    }

    public static DataPackConfig configurePackRepository(PackRepository p_129820_, DataPackConfig p_129821_, boolean p_129822_)
    {
        p_129820_.reload();

        if (p_129822_)
        {
            p_129820_.setSelected(Collections.singleton("vanilla"));
            return new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of());
        }
        else
        {
            Set<String> set = Sets.newLinkedHashSet();

            for (String s : p_129821_.getEnabled())
            {
                if (p_129820_.isAvailable(s))
                {
                    set.add(s);
                }
                else
                {
                    LOGGER.warn("Missing data pack {}", (Object)s);
                }
            }

            for (Pack pack : p_129820_.getAvailablePacks())
            {
                String s1 = pack.getId();

                if (!p_129821_.getDisabled().contains(s1) && !set.contains(s1))
                {
                    LOGGER.info("Found new data pack {}, loading it automatically", (Object)s1);
                    set.add(s1);
                }
            }

            if (set.isEmpty())
            {
                LOGGER.info("No datapacks selected, forcing vanilla");
                set.add("vanilla");
            }

            p_129820_.setSelected(set);
            return getSelectedPacks(p_129820_);
        }
    }

    private static DataPackConfig getSelectedPacks(PackRepository p_129818_)
    {
        Collection<String> collection = p_129818_.getSelectedIds();
        List<String> list = ImmutableList.copyOf(collection);
        List<String> list1 = p_129818_.getAvailableIds().stream().filter((p_177914_) ->
        {
            return !collection.contains(p_177914_);
        }).collect(ImmutableList.toImmutableList());
        return new DataPackConfig(list, list1);
    }

    public void kickUnlistedPlayers(CommandSourceStack pCommandSource)
    {
        if (this.isEnforceWhitelist())
        {
            PlayerList playerlist = pCommandSource.getServer().getPlayerList();
            UserWhiteList userwhitelist = playerlist.getWhiteList();

            for (ServerPlayer serverplayer : Lists.newArrayList(playerlist.getPlayers()))
            {
                if (!userwhitelist.isWhiteListed(serverplayer.getGameProfile()))
                {
                    serverplayer.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.not_whitelisted"));
                }
            }
        }
    }

    public PackRepository getPackRepository()
    {
        return this.packRepository;
    }

    public Commands getCommands()
    {
        return this.resources.getCommands();
    }

    public CommandSourceStack createCommandSourceStack()
    {
        ServerLevel serverlevel = this.overworld();
        return new CommandSourceStack(this, serverlevel == null ? Vec3.ZERO : Vec3.atLowerCornerOf(serverlevel.getSharedSpawnPos()), Vec2.ZERO, serverlevel, 4, "Server", new TextComponent("Server"), this, (Entity)null);
    }

    public boolean acceptsSuccess()
    {
        return true;
    }

    public boolean acceptsFailure()
    {
        return true;
    }

    public abstract boolean shouldInformAdmins();

    public RecipeManager getRecipeManager()
    {
        return this.resources.getRecipeManager();
    }

    public TagContainer getTags()
    {
        return this.resources.getTags();
    }

    public ServerScoreboard getScoreboard()
    {
        return this.scoreboard;
    }

    public CommandStorage getCommandStorage()
    {
        if (this.commandStorage == null)
        {
            throw new NullPointerException("Called before server init");
        }
        else
        {
            return this.commandStorage;
        }
    }

    public LootTables getLootTables()
    {
        return this.resources.getLootTables();
    }

    public PredicateManager getPredicateManager()
    {
        return this.resources.getPredicateManager();
    }

    public ItemModifierManager getItemModifierManager()
    {
        return this.resources.getItemModifierManager();
    }

    public GameRules getGameRules()
    {
        return this.overworld().getGameRules();
    }

    public CustomBossEvents getCustomBossEvents()
    {
        return this.customBossEvents;
    }

    public boolean isEnforceWhitelist()
    {
        return this.enforceWhitelist;
    }

    public void setEnforceWhitelist(boolean pWhitelistEnabled)
    {
        this.enforceWhitelist = pWhitelistEnabled;
    }

    public float getAverageTickTime()
    {
        return this.averageTickTime;
    }

    public int getProfilePermissions(GameProfile pProfile)
    {
        if (this.getPlayerList().isOp(pProfile))
        {
            ServerOpListEntry serveroplistentry = this.getPlayerList().getOps().get(pProfile);

            if (serveroplistentry != null)
            {
                return serveroplistentry.getLevel();
            }
            else if (this.isSingleplayerOwner(pProfile))
            {
                return 4;
            }
            else if (this.isSingleplayer())
            {
                return this.getPlayerList().isAllowCheatsForAllPlayers() ? 4 : 0;
            }
            else
            {
                return this.getOperatorUserPermissionLevel();
            }
        }
        else
        {
            return 0;
        }
    }

    public FrameTimer getFrameTimer()
    {
        return this.frameTimer;
    }

    public ProfilerFiller getProfiler()
    {
        return this.profiler;
    }

    public abstract boolean isSingleplayerOwner(GameProfile pProfile);

    public void dumpServerProperties(Path p_177911_) throws IOException
    {
    }

    private void saveDebugReport(Path p_129860_)
    {
        Path path = p_129860_.resolve("levels");

        try
        {
            for (Entry<ResourceKey<Level>, ServerLevel> entry : this.levels.entrySet())
            {
                ResourceLocation resourcelocation = entry.getKey().location();
                Path path1 = path.resolve(resourcelocation.getNamespace()).resolve(resourcelocation.getPath());
                Files.createDirectories(path1);
                entry.getValue().saveDebugReport(path1);
            }

            this.dumpGameRules(p_129860_.resolve("gamerules.txt"));
            this.dumpClasspath(p_129860_.resolve("classpath.txt"));
            this.dumpMiscStats(p_129860_.resolve("stats.txt"));
            this.dumpThreads(p_129860_.resolve("threads.txt"));
            this.dumpServerProperties(p_129860_.resolve("server.properties.txt"));
        }
        catch (IOException ioexception)
        {
            LOGGER.warn("Failed to save debug report", (Throwable)ioexception);
        }
    }

    private void dumpMiscStats(Path p_129951_) throws IOException
    {
        Writer writer = Files.newBufferedWriter(p_129951_);

        try
        {
            writer.write(String.format("pending_tasks: %d\n", this.getPendingTasksCount()));
            writer.write(String.format("average_tick_time: %f\n", this.getAverageTickTime()));
            writer.write(String.format("tick_times: %s\n", Arrays.toString(this.tickTimes)));
            writer.write(String.format("queue: %s\n", Util.backgroundExecutor()));
        }
        catch (Throwable throwable1)
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (Throwable throwable)
                {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (writer != null)
        {
            writer.close();
        }
    }

    private void dumpGameRules(Path p_129984_) throws IOException
    {
        Writer writer = Files.newBufferedWriter(p_129984_);

        try
        {
            final List<String> list = Lists.newArrayList();
            final GameRules gamerules = this.getGameRules();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor()
            {
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> pKey, GameRules.Type<T> pType)
                {
                    list.add(String.format("%s=%s\n", pKey.getId(), gamerules.<T>getRule(pKey)));
                }
            });

            for (String s : list)
            {
                writer.write(s);
            }
        }
        catch (Throwable throwable1)
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (Throwable throwable)
                {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (writer != null)
        {
            writer.close();
        }
    }

    private void dumpClasspath(Path p_129992_) throws IOException
    {
        Writer writer = Files.newBufferedWriter(p_129992_);

        try
        {
            String s = System.getProperty("java.class.path");
            String s1 = System.getProperty("path.separator");

            for (String s2 : Splitter.on(s1).split(s))
            {
                writer.write(s2);
                writer.write("\n");
            }
        }
        catch (Throwable throwable1)
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (Throwable throwable)
                {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (writer != null)
        {
            writer.close();
        }
    }

    private void dumpThreads(Path p_129996_) throws IOException
    {
        ThreadMXBean threadmxbean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] athreadinfo = threadmxbean.dumpAllThreads(true, true);
        Arrays.sort(athreadinfo, Comparator.comparing(ThreadInfo::getThreadName));
        Writer writer = Files.newBufferedWriter(p_129996_);

        try
        {
            for (ThreadInfo threadinfo : athreadinfo)
            {
                writer.write(threadinfo.toString());
                writer.write(10);
            }
        }
        catch (Throwable throwable1)
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (Throwable throwable)
                {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (writer != null)
        {
            writer.close();
        }
    }

    private void startMetricsRecordingTick()
    {
        if (this.willStartRecordingMetrics)
        {
            this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()), Util.timeSource, Util.ioPool(), new MetricsPersister("server"), this.onMetricsRecordingStopped, (p_177952_) ->
            {
                this.executeBlocking(() -> {
                    this.saveDebugReport(p_177952_.resolve("server"));
                });
                this.onMetricsRecordingFinished.accept(p_177952_);
            });
            this.willStartRecordingMetrics = false;
        }

        this.profiler = SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
        this.metricsRecorder.startTick();
        this.profiler.startTick();
    }

    private void endMetricsRecordingTick()
    {
        this.profiler.endTick();
        this.metricsRecorder.endTick();
    }

    public boolean isRecordingMetrics()
    {
        return this.metricsRecorder.isRecording();
    }

    public void startRecordingMetrics(Consumer<ProfileResults> p_177924_, Consumer<Path> p_177925_)
    {
        this.onMetricsRecordingStopped = (p_177922_) ->
        {
            this.stopRecordingMetrics();
            p_177924_.accept(p_177922_);
        };
        this.onMetricsRecordingFinished = p_177925_;
        this.willStartRecordingMetrics = true;
    }

    public void stopRecordingMetrics()
    {
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    }

    public void finishRecordingMetrics()
    {
        this.metricsRecorder.end();
    }

    public Path getWorldPath(LevelResource p_129844_)
    {
        return this.storageSource.getLevelPath(p_129844_);
    }

    public boolean forceSynchronousWrites()
    {
        return true;
    }

    public StructureManager getStructureManager()
    {
        return this.structureManager;
    }

    public WorldData getWorldData()
    {
        return this.worldData;
    }

    public RegistryAccess registryAccess()
    {
        return this.registryHolder;
    }

    public TextFilter createTextFilterForPlayer(ServerPlayer p_129814_)
    {
        return TextFilter.DUMMY;
    }

    public boolean isResourcePackRequired()
    {
        return false;
    }

    public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer p_177934_)
    {
        return (ServerPlayerGameMode)(this.isDemo() ? new DemoMode(p_177934_) : new ServerPlayerGameMode(p_177934_));
    }

    @Nullable
    public GameType getForcedGameType()
    {
        return null;
    }

    public ResourceManager getResourceManager()
    {
        return this.resources.getResourceManager();
    }

    @Nullable
    public Component getResourcePackPrompt()
    {
        return null;
    }

    public boolean isTimeProfilerRunning()
    {
        return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
    }

    public void startTimeProfiler()
    {
        this.debugCommandProfilerDelayStart = true;
    }

    public ProfileResults stopTimeProfiler()
    {
        if (this.debugCommandProfiler == null)
        {
            return EmptyProfileResults.EMPTY;
        }
        else
        {
            ProfileResults profileresults = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
            this.debugCommandProfiler = null;
            return profileresults;
        }
    }

    static class TimeProfiler
    {
        final long startNanos;
        final int startTick;

        TimeProfiler(long p_177958_, int p_177959_)
        {
            this.startNanos = p_177958_;
            this.startTick = p_177959_;
        }

        ProfileResults stop(final long p_177961_, final int p_177962_)
        {
            return new ProfileResults()
            {
                public List<ResultField> getTimes(String p_177972_)
                {
                    return Collections.emptyList();
                }
                public boolean saveResults(Path p_177974_)
                {
                    return false;
                }
                public long getStartTimeNano()
                {
                    return TimeProfiler.this.startNanos;
                }
                public int getStartTimeTicks()
                {
                    return TimeProfiler.this.startTick;
                }
                public long getEndTimeNano()
                {
                    return p_177961_;
                }
                public int getEndTimeTicks()
                {
                    return p_177962_;
                }
                public String getProfilerResults()
                {
                    return "";
                }
            };
        }
    }
}
