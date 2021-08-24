package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class BastionFeature extends JigsawFeature {
   private static final int BASTION_SPAWN_HEIGHT = 33;

   public BastionFeature(Codec<JigsawConfiguration> p_65226_) {
      super(p_65226_, 33, false, false);
   }

   protected boolean isFeatureChunk(ChunkGenerator p_159461_, BiomeSource p_159462_, long p_159463_, WorldgenRandom p_159464_, ChunkPos p_159465_, Biome p_159466_, ChunkPos p_159467_, JigsawConfiguration p_159468_, LevelHeightAccessor p_159469_) {
      return p_159464_.nextInt(5) >= 2;
   }
}