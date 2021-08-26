package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

public enum AddMushroomIslandLayer implements BishopTransformer
{
    INSTANCE;

    public int apply(Context pContext, int pX, int pSouthEast, int p_76639_, int p_76640_, int p_76641_)
    {
        return Layers.isShallowOcean(p_76641_) && Layers.isShallowOcean(p_76640_) && Layers.isShallowOcean(pX) && Layers.isShallowOcean(p_76639_) && Layers.isShallowOcean(pSouthEast) && pContext.nextRandom(100) == 0 ? 14 : p_76641_;
    }
}
