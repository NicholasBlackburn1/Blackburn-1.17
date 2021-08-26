package net.minecraft.world.level.biome;

import net.minecraft.util.LinearCongruentialGenerator;

public enum FuzzyOffsetBiomeZoomer implements BiomeZoomer
{
    INSTANCE;

    private static final int ZOOM_BITS = 2;
    private static final int ZOOM = 4;
    private static final int ZOOM_MASK = 3;

    public Biome getBiome(long pSeed, int p_48303_, int pX, int pY, BiomeManager.NoiseBiomeSource pZ)
    {
        int i = p_48303_ - 2;
        int j = pX - 2;
        int k = pY - 2;
        int l = i >> 2;
        int i1 = j >> 2;
        int j1 = k >> 2;
        double d0 = (double)(i & 3) / 4.0D;
        double d1 = (double)(j & 3) / 4.0D;
        double d2 = (double)(k & 3) / 4.0D;
        int k1 = 0;
        double d3 = Double.POSITIVE_INFINITY;

        for (int l1 = 0; l1 < 8; ++l1)
        {
            boolean flag = (l1 & 4) == 0;
            boolean flag1 = (l1 & 2) == 0;
            boolean flag2 = (l1 & 1) == 0;
            int i2 = flag ? l : l + 1;
            int j2 = flag1 ? i1 : i1 + 1;
            int k2 = flag2 ? j1 : j1 + 1;
            double d4 = flag ? d0 : d0 - 1.0D;
            double d5 = flag1 ? d1 : d1 - 1.0D;
            double d6 = flag2 ? d2 : d2 - 1.0D;
            double d7 = getFiddledDistance(pSeed, i2, j2, k2, d4, d5, d6);

            if (d3 > d7)
            {
                k1 = l1;
                d3 = d7;
            }
        }

        int l2 = (k1 & 4) == 0 ? l : l + 1;
        int i3 = (k1 & 2) == 0 ? i1 : i1 + 1;
        int j3 = (k1 & 1) == 0 ? j1 : j1 + 1;
        return pZ.getNoiseBiome(l2, i3, j3);
    }

    private static double getFiddledDistance(long pSeed, int p_48295_, int pX, int pY, double pZ, double pScaleX, double p_48300_)
    {
        long i = LinearCongruentialGenerator.next(pSeed, (long)p_48295_);
        i = LinearCongruentialGenerator.next(i, (long)pX);
        i = LinearCongruentialGenerator.next(i, (long)pY);
        i = LinearCongruentialGenerator.next(i, (long)p_48295_);
        i = LinearCongruentialGenerator.next(i, (long)pX);
        i = LinearCongruentialGenerator.next(i, (long)pY);
        double d0 = getFiddle(i);
        i = LinearCongruentialGenerator.next(i, pSeed);
        double d1 = getFiddle(i);
        i = LinearCongruentialGenerator.next(i, pSeed);
        double d2 = getFiddle(i);
        return sqr(p_48300_ + d2) + sqr(pScaleX + d1) + sqr(pZ + d0);
    }

    private static double getFiddle(long pSeed)
    {
        double d0 = (double)Math.floorMod(pSeed >> 24, 1024) / 1024.0D;
        return (d0 - 0.5D) * 0.9D;
    }

    private static double sqr(double pX)
    {
        return pX * pX;
    }
}
