package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class EmptyLevelChunk extends LevelChunk {
   public EmptyLevelChunk(Level p_62584_, ChunkPos p_62585_) {
      super(p_62584_, p_62585_, new EmptyLevelChunk.EmptyChunkBiomeContainer(p_62584_));
   }

   public BlockState getBlockState(BlockPos p_62625_) {
      return Blocks.VOID_AIR.defaultBlockState();
   }

   @Nullable
   public BlockState setBlockState(BlockPos p_62605_, BlockState p_62606_, boolean p_62607_) {
      return null;
   }

   public FluidState getFluidState(BlockPos p_62621_) {
      return Fluids.EMPTY.defaultFluidState();
   }

   public int getLightEmission(BlockPos p_62628_) {
      return 0;
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos p_62609_, LevelChunk.EntityCreationType p_62610_) {
      return null;
   }

   public void addAndRegisterBlockEntity(BlockEntity p_156346_) {
   }

   public void setBlockEntity(BlockEntity p_156344_) {
   }

   public void removeBlockEntity(BlockPos p_62623_) {
   }

   public void markUnsaved() {
   }

   public boolean isEmpty() {
      return true;
   }

   public boolean isYSpaceEmpty(int p_62587_, int p_62588_) {
      return true;
   }

   public ChunkHolder.FullChunkStatus getFullStatus() {
      return ChunkHolder.FullChunkStatus.BORDER;
   }

   static class EmptyChunkBiomeContainer extends ChunkBiomeContainer {
      private static final Biome[] EMPTY_BIOMES = new Biome[0];

      public EmptyChunkBiomeContainer(Level p_156350_) {
         super(p_156350_.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), p_156350_, EMPTY_BIOMES);
      }

      public int[] writeBiomes() {
         throw new UnsupportedOperationException("Can not write biomes of an empty chunk");
      }

      public Biome getNoiseBiome(int p_156353_, int p_156354_, int p_156355_) {
         return Biomes.PLAINS;
      }
   }
}