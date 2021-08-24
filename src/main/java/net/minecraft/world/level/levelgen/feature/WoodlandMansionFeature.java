package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
   public WoodlandMansionFeature(Codec<NoneFeatureConfiguration> p_67427_) {
      super(p_67427_);
   }

   protected boolean linearSeparation() {
      return false;
   }

   protected boolean isFeatureChunk(ChunkGenerator p_160677_, BiomeSource p_160678_, long p_160679_, WorldgenRandom p_160680_, ChunkPos p_160681_, Biome p_160682_, ChunkPos p_160683_, NoneFeatureConfiguration p_160684_, LevelHeightAccessor p_160685_) {
      for(Biome biome : p_160678_.getBiomesWithin(p_160681_.getBlockX(9), p_160677_.getSeaLevel(), p_160681_.getBlockZ(9), 32)) {
         if (!biome.getGenerationSettings().isValidStart(this)) {
            return false;
         }
      }

      return true;
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return WoodlandMansionFeature.WoodlandMansionStart::new;
   }

   public static class WoodlandMansionStart extends StructureStart<NoneFeatureConfiguration> {
      public WoodlandMansionStart(StructureFeature<NoneFeatureConfiguration> p_160687_, ChunkPos p_160688_, int p_160689_, long p_160690_) {
         super(p_160687_, p_160688_, p_160689_, p_160690_);
      }

      public void generatePieces(RegistryAccess p_160700_, ChunkGenerator p_160701_, StructureManager p_160702_, ChunkPos p_160703_, Biome p_160704_, NoneFeatureConfiguration p_160705_, LevelHeightAccessor p_160706_) {
         Rotation rotation = Rotation.getRandom(this.random);
         int i = 5;
         int j = 5;
         if (rotation == Rotation.CLOCKWISE_90) {
            i = -5;
         } else if (rotation == Rotation.CLOCKWISE_180) {
            i = -5;
            j = -5;
         } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            j = -5;
         }

         int k = p_160703_.getBlockX(7);
         int l = p_160703_.getBlockZ(7);
         int i1 = p_160701_.getFirstOccupiedHeight(k, l, Heightmap.Types.WORLD_SURFACE_WG, p_160706_);
         int j1 = p_160701_.getFirstOccupiedHeight(k, l + j, Heightmap.Types.WORLD_SURFACE_WG, p_160706_);
         int k1 = p_160701_.getFirstOccupiedHeight(k + i, l, Heightmap.Types.WORLD_SURFACE_WG, p_160706_);
         int l1 = p_160701_.getFirstOccupiedHeight(k + i, l + j, Heightmap.Types.WORLD_SURFACE_WG, p_160706_);
         int i2 = Math.min(Math.min(i1, j1), Math.min(k1, l1));
         if (i2 >= 60) {
            BlockPos blockpos = new BlockPos(p_160703_.getBlockX(8), i2 + 1, p_160703_.getBlockZ(8));
            List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.newLinkedList();
            WoodlandMansionPieces.generateMansion(p_160702_, blockpos, rotation, list, this.random);
            list.forEach(this::addPiece);
         }
      }

      public void placeInChunk(WorldGenLevel p_67458_, StructureFeatureManager p_67459_, ChunkGenerator p_67460_, Random p_67461_, BoundingBox p_67462_, ChunkPos p_67463_) {
         super.placeInChunk(p_67458_, p_67459_, p_67460_, p_67461_, p_67462_, p_67463_);
         BoundingBox boundingbox = this.getBoundingBox();
         int i = boundingbox.minY();

         for(int j = p_67462_.minX(); j <= p_67462_.maxX(); ++j) {
            for(int k = p_67462_.minZ(); k <= p_67462_.maxZ(); ++k) {
               BlockPos blockpos = new BlockPos(j, i, k);
               if (!p_67458_.isEmptyBlock(blockpos) && boundingbox.isInside(blockpos) && this.isInsidePiece(blockpos)) {
                  for(int l = i - 1; l > 1; --l) {
                     BlockPos blockpos1 = new BlockPos(j, l, k);
                     if (!p_67458_.isEmptyBlock(blockpos1) && !p_67458_.getBlockState(blockpos1).getMaterial().isLiquid()) {
                        break;
                     }

                     p_67458_.setBlock(blockpos1, Blocks.COBBLESTONE.defaultBlockState(), 2);
                  }
               }
            }
         }

      }
   }
}