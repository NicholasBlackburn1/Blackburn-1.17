package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFortressFeature extends StructureFeature<NoneFeatureConfiguration> {
   private static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3), new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4), new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5), new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5), new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4));

   public NetherFortressFeature(Codec<NoneFeatureConfiguration> p_66384_) {
      super(p_66384_);
   }

   protected boolean isFeatureChunk(ChunkGenerator p_160080_, BiomeSource p_160081_, long p_160082_, WorldgenRandom p_160083_, ChunkPos p_160084_, Biome p_160085_, ChunkPos p_160086_, NoneFeatureConfiguration p_160087_, LevelHeightAccessor p_160088_) {
      return p_160083_.nextInt(5) < 2;
   }

   public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
      return NetherFortressFeature.NetherBridgeStart::new;
   }

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
      return FORTRESS_ENEMIES;
   }

   public static class NetherBridgeStart extends StructureStart<NoneFeatureConfiguration> {
      public NetherBridgeStart(StructureFeature<NoneFeatureConfiguration> p_160091_, ChunkPos p_160092_, int p_160093_, long p_160094_) {
         super(p_160091_, p_160092_, p_160093_, p_160094_);
      }

      public void generatePieces(RegistryAccess p_160104_, ChunkGenerator p_160105_, StructureManager p_160106_, ChunkPos p_160107_, Biome p_160108_, NoneFeatureConfiguration p_160109_, LevelHeightAccessor p_160110_) {
         NetherBridgePieces.StartPiece netherbridgepieces$startpiece = new NetherBridgePieces.StartPiece(this.random, p_160107_.getBlockX(2), p_160107_.getBlockZ(2));
         this.addPiece(netherbridgepieces$startpiece);
         netherbridgepieces$startpiece.addChildren(netherbridgepieces$startpiece, this, this.random);
         List<StructurePiece> list = netherbridgepieces$startpiece.pendingChildren;

         while(!list.isEmpty()) {
            int i = this.random.nextInt(list.size());
            StructurePiece structurepiece = list.remove(i);
            structurepiece.addChildren(netherbridgepieces$startpiece, this, this.random);
         }

         this.moveInsideHeights(this.random, 48, 70);
      }
   }
}