package net.minecraft.world.level.biome;

import net.minecraft.core.QuartPos;

public enum NearestNeighborBiomeZoomer implements BiomeZoomer
{
    INSTANCE;

    public Biome getBiome(long pSeed, int p_48574_, int pX, int pY, BiomeManager.NoiseBiomeSource pZ)
    {
        return pZ.getNoiseBiome(QuartPos.fromBlock(p_48574_), QuartPos.fromBlock(pX), QuartPos.fromBlock(pY));
    }
}
