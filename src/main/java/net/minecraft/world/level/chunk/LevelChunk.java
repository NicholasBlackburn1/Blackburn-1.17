package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.EuclideanGameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelChunk implements ChunkAccess {
   static final Logger LOGGER = LogManager.getLogger();
   private static final TickingBlockEntity NULL_TICKER = new TickingBlockEntity() {
      public void tick() {
      }

      public boolean isRemoved() {
         return true;
      }

      public BlockPos getPos() {
         return BlockPos.ZERO;
      }

      public String getType() {
         return "<null>";
      }
   };
   @Nullable
   public static final LevelChunkSection EMPTY_SECTION = null;
   private final LevelChunkSection[] sections;
   private ChunkBiomeContainer biomes;
   private final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
   private final Map<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.newHashMap();
   private boolean loaded;
   final Level level;
   private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
   private final UpgradeData upgradeData;
   private final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
   private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
   private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.newHashMap();
   private final ShortList[] postProcessing;
   private TickList<Block> blockTicks;
   private TickList<Fluid> liquidTicks;
   private volatile boolean unsaved;
   private long inhabitedTime;
   @Nullable
   private Supplier<ChunkHolder.FullChunkStatus> fullStatus;
   @Nullable
   private Consumer<LevelChunk> postLoad;
   private final ChunkPos chunkPos;
   private volatile boolean isLightCorrect;
   private final Int2ObjectMap<GameEventDispatcher> gameEventDispatcherSections;

   public LevelChunk(Level p_62796_, ChunkPos p_62797_, ChunkBiomeContainer p_62798_) {
      this(p_62796_, p_62797_, p_62798_, UpgradeData.EMPTY, EmptyTickList.empty(), EmptyTickList.empty(), 0L, (LevelChunkSection[])null, (Consumer<LevelChunk>)null);
   }

   public LevelChunk(Level p_62800_, ChunkPos p_62801_, ChunkBiomeContainer p_62802_, UpgradeData p_62803_, TickList<Block> p_62804_, TickList<Fluid> p_62805_, long p_62806_, @Nullable LevelChunkSection[] p_62807_, @Nullable Consumer<LevelChunk> p_62808_) {
      this.level = p_62800_;
      this.chunkPos = p_62801_;
      this.upgradeData = p_62803_;
      this.gameEventDispatcherSections = new Int2ObjectOpenHashMap<>();

      for(Heightmap.Types heightmap$types : Heightmap.Types.values()) {
         if (ChunkStatus.FULL.heightmapsAfter().contains(heightmap$types)) {
            this.heightmaps.put(heightmap$types, new Heightmap(this, heightmap$types));
         }
      }

      this.biomes = p_62802_;
      this.blockTicks = p_62804_;
      this.liquidTicks = p_62805_;
      this.inhabitedTime = p_62806_;
      this.postLoad = p_62808_;
      this.sections = new LevelChunkSection[p_62800_.getSectionsCount()];
      if (p_62807_ != null) {
         if (this.sections.length == p_62807_.length) {
            System.arraycopy(p_62807_, 0, this.sections, 0, this.sections.length);
         } else {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", p_62807_.length, this.sections.length);
         }
      }

      this.postProcessing = new ShortList[p_62800_.getSectionsCount()];
   }

   public LevelChunk(ServerLevel p_156365_, ProtoChunk p_156366_, @Nullable Consumer<LevelChunk> p_156367_) {
      this(p_156365_, p_156366_.getPos(), p_156366_.getBiomes(), p_156366_.getUpgradeData(), p_156366_.getBlockTicks(), p_156366_.getLiquidTicks(), p_156366_.getInhabitedTime(), p_156366_.getSections(), p_156367_);

      for(BlockEntity blockentity : p_156366_.getBlockEntities().values()) {
         this.setBlockEntity(blockentity);
      }

      this.pendingBlockEntities.putAll(p_156366_.getBlockEntityNbts());

      for(int i = 0; i < p_156366_.getPostProcessing().length; ++i) {
         this.postProcessing[i] = p_156366_.getPostProcessing()[i];
      }

      this.setAllStarts(p_156366_.getAllStarts());
      this.setAllReferences(p_156366_.getAllReferences());

      for(Entry<Heightmap.Types, Heightmap> entry : p_156366_.getHeightmaps()) {
         if (ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) {
            this.setHeightmap(entry.getKey(), entry.getValue().getRawData());
         }
      }

      this.setLightCorrect(p_156366_.isLightCorrect());
      this.unsaved = true;
   }

   public GameEventDispatcher getEventDispatcher(int p_156372_) {
      return this.gameEventDispatcherSections.computeIfAbsent(p_156372_, (p_156395_) -> {
         return new EuclideanGameEventDispatcher(this.level);
      });
   }

   public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types p_62845_) {
      return this.heightmaps.computeIfAbsent(p_62845_, (p_62908_) -> {
         return new Heightmap(this, p_62908_);
      });
   }

   public Set<BlockPos> getBlockEntitiesPos() {
      Set<BlockPos> set = Sets.newHashSet(this.pendingBlockEntities.keySet());
      set.addAll(this.blockEntities.keySet());
      return set;
   }

   public LevelChunkSection[] getSections() {
      return this.sections;
   }

   public BlockState getBlockState(BlockPos p_62923_) {
      int i = p_62923_.getX();
      int j = p_62923_.getY();
      int k = p_62923_.getZ();
      if (this.level.isDebug()) {
         BlockState blockstate = null;
         if (j == 60) {
            blockstate = Blocks.BARRIER.defaultBlockState();
         }

         if (j == 70) {
            blockstate = DebugLevelSource.getBlockStateFor(i, k);
         }

         return blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate;
      } else {
         try {
            int l = this.getSectionIndex(j);
            if (l >= 0 && l < this.sections.length) {
               LevelChunkSection levelchunksection = this.sections[l];
               if (!LevelChunkSection.isEmpty(levelchunksection)) {
                  return levelchunksection.getBlockState(i & 15, j & 15, k & 15);
               }
            }

            return Blocks.AIR.defaultBlockState();
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting block state");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
            crashreportcategory.setDetail("Location", () -> {
               return CrashReportCategory.formatLocation(this, i, j, k);
            });
            throw new ReportedException(crashreport);
         }
      }
   }

   public FluidState getFluidState(BlockPos p_62895_) {
      return this.getFluidState(p_62895_.getX(), p_62895_.getY(), p_62895_.getZ());
   }

   public FluidState getFluidState(int p_62815_, int p_62816_, int p_62817_) {
      try {
         int i = this.getSectionIndex(p_62816_);
         if (i >= 0 && i < this.sections.length) {
            LevelChunkSection levelchunksection = this.sections[i];
            if (!LevelChunkSection.isEmpty(levelchunksection)) {
               return levelchunksection.getFluidState(p_62815_ & 15, p_62816_ & 15, p_62817_ & 15);
            }
         }

         return Fluids.EMPTY.defaultFluidState();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting fluid state");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
         crashreportcategory.setDetail("Location", () -> {
            return CrashReportCategory.formatLocation(this, p_62815_, p_62816_, p_62817_);
         });
         throw new ReportedException(crashreport);
      }
   }

   @Nullable
   public BlockState setBlockState(BlockPos p_62865_, BlockState p_62866_, boolean p_62867_) {
      int i = p_62865_.getY();
      int j = this.getSectionIndex(i);
      LevelChunkSection levelchunksection = this.sections[j];
      if (levelchunksection == EMPTY_SECTION) {
         if (p_62866_.isAir()) {
            return null;
         }

         levelchunksection = new LevelChunkSection(SectionPos.blockToSectionCoord(i));
         this.sections[j] = levelchunksection;
      }

      boolean flag = levelchunksection.isEmpty();
      int k = p_62865_.getX() & 15;
      int l = i & 15;
      int i1 = p_62865_.getZ() & 15;
      BlockState blockstate = levelchunksection.setBlockState(k, l, i1, p_62866_);
      if (blockstate == p_62866_) {
         return null;
      } else {
         Block block = p_62866_.getBlock();
         this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(k, i, i1, p_62866_);
         this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(k, i, i1, p_62866_);
         this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(k, i, i1, p_62866_);
         this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(k, i, i1, p_62866_);
         boolean flag1 = levelchunksection.isEmpty();
         if (flag != flag1) {
            this.level.getChunkSource().getLightEngine().updateSectionStatus(p_62865_, flag1);
         }

         boolean flag2 = blockstate.hasBlockEntity();
         if (!this.level.isClientSide) {
            blockstate.onRemove(this.level, p_62865_, p_62866_, p_62867_);
         } else if (!blockstate.is(block) && flag2) {
            this.removeBlockEntity(p_62865_);
         }

         if (!levelchunksection.getBlockState(k, l, i1).is(block)) {
            return null;
         } else {
            if (!this.level.isClientSide) {
               p_62866_.onPlace(this.level, p_62865_, blockstate, p_62867_);
            }

            if (p_62866_.hasBlockEntity()) {
               BlockEntity blockentity = this.getBlockEntity(p_62865_, LevelChunk.EntityCreationType.CHECK);
               if (blockentity == null) {
                  blockentity = ((EntityBlock)block).newBlockEntity(p_62865_, p_62866_);
                  if (blockentity != null) {
                     this.addAndRegisterBlockEntity(blockentity);
                  }
               } else {
                  blockentity.setBlockState(p_62866_);
                  this.updateBlockEntityTicker(blockentity);
               }
            }

            this.unsaved = true;
            return blockstate;
         }
      }
   }

   @Deprecated
   public void addEntity(Entity p_62826_) {
   }

   public int getHeight(Heightmap.Types p_62847_, int p_62848_, int p_62849_) {
      return this.heightmaps.get(p_62847_).getFirstAvailable(p_62848_ & 15, p_62849_ & 15) - 1;
   }

   public BlockPos getHeighestPosition(Heightmap.Types p_156393_) {
      ChunkPos chunkpos = this.getPos();
      int i = this.getMinBuildHeight();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = chunkpos.getMinBlockX(); j <= chunkpos.getMaxBlockX(); ++j) {
         for(int k = chunkpos.getMinBlockZ(); k <= chunkpos.getMaxBlockZ(); ++k) {
            int l = this.getHeight(p_156393_, j & 15, k & 15);
            if (l > i) {
               i = l;
               blockpos$mutableblockpos.set(j, l, k);
            }
         }
      }

      return blockpos$mutableblockpos.immutable();
   }

   @Nullable
   private BlockEntity createBlockEntity(BlockPos p_62935_) {
      BlockState blockstate = this.getBlockState(p_62935_);
      return !blockstate.hasBlockEntity() ? null : ((EntityBlock)blockstate.getBlock()).newBlockEntity(p_62935_, blockstate);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos p_62912_) {
      return this.getBlockEntity(p_62912_, LevelChunk.EntityCreationType.CHECK);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos p_62868_, LevelChunk.EntityCreationType p_62869_) {
      BlockEntity blockentity = this.blockEntities.get(p_62868_);
      if (blockentity == null) {
         CompoundTag compoundtag = this.pendingBlockEntities.remove(p_62868_);
         if (compoundtag != null) {
            BlockEntity blockentity1 = this.promotePendingBlockEntity(p_62868_, compoundtag);
            if (blockentity1 != null) {
               return blockentity1;
            }
         }
      }

      if (blockentity == null) {
         if (p_62869_ == LevelChunk.EntityCreationType.IMMEDIATE) {
            blockentity = this.createBlockEntity(p_62868_);
            if (blockentity != null) {
               this.addAndRegisterBlockEntity(blockentity);
            }
         }
      } else if (blockentity.isRemoved()) {
         this.blockEntities.remove(p_62868_);
         return null;
      }

      return blockentity;
   }

   public void addAndRegisterBlockEntity(BlockEntity p_156391_) {
      this.setBlockEntity(p_156391_);
      if (this.isInLevel()) {
         this.addGameEventListener(p_156391_);
         this.updateBlockEntityTicker(p_156391_);
      }

   }

   private boolean isInLevel() {
      return this.loaded || this.level.isClientSide();
   }

   boolean isTicking(BlockPos p_156411_) {
      if (!this.level.getWorldBorder().isWithinBounds(p_156411_)) {
         return false;
      } else if (!(this.level instanceof ServerLevel)) {
         return true;
      } else {
         return this.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING) && ((ServerLevel)this.level).areEntitiesLoaded(ChunkPos.asLong(p_156411_));
      }
   }

   public void setBlockEntity(BlockEntity p_156374_) {
      BlockPos blockpos = p_156374_.getBlockPos();
      if (this.getBlockState(blockpos).hasBlockEntity()) {
         p_156374_.setLevel(this.level);
         p_156374_.clearRemoved();
         BlockEntity blockentity = this.blockEntities.put(blockpos.immutable(), p_156374_);
         if (blockentity != null && blockentity != p_156374_) {
            blockentity.setRemoved();
         }

      }
   }

   public void setBlockEntityNbt(CompoundTag p_62882_) {
      this.pendingBlockEntities.put(new BlockPos(p_62882_.getInt("x"), p_62882_.getInt("y"), p_62882_.getInt("z")), p_62882_);
   }

   @Nullable
   public CompoundTag getBlockEntityNbtForSaving(BlockPos p_62932_) {
      BlockEntity blockentity = this.getBlockEntity(p_62932_);
      if (blockentity != null && !blockentity.isRemoved()) {
         CompoundTag compoundtag1 = blockentity.save(new CompoundTag());
         compoundtag1.putBoolean("keepPacked", false);
         return compoundtag1;
      } else {
         CompoundTag compoundtag = this.pendingBlockEntities.get(p_62932_);
         if (compoundtag != null) {
            compoundtag = compoundtag.copy();
            compoundtag.putBoolean("keepPacked", true);
         }

         return compoundtag;
      }
   }

   public void removeBlockEntity(BlockPos p_62919_) {
      if (this.isInLevel()) {
         BlockEntity blockentity = this.blockEntities.remove(p_62919_);
         if (blockentity != null) {
            this.removeGameEventListener(blockentity);
            blockentity.setRemoved();
         }
      }

      this.removeBlockEntityTicker(p_62919_);
   }

   private <T extends BlockEntity> void removeGameEventListener(T p_156397_) {
      if (!this.level.isClientSide) {
         Block block = p_156397_.getBlockState().getBlock();
         if (block instanceof EntityBlock) {
            GameEventListener gameeventlistener = ((EntityBlock)block).getListener(this.level, p_156397_);
            if (gameeventlistener != null) {
               int i = SectionPos.blockToSectionCoord(p_156397_.getBlockPos().getY());
               GameEventDispatcher gameeventdispatcher = this.getEventDispatcher(i);
               gameeventdispatcher.unregister(gameeventlistener);
               if (gameeventdispatcher.isEmpty()) {
                  this.gameEventDispatcherSections.remove(i);
               }
            }
         }

      }
   }

   private void removeBlockEntityTicker(BlockPos p_156413_) {
      LevelChunk.RebindableTickingBlockEntityWrapper levelchunk$rebindabletickingblockentitywrapper = this.tickersInLevel.remove(p_156413_);
      if (levelchunk$rebindabletickingblockentitywrapper != null) {
         levelchunk$rebindabletickingblockentitywrapper.rebind(NULL_TICKER);
      }

   }

   public void runPostLoad() {
      if (this.postLoad != null) {
         this.postLoad.accept(this);
         this.postLoad = null;
      }

   }

   public void markUnsaved() {
      this.unsaved = true;
   }

   public boolean isEmpty() {
      return false;
   }

   public ChunkPos getPos() {
      return this.chunkPos;
   }

   public void replaceWithPacketData(@Nullable ChunkBiomeContainer p_156384_, FriendlyByteBuf p_156385_, CompoundTag p_156386_, BitSet p_156387_) {
      boolean flag = p_156384_ != null;
      if (flag) {
         this.blockEntities.values().forEach(this::onBlockEntityRemove);
         this.blockEntities.clear();
      } else {
         this.blockEntities.values().removeIf((p_156390_) -> {
            int j = this.getSectionIndex(p_156390_.getBlockPos().getY());
            if (p_156387_.get(j)) {
               p_156390_.setRemoved();
               return true;
            } else {
               return false;
            }
         });
      }

      for(int i = 0; i < this.sections.length; ++i) {
         LevelChunkSection levelchunksection = this.sections[i];
         if (!p_156387_.get(i)) {
            if (flag && levelchunksection != EMPTY_SECTION) {
               this.sections[i] = EMPTY_SECTION;
            }
         } else {
            if (levelchunksection == EMPTY_SECTION) {
               levelchunksection = new LevelChunkSection(this.getSectionYFromSectionIndex(i));
               this.sections[i] = levelchunksection;
            }

            levelchunksection.read(p_156385_);
         }
      }

      if (p_156384_ != null) {
         this.biomes = p_156384_;
      }

      for(Heightmap.Types heightmap$types : Heightmap.Types.values()) {
         String s = heightmap$types.getSerializationKey();
         if (p_156386_.contains(s, 12)) {
            this.setHeightmap(heightmap$types, p_156386_.getLongArray(s));
         }
      }

   }

   private void onBlockEntityRemove(BlockEntity p_156401_) {
      p_156401_.setRemoved();
      this.tickersInLevel.remove(p_156401_.getBlockPos());
   }

   public ChunkBiomeContainer getBiomes() {
      return this.biomes;
   }

   public void setLoaded(boolean p_62914_) {
      this.loaded = p_62914_;
   }

   public Level getLevel() {
      return this.level;
   }

   public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
      return Collections.unmodifiableSet(this.heightmaps.entrySet());
   }

   public Map<BlockPos, BlockEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public CompoundTag getBlockEntityNbt(BlockPos p_62929_) {
      return this.pendingBlockEntities.get(p_62929_);
   }

   public Stream<BlockPos> getLights() {
      return StreamSupport.stream(BlockPos.betweenClosed(this.chunkPos.getMinBlockX(), this.getMinBuildHeight(), this.chunkPos.getMinBlockZ(), this.chunkPos.getMaxBlockX(), this.getMaxBuildHeight() - 1, this.chunkPos.getMaxBlockZ()).spliterator(), false).filter((p_156419_) -> {
         return this.getBlockState(p_156419_).getLightEmission() != 0;
      });
   }

   public TickList<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public TickList<Fluid> getLiquidTicks() {
      return this.liquidTicks;
   }

   public void setUnsaved(boolean p_62884_) {
      this.unsaved = p_62884_;
   }

   public boolean isUnsaved() {
      return this.unsaved;
   }

   @Nullable
   public StructureStart<?> getStartForFeature(StructureFeature<?> p_62854_) {
      return this.structureStarts.get(p_62854_);
   }

   public void setStartForFeature(StructureFeature<?> p_62859_, StructureStart<?> p_62860_) {
      this.structureStarts.put(p_62859_, p_62860_);
   }

   public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
      return this.structureStarts;
   }

   public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> p_62878_) {
      this.structureStarts.clear();
      this.structureStarts.putAll(p_62878_);
   }

   public LongSet getReferencesForFeature(StructureFeature<?> p_62893_) {
      return this.structuresRefences.computeIfAbsent(p_62893_, (p_156403_) -> {
         return new LongOpenHashSet();
      });
   }

   public void addReferenceForFeature(StructureFeature<?> p_62856_, long p_62857_) {
      this.structuresRefences.computeIfAbsent(p_62856_, (p_156399_) -> {
         return new LongOpenHashSet();
      }).add(p_62857_);
   }

   public Map<StructureFeature<?>, LongSet> getAllReferences() {
      return this.structuresRefences;
   }

   public void setAllReferences(Map<StructureFeature<?>, LongSet> p_62897_) {
      this.structuresRefences.clear();
      this.structuresRefences.putAll(p_62897_);
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void setInhabitedTime(long p_62890_) {
      this.inhabitedTime = p_62890_;
   }

   public void postProcessGeneration() {
      ChunkPos chunkpos = this.getPos();

      for(int i = 0; i < this.postProcessing.length; ++i) {
         if (this.postProcessing[i] != null) {
            for(Short oshort : this.postProcessing[i]) {
               BlockPos blockpos = ProtoChunk.unpackOffsetCoordinates(oshort, this.getSectionYFromSectionIndex(i), chunkpos);
               BlockState blockstate = this.getBlockState(blockpos);
               BlockState blockstate1 = Block.updateFromNeighbourShapes(blockstate, this.level, blockpos);
               this.level.setBlock(blockpos, blockstate1, 20);
            }

            this.postProcessing[i].clear();
         }
      }

      this.unpackTicks();

      for(BlockPos blockpos1 : ImmutableList.copyOf(this.pendingBlockEntities.keySet())) {
         this.getBlockEntity(blockpos1);
      }

      this.pendingBlockEntities.clear();
      this.upgradeData.upgrade(this);
   }

   @Nullable
   private BlockEntity promotePendingBlockEntity(BlockPos p_62871_, CompoundTag p_62872_) {
      BlockState blockstate = this.getBlockState(p_62871_);
      BlockEntity blockentity;
      if ("DUMMY".equals(p_62872_.getString("id"))) {
         if (blockstate.hasBlockEntity()) {
            blockentity = ((EntityBlock)blockstate.getBlock()).newBlockEntity(p_62871_, blockstate);
         } else {
            blockentity = null;
            LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", p_62871_, blockstate);
         }
      } else {
         blockentity = BlockEntity.loadStatic(p_62871_, blockstate, p_62872_);
      }

      if (blockentity != null) {
         blockentity.setLevel(this.level);
         this.addAndRegisterBlockEntity(blockentity);
      } else {
         LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", blockstate, p_62871_);
      }

      return blockentity;
   }

   public UpgradeData getUpgradeData() {
      return this.upgradeData;
   }

   public ShortList[] getPostProcessing() {
      return this.postProcessing;
   }

   public void unpackTicks() {
      if (this.blockTicks instanceof ProtoTickList) {
         ((ProtoTickList)this.blockTicks).copyOut(this.level.getBlockTicks(), (p_156417_) -> {
            return this.getBlockState((BlockPos)p_156417_).getBlock();
         });
         this.blockTicks = EmptyTickList.empty();
      } else if (this.blockTicks instanceof ChunkTickList) {
         ((ChunkTickList)this.blockTicks).copyOut(this.level.getBlockTicks());
         this.blockTicks = EmptyTickList.empty();
      }

      if (this.liquidTicks instanceof ProtoTickList) {
         ((ProtoTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks(), (p_156415_) -> {
            return this.getFluidState((BlockPos)p_156415_).getType();
         });
         this.liquidTicks = EmptyTickList.empty();
      } else if (this.liquidTicks instanceof ChunkTickList) {
         ((ChunkTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks());
         this.liquidTicks = EmptyTickList.empty();
      }

   }

   public void packTicks(ServerLevel p_62824_) {
      if (this.blockTicks == EmptyTickList.<Block>empty()) {
         this.blockTicks = new ChunkTickList<>(Registry.BLOCK::getKey, p_62824_.getBlockTicks().fetchTicksInChunk(this.chunkPos, true, false), p_62824_.getGameTime());
         this.setUnsaved(true);
      }

      if (this.liquidTicks == EmptyTickList.<Fluid>empty()) {
         this.liquidTicks = new ChunkTickList<>(Registry.FLUID::getKey, p_62824_.getLiquidTicks().fetchTicksInChunk(this.chunkPos, true, false), p_62824_.getGameTime());
         this.setUnsaved(true);
      }

   }

   public int getMinBuildHeight() {
      return this.level.getMinBuildHeight();
   }

   public int getHeight() {
      return this.level.getHeight();
   }

   public ChunkStatus getStatus() {
      return ChunkStatus.FULL;
   }

   public ChunkHolder.FullChunkStatus getFullStatus() {
      return this.fullStatus == null ? ChunkHolder.FullChunkStatus.BORDER : this.fullStatus.get();
   }

   public void setFullStatus(Supplier<ChunkHolder.FullChunkStatus> p_62880_) {
      this.fullStatus = p_62880_;
   }

   public boolean isLightCorrect() {
      return this.isLightCorrect;
   }

   public void setLightCorrect(boolean p_62899_) {
      this.isLightCorrect = p_62899_;
      this.setUnsaved(true);
   }

   public void invalidateAllBlockEntities() {
      this.blockEntities.values().forEach(this::onBlockEntityRemove);
   }

   public void registerAllBlockEntitiesAfterLevelLoad() {
      this.blockEntities.values().forEach((p_156409_) -> {
         this.addGameEventListener(p_156409_);
         this.updateBlockEntityTicker(p_156409_);
      });
   }

   private <T extends BlockEntity> void addGameEventListener(T p_156405_) {
      if (!this.level.isClientSide) {
         Block block = p_156405_.getBlockState().getBlock();
         if (block instanceof EntityBlock) {
            GameEventListener gameeventlistener = ((EntityBlock)block).getListener(this.level, p_156405_);
            if (gameeventlistener != null) {
               GameEventDispatcher gameeventdispatcher = this.getEventDispatcher(SectionPos.blockToSectionCoord(p_156405_.getBlockPos().getY()));
               gameeventdispatcher.register(gameeventlistener);
            }
         }

      }
   }

   private <T extends BlockEntity> void updateBlockEntityTicker(T p_156407_) {
      BlockState blockstate = p_156407_.getBlockState();
      BlockEntityTicker<T> blockentityticker = (BlockEntityTicker<T>)blockstate.getTicker(this.level, p_156407_.getType());
      if (blockentityticker == null) {
         this.removeBlockEntityTicker(p_156407_.getBlockPos());
      } else {
         this.tickersInLevel.compute(p_156407_.getBlockPos(), (p_156381_, p_156382_) -> {
            TickingBlockEntity tickingblockentity = this.createTicker(p_156407_, blockentityticker);
            if (p_156382_ != null) {
               p_156382_.rebind(tickingblockentity);
               return p_156382_;
            } else if (this.isInLevel()) {
               LevelChunk.RebindableTickingBlockEntityWrapper levelchunk$rebindabletickingblockentitywrapper = new LevelChunk.RebindableTickingBlockEntityWrapper(tickingblockentity);
               this.level.addBlockEntityTicker(levelchunk$rebindabletickingblockentitywrapper);
               return levelchunk$rebindabletickingblockentitywrapper;
            } else {
               return null;
            }
         });
      }

   }

   private <T extends BlockEntity> TickingBlockEntity createTicker(T p_156376_, BlockEntityTicker<T> p_156377_) {
      return new LevelChunk.BoundTickingBlockEntity<>(p_156376_, p_156377_);
   }

   class BoundTickingBlockEntity<T extends BlockEntity> implements TickingBlockEntity {
      private final T blockEntity;
      private final BlockEntityTicker<T> ticker;
      private boolean loggedInvalidBlockState;

      BoundTickingBlockEntity(T p_156433_, BlockEntityTicker<T> p_156434_) {
         this.blockEntity = p_156433_;
         this.ticker = p_156434_;
      }

      public void tick() {
         if (!this.blockEntity.isRemoved() && this.blockEntity.hasLevel()) {
            BlockPos blockpos = this.blockEntity.getBlockPos();
            if (LevelChunk.this.isTicking(blockpos)) {
               try {
                  ProfilerFiller profilerfiller = LevelChunk.this.level.getProfiler();
                  profilerfiller.push(this::getType);
                  BlockState blockstate = LevelChunk.this.getBlockState(blockpos);
                  if (this.blockEntity.getType().isValid(blockstate)) {
                     this.ticker.tick(LevelChunk.this.level, this.blockEntity.getBlockPos(), blockstate, this.blockEntity);
                     this.loggedInvalidBlockState = false;
                  } else if (!this.loggedInvalidBlockState) {
                     this.loggedInvalidBlockState = true;
                     LevelChunk.LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", this::getType, this::getPos, () -> {
                        return blockstate;
                     });
                  }

                  profilerfiller.pop();
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking block entity");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Block entity being ticked");
                  this.blockEntity.fillCrashReportCategory(crashreportcategory);
                  throw new ReportedException(crashreport);
               }
            }
         }

      }

      public boolean isRemoved() {
         return this.blockEntity.isRemoved();
      }

      public BlockPos getPos() {
         return this.blockEntity.getBlockPos();
      }

      public String getType() {
         return BlockEntityType.getKey(this.blockEntity.getType()).toString();
      }

      public String toString() {
         return "Level ticker for " + this.getType() + "@" + this.getPos();
      }
   }

   public static enum EntityCreationType {
      IMMEDIATE,
      QUEUED,
      CHECK;
   }

   class RebindableTickingBlockEntityWrapper implements TickingBlockEntity {
      private TickingBlockEntity ticker;

      RebindableTickingBlockEntityWrapper(TickingBlockEntity p_156447_) {
         this.ticker = p_156447_;
      }

      void rebind(TickingBlockEntity p_156450_) {
         this.ticker = p_156450_;
      }

      public void tick() {
         this.ticker.tick();
      }

      public boolean isRemoved() {
         return this.ticker.isRemoved();
      }

      public BlockPos getPos() {
         return this.ticker.getPos();
      }

      public String getType() {
         return this.ticker.getType();
      }

      public String toString() {
         return this.ticker.toString() + " <wrapped>";
      }
   }
}
