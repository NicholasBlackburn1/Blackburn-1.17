package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProtoChunk implements ChunkAccess {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ChunkPos chunkPos;
   private volatile boolean isDirty;
   @Nullable
   private ChunkBiomeContainer biomes;
   @Nullable
   private volatile LevelLightEngine lightEngine;
   private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
   private volatile ChunkStatus status = ChunkStatus.EMPTY;
   private final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
   private final Map<BlockPos, CompoundTag> blockEntityNbts = Maps.newHashMap();
   private final LevelChunkSection[] sections;
   private final List<CompoundTag> entities = Lists.newArrayList();
   private final List<BlockPos> lights = Lists.newArrayList();
   private final ShortList[] postProcessing;
   private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
   private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.newHashMap();
   private final UpgradeData upgradeData;
   private final ProtoTickList<Block> blockTicks;
   private final ProtoTickList<Fluid> liquidTicks;
   private final LevelHeightAccessor levelHeightAccessor;
   private long inhabitedTime;
   private final Map<GenerationStep.Carving, BitSet> carvingMasks = new Object2ObjectArrayMap<>();
   private volatile boolean isLightCorrect;

   public ProtoChunk(ChunkPos p_156477_, UpgradeData p_156478_, LevelHeightAccessor p_156479_) {
      this(p_156477_, p_156478_, (LevelChunkSection[])null, new ProtoTickList<>((p_63185_) -> {
         return p_63185_ == null || p_63185_.defaultBlockState().isAir();
      }, p_156477_, p_156479_), new ProtoTickList<>((p_63212_) -> {
         return p_63212_ == null || p_63212_ == Fluids.EMPTY;
      }, p_156477_, p_156479_), p_156479_);
   }

   public ProtoChunk(ChunkPos p_156481_, UpgradeData p_156482_, @Nullable LevelChunkSection[] p_156483_, ProtoTickList<Block> p_156484_, ProtoTickList<Fluid> p_156485_, LevelHeightAccessor p_156486_) {
      this.chunkPos = p_156481_;
      this.upgradeData = p_156482_;
      this.blockTicks = p_156484_;
      this.liquidTicks = p_156485_;
      this.levelHeightAccessor = p_156486_;
      this.sections = new LevelChunkSection[p_156486_.getSectionsCount()];
      if (p_156483_ != null) {
         if (this.sections.length == p_156483_.length) {
            System.arraycopy(p_156483_, 0, this.sections, 0, this.sections.length);
         } else {
            LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", p_156483_.length, this.sections.length);
         }
      }

      this.postProcessing = new ShortList[p_156486_.getSectionsCount()];
   }

   public BlockState getBlockState(BlockPos p_63264_) {
      int i = p_63264_.getY();
      if (this.isOutsideBuildHeight(i)) {
         return Blocks.VOID_AIR.defaultBlockState();
      } else {
         LevelChunkSection levelchunksection = this.getSections()[this.getSectionIndex(i)];
         return LevelChunkSection.isEmpty(levelchunksection) ? Blocks.AIR.defaultBlockState() : levelchunksection.getBlockState(p_63264_.getX() & 15, i & 15, p_63264_.getZ() & 15);
      }
   }

   public FluidState getFluidState(BlockPos p_63239_) {
      int i = p_63239_.getY();
      if (this.isOutsideBuildHeight(i)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         LevelChunkSection levelchunksection = this.getSections()[this.getSectionIndex(i)];
         return LevelChunkSection.isEmpty(levelchunksection) ? Fluids.EMPTY.defaultFluidState() : levelchunksection.getFluidState(p_63239_.getX() & 15, i & 15, p_63239_.getZ() & 15);
      }
   }

   public Stream<BlockPos> getLights() {
      return this.lights.stream();
   }

   public ShortList[] getPackedLights() {
      ShortList[] ashortlist = new ShortList[this.getSectionsCount()];

      for(BlockPos blockpos : this.lights) {
         ChunkAccess.getOrCreateOffsetList(ashortlist, this.getSectionIndex(blockpos.getY())).add(packOffsetCoordinates(blockpos));
      }

      return ashortlist;
   }

   public void addLight(short p_63245_, int p_63246_) {
      this.addLight(unpackOffsetCoordinates(p_63245_, this.getSectionYFromSectionIndex(p_63246_), this.chunkPos));
   }

   public void addLight(BlockPos p_63278_) {
      this.lights.add(p_63278_.immutable());
   }

   @Nullable
   public BlockState setBlockState(BlockPos p_63217_, BlockState p_63218_, boolean p_63219_) {
      int i = p_63217_.getX();
      int j = p_63217_.getY();
      int k = p_63217_.getZ();
      if (j >= this.getMinBuildHeight() && j < this.getMaxBuildHeight()) {
         int l = this.getSectionIndex(j);
         if (this.sections[l] == LevelChunk.EMPTY_SECTION && p_63218_.is(Blocks.AIR)) {
            return p_63218_;
         } else {
            if (p_63218_.getLightEmission() > 0) {
               this.lights.add(new BlockPos((i & 15) + this.getPos().getMinBlockX(), j, (k & 15) + this.getPos().getMinBlockZ()));
            }

            LevelChunkSection levelchunksection = this.getOrCreateSection(l);
            BlockState blockstate = levelchunksection.setBlockState(i & 15, j & 15, k & 15, p_63218_);
            if (this.status.isOrAfter(ChunkStatus.FEATURES) && p_63218_ != blockstate && (p_63218_.getLightBlock(this, p_63217_) != blockstate.getLightBlock(this, p_63217_) || p_63218_.getLightEmission() != blockstate.getLightEmission() || p_63218_.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion())) {
               this.lightEngine.checkBlock(p_63217_);
            }

            EnumSet<Heightmap.Types> enumset = this.getStatus().heightmapsAfter();
            EnumSet<Heightmap.Types> enumset1 = null;

            for(Heightmap.Types heightmap$types : enumset) {
               Heightmap heightmap = this.heightmaps.get(heightmap$types);
               if (heightmap == null) {
                  if (enumset1 == null) {
                     enumset1 = EnumSet.noneOf(Heightmap.Types.class);
                  }

                  enumset1.add(heightmap$types);
               }
            }

            if (enumset1 != null) {
               Heightmap.primeHeightmaps(this, enumset1);
            }

            for(Heightmap.Types heightmap$types1 : enumset) {
               this.heightmaps.get(heightmap$types1).update(i & 15, j, k & 15, p_63218_);
            }

            return blockstate;
         }
      } else {
         return Blocks.VOID_AIR.defaultBlockState();
      }
   }

   public void setBlockEntity(BlockEntity p_156488_) {
      this.blockEntities.put(p_156488_.getBlockPos(), p_156488_);
   }

   public Set<BlockPos> getBlockEntitiesPos() {
      Set<BlockPos> set = Sets.newHashSet(this.blockEntityNbts.keySet());
      set.addAll(this.blockEntities.keySet());
      return set;
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos p_63257_) {
      return this.blockEntities.get(p_63257_);
   }

   public Map<BlockPos, BlockEntity> getBlockEntities() {
      return this.blockEntities;
   }

   public void addEntity(CompoundTag p_63243_) {
      this.entities.add(p_63243_);
   }

   public void addEntity(Entity p_63183_) {
      if (!p_63183_.isPassenger()) {
         CompoundTag compoundtag = new CompoundTag();
         p_63183_.save(compoundtag);
         this.addEntity(compoundtag);
      }
   }

   public List<CompoundTag> getEntities() {
      return this.entities;
   }

   public void setBiomes(ChunkBiomeContainer p_63186_) {
      this.biomes = p_63186_;
   }

   @Nullable
   public ChunkBiomeContainer getBiomes() {
      return this.biomes;
   }

   public void setUnsaved(boolean p_63232_) {
      this.isDirty = p_63232_;
   }

   public boolean isUnsaved() {
      return this.isDirty;
   }

   public ChunkStatus getStatus() {
      return this.status;
   }

   public void setStatus(ChunkStatus p_63187_) {
      this.status = p_63187_;
      this.setUnsaved(true);
   }

   public LevelChunkSection[] getSections() {
      return this.sections;
   }

   public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
      return Collections.unmodifiableSet(this.heightmaps.entrySet());
   }

   public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types p_63193_) {
      return this.heightmaps.computeIfAbsent(p_63193_, (p_63253_) -> {
         return new Heightmap(this, p_63253_);
      });
   }

   public int getHeight(Heightmap.Types p_63195_, int p_63196_, int p_63197_) {
      Heightmap heightmap = this.heightmaps.get(p_63195_);
      if (heightmap == null) {
         Heightmap.primeHeightmaps(this, EnumSet.of(p_63195_));
         heightmap = this.heightmaps.get(p_63195_);
      }

      return heightmap.getFirstAvailable(p_63196_ & 15, p_63197_ & 15) - 1;
   }

   public BlockPos getHeighestPosition(Heightmap.Types p_156490_) {
      int i = this.getMinBuildHeight();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = this.chunkPos.getMinBlockX(); j <= this.chunkPos.getMaxBlockX(); ++j) {
         for(int k = this.chunkPos.getMinBlockZ(); k <= this.chunkPos.getMaxBlockZ(); ++k) {
            int l = this.getHeight(p_156490_, j & 15, k & 15);
            if (l > i) {
               i = l;
               blockpos$mutableblockpos.set(j, l, k);
            }
         }
      }

      return blockpos$mutableblockpos.immutable();
   }

   public ChunkPos getPos() {
      return this.chunkPos;
   }

   @Nullable
   public StructureStart<?> getStartForFeature(StructureFeature<?> p_63202_) {
      return this.structureStarts.get(p_63202_);
   }

   public void setStartForFeature(StructureFeature<?> p_63207_, StructureStart<?> p_63208_) {
      this.structureStarts.put(p_63207_, p_63208_);
      this.isDirty = true;
   }

   public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
      return Collections.unmodifiableMap(this.structureStarts);
   }

   public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> p_63221_) {
      this.structureStarts.clear();
      this.structureStarts.putAll(p_63221_);
      this.isDirty = true;
   }

   public LongSet getReferencesForFeature(StructureFeature<?> p_63237_) {
      return this.structuresRefences.computeIfAbsent(p_63237_, (p_63260_) -> {
         return new LongOpenHashSet();
      });
   }

   public void addReferenceForFeature(StructureFeature<?> p_63204_, long p_63205_) {
      this.structuresRefences.computeIfAbsent(p_63204_, (p_63255_) -> {
         return new LongOpenHashSet();
      }).add(p_63205_);
      this.isDirty = true;
   }

   public Map<StructureFeature<?>, LongSet> getAllReferences() {
      return Collections.unmodifiableMap(this.structuresRefences);
   }

   public void setAllReferences(Map<StructureFeature<?>, LongSet> p_63241_) {
      this.structuresRefences.clear();
      this.structuresRefences.putAll(p_63241_);
      this.isDirty = true;
   }

   public static short packOffsetCoordinates(BlockPos p_63281_) {
      int i = p_63281_.getX();
      int j = p_63281_.getY();
      int k = p_63281_.getZ();
      int l = i & 15;
      int i1 = j & 15;
      int j1 = k & 15;
      return (short)(l | i1 << 4 | j1 << 8);
   }

   public static BlockPos unpackOffsetCoordinates(short p_63228_, int p_63229_, ChunkPos p_63230_) {
      int i = SectionPos.sectionToBlockCoord(p_63230_.x, p_63228_ & 15);
      int j = SectionPos.sectionToBlockCoord(p_63229_, p_63228_ >>> 4 & 15);
      int k = SectionPos.sectionToBlockCoord(p_63230_.z, p_63228_ >>> 8 & 15);
      return new BlockPos(i, j, k);
   }

   public void markPosForPostprocessing(BlockPos p_63266_) {
      if (!this.isOutsideBuildHeight(p_63266_)) {
         ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(p_63266_.getY())).add(packOffsetCoordinates(p_63266_));
      }

   }

   public ShortList[] getPostProcessing() {
      return this.postProcessing;
   }

   public void addPackedPostProcess(short p_63225_, int p_63226_) {
      ChunkAccess.getOrCreateOffsetList(this.postProcessing, p_63226_).add(p_63225_);
   }

   public ProtoTickList<Block> getBlockTicks() {
      return this.blockTicks;
   }

   public ProtoTickList<Fluid> getLiquidTicks() {
      return this.liquidTicks;
   }

   public UpgradeData getUpgradeData() {
      return this.upgradeData;
   }

   public void setInhabitedTime(long p_63234_) {
      this.inhabitedTime = p_63234_;
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void setBlockEntityNbt(CompoundTag p_63223_) {
      this.blockEntityNbts.put(new BlockPos(p_63223_.getInt("x"), p_63223_.getInt("y"), p_63223_.getInt("z")), p_63223_);
   }

   public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
      return Collections.unmodifiableMap(this.blockEntityNbts);
   }

   public CompoundTag getBlockEntityNbt(BlockPos p_63272_) {
      return this.blockEntityNbts.get(p_63272_);
   }

   @Nullable
   public CompoundTag getBlockEntityNbtForSaving(BlockPos p_63275_) {
      BlockEntity blockentity = this.getBlockEntity(p_63275_);
      return blockentity != null ? blockentity.save(new CompoundTag()) : this.blockEntityNbts.get(p_63275_);
   }

   public void removeBlockEntity(BlockPos p_63262_) {
      this.blockEntities.remove(p_63262_);
      this.blockEntityNbts.remove(p_63262_);
   }

   @Nullable
   public BitSet getCarvingMask(GenerationStep.Carving p_63188_) {
      return this.carvingMasks.get(p_63188_);
   }

   public BitSet getOrCreateCarvingMask(GenerationStep.Carving p_63235_) {
      return this.carvingMasks.computeIfAbsent(p_63235_, (p_63251_) -> {
         return new BitSet(65536);
      });
   }

   public void setCarvingMask(GenerationStep.Carving p_63190_, BitSet p_63191_) {
      this.carvingMasks.put(p_63190_, p_63191_);
   }

   public void setLightEngine(LevelLightEngine p_63210_) {
      this.lightEngine = p_63210_;
   }

   public boolean isLightCorrect() {
      return this.isLightCorrect;
   }

   public void setLightCorrect(boolean p_63248_) {
      this.isLightCorrect = p_63248_;
      this.setUnsaved(true);
   }

   public int getMinBuildHeight() {
      return this.levelHeightAccessor.getMinBuildHeight();
   }

   public int getHeight() {
      return this.levelHeightAccessor.getHeight();
   }
}