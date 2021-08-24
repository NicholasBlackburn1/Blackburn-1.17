package net.minecraft.world.level.biome;

import net.minecraft.core.QuartPos;

public enum NearestNeighborBiomeZoomer implements BiomeZoomer {
   INSTANCE;

   public Biome getBiome(long p_48573_, int p_48574_, int p_48575_, int p_48576_, BiomeManager.NoiseBiomeSource p_48577_) {
      return p_48577_.getNoiseBiome(QuartPos.fromBlock(p_48574_), QuartPos.fromBlock(p_48575_), QuartPos.fromBlock(p_48576_));
   }
}