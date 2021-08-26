package net.minecraft.world.level.newbiome.layer;

import net.minecraft.world.level.newbiome.context.Context;
import net.minecraft.world.level.newbiome.layer.traits.C1Transformer;

public enum AddSnowLayer implements C1Transformer
{
    INSTANCE;

    public int apply(Context pContext, int pValue)
    {
        if (Layers.isShallowOcean(pValue))
        {
            return pValue;
        }
        else
        {
            int i = pContext.nextRandom(6);

            if (i == 0)
            {
                return 4;
            }
            else
            {
                return i == 1 ? 3 : 1;
            }
        }
    }
}
