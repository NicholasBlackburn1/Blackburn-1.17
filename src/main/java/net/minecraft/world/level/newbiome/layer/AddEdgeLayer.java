package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C0Transformer;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public class AddEdgeLayer
{
    public static enum CoolWarm implements CastleTransformer
    {
        INSTANCE;

        public int apply(Context pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter)
        {
            return pCenter != 1 || pNorth != 3 && pWest != 3 && pEast != 3 && pSouth != 3 && pNorth != 4 && pWest != 4 && pEast != 4 && pSouth != 4 ? pCenter : 2;
        }
    }

    public static enum HeatIce implements CastleTransformer
    {
        INSTANCE;

        public int apply(Context pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter)
        {
            return pCenter != 4 || pNorth != 1 && pWest != 1 && pEast != 1 && pSouth != 1 && pNorth != 2 && pWest != 2 && pEast != 2 && pSouth != 2 ? pCenter : 3;
        }
    }

    public static enum IntroduceSpecial implements C0Transformer
    {
        INSTANCE;

        public int apply(Context pContext, int pValue)
        {
            if (!Layers.isShallowOcean(pValue) && pContext.nextRandom(13) == 0)
            {
                pValue |= 1 + pContext.nextRandom(15) << 8 & 3840;
            }

            return pValue;
        }
    }
}
