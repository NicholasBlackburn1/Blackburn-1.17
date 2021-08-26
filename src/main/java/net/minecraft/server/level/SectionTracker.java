package net.minecraft.server.level;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class SectionTracker extends DynamicGraphMinFixedPoint
{
    protected SectionTracker(int p_8274_, int p_8275_, int p_8276_)
    {
        super(p_8274_, p_8275_, p_8276_);
    }

    protected boolean isSource(long pPos)
    {
        return pPos == Long.MAX_VALUE;
    }

    protected void checkNeighborsAfterUpdate(long pPos, int p_8281_, boolean pLevel)
    {
        for (int i = -1; i <= 1; ++i)
        {
            for (int j = -1; j <= 1; ++j)
            {
                for (int k = -1; k <= 1; ++k)
                {
                    long l = SectionPos.offset(pPos, i, j, k);

                    if (l != pPos)
                    {
                        this.checkNeighbor(pPos, l, p_8281_, pLevel);
                    }
                }
            }
        }
    }

    protected int getComputedLevel(long pPos, long p_8285_, int pExcludedSourcePos)
    {
        int i = pExcludedSourcePos;

        for (int j = -1; j <= 1; ++j)
        {
            for (int k = -1; k <= 1; ++k)
            {
                for (int l = -1; l <= 1; ++l)
                {
                    long i1 = SectionPos.offset(pPos, j, k, l);

                    if (i1 == pPos)
                    {
                        i1 = Long.MAX_VALUE;
                    }

                    if (i1 != p_8285_)
                    {
                        int j1 = this.computeLevelFromNeighbor(i1, pPos, this.getLevel(i1));

                        if (i > j1)
                        {
                            i = j1;
                        }

                        if (i == 0)
                        {
                            return i;
                        }
                    }
                }
            }
        }

        return i;
    }

    protected int computeLevelFromNeighbor(long pStartPos, long p_8294_, int pEndPos)
    {
        return pStartPos == Long.MAX_VALUE ? this.getLevelFromSource(p_8294_) : pEndPos + 1;
    }

    protected abstract int getLevelFromSource(long pPos);

    public void update(long pPos, int p_8290_, boolean pLevel)
    {
        this.checkEdge(Long.MAX_VALUE, pPos, p_8290_, pLevel);
    }
}
