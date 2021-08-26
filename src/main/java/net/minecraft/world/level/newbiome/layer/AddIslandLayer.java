package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.BishopTransformer;

public enum AddIslandLayer implements BishopTransformer
{
    INSTANCE;

    public int apply(Context pContext, int pX, int pSouthEast, int p_76623_, int p_76624_, int p_76625_)
    {
        if (!Layers.isShallowOcean(p_76625_) || Layers.isShallowOcean(p_76624_) && Layers.isShallowOcean(p_76623_) && Layers.isShallowOcean(pX) && Layers.isShallowOcean(pSouthEast))
        {
            if (!Layers.isShallowOcean(p_76625_) && (Layers.isShallowOcean(p_76624_) || Layers.isShallowOcean(pX) || Layers.isShallowOcean(p_76623_) || Layers.isShallowOcean(pSouthEast)) && pContext.nextRandom(5) == 0)
            {
                if (Layers.isShallowOcean(p_76624_))
                {
                    return p_76625_ == 4 ? 4 : p_76624_;
                }

                if (Layers.isShallowOcean(pX))
                {
                    return p_76625_ == 4 ? 4 : pX;
                }

                if (Layers.isShallowOcean(p_76623_))
                {
                    return p_76625_ == 4 ? 4 : p_76623_;
                }

                if (Layers.isShallowOcean(pSouthEast))
                {
                    return p_76625_ == 4 ? 4 : pSouthEast;
                }
            }

            return p_76625_;
        }
        else
        {
            int i = 1;
            int j = 1;

            if (!Layers.isShallowOcean(p_76624_) && pContext.nextRandom(i++) == 0)
            {
                j = p_76624_;
            }

            if (!Layers.isShallowOcean(p_76623_) && pContext.nextRandom(i++) == 0)
            {
                j = p_76623_;
            }

            if (!Layers.isShallowOcean(pX) && pContext.nextRandom(i++) == 0)
            {
                j = pX;
            }

            if (!Layers.isShallowOcean(pSouthEast) && pContext.nextRandom(i++) == 0)
            {
                j = pSouthEast;
            }

            if (pContext.nextRandom(3) == 0)
            {
                return j;
            }
            else
            {
                return j == 4 ? 4 : p_76625_;
            }
        }
    }
}
