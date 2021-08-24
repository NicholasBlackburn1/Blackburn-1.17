package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature extends StructureFeature<NoneFeatureConfiguration> {
   private static final WeightedRandomList<MobSpawnSettings.SpawnerData> SWAMPHUT_ENEMIES = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1));
   private static final WeightedRandomList<MobSpawnSettings.SpawnerData> SWAMPHUT_ANIMALS = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1));

   public SwamplandHutFeature(Codec<NoneFeatureConfiguration> p_67173_) {
      super(p_67173_);
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return SwamplandHutFeature.FeatureStart::new;
   }

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
      return SWAMPHUT_ENEMIES;
   }

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialAnimals() {
      return SWAMPHUT_ANIMALS;
   }

   public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
      public FeatureStart(StructureFeature<NoneFeatureConfiguration> p_160489_, ChunkPos p_160490_, int p_160491_, long p_160492_) {
         super(p_160489_, p_160490_, p_160491_, p_160492_);
      }

      public void generatePieces(RegistryAccess p_160502_, ChunkGenerator p_160503_, StructureManager p_160504_, ChunkPos p_160505_, Biome p_160506_, NoneFeatureConfiguration p_160507_, LevelHeightAccessor p_160508_) {
         SwamplandHutPiece swamplandhutpiece = new SwamplandHutPiece(this.random, p_160505_.getMinBlockX(), p_160505_.getMinBlockZ());
         this.addPiece(swamplandhutpiece);
      }
   }
}