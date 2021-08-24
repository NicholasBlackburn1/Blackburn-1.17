package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public class PillagerOutpostFeature extends JigsawFeature {
   private static final WeightedRandomList<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

   public PillagerOutpostFeature(Codec<JigsawConfiguration> p_66562_) {
      super(p_66562_, 0, true, true);
   }

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
      return OUTPOST_ENEMIES;
   }

   protected boolean isFeatureChunk(ChunkGenerator p_160197_, BiomeSource p_160198_, long p_160199_, WorldgenRandom p_160200_, ChunkPos p_160201_, Biome p_160202_, ChunkPos p_160203_, JigsawConfiguration p_160204_, LevelHeightAccessor p_160205_) {
      int i = p_160201_.x >> 4;
      int j = p_160201_.z >> 4;
      p_160200_.setSeed((long)(i ^ j << 4) ^ p_160199_);
      p_160200_.nextInt();
      if (p_160200_.nextInt(5) != 0) {
         return false;
      } else {
         return !this.isNearVillage(p_160197_, p_160199_, p_160200_, p_160201_);
      }
   }

   private boolean isNearVillage(ChunkGenerator p_160182_, long p_160183_, WorldgenRandom p_160184_, ChunkPos p_160185_) {
      StructureFeatureConfiguration structurefeatureconfiguration = p_160182_.getSettings().getConfig(StructureFeature.VILLAGE);
      if (structurefeatureconfiguration == null) {
         return false;
      } else {
         int i = p_160185_.x;
         int j = p_160185_.z;

         for(int k = i - 10; k <= i + 10; ++k) {
            for(int l = j - 10; l <= j + 10; ++l) {
               ChunkPos chunkpos = StructureFeature.VILLAGE.getPotentialFeatureChunk(structurefeatureconfiguration, p_160183_, p_160184_, k, l);
               if (k == chunkpos.x && l == chunkpos.z) {
                  return true;
               }
            }
         }

         return false;
      }
   }
}