package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum AddDeepOceanLayer implements CastleTransformer
{
    INSTANCE;

    public int apply(Context pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter)
    {
        if (Layers.isShallowOcean(pCenter))
        {
            int i = 0;

            if (Layers.isShallowOcean(pNorth))
            {
                ++i;
            }

            if (Layers.isShallowOcean(pWest))
            {
                ++i;
            }

            if (Layers.isShallowOcean(pEast))
            {
                ++i;
            }

            if (Layers.isShallowOcean(pSouth))
            {
                ++i;
            }

            if (i > 3)
            {
                if (pCenter == 44)
                {
                    return 47;
                }

                if (pCenter == 45)
                {
                    return 48;
                }

                if (pCenter == 0)
                {
                    return 24;
                }

                if (pCenter == 46)
                {
                    return 49;
                }

                if (pCenter == 10)
                {
                    return 50;
                }

                return 24;
            }
        }

        return pCenter;
    }
}
