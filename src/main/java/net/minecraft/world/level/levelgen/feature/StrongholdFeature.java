package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StrongholdFeature extends StructureFeature<NoneFeatureConfiguration> {
   public StrongholdFeature(Codec<NoneFeatureConfiguration> p_66928_) {
      super(p_66928_);
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return StrongholdFeature.StrongholdStart::new;
   }

   protected boolean isFeatureChunk(ChunkGenerator p_160416_, BiomeSource p_160417_, long p_160418_, WorldgenRandom p_160419_, ChunkPos p_160420_, Biome p_160421_, ChunkPos p_160422_, NoneFeatureConfiguration p_160423_, LevelHeightAccessor p_160424_) {
      return p_160416_.hasStronghold(p_160420_);
   }

   public static class StrongholdStart extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {
      private final long seed;

      public StrongholdStart(StructureFeature<NoneFeatureConfiguration> p_160426_, ChunkPos p_160427_, int p_160428_, long p_160429_) {
         super(p_160426_, p_160427_, p_160428_, p_160429_);
         this.seed = p_160429_;
      }

      public void generatePieces(RegistryAccess p_160439_, ChunkGenerator p_160440_, StructureManager p_160441_, ChunkPos p_160442_, Biome p_160443_, NoneFeatureConfiguration p_160444_, LevelHeightAccessor p_160445_) {
         int i = 0;

         StrongholdPieces.StartPiece strongholdpieces$startpiece;
         do {
            this.clearPieces();
            this.random.setLargeFeatureSeed(this.seed + (long)(i++), p_160442_.x, p_160442_.z);
            StrongholdPieces.resetPieces();
            strongholdpieces$startpiece = new StrongholdPieces.StartPiece(this.random, p_160442_.getBlockX(2), p_160442_.getBlockZ(2));
            this.addPiece(strongholdpieces$startpiece);
            strongholdpieces$startpiece.addChildren(strongholdpieces$startpiece, this, this.random);
            List<StructurePiece> list = strongholdpieces$startpiece.pendingChildren;

            while(!list.isEmpty()) {
               int j = this.random.nextInt(list.size());
               StructurePiece structurepiece = list.remove(j);
               structurepiece.addChildren(strongholdpieces$startpiece, this, this.random);
            }

            this.moveBelowSeaLevel(p_160440_.getSeaLevel(), p_160440_.getMinY(), this.random, 10);
         } while(this.hasNoPieces() || strongholdpieces$startpiece.portalRoomPiece == null);

      }
   }
}