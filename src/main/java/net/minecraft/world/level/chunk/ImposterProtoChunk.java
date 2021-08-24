package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class ImposterProtoChunk extends ProtoChunk {
   private final LevelChunk wrapped;

   public ImposterProtoChunk(LevelChunk p_62687_) {
      super(p_62687_.getPos(), UpgradeData.EMPTY, p_62687_);
      this.wrapped = p_62687_;
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos p_62744_) {
      return this.wrapped.getBlockEntity(p_62744_);
   }

   @Nullable
   public BlockState getBlockState(BlockPos p_62749_) {
      return this.wrapped.getBlockState(p_62749_);
   }

   public FluidState getFluidState(BlockPos p_62736_) {
      return this.wrapped.getFluidState(p_62736_);
   }

   public int getMaxLightLevel() {
      return this.wrapped.getMaxLightLevel();
   }

   @Nullable
   public BlockState setBlockState(BlockPos p_62722_, BlockState p_62723_, boolean p_62724_) {
      return null;
   }

   public void setBlockEntity(BlockEntity p_156358_) {
   }

   public void addEntity(Entity p_62692_) {
   }

   public void setStatus(ChunkStatus p_62698_) {
   }

   public LevelChunkSection[] getSections() {
      return this.wrapped.getSections();
   }

   public void setHeightmap(Heightmap.Types p_62706_, long[] p_62707_) {
   }

   private Heightmap.Types fixType(Heightmap.Types p_62742_) {
      if (p_62742_ == Heightmap.Types.WORLD_SURFACE_WG) {
         return Heightmap.Types.WORLD_SURFACE;
      } else {
         return p_62742_ == Heightmap.Types.OCEAN_FLOOR_WG ? Heightmap.Types.OCEAN_FLOOR : p_62742_;
      }
   }

   public int getHeight(Heightmap.Types p_62702_, int p_62703_, int p_62704_) {
      return this.wrapped.getHeight(this.fixType(p_62702_), p_62703_, p_62704_);
   }

   public BlockPos getHeighestPosition(Heightmap.Types p_156360_) {
      return this.wrapped.getHeighestPosition(this.fixType(p_156360_));
   }

   public ChunkPos getPos() {
      return this.wrapped.getPos();
   }

   @Nullable
   public StructureStart<?> getStartForFeature(StructureFeature<?> p_62709_) {
      return this.wrapped.getStartForFeature(p_62709_);
   }

   public void setStartForFeature(StructureFeature<?> p_62714_, StructureStart<?> p_62715_) {
   }

   public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
      return this.wrapped.getAllStarts();
   }

   public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> p_62726_) {
   }

   public LongSet getReferencesForFeature(StructureFeature<?> p_62734_) {
      return this.wrapped.getReferencesForFeature(p_62734_);
   }

   public void addReferenceForFeature(StructureFeature<?> p_62711_, long p_62712_) {
   }

   public Map<StructureFeature<?>, LongSet> getAllReferences() {
      return this.wrapped.getAllReferences();
   }

   public void setAllReferences(Map<StructureFeature<?>, LongSet> p_62738_) {
   }

   public ChunkBiomeContainer getBiomes() {
      return this.wrapped.getBiomes();
   }

   public void setUnsaved(boolean p_62730_) {
   }

   public boolean isUnsaved() {
      return false;
   }

   public ChunkStatus getStatus() {
      return this.wrapped.getStatus();
   }

   public void removeBlockEntity(BlockPos p_62747_) {
   }

   public void markPosForPostprocessing(BlockPos p_62752_) {
   }

   public void setBlockEntityNbt(CompoundTag p_62728_) {
   }

   @Nullable
   public CompoundTag getBlockEntityNbt(BlockPos p_62757_) {
      return this.wrapped.getBlockEntityNbt(p_62757_);
   }

   @Nullable
   public CompoundTag getBlockEntityNbtForSaving(BlockPos p_62760_) {
      return this.wrapped.getBlockEntityNbtForSaving(p_62760_);
   }

   public void setBiomes(ChunkBiomeContainer p_62696_) {
   }

   public Stream<BlockPos> getLights() {
      return this.wrapped.getLights();
   }

   public ProtoTickList<Block> getBlockTicks() {
      return new ProtoTickList<>((p_62694_) -> {
         return p_62694_.defaultBlockState().isAir();
      }, this.getPos(), this);
   }

   public ProtoTickList<Fluid> getLiquidTicks() {
      return new ProtoTickList<>((p_62717_) -> {
         return p_62717_ == Fluids.EMPTY;
      }, this.getPos(), this);
   }

   public BitSet getCarvingMask(GenerationStep.Carving p_62700_) {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
   }

   public BitSet getOrCreateCarvingMask(GenerationStep.Carving p_62732_) {
      throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
   }

   public LevelChunk getWrapped() {
      return this.wrapped;
   }

   public boolean isLightCorrect() {
      return this.wrapped.isLightCorrect();
   }

   public void setLightCorrect(boolean p_62740_) {
      this.wrapped.setLightCorrect(p_62740_);
   }
}