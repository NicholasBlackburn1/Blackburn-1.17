package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.IglooPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class IglooFeature extends StructureFeature<NoneFeatureConfiguration> {
   public IglooFeature(Codec<NoneFeatureConfiguration> p_66121_) {
      super(p_66121_);
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return IglooFeature.FeatureStart::new;
   }

   public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
      public FeatureStart(StructureFeature<NoneFeatureConfiguration> p_159888_, ChunkPos p_159889_, int p_159890_, long p_159891_) {
         super(p_159888_, p_159889_, p_159890_, p_159891_);
      }

      public void generatePieces(RegistryAccess p_159901_, ChunkGenerator p_159902_, StructureManager p_159903_, ChunkPos p_159904_, Biome p_159905_, NoneFeatureConfiguration p_159906_, LevelHeightAccessor p_159907_) {
         BlockPos blockpos = new BlockPos(p_159904_.getMinBlockX(), 90, p_159904_.getMinBlockZ());
         Rotation rotation = Rotation.getRandom(this.random);
         IglooPieces.addPieces(p_159903_, blockpos, rotation, this, this.random);
      }
   }
}