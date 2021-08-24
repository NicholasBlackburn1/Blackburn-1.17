package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.JunglePyramidPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JunglePyramidFeature extends StructureFeature<NoneFeatureConfiguration> {
   public JunglePyramidFeature(Codec<NoneFeatureConfiguration> p_66193_) {
      super(p_66193_);
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return JunglePyramidFeature.FeatureStart::new;
   }

   public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
      public FeatureStart(StructureFeature<NoneFeatureConfiguration> p_159935_, ChunkPos p_159936_, int p_159937_, long p_159938_) {
         super(p_159935_, p_159936_, p_159937_, p_159938_);
      }

      public void generatePieces(RegistryAccess p_159948_, ChunkGenerator p_159949_, StructureManager p_159950_, ChunkPos p_159951_, Biome p_159952_, NoneFeatureConfiguration p_159953_, LevelHeightAccessor p_159954_) {
         JunglePyramidPiece junglepyramidpiece = new JunglePyramidPiece(this.random, p_159951_.getMinBlockX(), p_159951_.getMinBlockZ());
         this.addPiece(junglepyramidpiece);
      }
   }
}