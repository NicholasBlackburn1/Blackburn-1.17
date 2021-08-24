package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.DesertPyramidPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class DesertPyramidFeature extends StructureFeature<NoneFeatureConfiguration> {
   public DesertPyramidFeature(Codec<NoneFeatureConfiguration> p_65568_) {
      super(p_65568_);
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return DesertPyramidFeature.FeatureStart::new;
   }

   public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
      public FeatureStart(StructureFeature<NoneFeatureConfiguration> p_159550_, ChunkPos p_159551_, int p_159552_, long p_159553_) {
         super(p_159550_, p_159551_, p_159552_, p_159553_);
      }

      public void generatePieces(RegistryAccess p_159563_, ChunkGenerator p_159564_, StructureManager p_159565_, ChunkPos p_159566_, Biome p_159567_, NoneFeatureConfiguration p_159568_, LevelHeightAccessor p_159569_) {
         DesertPyramidPiece desertpyramidpiece = new DesertPyramidPiece(this.random, p_159566_.getMinBlockX(), p_159566_.getMinBlockZ());
         this.addPiece(desertpyramidpiece);
      }
   }
}