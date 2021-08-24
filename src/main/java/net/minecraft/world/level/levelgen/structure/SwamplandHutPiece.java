package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;

public class SwamplandHutPiece extends ScatteredFeaturePiece {
   private boolean spawnedWitch;
   private boolean spawnedCat;

   public SwamplandHutPiece(Random p_73640_, int p_73641_, int p_73642_) {
      super(StructurePieceType.SWAMPLAND_HUT, p_73641_, 64, p_73642_, 7, 7, 9, getRandomHorizontalDirection(p_73640_));
   }

   public SwamplandHutPiece(ServerLevel p_163653_, CompoundTag p_163654_) {
      super(StructurePieceType.SWAMPLAND_HUT, p_163654_);
      this.spawnedWitch = p_163654_.getBoolean("Witch");
      this.spawnedCat = p_163654_.getBoolean("Cat");
   }

   protected void addAdditionalSaveData(ServerLevel p_163656_, CompoundTag p_163657_) {
      super.addAdditionalSaveData(p_163656_, p_163657_);
      p_163657_.putBoolean("Witch", this.spawnedWitch);
      p_163657_.putBoolean("Cat", this.spawnedCat);
   }

   public boolean postProcess(WorldGenLevel p_73647_, StructureFeatureManager p_73648_, ChunkGenerator p_73649_, Random p_73650_, BoundingBox p_73651_, ChunkPos p_73652_, BlockPos p_73653_) {
      if (!this.updateAverageGroundHeight(p_73647_, p_73651_, 0)) {
         return false;
      } else {
         this.generateBox(p_73647_, p_73651_, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(p_73647_, p_73651_, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.placeBlock(p_73647_, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, p_73651_);
         this.placeBlock(p_73647_, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, p_73651_);
         this.placeBlock(p_73647_, Blocks.AIR.defaultBlockState(), 1, 3, 4, p_73651_);
         this.placeBlock(p_73647_, Blocks.AIR.defaultBlockState(), 5, 3, 4, p_73651_);
         this.placeBlock(p_73647_, Blocks.AIR.defaultBlockState(), 5, 3, 5, p_73651_);
         this.placeBlock(p_73647_, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, p_73651_);
         this.placeBlock(p_73647_, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, p_73651_);
         this.placeBlock(p_73647_, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, p_73651_);
         this.placeBlock(p_73647_, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, p_73651_);
         this.placeBlock(p_73647_, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, p_73651_);
         BlockState blockstate = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         BlockState blockstate1 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
         BlockState blockstate2 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
         BlockState blockstate3 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         this.generateBox(p_73647_, p_73651_, 0, 4, 1, 6, 4, 1, blockstate, blockstate, false);
         this.generateBox(p_73647_, p_73651_, 0, 4, 2, 0, 4, 7, blockstate1, blockstate1, false);
         this.generateBox(p_73647_, p_73651_, 6, 4, 2, 6, 4, 7, blockstate2, blockstate2, false);
         this.generateBox(p_73647_, p_73651_, 0, 4, 8, 6, 4, 8, blockstate3, blockstate3, false);
         this.placeBlock(p_73647_, blockstate.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, p_73651_);
         this.placeBlock(p_73647_, blockstate.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, p_73651_);
         this.placeBlock(p_73647_, blockstate3.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, p_73651_);
         this.placeBlock(p_73647_, blockstate3.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, p_73651_);

         for(int i = 2; i <= 7; i += 5) {
            for(int j = 1; j <= 5; j += 4) {
               this.fillColumnDown(p_73647_, Blocks.OAK_LOG.defaultBlockState(), j, -1, i, p_73651_);
            }
         }

         if (!this.spawnedWitch) {
            BlockPos blockpos = this.getWorldPos(2, 2, 5);
            if (p_73651_.isInside(blockpos)) {
               this.spawnedWitch = true;
               Witch witch = EntityType.WITCH.create(p_73647_.getLevel());
               witch.setPersistenceRequired();
               witch.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
               witch.finalizeSpawn(p_73647_, p_73647_.getCurrentDifficultyAt(blockpos), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
               p_73647_.addFreshEntityWithPassengers(witch);
            }
         }

         this.spawnCat(p_73647_, p_73651_);
         return true;
      }
   }

   private void spawnCat(ServerLevelAccessor p_73644_, BoundingBox p_73645_) {
      if (!this.spawnedCat) {
         BlockPos blockpos = this.getWorldPos(2, 2, 5);
         if (p_73645_.isInside(blockpos)) {
            this.spawnedCat = true;
            Cat cat = EntityType.CAT.create(p_73644_.getLevel());
            cat.setPersistenceRequired();
            cat.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
            cat.finalizeSpawn(p_73644_, p_73644_.getCurrentDifficultyAt(blockpos), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
            p_73644_.addFreshEntityWithPassengers(cat);
         }
      }

   }
}