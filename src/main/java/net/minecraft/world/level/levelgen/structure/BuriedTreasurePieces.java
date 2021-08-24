package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuriedTreasurePieces {
   public static class BuriedTreasurePiece extends StructurePiece {
      public BuriedTreasurePiece(BlockPos p_71068_) {
         super(StructurePieceType.BURIED_TREASURE_PIECE, 0, new BoundingBox(p_71068_));
      }

      public BuriedTreasurePiece(ServerLevel p_162404_, CompoundTag p_162405_) {
         super(StructurePieceType.BURIED_TREASURE_PIECE, p_162405_);
      }

      protected void addAdditionalSaveData(ServerLevel p_162407_, CompoundTag p_162408_) {
      }

      public boolean postProcess(WorldGenLevel p_71070_, StructureFeatureManager p_71071_, ChunkGenerator p_71072_, Random p_71073_, BoundingBox p_71074_, ChunkPos p_71075_, BlockPos p_71076_) {
         int i = p_71070_.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.boundingBox.minX(), this.boundingBox.minZ());
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(this.boundingBox.minX(), i, this.boundingBox.minZ());

         while(blockpos$mutableblockpos.getY() > p_71070_.getMinBuildHeight()) {
            BlockState blockstate = p_71070_.getBlockState(blockpos$mutableblockpos);
            BlockState blockstate1 = p_71070_.getBlockState(blockpos$mutableblockpos.below());
            if (blockstate1 == Blocks.SANDSTONE.defaultBlockState() || blockstate1 == Blocks.STONE.defaultBlockState() || blockstate1 == Blocks.ANDESITE.defaultBlockState() || blockstate1 == Blocks.GRANITE.defaultBlockState() || blockstate1 == Blocks.DIORITE.defaultBlockState()) {
               BlockState blockstate2 = !blockstate.isAir() && !this.isLiquid(blockstate) ? blockstate : Blocks.SAND.defaultBlockState();

               for(Direction direction : Direction.values()) {
                  BlockPos blockpos = blockpos$mutableblockpos.relative(direction);
                  BlockState blockstate3 = p_71070_.getBlockState(blockpos);
                  if (blockstate3.isAir() || this.isLiquid(blockstate3)) {
                     BlockPos blockpos1 = blockpos.below();
                     BlockState blockstate4 = p_71070_.getBlockState(blockpos1);
                     if ((blockstate4.isAir() || this.isLiquid(blockstate4)) && direction != Direction.UP) {
                        p_71070_.setBlock(blockpos, blockstate1, 3);
                     } else {
                        p_71070_.setBlock(blockpos, blockstate2, 3);
                     }
                  }
               }

               this.boundingBox = new BoundingBox(blockpos$mutableblockpos);
               return this.createChest(p_71070_, p_71074_, p_71073_, blockpos$mutableblockpos, BuiltInLootTables.BURIED_TREASURE, (BlockState)null);
            }

            blockpos$mutableblockpos.move(0, -1, 0);
         }

         return false;
      }

      private boolean isLiquid(BlockState p_71078_) {
         return p_71078_ == Blocks.WATER.defaultBlockState() || p_71078_ == Blocks.LAVA.defaultBlockState();
      }
   }
}