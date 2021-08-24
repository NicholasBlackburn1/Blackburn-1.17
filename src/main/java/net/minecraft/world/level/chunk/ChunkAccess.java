package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;

public interface ChunkAccess extends BlockGetter, FeatureAccess {
   default GameEventDispatcher getEventDispatcher(int p_156113_) {
      return GameEventDispatcher.NOOP;
   }

   @Nullable
   BlockState setBlockState(BlockPos p_62087_, BlockState p_62088_, boolean p_62089_);

   void setBlockEntity(BlockEntity p_156114_);

   void addEntity(Entity p_62078_);

   @Nullable
   default LevelChunkSection getHighestSection() {
      LevelChunkSection[] alevelchunksection = this.getSections();

      for(int i = alevelchunksection.length - 1; i >= 0; --i) {
         LevelChunkSection levelchunksection = alevelchunksection[i];
         if (!LevelChunkSection.isEmpty(levelchunksection)) {
            return levelchunksection;
         }
      }

      return null;
   }

   default int getHighestSectionPosition() {
      LevelChunkSection levelchunksection = this.getHighestSection();
      return levelchunksection == null ? this.getMinBuildHeight() : levelchunksection.bottomBlockY();
   }

   Set<BlockPos> getBlockEntitiesPos();

   LevelChunkSection[] getSections();

   default LevelChunkSection getOrCreateSection(int p_156116_) {
      LevelChunkSection[] alevelchunksection = this.getSections();
      if (alevelchunksection[p_156116_] == LevelChunk.EMPTY_SECTION) {
         alevelchunksection[p_156116_] = new LevelChunkSection(this.getSectionYFromSectionIndex(p_156116_));
      }

      return alevelchunksection[p_156116_];
   }

   Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps();

   default void setHeightmap(Heightmap.Types p_62083_, long[] p_62084_) {
      this.getOrCreateHeightmapUnprimed(p_62083_).setRawData(this, p_62083_, p_62084_);
   }

   Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types p_62079_);

   int getHeight(Heightmap.Types p_62080_, int p_62081_, int p_62082_);

   BlockPos getHeighestPosition(Heightmap.Types p_156117_);

   ChunkPos getPos();

   Map<StructureFeature<?>, StructureStart<?>> getAllStarts();

   void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> p_62090_);

   default boolean isYSpaceEmpty(int p_62075_, int p_62076_) {
      if (p_62075_ < this.getMinBuildHeight()) {
         p_62075_ = this.getMinBuildHeight();
      }

      if (p_62076_ >= this.getMaxBuildHeight()) {
         p_62076_ = this.getMaxBuildHeight() - 1;
      }

      for(int i = p_62075_; i <= p_62076_; i += 16) {
         if (!LevelChunkSection.isEmpty(this.getSections()[this.getSectionIndex(i)])) {
            return false;
         }
      }

      return true;
   }

   @Nullable
   ChunkBiomeContainer getBiomes();

   void setUnsaved(boolean p_62094_);

   boolean isUnsaved();

   ChunkStatus getStatus();

   void removeBlockEntity(BlockPos p_62101_);

   default void markPosForPostprocessing(BlockPos p_62102_) {
      LogManager.getLogger().warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", (Object)p_62102_);
   }

   ShortList[] getPostProcessing();

   default void addPackedPostProcess(short p_62092_, int p_62093_) {
      getOrCreateOffsetList(this.getPostProcessing(), p_62093_).add(p_62092_);
   }

   default void setBlockEntityNbt(CompoundTag p_62091_) {
      LogManager.getLogger().warn("Trying to set a BlockEntity, but this operation is not supported.");
   }

   @Nullable
   CompoundTag getBlockEntityNbt(BlockPos p_62103_);

   @Nullable
   CompoundTag getBlockEntityNbtForSaving(BlockPos p_62104_);

   Stream<BlockPos> getLights();

   TickList<Block> getBlockTicks();

   TickList<Fluid> getLiquidTicks();

   UpgradeData getUpgradeData();

   void setInhabitedTime(long p_62099_);

   long getInhabitedTime();

   static ShortList getOrCreateOffsetList(ShortList[] p_62096_, int p_62097_) {
      if (p_62096_[p_62097_] == null) {
         p_62096_[p_62097_] = new ShortArrayList();
      }

      return p_62096_[p_62097_];
   }

   boolean isLightCorrect();

   void setLightCorrect(boolean p_62100_);
}