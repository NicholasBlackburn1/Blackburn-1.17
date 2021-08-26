package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum SmoothLayer implements CastleTransformer
{
    INSTANCE;

    public int apply(Context pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter)
    {
        boolean flag = pWest == pEast;
        boolean flag1 = pNorth == pSouth;

        if (flag == flag1)
        {
            if (flag)
            {
                return pContext.nextRandom(2) == 0 ? pEast : pNorth;
            }
            else
            {
                return pCenter;
            }
        }
        else
        {
            return flag ? pEast : pNorth;
        }
    }
}
