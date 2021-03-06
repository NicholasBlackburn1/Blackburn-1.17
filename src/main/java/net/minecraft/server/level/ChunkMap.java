package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkMap extends ChunkStorage implements ChunkHolder.PlayerProvider {
   private static final byte CHUNK_TYPE_REPLACEABLE = -1;
   private static final byte CHUNK_TYPE_UNKNOWN = 0;
   private static final byte CHUNK_TYPE_FULL = 1;
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int CHUNK_SAVED_PER_TICK = 200;
   private static final int MIN_VIEW_DISTANCE = 3;
   public static final int MAX_VIEW_DISTANCE = 33;
   public static final int MAX_CHUNK_DISTANCE = 33 + ChunkStatus.maxDistance();
   public static final int FORCED_TICKET_LEVEL = 31;
   private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap<>();
   private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
   private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap<>();
   private final LongSet entitiesInLevel = new LongOpenHashSet();
   final ServerLevel level;
   private final ThreadedLevelLightEngine lightEngine;
   private final BlockableEventLoop<Runnable> mainThreadExecutor;
   private final ChunkGenerator generator;
   private final Supplier<DimensionDataStorage> overworldDataStorage;
   private final PoiManager poiManager;
   final LongSet toDrop = new LongOpenHashSet();
   private boolean modified;
   private final ChunkTaskPriorityQueueSorter queueSorter;
   private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
   private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
   private final ChunkProgressListener progressListener;
   private final ChunkStatusUpdateListener chunkStatusListener;
   private final ChunkMap.DistanceManager distanceManager;
   private final AtomicInteger tickingGenerated = new AtomicInteger();
   private final StructureManager structureManager;
   private final String storageName;
   private final PlayerMap playerMap = new PlayerMap();
   private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
   private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
   private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
   int viewDistance;

   public ChunkMap(ServerLevel p_143040_, LevelStorageSource.LevelStorageAccess p_143041_, DataFixer p_143042_, StructureManager p_143043_, Executor p_143044_, BlockableEventLoop<Runnable> p_143045_, LightChunkGetter p_143046_, ChunkGenerator p_143047_, ChunkProgressListener p_143048_, ChunkStatusUpdateListener p_143049_, Supplier<DimensionDataStorage> p_143050_, int p_143051_, boolean p_143052_) {
      super(new File(p_143041_.getDimensionPath(p_143040_.dimension()), "region"), p_143042_, p_143052_);
      this.structureManager = p_143043_;
      File file1 = p_143041_.getDimensionPath(p_143040_.dimension());
      this.storageName = file1.getName();
      this.level = p_143040_;
      this.generator = p_143047_;
      this.mainThreadExecutor = p_143045_;
      ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(p_143044_, "worldgen");
      ProcessorHandle<Runnable> processorhandle = ProcessorHandle.of("main", p_143045_::tell);
      this.progressListener = p_143048_;
      this.chunkStatusListener = p_143049_;
      ProcessorMailbox<Runnable> processormailbox1 = ProcessorMailbox.create(p_143044_, "light");
      this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processormailbox, processorhandle, processormailbox1), p_143044_, Integer.MAX_VALUE);
      this.worldgenMailbox = this.queueSorter.getProcessor(processormailbox, false);
      this.mainThreadMailbox = this.queueSorter.getProcessor(processorhandle, false);
      this.lightEngine = new ThreadedLevelLightEngine(p_143046_, this, this.level.dimensionType().hasSkyLight(), processormailbox1, this.queueSorter.getProcessor(processormailbox1, false));
      this.distanceManager = new ChunkMap.DistanceManager(p_143044_, p_143045_);
      this.overworldDataStorage = p_143050_;
      this.poiManager = new PoiManager(new File(file1, "poi"), p_143042_, p_143052_, p_143040_);
      this.setViewDistance(p_143051_);
   }

   private static double euclideanDistanceSquared(ChunkPos p_140227_, Entity p_140228_) {
      double d0 = (double)SectionPos.sectionToBlockCoord(p_140227_.x, 8);
      double d1 = (double)SectionPos.sectionToBlockCoord(p_140227_.z, 8);
      double d2 = d0 - p_140228_.getX();
      double d3 = d1 - p_140228_.getZ();
      return d2 * d2 + d3 * d3;
   }

   private static int checkerboardDistance(ChunkPos p_140339_, ServerPlayer p_140340_, boolean p_140341_) {
      int i;
      int j;
      if (p_140341_) {
         SectionPos sectionpos = p_140340_.getLastSectionPos();
         i = sectionpos.x();
         j = sectionpos.z();
      } else {
         i = SectionPos.blockToSectionCoord(p_140340_.getBlockX());
         j = SectionPos.blockToSectionCoord(p_140340_.getBlockZ());
      }

      return checkerboardDistance(p_140339_, i, j);
   }

   private static int checkerboardDistance(ChunkPos p_143120_, Entity p_143121_) {
      return checkerboardDistance(p_143120_, SectionPos.blockToSectionCoord(p_143121_.getBlockX()), SectionPos.blockToSectionCoord(p_143121_.getBlockZ()));
   }

   private static int checkerboardDistance(ChunkPos p_140207_, int p_140208_, int p_140209_) {
      int i = p_140207_.x - p_140208_;
      int j = p_140207_.z - p_140209_;
      return Math.max(Math.abs(i), Math.abs(j));
   }

   protected ThreadedLevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   @Nullable
   protected ChunkHolder getUpdatingChunkIfPresent(long p_140175_) {
      return this.updatingChunkMap.get(p_140175_);
   }

   @Nullable
   protected ChunkHolder getVisibleChunkIfPresent(long p_140328_) {
      return this.visibleChunkMap.get(p_140328_);
   }

   protected IntSupplier getChunkQueueLevel(long p_140372_) {
      return () -> {
         ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_140372_);
         return chunkholder == null ? ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1 : Math.min(chunkholder.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
      };
   }

   public String getChunkDebugData(ChunkPos p_140205_) {
      ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_140205_.toLong());
      if (chunkholder == null) {
         return "null";
      } else {
         String s = chunkholder.getTicketLevel() + "\n";
         ChunkStatus chunkstatus = chunkholder.getLastAvailableStatus();
         ChunkAccess chunkaccess = chunkholder.getLastAvailable();
         if (chunkstatus != null) {
            s = s + "St: \u00a7" + chunkstatus.getIndex() + chunkstatus + "\u00a7r\n";
         }

         if (chunkaccess != null) {
            s = s + "Ch: \u00a7" + chunkaccess.getStatus().getIndex() + chunkaccess.getStatus() + "\u00a7r\n";
         }

         ChunkHolder.FullChunkStatus chunkholder$fullchunkstatus = chunkholder.getFullStatus();
         s = s + "\u00a7" + chunkholder$fullchunkstatus.ordinal() + chunkholder$fullchunkstatus;
         return s + "\u00a7r";
      }
   }

   private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(ChunkPos p_140211_, int p_140212_, IntFunction<ChunkStatus> p_140213_) {
      List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> list = Lists.newArrayList();
      int i = p_140211_.x;
      int j = p_140211_.z;

      for(int k = -p_140212_; k <= p_140212_; ++k) {
         for(int l = -p_140212_; l <= p_140212_; ++l) {
            int i1 = Math.max(Math.abs(l), Math.abs(k));
            final ChunkPos chunkpos = new ChunkPos(i + l, j + k);
            long j1 = chunkpos.toLong();
            ChunkHolder chunkholder = this.getUpdatingChunkIfPresent(j1);
            if (chunkholder == null) {
               return CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                  public String toString() {
                     return "Unloaded " + chunkpos;
                  }
               }));
            }

            ChunkStatus chunkstatus = p_140213_.apply(i1);
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = chunkholder.getOrScheduleFuture(chunkstatus, this);
            list.add(completablefuture);
         }
      }

      CompletableFuture<List<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completablefuture1 = Util.sequence(list);
      return completablefuture1.thenApply((p_140173_) -> {
         List<ChunkAccess> list1 = Lists.newArrayList();
         int k1 = 0;

         for(final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either : p_140173_) {
            Optional<ChunkAccess> optional = either.left();
            if (!optional.isPresent()) {
               final int l1 = k1;
               return Either.right(new ChunkHolder.ChunkLoadingFailure() {
                  public String toString() {
                     return "Unloaded " + new ChunkPos(i + l1 % (p_140212_ * 2 + 1), j + l1 / (p_140212_ * 2 + 1)) + " " + either.right().get();
                  }
               });
            }

            list1.add(optional.get());
            ++k1;
         }

         return Either.left(list1);
      });
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareEntityTickingChunk(ChunkPos p_143118_) {
      return this.getChunkRangeFuture(p_143118_, 2, (p_143129_) -> {
         return ChunkStatus.FULL;
      }).thenApplyAsync((p_143137_) -> {
         return p_143137_.mapLeft((p_143139_) -> {
            return (LevelChunk)p_143139_.get(p_143139_.size() / 2);
         });
      }, this.mainThreadExecutor);
   }

   @Nullable
   ChunkHolder updateChunkScheduling(long p_140177_, int p_140178_, @Nullable ChunkHolder p_140179_, int p_140180_) {
      if (p_140180_ > MAX_CHUNK_DISTANCE && p_140178_ > MAX_CHUNK_DISTANCE) {
         return p_140179_;
      } else {
         if (p_140179_ != null) {
            p_140179_.setTicketLevel(p_140178_);
         }

         if (p_140179_ != null) {
            if (p_140178_ > MAX_CHUNK_DISTANCE) {
               this.toDrop.add(p_140177_);
            } else {
               this.toDrop.remove(p_140177_);
            }
         }

         if (p_140178_ <= MAX_CHUNK_DISTANCE && p_140179_ == null) {
            p_140179_ = this.pendingUnloads.remove(p_140177_);
            if (p_140179_ != null) {
               p_140179_.setTicketLevel(p_140178_);
            } else {
               p_140179_ = new ChunkHolder(new ChunkPos(p_140177_), p_140178_, this.level, this.lightEngine, this.queueSorter, this);
            }

            this.updatingChunkMap.put(p_140177_, p_140179_);
            this.modified = true;
         }

         return p_140179_;
      }
   }

   public void close() throws IOException {
      try {
         this.queueSorter.close();
         this.poiManager.close();
      } finally {
         super.close();
      }

   }

   protected void saveAllChunks(boolean p_140319_) {
      if (p_140319_) {
         List<ChunkHolder> list = this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).peek(ChunkHolder::refreshAccessibility).collect(Collectors.toList());
         MutableBoolean mutableboolean = new MutableBoolean();

         do {
            mutableboolean.setFalse();
            list.stream().map((p_140420_) -> {
               CompletableFuture<ChunkAccess> completablefuture;
               do {
                  completablefuture = p_140420_.getChunkToSave();
                  this.mainThreadExecutor.managedBlock(completablefuture::isDone);
               } while(completablefuture != p_140420_.getChunkToSave());

               return completablefuture.join();
            }).filter((p_140400_) -> {
               return p_140400_ instanceof ImposterProtoChunk || p_140400_ instanceof LevelChunk;
            }).filter(this::save).forEach((p_140284_) -> {
               mutableboolean.setTrue();
            });
         } while(mutableboolean.isTrue());

         this.processUnloads(() -> {
            return true;
         });
         this.flushWorker();
      } else {
         this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).forEach((p_140412_) -> {
            ChunkAccess chunkaccess = p_140412_.getChunkToSave().getNow((ChunkAccess)null);
            if (chunkaccess instanceof ImposterProtoChunk || chunkaccess instanceof LevelChunk) {
               this.save(chunkaccess);
               p_140412_.refreshAccessibility();
            }

         });
      }

   }

   protected void tick(BooleanSupplier p_140281_) {
      ProfilerFiller profilerfiller = this.level.getProfiler();
      profilerfiller.push("poi");
      this.poiManager.tick(p_140281_);
      profilerfiller.popPush("chunk_unload");
      if (!this.level.noSave()) {
         this.processUnloads(p_140281_);
      }

      profilerfiller.pop();
   }

   private void processUnloads(BooleanSupplier p_140354_) {
      LongIterator longiterator = this.toDrop.iterator();

      for(int i = 0; longiterator.hasNext() && (p_140354_.getAsBoolean() || i < 200 || this.toDrop.size() > 2000); longiterator.remove()) {
         long j = longiterator.nextLong();
         ChunkHolder chunkholder = this.updatingChunkMap.remove(j);
         if (chunkholder != null) {
            this.pendingUnloads.put(j, chunkholder);
            this.modified = true;
            ++i;
            this.scheduleUnload(j, chunkholder);
         }
      }

      Runnable runnable;
      while((p_140354_.getAsBoolean() || this.unloadQueue.size() > 2000) && (runnable = this.unloadQueue.poll()) != null) {
         runnable.run();
      }

   }

   private void scheduleUnload(long p_140182_, ChunkHolder p_140183_) {
      CompletableFuture<ChunkAccess> completablefuture = p_140183_.getChunkToSave();
      completablefuture.thenAcceptAsync((p_140309_) -> {
         CompletableFuture<ChunkAccess> completablefuture1 = p_140183_.getChunkToSave();
         if (completablefuture1 != completablefuture) {
            this.scheduleUnload(p_140182_, p_140183_);
         } else {
            if (this.pendingUnloads.remove(p_140182_, p_140183_) && p_140309_ != null) {
               if (p_140309_ instanceof LevelChunk) {
                  ((LevelChunk)p_140309_).setLoaded(false);
               }

               this.save(p_140309_);
               if (this.entitiesInLevel.remove(p_140182_) && p_140309_ instanceof LevelChunk) {
                  LevelChunk levelchunk = (LevelChunk)p_140309_;
                  this.level.unload(levelchunk);
               }

               this.lightEngine.updateChunkStatus(p_140309_.getPos());
               this.lightEngine.tryScheduleUpdate();
               this.progressListener.onStatusChange(p_140309_.getPos(), (ChunkStatus)null);
            }

         }
      }, this.unloadQueue::add).whenComplete((p_140303_, p_140304_) -> {
         if (p_140304_ != null) {
            LOGGER.error("Failed to save chunk {}", p_140183_.getPos(), p_140304_);
         }

      });
   }

   protected boolean promoteChunkMap() {
      if (!this.modified) {
         return false;
      } else {
         this.visibleChunkMap = this.updatingChunkMap.clone();
         this.modified = false;
         return true;
      }
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> schedule(ChunkHolder p_140293_, ChunkStatus p_140294_) {
      ChunkPos chunkpos = p_140293_.getPos();
      if (p_140294_ == ChunkStatus.EMPTY) {
         return this.scheduleChunkLoad(chunkpos);
      } else {
         if (p_140294_ == ChunkStatus.LIGHT) {
            this.distanceManager.addTicket(TicketType.LIGHT, chunkpos, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), chunkpos);
         }

         Optional<ChunkAccess> optional = p_140293_.getOrScheduleFuture(p_140294_.getParent(), this).getNow(ChunkHolder.UNLOADED_CHUNK).left();
         if (optional.isPresent() && optional.get().getStatus().isOrAfter(p_140294_)) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = p_140294_.load(this.level, this.structureManager, this.lightEngine, (p_143132_) -> {
               return this.protoChunkToFullChunk(p_140293_);
            }, optional.get());
            this.progressListener.onStatusChange(chunkpos, p_140294_);
            return completablefuture;
         } else {
            return this.scheduleChunkGeneration(p_140293_, p_140294_);
         }
      }
   }

   private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos p_140418_) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            this.level.getProfiler().incrementCounter("chunkLoad");
            CompoundTag compoundtag = this.readChunk(p_140418_);
            if (compoundtag != null) {
               boolean flag = compoundtag.contains("Level", 10) && compoundtag.getCompound("Level").contains("Status", 8);
               if (flag) {
                  ChunkAccess chunkaccess = ChunkSerializer.read(this.level, this.structureManager, this.poiManager, p_140418_, compoundtag);
                  this.markPosition(p_140418_, chunkaccess.getStatus().getChunkType());
                  return Either.left(chunkaccess);
               }

               LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)p_140418_);
            }
         } catch (ReportedException reportedexception) {
            Throwable throwable = reportedexception.getCause();
            if (!(throwable instanceof IOException)) {
               this.markPositionReplaceable(p_140418_);
               throw reportedexception;
            }

            LOGGER.error("Couldn't load chunk {}", p_140418_, throwable);
         } catch (Exception exception) {
            LOGGER.error("Couldn't load chunk {}", p_140418_, exception);
         }

         this.markPositionReplaceable(p_140418_);
         return Either.left(new ProtoChunk(p_140418_, UpgradeData.EMPTY, this.level));
      }, this.mainThreadExecutor);
   }

   private void markPositionReplaceable(ChunkPos p_140423_) {
      this.chunkTypeCache.put(p_140423_.toLong(), (byte)-1);
   }

   private byte markPosition(ChunkPos p_140230_, ChunkStatus.ChunkType p_140231_) {
      return this.chunkTypeCache.put(p_140230_.toLong(), (byte)(p_140231_ == ChunkStatus.ChunkType.PROTOCHUNK ? -1 : 1));
   }

   private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkGeneration(ChunkHolder p_140361_, ChunkStatus p_140362_) {
      ChunkPos chunkpos = p_140361_.getPos();
      CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkRangeFuture(chunkpos, p_140362_.getRange(), (p_140346_) -> {
         return this.getDependencyStatus(p_140362_, p_140346_);
      });
      this.level.getProfiler().incrementCounter(() -> {
         return "chunkGenerate " + p_140362_.getName();
      });
      Executor executor = (p_143148_) -> {
         this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_140361_, p_143148_));
      };
      return completablefuture.thenComposeAsync((p_143083_) -> {
         return p_143083_.map((p_143089_) -> {
            try {
               CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture1 = p_140362_.generate(executor, this.level, this.generator, this.structureManager, this.lightEngine, (p_143113_) -> {
                  return this.protoChunkToFullChunk(p_140361_);
               }, p_143089_);
               this.progressListener.onStatusChange(chunkpos, p_140362_);
               return completablefuture1;
            } catch (Exception exception) {
               exception.getStackTrace();
               CrashReport crashreport = CrashReport.forThrowable(exception, "Exception generating new chunk");
               CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk to be generated");
               crashreportcategory.setDetail("Location", String.format("%d,%d", chunkpos.x, chunkpos.z));
               crashreportcategory.setDetail("Position hash", ChunkPos.asLong(chunkpos.x, chunkpos.z));
               crashreportcategory.setDetail("Generator", this.generator);
               throw new ReportedException(crashreport);
            }
         }, (p_143074_) -> {
            this.releaseLightTicket(chunkpos);
            return CompletableFuture.completedFuture(Either.right(p_143074_));
         });
      }, executor);
   }

   protected void releaseLightTicket(ChunkPos p_140376_) {
      this.mainThreadExecutor.tell(Util.name(() -> {
         this.distanceManager.removeTicket(TicketType.LIGHT, p_140376_, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), p_140376_);
      }, () -> {
         return "release light ticket " + p_140376_;
      }));
   }

   private ChunkStatus getDependencyStatus(ChunkStatus p_140263_, int p_140264_) {
      ChunkStatus chunkstatus;
      if (p_140264_ == 0) {
         chunkstatus = p_140263_.getParent();
      } else {
         chunkstatus = ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance(p_140263_) + p_140264_);
      }

      return chunkstatus;
   }

   private static void postLoadProtoChunk(ServerLevel p_143065_, List<CompoundTag> p_143066_) {
      if (!p_143066_.isEmpty()) {
         p_143065_.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(p_143066_, p_143065_));
      }

   }

   private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder p_140384_) {
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = p_140384_.getFutureIfPresentUnchecked(ChunkStatus.FULL.getParent());
      return completablefuture.thenApplyAsync((p_143060_) -> {
         ChunkStatus chunkstatus = ChunkHolder.getStatus(p_140384_.getTicketLevel());
         return !chunkstatus.isOrAfter(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : p_143060_.mapLeft((p_143057_) -> {
            ChunkPos chunkpos = p_140384_.getPos();
            ProtoChunk protochunk = (ProtoChunk)p_143057_;
            LevelChunk levelchunk;
            if (protochunk instanceof ImposterProtoChunk) {
               levelchunk = ((ImposterProtoChunk)protochunk).getWrapped();
            } else {
               levelchunk = new LevelChunk(this.level, protochunk, (p_143098_) -> {
                  postLoadProtoChunk(this.level, protochunk.getEntities());
               });
               p_140384_.replaceProtoChunk(new ImposterProtoChunk(levelchunk));
            }

            levelchunk.setFullStatus(() -> {
               return ChunkHolder.getFullChunkStatus(p_140384_.getTicketLevel());
            });
            levelchunk.runPostLoad();
            if (this.entitiesInLevel.add(chunkpos.toLong())) {
               levelchunk.setLoaded(true);
               levelchunk.registerAllBlockEntitiesAfterLevelLoad();
            }

            return levelchunk;
         });
      }, (p_143144_) -> {
         this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143144_, p_140384_.getPos().toLong(), p_140384_::getTicketLevel));
      });
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareTickingChunk(ChunkHolder p_143054_) {
      ChunkPos chunkpos = p_143054_.getPos();
      CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkRangeFuture(chunkpos, 1, (p_143108_) -> {
         return ChunkStatus.FULL;
      });
      CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture1 = completablefuture.thenApplyAsync((p_143125_) -> {
         return p_143125_.flatMap((p_143127_) -> {
            LevelChunk levelchunk = (LevelChunk)p_143127_.get(p_143127_.size() / 2);
            levelchunk.postProcessGeneration();
            return Either.left(levelchunk);
         });
      }, (p_143135_) -> {
         this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143054_, p_143135_));
      });
      completablefuture1.thenAcceptAsync((p_143095_) -> {
         p_143095_.ifLeft((p_143092_) -> {
            this.tickingGenerated.getAndIncrement();
            Packet<?>[] packet = new Packet[2];
            this.getPlayers(chunkpos, false).forEach((p_143106_) -> {
               this.playerLoadedChunk(p_143106_, packet, p_143092_);
            });
         });
      }, (p_143116_) -> {
         this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143054_, p_143116_));
      });
      return completablefuture1;
   }

   public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareAccessibleChunk(ChunkHolder p_143110_) {
      return this.getChunkRangeFuture(p_143110_.getPos(), 1, ChunkStatus::getStatusAroundFullChunk).thenApplyAsync((p_143100_) -> {
         return p_143100_.mapLeft((p_143102_) -> {
            LevelChunk levelchunk = (LevelChunk)p_143102_.get(p_143102_.size() / 2);
            levelchunk.unpackTicks();
            return levelchunk;
         });
      }, (p_143063_) -> {
         this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143110_, p_143063_));
      });
   }

   public int getTickingGenerated() {
      return this.tickingGenerated.get();
   }

   private boolean save(ChunkAccess p_140259_) {
      this.poiManager.flush(p_140259_.getPos());
      if (!p_140259_.isUnsaved()) {
         return false;
      } else {
         p_140259_.setUnsaved(false);
         ChunkPos chunkpos = p_140259_.getPos();

         try {
            ChunkStatus chunkstatus = p_140259_.getStatus();
            if (chunkstatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
               if (this.isExistingChunkFull(chunkpos)) {
                  return false;
               }

               if (chunkstatus == ChunkStatus.EMPTY && p_140259_.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                  return false;
               }
            }

            this.level.getProfiler().incrementCounter("chunkSave");
            CompoundTag compoundtag = ChunkSerializer.write(this.level, p_140259_);
            this.write(chunkpos, compoundtag);
            this.markPosition(chunkpos, chunkstatus.getChunkType());
            return true;
         } catch (Exception exception) {
            LOGGER.error("Failed to save chunk {},{}", chunkpos.x, chunkpos.z, exception);
            return false;
         }
      }
   }

   private boolean isExistingChunkFull(ChunkPos p_140426_) {
      byte b0 = this.chunkTypeCache.get(p_140426_.toLong());
      if (b0 != 0) {
         return b0 == 1;
      } else {
         CompoundTag compoundtag;
         try {
            compoundtag = this.readChunk(p_140426_);
            if (compoundtag == null) {
               this.markPositionReplaceable(p_140426_);
               return false;
            }
         } catch (Exception exception) {
            LOGGER.error("Failed to read chunk {}", p_140426_, exception);
            this.markPositionReplaceable(p_140426_);
            return false;
         }

         ChunkStatus.ChunkType chunkstatus$chunktype = ChunkSerializer.getChunkTypeFromTag(compoundtag);
         return this.markPosition(p_140426_, chunkstatus$chunktype) == 1;
      }
   }

   protected void setViewDistance(int p_140168_) {
      int i = Mth.clamp(p_140168_ + 1, 3, 33);
      if (i != this.viewDistance) {
         int j = this.viewDistance;
         this.viewDistance = i;
         this.distanceManager.updatePlayerTickets(this.viewDistance);

         for(ChunkHolder chunkholder : this.updatingChunkMap.values()) {
            ChunkPos chunkpos = chunkholder.getPos();
            Packet<?>[] packet = new Packet[2];
            this.getPlayers(chunkpos, false).forEach((p_143071_) -> {
               int k = checkerboardDistance(chunkpos, p_143071_, true);
               boolean flag = k <= j;
               boolean flag1 = k <= this.viewDistance;
               this.updateChunkTracking(p_143071_, chunkpos, packet, flag, flag1);
            });
         }
      }

   }

   protected void updateChunkTracking(ServerPlayer p_140187_, ChunkPos p_140188_, Packet<?>[] p_140189_, boolean p_140190_, boolean p_140191_) {
      if (p_140187_.level == this.level) {
         if (p_140191_ && !p_140190_) {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_140188_.toLong());
            if (chunkholder != null) {
               LevelChunk levelchunk = chunkholder.getTickingChunk();
               if (levelchunk != null) {
                  this.playerLoadedChunk(p_140187_, p_140189_, levelchunk);
               }

               DebugPackets.sendPoiPacketsForChunk(this.level, p_140188_);
            }
         }

         if (!p_140191_ && p_140190_) {
            p_140187_.untrackChunk(p_140188_);
         }

      }
   }

   public int size() {
      return this.visibleChunkMap.size();
   }

   protected net.minecraft.server.level.DistanceManager getDistanceManager() {
      return this.distanceManager;
   }

   protected Iterable<ChunkHolder> getChunks() {
      return Iterables.unmodifiableIterable(this.visibleChunkMap.values());
   }

   void dumpChunks(Writer p_140275_) throws IOException {
      CsvOutput csvoutput = CsvOutput.builder().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").build(p_140275_);

      for(Entry<ChunkHolder> entry : this.visibleChunkMap.long2ObjectEntrySet()) {
         ChunkPos chunkpos = new ChunkPos(entry.getLongKey());
         ChunkHolder chunkholder = entry.getValue();
         Optional<ChunkAccess> optional = Optional.ofNullable(chunkholder.getLastAvailable());
         Optional<LevelChunk> optional1 = optional.flatMap((p_143123_) -> {
            return p_143123_ instanceof LevelChunk ? Optional.of((LevelChunk)p_143123_) : Optional.empty();
         });
         csvoutput.writeRow(chunkpos.x, chunkpos.z, chunkholder.getTicketLevel(), optional.isPresent(), optional.map(ChunkAccess::getStatus).orElse((ChunkStatus)null), optional1.map(LevelChunk::getFullStatus).orElse((ChunkHolder.FullChunkStatus)null), printFuture(chunkholder.getFullChunkFuture()), printFuture(chunkholder.getTickingChunkFuture()), printFuture(chunkholder.getEntityTickingChunkFuture()), this.distanceManager.getTicketDebugString(entry.getLongKey()), !this.noPlayersCloseForSpawning(chunkpos), optional1.map((p_140402_) -> {
            return p_140402_.getBlockEntities().size();
         }).orElse(0));
      }

   }

   private static String printFuture(CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> p_140279_) {
      try {
         Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = p_140279_.getNow((Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>)null);
         return either != null ? either.map((p_140380_) -> {
            return "done";
         }, (p_140286_) -> {
            return "unloaded";
         }) : "not completed";
      } catch (CompletionException completionexception) {
         return "failed " + completionexception.getCause().getMessage();
      } catch (CancellationException cancellationexception) {
         return "cancelled";
      }
   }

   @Nullable
   private CompoundTag readChunk(ChunkPos p_140428_) throws IOException {
      CompoundTag compoundtag = this.read(p_140428_);
      return compoundtag == null ? null : this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, compoundtag);
   }

   boolean noPlayersCloseForSpawning(ChunkPos p_140398_) {
      long i = p_140398_.toLong();
      return !this.distanceManager.hasPlayersNearby(i) ? true : this.playerMap.getPlayers(i).noneMatch((p_140221_) -> {
         return !p_140221_.isSpectator() && euclideanDistanceSquared(p_140398_, p_140221_) < 16384.0D;
      });
   }

   private boolean skipPlayer(ServerPlayer p_140330_) {
      return p_140330_.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
   }

   void updatePlayerStatus(ServerPlayer p_140193_, boolean p_140194_) {
      boolean flag = this.skipPlayer(p_140193_);
      boolean flag1 = this.playerMap.ignoredOrUnknown(p_140193_);
      int i = SectionPos.blockToSectionCoord(p_140193_.getBlockX());
      int j = SectionPos.blockToSectionCoord(p_140193_.getBlockZ());
      if (p_140194_) {
         this.playerMap.addPlayer(ChunkPos.asLong(i, j), p_140193_, flag);
         this.updatePlayerPos(p_140193_);
         if (!flag) {
            this.distanceManager.addPlayer(SectionPos.of(p_140193_), p_140193_);
         }
      } else {
         SectionPos sectionpos = p_140193_.getLastSectionPos();
         this.playerMap.removePlayer(sectionpos.chunk().toLong(), p_140193_);
         if (!flag1) {
            this.distanceManager.removePlayer(sectionpos, p_140193_);
         }
      }

      for(int l = i - this.viewDistance; l <= i + this.viewDistance; ++l) {
         for(int k = j - this.viewDistance; k <= j + this.viewDistance; ++k) {
            ChunkPos chunkpos = new ChunkPos(l, k);
            this.updateChunkTracking(p_140193_, chunkpos, new Packet[2], !p_140194_, p_140194_);
         }
      }

   }

   private SectionPos updatePlayerPos(ServerPlayer p_140374_) {
      SectionPos sectionpos = SectionPos.of(p_140374_);
      p_140374_.setLastSectionPos(sectionpos);
      p_140374_.connection.send(new ClientboundSetChunkCacheCenterPacket(sectionpos.x(), sectionpos.z()));
      return sectionpos;
   }

   public void move(ServerPlayer p_140185_) {
      for(ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
         if (chunkmap$trackedentity.entity == p_140185_) {
            chunkmap$trackedentity.updatePlayers(this.level.players());
         } else {
            chunkmap$trackedentity.updatePlayer(p_140185_);
         }
      }

      int l1 = SectionPos.blockToSectionCoord(p_140185_.getBlockX());
      int i2 = SectionPos.blockToSectionCoord(p_140185_.getBlockZ());
      SectionPos sectionpos = p_140185_.getLastSectionPos();
      SectionPos sectionpos1 = SectionPos.of(p_140185_);
      long i = sectionpos.chunk().toLong();
      long j = sectionpos1.chunk().toLong();
      boolean flag = this.playerMap.ignored(p_140185_);
      boolean flag1 = this.skipPlayer(p_140185_);
      boolean flag2 = sectionpos.asLong() != sectionpos1.asLong();
      if (flag2 || flag != flag1) {
         this.updatePlayerPos(p_140185_);
         if (!flag) {
            this.distanceManager.removePlayer(sectionpos, p_140185_);
         }

         if (!flag1) {
            this.distanceManager.addPlayer(sectionpos1, p_140185_);
         }

         if (!flag && flag1) {
            this.playerMap.ignorePlayer(p_140185_);
         }

         if (flag && !flag1) {
            this.playerMap.unIgnorePlayer(p_140185_);
         }

         if (i != j) {
            this.playerMap.updatePlayer(i, j, p_140185_);
         }
      }

      int k = sectionpos.x();
      int l = sectionpos.z();
      if (Math.abs(k - l1) <= this.viewDistance * 2 && Math.abs(l - i2) <= this.viewDistance * 2) {
         int k2 = Math.min(l1, k) - this.viewDistance;
         int i3 = Math.min(i2, l) - this.viewDistance;
         int j3 = Math.max(l1, k) + this.viewDistance;
         int k3 = Math.max(i2, l) + this.viewDistance;

         for(int l3 = k2; l3 <= j3; ++l3) {
            for(int k1 = i3; k1 <= k3; ++k1) {
               ChunkPos chunkpos1 = new ChunkPos(l3, k1);
               boolean flag5 = checkerboardDistance(chunkpos1, k, l) <= this.viewDistance;
               boolean flag6 = checkerboardDistance(chunkpos1, l1, i2) <= this.viewDistance;
               this.updateChunkTracking(p_140185_, chunkpos1, new Packet[2], flag5, flag6);
            }
         }
      } else {
         for(int i1 = k - this.viewDistance; i1 <= k + this.viewDistance; ++i1) {
            for(int j1 = l - this.viewDistance; j1 <= l + this.viewDistance; ++j1) {
               ChunkPos chunkpos = new ChunkPos(i1, j1);
               boolean flag3 = true;
               boolean flag4 = false;
               this.updateChunkTracking(p_140185_, chunkpos, new Packet[2], true, false);
            }
         }

         for(int j2 = l1 - this.viewDistance; j2 <= l1 + this.viewDistance; ++j2) {
            for(int l2 = i2 - this.viewDistance; l2 <= i2 + this.viewDistance; ++l2) {
               ChunkPos chunkpos2 = new ChunkPos(j2, l2);
               boolean flag7 = false;
               boolean flag8 = true;
               this.updateChunkTracking(p_140185_, chunkpos2, new Packet[2], false, true);
            }
         }
      }

   }

   public Stream<ServerPlayer> getPlayers(ChunkPos p_140252_, boolean p_140253_) {
      return this.playerMap.getPlayers(p_140252_.toLong()).filter((p_140257_) -> {
         int i = checkerboardDistance(p_140252_, p_140257_, true);
         if (i > this.viewDistance) {
            return false;
         } else {
            return !p_140253_ || i == this.viewDistance;
         }
      });
   }

   protected void addEntity(Entity p_140200_) {
      if (!(p_140200_ instanceof EnderDragonPart)) {
         EntityType<?> entitytype = p_140200_.getType();
         int i = entitytype.clientTrackingRange() * 16;
         if (i != 0) {
            int j = entitytype.updateInterval();
            if (this.entityMap.containsKey(p_140200_.getId())) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
            } else {
               ChunkMap.TrackedEntity chunkmap$trackedentity = new ChunkMap.TrackedEntity(p_140200_, i, j, entitytype.trackDeltas());
               this.entityMap.put(p_140200_.getId(), chunkmap$trackedentity);
               chunkmap$trackedentity.updatePlayers(this.level.players());
               if (p_140200_ instanceof ServerPlayer) {
                  ServerPlayer serverplayer = (ServerPlayer)p_140200_;
                  this.updatePlayerStatus(serverplayer, true);

                  for(ChunkMap.TrackedEntity chunkmap$trackedentity1 : this.entityMap.values()) {
                     if (chunkmap$trackedentity1.entity != serverplayer) {
                        chunkmap$trackedentity1.updatePlayer(serverplayer);
                     }
                  }
               }

            }
         }
      }
   }

   protected void removeEntity(Entity p_140332_) {
      if (p_140332_ instanceof ServerPlayer) {
         ServerPlayer serverplayer = (ServerPlayer)p_140332_;
         this.updatePlayerStatus(serverplayer, false);

         for(ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
            chunkmap$trackedentity.removePlayer(serverplayer);
         }
      }

      ChunkMap.TrackedEntity chunkmap$trackedentity1 = this.entityMap.remove(p_140332_.getId());
      if (chunkmap$trackedentity1 != null) {
         chunkmap$trackedentity1.broadcastRemoved();
      }

   }

   protected void tick() {
      List<ServerPlayer> list = Lists.newArrayList();
      List<ServerPlayer> list1 = this.level.players();

      for(ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
         SectionPos sectionpos = chunkmap$trackedentity.lastSectionPos;
         SectionPos sectionpos1 = SectionPos.of(chunkmap$trackedentity.entity);
         if (!Objects.equals(sectionpos, sectionpos1)) {
            chunkmap$trackedentity.updatePlayers(list1);
            Entity entity = chunkmap$trackedentity.entity;
            if (entity instanceof ServerPlayer) {
               list.add((ServerPlayer)entity);
            }

            chunkmap$trackedentity.lastSectionPos = sectionpos1;
         }

         chunkmap$trackedentity.serverEntity.sendChanges();
      }

      if (!list.isEmpty()) {
         for(ChunkMap.TrackedEntity chunkmap$trackedentity1 : this.entityMap.values()) {
            chunkmap$trackedentity1.updatePlayers(list);
         }
      }

   }

   public void broadcast(Entity p_140202_, Packet<?> p_140203_) {
      ChunkMap.TrackedEntity chunkmap$trackedentity = this.entityMap.get(p_140202_.getId());
      if (chunkmap$trackedentity != null) {
         chunkmap$trackedentity.broadcast(p_140203_);
      }

   }

   protected void broadcastAndSend(Entity p_140334_, Packet<?> p_140335_) {
      ChunkMap.TrackedEntity chunkmap$trackedentity = this.entityMap.get(p_140334_.getId());
      if (chunkmap$trackedentity != null) {
         chunkmap$trackedentity.broadcastAndSend(p_140335_);
      }

   }

   private void playerLoadedChunk(ServerPlayer p_140196_, Packet<?>[] p_140197_, LevelChunk p_140198_) {
      if (p_140197_[0] == null) {
         p_140197_[0] = new ClientboundLevelChunkPacket(p_140198_);
         p_140197_[1] = new ClientboundLightUpdatePacket(p_140198_.getPos(), this.lightEngine, (BitSet)null, (BitSet)null, true);
      }

      p_140196_.trackChunk(p_140198_.getPos(), p_140197_[0], p_140197_[1]);
      DebugPackets.sendPoiPacketsForChunk(this.level, p_140198_.getPos());
      List<Entity> list = Lists.newArrayList();
      List<Entity> list1 = Lists.newArrayList();

      for(ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
         Entity entity = chunkmap$trackedentity.entity;
         if (entity != p_140196_ && entity.chunkPosition().equals(p_140198_.getPos())) {
            chunkmap$trackedentity.updatePlayer(p_140196_);
            if (entity instanceof Mob && ((Mob)entity).getLeashHolder() != null) {
               list.add(entity);
            }

            if (!entity.getPassengers().isEmpty()) {
               list1.add(entity);
            }
         }
      }

      if (!list.isEmpty()) {
         for(Entity entity1 : list) {
            p_140196_.connection.send(new ClientboundSetEntityLinkPacket(entity1, ((Mob)entity1).getLeashHolder()));
         }
      }

      if (!list1.isEmpty()) {
         for(Entity entity2 : list1) {
            p_140196_.connection.send(new ClientboundSetPassengersPacket(entity2));
         }
      }

   }

   protected PoiManager getPoiManager() {
      return this.poiManager;
   }

   public String getStorageName() {
      return this.storageName;
   }

   public CompletableFuture<Void> packTicks(LevelChunk p_140271_) {
      return this.mainThreadExecutor.submit(() -> {
         p_140271_.packTicks(this.level);
      });
   }

   void onFullChunkStatusChange(ChunkPos p_143076_, ChunkHolder.FullChunkStatus p_143077_) {
      this.chunkStatusListener.onChunkStatusChange(p_143076_, p_143077_);
   }

   class DistanceManager extends net.minecraft.server.level.DistanceManager {
      protected DistanceManager(Executor p_140459_, Executor p_140460_) {
         super(p_140459_, p_140460_);
      }

      protected boolean isChunkToRemove(long p_140462_) {
         return ChunkMap.this.toDrop.contains(p_140462_);
      }

      @Nullable
      protected ChunkHolder getChunk(long p_140469_) {
         return ChunkMap.this.getUpdatingChunkIfPresent(p_140469_);
      }

      @Nullable
      protected ChunkHolder updateChunkScheduling(long p_140464_, int p_140465_, @Nullable ChunkHolder p_140466_, int p_140467_) {
         return ChunkMap.this.updateChunkScheduling(p_140464_, p_140465_, p_140466_, p_140467_);
      }
   }

   class TrackedEntity {
      final ServerEntity serverEntity;
      final Entity entity;
      private final int range;
      SectionPos lastSectionPos;
      private final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

      public TrackedEntity(Entity p_140478_, int p_140479_, int p_140480_, boolean p_140481_) {
         this.serverEntity = new ServerEntity(ChunkMap.this.level, p_140478_, p_140480_, p_140481_, this::broadcast);
         this.entity = p_140478_;
         this.range = p_140479_;
         this.lastSectionPos = SectionPos.of(p_140478_);
      }

      public boolean equals(Object p_140506_) {
         if (p_140506_ instanceof ChunkMap.TrackedEntity) {
            return ((ChunkMap.TrackedEntity)p_140506_).entity.getId() == this.entity.getId();
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.entity.getId();
      }

      public void broadcast(Packet<?> p_140490_) {
         for(ServerPlayerConnection serverplayerconnection : this.seenBy) {
            serverplayerconnection.send(p_140490_);
         }

      }

      public void broadcastAndSend(Packet<?> p_140500_) {
         this.broadcast(p_140500_);
         if (this.entity instanceof ServerPlayer) {
            ((ServerPlayer)this.entity).connection.send(p_140500_);
         }

      }

      public void broadcastRemoved() {
         for(ServerPlayerConnection serverplayerconnection : this.seenBy) {
            this.serverEntity.removePairing(serverplayerconnection.getPlayer());
         }

      }

      public void removePlayer(ServerPlayer p_140486_) {
         if (this.seenBy.remove(p_140486_.connection)) {
            this.serverEntity.removePairing(p_140486_);
         }

      }

      public void updatePlayer(ServerPlayer p_140498_) {
         if (p_140498_ != this.entity) {
            Vec3 vec3 = p_140498_.position().subtract(this.serverEntity.sentPos());
            int i = Math.min(this.getEffectiveRange(), (ChunkMap.this.viewDistance - 1) * 16);
            boolean flag = vec3.x >= (double)(-i) && vec3.x <= (double)i && vec3.z >= (double)(-i) && vec3.z <= (double)i && this.entity.broadcastToPlayer(p_140498_);
            if (flag) {
               if (this.seenBy.add(p_140498_.connection)) {
                  this.serverEntity.addPairing(p_140498_);
               }
            } else if (this.seenBy.remove(p_140498_.connection)) {
               this.serverEntity.removePairing(p_140498_);
            }

         }
      }

      private int scaledRange(int p_140484_) {
         return ChunkMap.this.level.getServer().getScaledTrackingDistance(p_140484_);
      }

      private int getEffectiveRange() {
         int i = this.range;

         for(Entity entity : this.entity.getIndirectPassengers()) {
            int j = entity.getType().clientTrackingRange() * 16;
            if (j > i) {
               i = j;
            }
         }

         return this.scaledRange(i);
      }

      public void updatePlayers(List<ServerPlayer> p_140488_) {
         for(ServerPlayer serverplayer : p_140488_) {
            this.updatePlayer(serverplayer);
         }

      }
   }
}