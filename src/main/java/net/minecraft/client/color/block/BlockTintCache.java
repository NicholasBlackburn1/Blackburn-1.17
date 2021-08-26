package net.minecraft.client.color.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public class BlockTintCache
{
    private static final int MAX_CACHE_ENTRIES = 256;
    private final ThreadLocal<BlockTintCache.LatestCacheInfo> latestChunkOnThread = ThreadLocal.withInitial(BlockTintCache.LatestCacheInfo::new);
    private final Long2ObjectLinkedOpenHashMap<int[]> cache = new Long2ObjectLinkedOpenHashMap<>(256, 0.25F);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public int getColor(BlockPos pBlockPos, IntSupplier pColorSupplier)
    {
        int i = SectionPos.blockToSectionCoord(pBlockPos.getX());
        int j = SectionPos.blockToSectionCoord(pBlockPos.getZ());
        BlockTintCache.LatestCacheInfo blocktintcache$latestcacheinfo = this.latestChunkOnThread.get();

        if (blocktintcache$latestcacheinfo.x != i || blocktintcache$latestcacheinfo.z != j)
        {
            blocktintcache$latestcacheinfo.x = i;
            blocktintcache$latestcacheinfo.z = j;
            blocktintcache$latestcacheinfo.cache = this.findOrCreateChunkCache(i, j);
        }

        int k = pBlockPos.getX() & 15;
        int l = pBlockPos.getZ() & 15;
        int i1 = l << 4 | k;
        int j1 = blocktintcache$latestcacheinfo.cache[i1];

        if (j1 != -1)
        {
            return j1;
        }
        else
        {
            int k1 = pColorSupplier.getAsInt();
            blocktintcache$latestcacheinfo.cache[i1] = k1;
            return k1;
        }
    }

    public void invalidateForChunk(int pChunkX, int pChunkZ)
    {
        try
        {
            this.lock.writeLock().lock();

            for (int i = -1; i <= 1; ++i)
            {
                for (int j = -1; j <= 1; ++j)
                {
                    long k = ChunkPos.asLong(pChunkX + i, pChunkZ + j);
                    this.cache.remove(k);
                }
            }
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    public void invalidateAll()
    {
        try
        {
            this.lock.writeLock().lock();
            this.cache.clear();
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }

    private int[] findOrCreateChunkCache(int pChunkX, int pChunkZ)
    {
        long i = ChunkPos.asLong(pChunkX, pChunkZ);
        this.lock.readLock().lock();
        int[] aint;

        try
        {
            aint = this.cache.get(i);
        }
        finally
        {
            this.lock.readLock().unlock();
        }

        if (aint != null)
        {
            return aint;
        }
        else
        {
            int[] aint1 = new int[256];
            Arrays.fill(aint1, -1);

            try
            {
                this.lock.writeLock().lock();

                if (this.cache.size() >= 256)
                {
                    this.cache.removeFirst();
                }

                this.cache.put(i, aint1);
            }
            finally
            {
                this.lock.writeLock().unlock();
            }

            return aint1;
        }
    }

    static class LatestCacheInfo
    {
        public int x = Integer.MIN_VALUE;
        public int z = Integer.MIN_VALUE;
        public int[] cache;

        private LatestCacheInfo()
        {
        }
    }
}
