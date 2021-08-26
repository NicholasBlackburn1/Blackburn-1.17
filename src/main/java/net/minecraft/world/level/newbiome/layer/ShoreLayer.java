package net.minecraft.world.level.newbiome.layer;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum ShoreLayer implements CastleTransformer
{
    INSTANCE;

    private static final IntSet SNOWY = new IntOpenHashSet(new int[]{26, 11, 12, 13, 140, 30, 31, 158, 10});
    private static final IntSet JUNGLES = new IntOpenHashSet(new int[]{168, 169, 21, 22, 23, 149, 151});

    public int apply(Context pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter)
    {
        if (pCenter == 14)
        {
            if (Layers.isShallowOcean(pNorth) || Layers.isShallowOcean(pWest) || Layers.isShallowOcean(pSouth) || Layers.isShallowOcean(pEast))
            {
                return 15;
            }
        }
        else if (JUNGLES.contains(pCenter))
        {
            if (!isJungleCompatible(pNorth) || !isJungleCompatible(pWest) || !isJungleCompatible(pSouth) || !isJungleCompatible(pEast))
            {
                return 23;
            }

            if (Layers.isOcean(pNorth) || Layers.isOcean(pWest) || Layers.isOcean(pSouth) || Layers.isOcean(pEast))
            {
                return 16;
            }
        }
        else if (pCenter != 3 && pCenter != 34 && pCenter != 20)
        {
            if (SNOWY.contains(pCenter))
            {
                if (!Layers.isOcean(pCenter) && (Layers.isOcean(pNorth) || Layers.isOcean(pWest) || Layers.isOcean(pSouth) || Layers.isOcean(pEast)))
                {
                    return 26;
                }
            }
            else if (pCenter != 37 && pCenter != 38)
            {
                if (!Layers.isOcean(pCenter) && pCenter != 7 && pCenter != 6 && (Layers.isOcean(pNorth) || Layers.isOcean(pWest) || Layers.isOcean(pSouth) || Layers.isOcean(pEast)))
                {
                    return 16;
                }
            }
            else if (!Layers.isOcean(pNorth) && !Layers.isOcean(pWest) && !Layers.isOcean(pSouth) && !Layers.isOcean(pEast) && (!this.isMesa(pNorth) || !this.isMesa(pWest) || !this.isMesa(pSouth) || !this.isMesa(pEast)))
            {
                return 2;
            }
        }
        else if (!Layers.isOcean(pCenter) && (Layers.isOcean(pNorth) || Layers.isOcean(pWest) || Layers.isOcean(pSouth) || Layers.isOcean(pEast)))
        {
            return 25;
        }

        return pCenter;
    }

    private static boolean isJungleCompatible(int p_76925_)
    {
        return JUNGLES.contains(p_76925_) || p_76925_ == 4 || p_76925_ == 5 || Layers.isOcean(p_76925_);
    }

    private boolean isMesa(int p_76927_)
    {
        return p_76927_ == 37 || p_76927_ == 38 || p_76927_ == 39 || p_76927_ == 165 || p_76927_ == 166 || p_76927_ == 167;
    }
}
