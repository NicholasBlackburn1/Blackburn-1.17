package net.minecraft.world.level.biome;

public enum FuzzyOffsetConstantColumnBiomeZoomer implements BiomeZoomer
{
    INSTANCE;

    public Biome getBiome(long pSeed, int p_48318_, int pX, int pY, BiomeManager.NoiseBiomeSource pZ)
    {
        return FuzzyOffsetBiomeZoomer.INSTANCE.getBiome(pSeed, p_48318_, 0, pY, pZ);
    }
}
