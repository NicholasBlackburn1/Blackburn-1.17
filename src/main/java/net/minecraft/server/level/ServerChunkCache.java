package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ServerChunkCache extends ChunkSource
{
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private final DistanceManager distanceManager;
    private final ChunkGenerator generator;
    final ServerLevel level;
    final Thread mainThread;
    final ThreadedLevelLightEngine lightEngine;
    private final ServerChunkCache.MainThreadExecutor mainThreadProcessor;
    public final ChunkMap chunkMap;
    private final DimensionDataStorage dataStorage;
    private long lastInhabitedUpdate;
    private boolean spawnEnemies = true;
    private boolean spawnFriendlies = true;
    private static final int CACHE_SIZE = 4;
    private final long[] lastChunkPos = new long[4];
    private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
    private final ChunkAccess[] lastChunk = new ChunkAccess[4];
    @Nullable
    @VisibleForDebug
    private NaturalSpawner.SpawnState lastSpawnState;

    public ServerChunkCache(ServerLevel p_143228_, LevelStorageSource.LevelStorageAccess p_143229_, DataFixer p_143230_, StructureManager p_143231_, Executor p_143232_, ChunkGenerator p_143233_, int p_143234_, boolean p_143235_, ChunkProgressListener p_143236_, ChunkStatusUpdateListener p_143237_, Supplier<DimensionDataStorage> p_143238_)
    {
        this.level = p_143228_;
        this.mainThreadProcessor = new ServerChunkCache.MainThreadExecutor(p_143228_);
        this.generator = p_143233_;
        this.mainThread = Thread.currentThread();
        File file1 = p_143229_.getDimensionPath(p_143228_.dimension());
        File file2 = new File(file1, "data");
        file2.mkdirs();
        this.dataStorage = new DimensionDataStorage(file2, p_143230_);
        this.chunkMap = new ChunkMap(p_143228_, p_143229_, p_143230_, p_143231_, p_143232_, this.mainThreadProcessor, this, this.getGenerator(), p_143236_, p_143237_, p_143238_, p_143234_, p_143235_);
        this.lightEngine = this.chunkMap.getLightEngine();
        this.distanceManager = this.chunkMap.getDistanceManager();
        this.clearCache();
    }

    public ThreadedLevelLightEngine getLightEngine()
    {
        return this.lightEngine;
    }

    @Nullable
    private ChunkHolder getVisibleChunkIfPresent(long p_8365_)
    {
        return this.chunkMap.getVisibleChunkIfPresent(p_8365_);
    }

    public int getTickingGenerated()
    {
        return this.chunkMap.getTickingGenerated();
    }

    private void storeInCache(long p_8367_, ChunkAccess p_8368_, ChunkStatus p_8369_)
    {
        for (int i = 3; i > 0; --i)
        {
            this.lastChunkPos[i] = this.lastChunkPos[i - 1];
            this.lastChunkStatus[i] = this.lastChunkStatus[i - 1];
            this.lastChunk[i] = this.lastChunk[i - 1];
        }

        this.lastChunkPos[0] = p_8367_;
        this.lastChunkStatus[0] = p_8369_;
        this.lastChunk[0] = p_8368_;
    }

    @Nullable
    public ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad)
    {
        if (Thread.currentThread() != this.mainThread)
        {
            return CompletableFuture.supplyAsync(() ->
            {
                return this.getChunk(pChunkX, pChunkZ, pRequiredStatus, pLoad);
            }, this.mainThreadProcessor).join();
        }
        else
        {
            ProfilerFiller profilerfiller = this.level.getProfiler();
            profilerfiller.incrementCounter("getChunk");
            long i = ChunkPos.asLong(pChunkX, pChunkZ);

            for (int j = 0; j < 4; ++j)
            {
                if (i == this.lastChunkPos[j] && pRequiredStatus == this.lastChunkStatus[j])
                {
                    ChunkAccess chunkaccess = this.lastChunk[j];

                    if (chunkaccess != null || !pLoad)
                    {
                        return chunkaccess;
                    }
                }
            }

            profilerfiller.incrementCounter("getChunkCacheMiss");
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkFutureMainThread(pChunkX, pChunkZ, pRequiredStatus, pLoad);
            this.mainThreadProcessor.managedBlock(completablefuture::isDone);
            ChunkAccess chunkaccess1 = completablefuture.join().map((p_8406_) ->
            {
                return p_8406_;
            }, (p_8423_) ->
            {
                if (pLoad)
                {
                    throw(IllegalStateException)Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + p_8423_));
                }
                else {
                    return null;
                }
            });
            this.storeInCache(i, chunkaccess1, pRequiredStatus);
            return chunkaccess1;
        }
    }

    @Nullable
    public LevelChunk getChunkNow(int pChunkX, int pChunkZ)
    {
        if (Thread.currentThread() != this.mainThread)
        {
            return null;
        }
        else
        {
            this.level.getProfiler().incrementCounter("getChunkNow");
            long i = ChunkPos.asLong(pChunkX, pChunkZ);

            for (int j = 0; j < 4; ++j)
            {
                if (i == this.lastChunkPos[j] && this.lastChunkStatus[j] == ChunkStatus.FULL)
                {
                    ChunkAccess chunkaccess = this.lastChunk[j];
                    return chunkaccess instanceof LevelChunk ? (LevelChunk)chunkaccess : null;
                }
            }

            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);

            if (chunkholder == null)
            {
                return null;
            }
            else
            {
                Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either = chunkholder.getFutureIfPresent(ChunkStatus.FULL).getNow((Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>)null);

                if (either == null)
                {
                    return null;
                }
                else
                {
                    ChunkAccess chunkaccess1 = either.left().orElse((ChunkAccess)null);

                    if (chunkaccess1 != null)
                    {
                        this.storeInCache(i, chunkaccess1, ChunkStatus.FULL);

                        if (chunkaccess1 instanceof LevelChunk)
                        {
                            return (LevelChunk)chunkaccess1;
                        }
                    }

                    return null;
                }
            }
        }
    }

    private void clearCache()
    {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunkStatus, (Object)null);
        Arrays.fill(this.lastChunk, (Object)null);
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int p_8432_, int p_8433_, ChunkStatus p_8434_, boolean p_8435_)
    {
        boolean flag = Thread.currentThread() == this.mainThread;
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture;

        if (flag)
        {
            completablefuture = this.getChunkFutureMainThread(p_8432_, p_8433_, p_8434_, p_8435_);
            this.mainThreadProcessor.managedBlock(completablefuture::isDone);
        }
        else
        {
            completablefuture = CompletableFuture.supplyAsync(() ->
            {
                return this.getChunkFutureMainThread(p_8432_, p_8433_, p_8434_, p_8435_);
            }, this.mainThreadProcessor).thenCompose((p_8413_) ->
            {
                return p_8413_;
            });
        }

        return completablefuture;
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(int p_8457_, int p_8458_, ChunkStatus p_8459_, boolean p_8460_)
    {
        ChunkPos chunkpos = new ChunkPos(p_8457_, p_8458_);
        long i = chunkpos.toLong();
        int j = 33 + ChunkStatus.getDistance(p_8459_);
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);

        if (p_8460_)
        {
            this.distanceManager.addTicket(TicketType.UNKNOWN, chunkpos, j, chunkpos);

            if (this.chunkAbsent(chunkholder, j))
            {
                ProfilerFiller profilerfiller = this.level.getProfiler();
                profilerfiller.push("chunkLoad");
                this.runDistanceManagerUpdates();
                chunkholder = this.getVisibleChunkIfPresent(i);
                profilerfiller.pop();

                if (this.chunkAbsent(chunkholder, j))
                {
                    throw(IllegalStateException)Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }

        return this.chunkAbsent(chunkholder, j) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : chunkholder.getOrScheduleFuture(p_8459_, this.chunkMap);
    }

    private boolean chunkAbsent(@Nullable ChunkHolder p_8417_, int p_8418_)
    {
        return p_8417_ == null || p_8417_.getTicketLevel() > p_8418_;
    }

    public boolean hasChunk(int pX, int pZ)
    {
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent((new ChunkPos(pX, pZ)).toLong());
        int i = 33 + ChunkStatus.getDistance(ChunkStatus.FULL);
        return !this.chunkAbsent(chunkholder, i);
    }

    public BlockGetter getChunkForLighting(int pChunkX, int pChunkZ)
    {
        long i = ChunkPos.asLong(pChunkX, pChunkZ);
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(i);

        if (chunkholder == null)
        {
            return null;
        }
        else
        {
            int j = CHUNK_STATUSES.size() - 1;

            while (true)
            {
                ChunkStatus chunkstatus = CHUNK_STATUSES.get(j);
                Optional<ChunkAccess> optional = chunkholder.getFutureIfPresentUnchecked(chunkstatus).getNow(ChunkHolder.UNLOADED_CHUNK).left();

                if (optional.isPresent())
                {
                    return optional.get();
                }

                if (chunkstatus == ChunkStatus.LIGHT.getParent())
                {
                    return null;
                }

                --j;
            }
        }
    }

    public Level getLevel()
    {
        return this.level;
    }

    public boolean pollTask()
    {
        return this.mainThreadProcessor.pollTask();
    }

    boolean runDistanceManagerUpdates()
    {
        boolean flag = this.distanceManager.runAllUpdates(this.chunkMap);
        boolean flag1 = this.chunkMap.promoteChunkMap();

        if (!flag && !flag1)
        {
            return false;
        }
        else
        {
            this.clearCache();
            return true;
        }
    }

    public boolean isPositionTicking(long p_143240_)
    {
        return this.checkChunkFuture(p_143240_, ChunkHolder::getTickingChunkFuture);
    }

    private boolean checkChunkFuture(long pPos, Function<ChunkHolder, CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>>> p_8375_)
    {
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pPos);

        if (chunkholder == null)
        {
            return false;
        }
        else
        {
            Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = p_8375_.apply(chunkholder).getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK);
            return either.left().isPresent();
        }
    }

    public void save(boolean pFlush)
    {
        this.runDistanceManagerUpdates();
        this.chunkMap.saveAllChunks(pFlush);
    }

    public void close() throws IOException
    {
        this.save(true);
        this.lightEngine.close();
        this.chunkMap.close();
    }

    public void tick(BooleanSupplier p_8415_)
    {
        this.level.getProfiler().push("purge");
        this.distanceManager.purgeStaleTickets();
        this.runDistanceManagerUpdates();
        this.level.getProfiler().popPush("chunks");
        this.tickChunks();
        this.level.getProfiler().popPush("unload");
        this.chunkMap.tick(p_8415_);
        this.level.getProfiler().pop();
        this.clearCache();
    }

    private void tickChunks()
    {
        long i = this.level.getGameTime();
        long j = i - this.lastInhabitedUpdate;
        this.lastInhabitedUpdate = i;
        LevelData leveldata = this.level.getLevelData();
        boolean flag = this.level.isDebug();
        boolean flag1 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);

        if (!flag)
        {
            this.level.getProfiler().push("pollingChunks");
            int k = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            boolean flag2 = leveldata.getGameTime() % 400L == 0L;
            this.level.getProfiler().push("naturalSpawnCount");
            int l = this.distanceManager.getNaturalSpawnChunkCount();
            NaturalSpawner.SpawnState naturalspawner$spawnstate = NaturalSpawner.createState(l, this.level.getAllEntities(), this::getFullChunk);
            this.lastSpawnState = naturalspawner$spawnstate;
            this.level.getProfiler().pop();
            List<ChunkHolder> list = Lists.newArrayList(this.chunkMap.getChunks());
            Collections.shuffle(list);
            list.forEach((p_8382_) ->
            {
                Optional<LevelChunk> optional = p_8382_.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();

                if (optional.isPresent())
                {
                    LevelChunk levelchunk = optional.get();
                    ChunkPos chunkpos = levelchunk.getPos();

                    if (this.level.isPositionEntityTicking(chunkpos) && !this.chunkMap.noPlayersCloseForSpawning(chunkpos))
                    {
                        levelchunk.setInhabitedTime(levelchunk.getInhabitedTime() + j);

                        if (flag1 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(chunkpos))
                        {
                            NaturalSpawner.spawnForChunk(this.level, levelchunk, naturalspawner$spawnstate, this.spawnFriendlies, this.spawnEnemies, flag2);
                        }

                        this.level.tickChunk(levelchunk, k);
                    }
                }
            });
            this.level.getProfiler().push("customSpawners");

            if (flag1)
            {
                this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
            }

            this.level.getProfiler().popPush("broadcast");
            list.forEach((p_182287_) ->
            {
                p_182287_.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().ifPresent(p_182287_::broadcastChanges);
            });
            this.level.getProfiler().pop();
            this.level.getProfiler().pop();
        }

        this.chunkMap.tick();
    }

    private void getFullChunk(long p_8371_, Consumer<LevelChunk> p_8372_)
    {
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_8371_);

        if (chunkholder != null)
        {
            chunkholder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().ifPresent(p_8372_);
        }
    }

    public String gatherStats()
    {
        return Integer.toString(this.getLoadedChunksCount());
    }

    @VisibleForTesting
    public int getPendingTasksCount()
    {
        return this.mainThreadProcessor.getPendingTasksCount();
    }

    public ChunkGenerator getGenerator()
    {
        return this.generator;
    }

    public int getLoadedChunksCount()
    {
        return this.chunkMap.size();
    }

    public void blockChanged(BlockPos pPos)
    {
        int i = SectionPos.blockToSectionCoord(pPos.getX());
        int j = SectionPos.blockToSectionCoord(pPos.getZ());
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(ChunkPos.asLong(i, j));

        if (chunkholder != null)
        {
            chunkholder.blockChanged(pPos);
        }
    }

    public void onLightUpdate(LightLayer pType, SectionPos pPos)
    {
        this.mainThreadProcessor.execute(() ->
        {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pPos.chunk().toLong());

            if (chunkholder != null)
            {
                chunkholder.sectionLightChanged(pType, pPos.y());
            }
        });
    }

    public <T> void addRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue)
    {
        this.distanceManager.addRegionTicket(pType, pPos, pDistance, pValue);
    }

    public <T> void removeRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue)
    {
        this.distanceManager.removeRegionTicket(pType, pPos, pDistance, pValue);
    }

    public void updateChunkForced(ChunkPos pPos, boolean pAdd)
    {
        this.distanceManager.updateChunkForced(pPos, pAdd);
    }

    public void move(ServerPlayer pPlayer)
    {
        this.chunkMap.move(pPlayer);
    }

    public void removeEntity(Entity pEntity)
    {
        this.chunkMap.removeEntity(pEntity);
    }

    public void addEntity(Entity pEntity)
    {
        this.chunkMap.addEntity(pEntity);
    }

    public void broadcastAndSend(Entity pEntity, Packet<?> pPacket)
    {
        this.chunkMap.broadcastAndSend(pEntity, pPacket);
    }

    public void broadcast(Entity pEntity, Packet<?> pPacket)
    {
        this.chunkMap.broadcast(pEntity, pPacket);
    }

    public void setViewDistance(int pViewDistance)
    {
        this.chunkMap.setViewDistance(pViewDistance);
    }

    public void setSpawnSettings(boolean pHostile, boolean pPeaceful)
    {
        this.spawnEnemies = pHostile;
        this.spawnFriendlies = pPeaceful;
    }

    public String getChunkDebugData(ChunkPos pChunkPos)
    {
        return this.chunkMap.getChunkDebugData(pChunkPos);
    }

    public DimensionDataStorage getDataStorage()
    {
        return this.dataStorage;
    }

    public PoiManager getPoiManager()
    {
        return this.chunkMap.getPoiManager();
    }

    @Nullable
    @VisibleForDebug
    public NaturalSpawner.SpawnState getLastSpawnState()
    {
        return this.lastSpawnState;
    }

    final class MainThreadExecutor extends BlockableEventLoop<Runnable>
    {
        MainThreadExecutor(Level p_8494_)
        {
            super("Chunk source main thread executor for " + p_8494_.dimension().location());
        }

        protected Runnable wrapRunnable(Runnable pRunnable)
        {
            return pRunnable;
        }

        protected boolean shouldRun(Runnable pRunnable)
        {
            return true;
        }

        protected boolean scheduleExecutables()
        {
            return true;
        }

        protected Thread getRunningThread()
        {
            return ServerChunkCache.this.mainThread;
        }

        protected void doRunTask(Runnable pTask)
        {
            ServerChunkCache.this.level.getProfiler().incrementCounter("runTask");
            super.doRunTask(pTask);
        }

        public boolean pollTask()
        {
            if (ServerChunkCache.this.runDistanceManagerUpdates())
            {
                return true;
            }
            else
            {
                ServerChunkCache.this.lightEngine.tryScheduleUpdate();
                return super.pollTask();
            }
        }
    }
}
