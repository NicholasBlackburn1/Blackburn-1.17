package net.minecraft.world.level.levelgen.feature;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class FeaturePlaceContext<FC extends FeatureConfiguration> {
   private final WorldGenLevel level;
   private final ChunkGenerator chunkGenerator;
   private final Random random;
   private final BlockPos origin;
   private final FC config;

   public FeaturePlaceContext(WorldGenLevel p_159769_, ChunkGenerator p_159770_, Random p_159771_, BlockPos p_159772_, FC p_159773_) {
      this.level = p_159769_;
      this.chunkGenerator = p_159770_;
      this.random = p_159771_;
      this.origin = p_159772_;
      this.config = p_159773_;
   }

   public WorldGenLevel level() {
      return this.level;
   }

   public ChunkGenerator chunkGenerator() {
      return this.chunkGenerator;
   }

   public Random random() {
      return this.random;
   }

   public BlockPos origin() {
      return this.origin;
   }

   public FC config() {
      return this.config;
   }
}