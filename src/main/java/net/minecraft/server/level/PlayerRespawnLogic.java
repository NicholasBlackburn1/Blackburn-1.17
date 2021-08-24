package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
   @Nullable
   protected static BlockPos getOverworldRespawnPos(ServerLevel p_8265_, int p_8266_, int p_8267_, boolean p_8268_) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(p_8266_, p_8265_.getMinBuildHeight(), p_8267_);
      Biome biome = p_8265_.getBiome(blockpos$mutableblockpos);
      boolean flag = p_8265_.dimensionType().hasCeiling();
      BlockState blockstate = biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
      if (p_8268_ && !blockstate.is(BlockTags.VALID_SPAWN)) {
         return null;
      } else {
         LevelChunk levelchunk = p_8265_.getChunk(SectionPos.blockToSectionCoord(p_8266_), SectionPos.blockToSectionCoord(p_8267_));
         int i = flag ? p_8265_.getChunkSource().getGenerator().getSpawnHeight(p_8265_) : levelchunk.getHeight(Heightmap.Types.MOTION_BLOCKING, p_8266_ & 15, p_8267_ & 15);
         if (i < p_8265_.getMinBuildHeight()) {
            return null;
         } else {
            int j = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, p_8266_ & 15, p_8267_ & 15);
            if (j <= i && j > levelchunk.getHeight(Heightmap.Types.OCEAN_FLOOR, p_8266_ & 15, p_8267_ & 15)) {
               return null;
            } else {
               for(int k = i + 1; k >= p_8265_.getMinBuildHeight(); --k) {
                  blockpos$mutableblockpos.set(p_8266_, k, p_8267_);
                  BlockState blockstate1 = p_8265_.getBlockState(blockpos$mutableblockpos);
                  if (!blockstate1.getFluidState().isEmpty()) {
                     break;
                  }

                  if (blockstate1.equals(blockstate)) {
                     return blockpos$mutableblockpos.above().immutable();
                  }
               }

               return null;
            }
         }
      }
   }

   @Nullable
   public static BlockPos getSpawnPosInChunk(ServerLevel p_8270_, ChunkPos p_8271_, boolean p_8272_) {
      for(int i = p_8271_.getMinBlockX(); i <= p_8271_.getMaxBlockX(); ++i) {
         for(int j = p_8271_.getMinBlockZ(); j <= p_8271_.getMaxBlockZ(); ++j) {
            BlockPos blockpos = getOverworldRespawnPos(p_8270_, i, j, p_8272_);
            if (blockpos != null) {
               return blockpos;
            }
         }
      }

      return null;
   }
}