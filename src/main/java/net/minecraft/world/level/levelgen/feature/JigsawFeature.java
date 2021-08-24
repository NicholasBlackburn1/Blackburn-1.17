package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JigsawFeature extends StructureFeature<JigsawConfiguration> {
   final int startY;
   final boolean doExpansionHack;
   final boolean projectStartToHeightmap;

   public JigsawFeature(Codec<JigsawConfiguration> p_66150_, int p_66151_, boolean p_66152_, boolean p_66153_) {
      super(p_66150_);
      this.startY = p_66151_;
      this.doExpansionHack = p_66152_;
      this.projectStartToHeightmap = p_66153_;
   }

   public StructureFeature.StructureStartFactory<JigsawConfiguration> getStartFactory() {
      return (p_159909_, p_159910_, p_159911_, p_159912_) -> {
         return new JigsawFeature.FeatureStart(this, p_159910_, p_159911_, p_159912_);
      };
   }

   public static class FeatureStart extends NoiseAffectingStructureStart<JigsawConfiguration> {
      private final JigsawFeature feature;

      public FeatureStart(JigsawFeature p_159914_, ChunkPos p_159915_, int p_159916_, long p_159917_) {
         super(p_159914_, p_159915_, p_159916_, p_159917_);
         this.feature = p_159914_;
      }

      public void generatePieces(RegistryAccess p_159927_, ChunkGenerator p_159928_, StructureManager p_159929_, ChunkPos p_159930_, Biome p_159931_, JigsawConfiguration p_159932_, LevelHeightAccessor p_159933_) {
         BlockPos blockpos = new BlockPos(p_159930_.getMinBlockX(), this.feature.startY, p_159930_.getMinBlockZ());
         Pools.bootstrap();
         JigsawPlacement.addPieces(p_159927_, p_159932_, PoolElementStructurePiece::new, p_159928_, p_159929_, blockpos, this, this.random, this.feature.doExpansionHack, this.feature.projectStartToHeightmap, p_159933_);
      }
   }
}