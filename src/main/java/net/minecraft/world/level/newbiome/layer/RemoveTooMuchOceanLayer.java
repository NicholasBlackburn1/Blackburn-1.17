package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.CastleTransformer;

public enum RemoveTooMuchOceanLayer implements CastleTransformer
{
    INSTANCE;

    public int apply(Context pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter)
    {
        return Layers.isShallowOcean(pCenter) && Layers.isShallowOcean(pNorth) && Layers.isShallowOcean(pWest) && Layers.isShallowOcean(pEast) && Layers.isShallowOcean(pSouth) && pContext.nextRandom(2) == 0 ? 1 : pCenter;
    }
}
