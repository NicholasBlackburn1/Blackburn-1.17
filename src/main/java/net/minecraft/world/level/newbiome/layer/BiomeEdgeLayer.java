package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum BiomeEdgeLayer implements CastleTransformer
{
    INSTANCE;

    public int apply(Context pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter)
    {
        int[] aint = new int[1];

        if (!this.m_76670_(aint, pCenter) && !this.m_76673_(aint, pNorth, pWest, pSouth, pEast, pCenter, 38, 37) && !this.m_76673_(aint, pNorth, pWest, pSouth, pEast, pCenter, 39, 37) && !this.m_76673_(aint, pNorth, pWest, pSouth, pEast, pCenter, 32, 5))
        {
            if (pCenter != 2 || pNorth != 12 && pWest != 12 && pEast != 12 && pSouth != 12)
            {
                if (pCenter == 6)
                {
                    if (pNorth == 2 || pWest == 2 || pEast == 2 || pSouth == 2 || pNorth == 30 || pWest == 30 || pEast == 30 || pSouth == 30 || pNorth == 12 || pWest == 12 || pEast == 12 || pSouth == 12)
                    {
                        return 1;
                    }

                    if (pNorth == 21 || pSouth == 21 || pWest == 21 || pEast == 21 || pNorth == 168 || pSouth == 168 || pWest == 168 || pEast == 168)
                    {
                        return 23;
                    }
                }

                return pCenter;
            }
            else
            {
                return 34;
            }
        }
        else
        {
            return aint[0];
        }
    }

    private boolean m_76670_(int[] p_76671_, int p_76672_)
    {
        if (!Layers.isSame(p_76672_, 3))
        {
            return false;
        }
        else
        {
            p_76671_[0] = p_76672_;
            return true;
        }
    }

    private boolean m_76673_(int[] p_76674_, int p_76675_, int p_76676_, int p_76677_, int p_76678_, int p_76679_, int p_76680_, int p_76681_)
    {
        if (p_76679_ != p_76680_)
        {
            return false;
        }
        else
        {
            if (Layers.isSame(p_76675_, p_76680_) && Layers.isSame(p_76676_, p_76680_) && Layers.isSame(p_76678_, p_76680_) && Layers.isSame(p_76677_, p_76680_))
            {
                p_76674_[0] = p_76679_;
            }
            else
            {
                p_76674_[0] = p_76681_;
            }

            return true;
        }
    }
}
