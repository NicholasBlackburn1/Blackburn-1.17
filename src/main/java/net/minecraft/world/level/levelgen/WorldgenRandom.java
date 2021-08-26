package net.minecraft.world.level.levelgen;

import java.util.Random;

public class WorldgenRandom extends Random implements RandomSource
{
    private int count;

    public WorldgenRandom()
    {
    }

    public WorldgenRandom(long p_64679_)
    {
        super(p_64679_);
    }

    public int getCount()
    {
        return this.count;
    }

    public int next(int p_64708_)
    {
        ++this.count;
        return super.next(p_64708_);
    }

    public long setBaseChunkSeed(int pX, int pZ)
    {
        long i = (long)pX * 341873128712L + (long)pZ * 132897987541L;
        this.setSeed(i);
        return i;
    }

    public long setDecorationSeed(long pBaseSeed, int p_64692_, int pX)
    {
        this.setSeed(pBaseSeed);
        long i = this.nextLong() | 1L;
        long j = this.nextLong() | 1L;
        long k = (long)p_64692_ * i + (long)pX * j ^ pBaseSeed;
        this.setSeed(k);
        return k;
    }

    public long setFeatureSeed(long pBaseSeed, int p_64701_, int pX)
    {
        long i = pBaseSeed + (long)p_64701_ + (long)(10000 * pX);
        this.setSeed(i);
        return i;
    }

    public long setLargeFeatureSeed(long pSeed, int p_64705_, int pX)
    {
        this.setSeed(pSeed);
        long i = this.nextLong();
        long j = this.nextLong();
        long k = (long)p_64705_ * i ^ (long)pX * j ^ pSeed;
        this.setSeed(k);
        return k;
    }

    public long setBaseStoneSeed(long p_158962_, int p_158963_, int p_158964_, int p_158965_)
    {
        this.setSeed(p_158962_);
        long i = this.nextLong();
        long j = this.nextLong();
        long k = this.nextLong();
        long l = (long)p_158963_ * i ^ (long)p_158964_ * j ^ (long)p_158965_ * k ^ p_158962_;
        this.setSeed(l);
        return l;
    }

    public long setLargeFeatureWithSalt(long pBaseSeed, int p_64696_, int pX, int pZ)
    {
        long i = (long)p_64696_ * 341873128712L + (long)pX * 132897987541L + pBaseSeed + (long)pZ;
        this.setSeed(i);
        return i;
    }

    public static Random seedSlimeChunk(int p_64686_, int p_64687_, long p_64688_, long p_64689_)
    {
        return new Random(p_64688_ + (long)(p_64686_ * p_64686_ * 4987142) + (long)(p_64686_ * 5947611) + (long)(p_64687_ * p_64687_) * 4392871L + (long)(p_64687_ * 389711) ^ p_64689_);
    }
}
