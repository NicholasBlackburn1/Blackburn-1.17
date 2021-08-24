package net.minecraft.world.level.levelgen;

import java.util.Random;

public class WorldgenRandom extends Random implements RandomSource {
   private int count;

   public WorldgenRandom() {
   }

   public WorldgenRandom(long p_64679_) {
      super(p_64679_);
   }

   public int getCount() {
      return this.count;
   }

   public int next(int p_64708_) {
      ++this.count;
      return super.next(p_64708_);
   }

   public long setBaseChunkSeed(int p_64683_, int p_64684_) {
      long i = (long)p_64683_ * 341873128712L + (long)p_64684_ * 132897987541L;
      this.setSeed(i);
      return i;
   }

   public long setDecorationSeed(long p_64691_, int p_64692_, int p_64693_) {
      this.setSeed(p_64691_);
      long i = this.nextLong() | 1L;
      long j = this.nextLong() | 1L;
      long k = (long)p_64692_ * i + (long)p_64693_ * j ^ p_64691_;
      this.setSeed(k);
      return k;
   }

   public long setFeatureSeed(long p_64700_, int p_64701_, int p_64702_) {
      long i = p_64700_ + (long)p_64701_ + (long)(10000 * p_64702_);
      this.setSeed(i);
      return i;
   }

   public long setLargeFeatureSeed(long p_64704_, int p_64705_, int p_64706_) {
      this.setSeed(p_64704_);
      long i = this.nextLong();
      long j = this.nextLong();
      long k = (long)p_64705_ * i ^ (long)p_64706_ * j ^ p_64704_;
      this.setSeed(k);
      return k;
   }

   public long setBaseStoneSeed(long p_158962_, int p_158963_, int p_158964_, int p_158965_) {
      this.setSeed(p_158962_);
      long i = this.nextLong();
      long j = this.nextLong();
      long k = this.nextLong();
      long l = (long)p_158963_ * i ^ (long)p_158964_ * j ^ (long)p_158965_ * k ^ p_158962_;
      this.setSeed(l);
      return l;
   }

   public long setLargeFeatureWithSalt(long p_64695_, int p_64696_, int p_64697_, int p_64698_) {
      long i = (long)p_64696_ * 341873128712L + (long)p_64697_ * 132897987541L + p_64695_ + (long)p_64698_;
      this.setSeed(i);
      return i;
   }

   public static Random seedSlimeChunk(int p_64686_, int p_64687_, long p_64688_, long p_64689_) {
      return new Random(p_64688_ + (long)(p_64686_ * p_64686_ * 4987142) + (long)(p_64686_ * 5947611) + (long)(p_64687_ * p_64687_) * 4392871L + (long)(p_64687_ * 389711) ^ p_64689_);
   }
}