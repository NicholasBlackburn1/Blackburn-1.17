package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.function.LongPredicate;
import net.minecraft.util.Mth;

public abstract class DynamicGraphMinFixedPoint
{
    private static final int NO_COMPUTED_LEVEL = 255;
    private final int levelCount;
    private final LongLinkedOpenHashSet[] queues;
    private final Long2ByteMap computedLevels;
    private int firstQueuedLevel;
    private volatile boolean hasWork;

    protected DynamicGraphMinFixedPoint(int p_75543_, final int p_75544_, final int p_75545_)
    {
        if (p_75543_ >= 254)
        {
            throw new IllegalArgumentException("Level count must be < 254.");
        }
        else
        {
            this.levelCount = p_75543_;
            this.queues = new LongLinkedOpenHashSet[p_75543_];
            int i = p_75544_;
            int j = p_75545_;

            if (this.getClass() != BlockLightEngine.class && this.getClass() != SkyLightEngine.class)
            {
                if (this.getClass() == BlockLightSectionStorage.class || this.getClass() == SkyLightSectionStorage.class)
                {
                    i = Math.max(p_75544_, 2048);
                    j = Math.max(p_75545_, 2048);
                }
            }
            else
            {
                i = Math.max(p_75544_, 8192);
                j = Math.max(p_75545_, 8192);
            }

            for (int k = 0; k < p_75543_; ++k)
            {
                this.queues[k] = new LongLinkedOpenHashSet(i, 0.5F)
                {
                    protected void rehash(int p_75611_)
                    {
                        if (p_75611_ > p_75544_)
                        {
                            super.rehash(p_75611_);
                        }
                    }
                };
            }

            this.computedLevels = new Long2ByteOpenHashMap(j, 0.5F)
            {
                protected void rehash(int p_75620_)
                {
                    if (p_75620_ > p_75545_)
                    {
                        super.rehash(p_75620_);
                    }
                }
            };
            this.computedLevels.defaultReturnValue((byte) - 1);
            this.firstQueuedLevel = p_75543_;
        }
    }

    private int getKey(int pLevel1, int pLevel2)
    {
        int i = pLevel1;

        if (pLevel1 > pLevel2)
        {
            i = pLevel2;
        }

        if (i > this.levelCount - 1)
        {
            i = this.levelCount - 1;
        }

        return i;
    }

    private void checkFirstQueuedLevel(int pMaxLevel)
    {
        int i = this.firstQueuedLevel;
        this.firstQueuedLevel = pMaxLevel;

        for (int j = i + 1; j < pMaxLevel; ++j)
        {
            if (!this.queues[j].isEmpty())
            {
                this.firstQueuedLevel = j;
                break;
            }
        }
    }

    protected void removeFromQueue(long pPosition)
    {
        int i = this.computedLevels.get(pPosition) & 255;

        if (i != 255)
        {
            int j = this.getLevel(pPosition);
            int k = this.getKey(j, i);
            this.dequeue(pPosition, k, this.levelCount, true);
            this.hasWork = this.firstQueuedLevel < this.levelCount;
        }
    }

    public void removeIf(LongPredicate p_75582_)
    {
        LongList longlist = new LongArrayList();
        this.computedLevels.keySet().forEach((long p_75583_2_) ->
        {
            if (p_75582_.test(p_75583_2_))
            {
                longlist.add(p_75583_2_);
            }
        });
        longlist.forEach((long derp) ->{
        	removeFromQueue(derp);
        });
    }

    private void dequeue(long pPos, int p_75560_, int pLevel, boolean pMaxLevel)
    {
        if (pMaxLevel)
        {
            this.computedLevels.remove(pPos);
        }

        this.queues[p_75560_].remove(pPos);

        if (this.queues[p_75560_].isEmpty() && this.firstQueuedLevel == p_75560_)
        {
            this.checkFirstQueuedLevel(pLevel);
        }
    }

    private void enqueue(long pPos, int p_75556_, int pLevelToSet)
    {
        this.computedLevels.put(pPos, (byte)p_75556_);
        this.queues[pLevelToSet].add(pPos);

        if (this.firstQueuedLevel > pLevelToSet)
        {
            this.firstQueuedLevel = pLevelToSet;
        }
    }

    protected void checkNode(long pLevelPos)
    {
        this.checkEdge(pLevelPos, pLevelPos, this.levelCount - 1, false);
    }

    protected void checkEdge(long pFromPos, long p_75578_, int pToPos, boolean p_75580_)
    {
        this.checkEdge(pFromPos, p_75578_, pToPos, this.getLevel(p_75578_), this.computedLevels.get(p_75578_) & 255, p_75580_);
        this.hasWork = this.firstQueuedLevel < this.levelCount;
    }

    private void checkEdge(long pFromPos, long p_75571_, int pToPos, int p_75573_, int pNewLevel, boolean pPreviousLevel)
    {
        if (!this.isSource(p_75571_))
        {
            pToPos = Mth.clamp(pToPos, 0, this.levelCount - 1);
            p_75573_ = Mth.clamp(p_75573_, 0, this.levelCount - 1);
            boolean flag;

            if (pNewLevel == 255)
            {
                flag = true;
                pNewLevel = p_75573_;
            }
            else
            {
                flag = false;
            }

            int i;

            if (pPreviousLevel)
            {
                i = Math.min(pNewLevel, pToPos);
            }
            else
            {
                i = Mth.clamp(this.getComputedLevel(p_75571_, pFromPos, pToPos), 0, this.levelCount - 1);
            }

            int j = this.getKey(p_75573_, pNewLevel);

            if (p_75573_ != i)
            {
                int k = this.getKey(p_75573_, i);

                if (j != k && !flag)
                {
                    this.dequeue(p_75571_, j, k, false);
                }

                this.enqueue(p_75571_, i, k);
            }
            else if (!flag)
            {
                this.dequeue(p_75571_, j, this.levelCount, true);
            }
        }
    }

    protected final void checkNeighbor(long pFromPos, long p_75595_, int pToPos, boolean p_75597_)
    {
        int i = this.computedLevels.get(p_75595_) & 255;
        int j = Mth.clamp(this.computeLevelFromNeighbor(pFromPos, p_75595_, pToPos), 0, this.levelCount - 1);

        if (p_75597_)
        {
            this.checkEdge(pFromPos, p_75595_, j, this.getLevel(p_75595_), i, true);
        }
        else
        {
            int k;
            boolean flag;

            if (i == 255)
            {
                flag = true;
                k = Mth.clamp(this.getLevel(p_75595_), 0, this.levelCount - 1);
            }
            else
            {
                k = i;
                flag = false;
            }

            if (j == k)
            {
                this.checkEdge(pFromPos, p_75595_, this.levelCount - 1, flag ? k : this.getLevel(p_75595_), i, false);
            }
        }
    }

    protected final boolean hasWork()
    {
        return this.hasWork;
    }

    protected final int runUpdates(int pToUpdateCount)
    {
        if (this.firstQueuedLevel >= this.levelCount)
        {
            return pToUpdateCount;
        }
        else
        {
            while (this.firstQueuedLevel < this.levelCount && pToUpdateCount > 0)
            {
                --pToUpdateCount;
                LongLinkedOpenHashSet longlinkedopenhashset = this.queues[this.firstQueuedLevel];
                long i = longlinkedopenhashset.removeFirstLong();
                int j = Mth.clamp(this.getLevel(i), 0, this.levelCount - 1);

                if (longlinkedopenhashset.isEmpty())
                {
                    this.checkFirstQueuedLevel(this.levelCount);
                }

                int k = this.computedLevels.remove(i) & 255;

                if (k < j)
                {
                    this.setLevel(i, k);
                    this.checkNeighborsAfterUpdate(i, k, true);
                }
                else if (k > j)
                {
                    this.enqueue(i, k, this.getKey(this.levelCount - 1, k));
                    this.setLevel(i, this.levelCount - 1);
                    this.checkNeighborsAfterUpdate(i, j, false);
                }
            }

            this.hasWork = this.firstQueuedLevel < this.levelCount;
            return pToUpdateCount;
        }
    }

    public int getQueueSize()
    {
        return this.computedLevels.size();
    }

    protected abstract boolean isSource(long pPos);

    protected abstract int getComputedLevel(long pPos, long p_75567_, int pExcludedSourcePos);

    protected abstract void checkNeighborsAfterUpdate(long pPos, int p_75564_, boolean pLevel);

    protected abstract int getLevel(long pSectionPos);

    protected abstract void setLevel(long pSectionPos, int p_75553_);

    protected abstract int computeLevelFromNeighbor(long pStartPos, long p_75591_, int pEndPos);

    protected int queuedUpdateSize()
    {
        return this.computedLevels.size();
    }
}
