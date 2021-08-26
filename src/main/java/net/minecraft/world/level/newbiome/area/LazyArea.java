package net.minecraft.world.level.newbiome.area;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.newbiome.layer.traits.PixelTransformer;

public final class LazyArea implements Area
{
    private final PixelTransformer transformer;
    private final Long2IntLinkedOpenHashMap cache;
    private final int maxCache;

    public LazyArea(Long2IntLinkedOpenHashMap p_76493_, int p_76494_, PixelTransformer p_76495_)
    {
        this.cache = p_76493_;
        this.maxCache = p_76494_;
        this.transformer = p_76495_;
    }

    public int get(int pX, int pZ)
    {
        long i = ChunkPos.asLong(pX, pZ);

        synchronized (this.cache)
        {
            int j = this.cache.get(i);

            if (j != Integer.MIN_VALUE)
            {
                return j;
            }
            else
            {
                int k = this.transformer.apply(pX, pZ);
                this.cache.put(i, k);

                if (this.cache.size() > this.maxCache)
                {
                    for (int l = 0; l < this.maxCache / 16; ++l)
                    {
                        this.cache.removeFirstInt();
                    }
                }

                return k;
            }
        }
    }

    public int getMaxCache()
    {
        return this.maxCache;
    }
}
