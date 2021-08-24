package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
   private static final WeightedRandomList<MobSpawnSettings.SpawnerData> MONUMENT_ENEMIES = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4));

   public OceanMonumentFeature(Codec<NoneFeatureConfiguration> p_66472_) {
      super(p_66472_);
   }

   protected boolean linearSeparation() {
      return false;
   }

   protected boolean isFeatureChunk(ChunkGenerator p_160136_, BiomeSource p_160137_, long p_160138_, WorldgenRandom p_160139_, ChunkPos p_160140_, Biome p_160141_, ChunkPos p_160142_, NoneFeatureConfiguration p_160143_, LevelHeightAccessor p_160144_) {
      int i = p_160140_.getBlockX(9);
      int j = p_160140_.getBlockZ(9);

      for(Biome biome : p_160137_.getBiomesWithin(i, p_160136_.getSeaLevel(), j, 16)) {
         if (!biome.getGenerationSettings().isValidStart(this)) {
            return false;
         }
      }

      for(Biome biome1 : p_160137_.getBiomesWithin(i, p_160136_.getSeaLevel(), j, 29)) {
         if (biome1.getBiomeCategory() != Biome.BiomeCategory.OCEAN && biome1.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
            return false;
         }
      }

      return true;
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return OceanMonumentFeature.OceanMonumentStart::new;
   }

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
      return MONUMENT_ENEMIES;
   }

   public static class OceanMonumentStart extends StructureStart<NoneFeatureConfiguration> {
      private boolean isCreated;

      public OceanMonumentStart(StructureFeature<NoneFeatureConfiguration> p_160147_, ChunkPos p_160148_, int p_160149_, long p_160150_) {
         super(p_160147_, p_160148_, p_160149_, p_160150_);
      }

      public void generatePieces(RegistryAccess p_160162_, ChunkGenerator p_160163_, StructureManager p_160164_, ChunkPos p_160165_, Biome p_160166_, NoneFeatureConfiguration p_160167_, LevelHeightAccessor p_160168_) {
         this.generatePieces(p_160165_);
      }

      private void generatePieces(ChunkPos p_160152_) {
         int i = p_160152_.getMinBlockX() - 29;
         int j = p_160152_.getMinBlockZ() - 29;
         Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
         this.addPiece(new OceanMonumentPieces.MonumentBuilding(this.random, i, j, direction));
         this.isCreated = true;
      }

      public void placeInChunk(WorldGenLevel p_66505_, StructureFeatureManager p_66506_, ChunkGenerator p_66507_, Random p_66508_, BoundingBox p_66509_, ChunkPos p_66510_) {
         if (!this.isCreated) {
            this.pieces.clear();
            this.generatePieces(this.getChunkPos());
         }

         super.placeInChunk(p_66505_, p_66506_, p_66507_, p_66508_, p_66509_, p_66510_);
      }
   }
}