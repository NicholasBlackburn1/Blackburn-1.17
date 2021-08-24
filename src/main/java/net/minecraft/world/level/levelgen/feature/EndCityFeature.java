package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
   private static final int RANDOM_SALT = 10387313;

   public EndCityFeature(Codec<NoneFeatureConfiguration> p_65627_) {
      super(p_65627_);
   }

   protected boolean linearSeparation() {
      return false;
   }

   protected boolean isFeatureChunk(ChunkGenerator p_159684_, BiomeSource p_159685_, long p_159686_, WorldgenRandom p_159687_, ChunkPos p_159688_, Biome p_159689_, ChunkPos p_159690_, NoneFeatureConfiguration p_159691_, LevelHeightAccessor p_159692_) {
      return getYPositionForFeature(p_159688_, p_159684_, p_159692_) >= 60;
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return EndCityFeature.EndCityStart::new;
   }

   static int getYPositionForFeature(ChunkPos p_159670_, ChunkGenerator p_159671_, LevelHeightAccessor p_159672_) {
      Random random = new Random((long)(p_159670_.x + p_159670_.z * 10387313));
      Rotation rotation = Rotation.getRandom(random);
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

      int k = p_159670_.getBlockX(7);
      int l = p_159670_.getBlockZ(7);
      int i1 = p_159671_.getFirstOccupiedHeight(k, l, Heightmap.Types.WORLD_SURFACE_WG, p_159672_);
      int j1 = p_159671_.getFirstOccupiedHeight(k, l + j, Heightmap.Types.WORLD_SURFACE_WG, p_159672_);
      int k1 = p_159671_.getFirstOccupiedHeight(k + i, l, Heightmap.Types.WORLD_SURFACE_WG, p_159672_);
      int l1 = p_159671_.getFirstOccupiedHeight(k + i, l + j, Heightmap.Types.WORLD_SURFACE_WG, p_159672_);
      return Math.min(Math.min(i1, j1), Math.min(k1, l1));
   }

   public static class EndCityStart extends StructureStart<NoneFeatureConfiguration> {
      public EndCityStart(StructureFeature<NoneFeatureConfiguration> p_159694_, ChunkPos p_159695_, int p_159696_, long p_159697_) {
         super(p_159694_, p_159695_, p_159696_, p_159697_);
      }

      public void generatePieces(RegistryAccess p_159707_, ChunkGenerator p_159708_, StructureManager p_159709_, ChunkPos p_159710_, Biome p_159711_, NoneFeatureConfiguration p_159712_, LevelHeightAccessor p_159713_) {
         Rotation rotation = Rotation.getRandom(this.random);
         int i = EndCityFeature.getYPositionForFeature(p_159710_, p_159708_, p_159713_);
         if (i >= 60) {
            BlockPos blockpos = p_159710_.getMiddleBlockPosition(i);
            List<StructurePiece> list = Lists.newArrayList();
            EndCityPieces.startHouseTower(p_159709_, blockpos, rotation, list, this.random);
            list.forEach(this::addPiece);
         }
      }
   }
}